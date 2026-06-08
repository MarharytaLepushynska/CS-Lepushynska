package Db;

import Tools.Product;
import Tools.ProductFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.sql.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StorageDb implements Db {

    private final Connection connection;

    public StorageDb(String dbUrl, String dbUser, String dbPass) {
        try {
            this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        } catch (SQLException e) {
            throw new RuntimeException("Can't connect to MySQL DB", e);
        }
    }

    @Override
    public int insert(Product product) {
        try(PreparedStatement ps = connection.prepareStatement("INSERT INTO Storage(product_name, product_amount, product_price, category) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getAmount());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getCategory());

            int inserted = ps.executeUpdate();
            if (inserted < 1) {
                throw new RuntimeException("Insert failed");
            }

            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) {
                return rs.getInt(1);
            }
            throw new RuntimeException("Insert failed");
        } catch (SQLException e) {
            throw new RuntimeException("Can't insert product: " + product, e);
        }
    }

    @Override
    public Product update(Product product) {

        try(PreparedStatement ps = connection.prepareStatement("""
                                                                UPDATE Storage
                                                                SET product_name = ?, product_amount = ?, product_price = ?, category = ?
                                                                WHERE product_id = ?
                                                                """)) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getAmount());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getCategory());
            ps.setInt(5, product.getId());

            int updated = ps.executeUpdate();
            if (updated < 1) {
                throw new RuntimeException("Update failed");
            }
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Can't update product: " + product, e);
        }
    }

    @Override
    public int count() {
        try (PreparedStatement ps = connection.prepareStatement("select count(*) from Storage")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Can't count products", e);
        }
    }

    private static String stringEquals(String colName, String value, List<Object> params) {
        if(value == null) {
            return null;
        }

        params.add(value);
        return colName + " = ? ";
    }

    private static String stringMinMax(String colName, Number from, Number to, List<Object> params) {
        ArrayList<String> con = new ArrayList<>();

        if(from != null) {
            con.add(colName + " >= ? ");
            params.add(from);
        }
        if(to != null) {
            con.add(colName + " <= ? ");
            params.add(to);
        }

        return con.isEmpty() ? null : String.join(" and ", con);
    }

    @Override
    public List<Product> getAll(ProductFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Storage");
        ArrayList<Object> params = new ArrayList<>();

        String filterPart = Stream.of(stringEquals("product_name", filter.getName(), params),
                        stringMinMax("product_amount", filter.getAmountFrom(), filter.getAmountTo(), params),
                        stringMinMax("product_price", filter.getPriceFrom(), filter.getPriceTo(), params),
                        stringEquals("category", filter.getCategory(), params))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" and "));

        if(!filterPart.isEmpty()) {
            sql.append(" WHERE ").append(filterPart);
        }

        try(PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for(int i = 1; i <= params.size(); i++) {
                ps.setObject(i, params.get(i - 1));
            }

            List<Product> products = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getInt("product_amount"), rs.getDouble("product_price"), rs.getString("category")));
                }
            }

            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Can't get products", e);
        }
    }

    @Override
    public Product getById(int id) {
        try(PreparedStatement ps = connection.prepareStatement("select * from Storage where product_id = ?")) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getInt("product_amount"), rs.getDouble("product_price"), rs.getString("category"));
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Can't get product by id: " + id, e);
        }
    }

    @Override
    public int deleteAll() {
        try(PreparedStatement ps = connection.prepareStatement("delete from Storage")) {
            int deleted = ps.executeUpdate();
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete all products", e);
        }
    }

    @Override
    public int deleteById(int id) {
        try(PreparedStatement ps = connection.prepareStatement("delete from Storage where product_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete all products", e);
        }
    }

    public Product getProductByName(String name) {
        try(PreparedStatement ps = connection.prepareStatement("select* from Storage where product_name = ?")) {
            ps.setString(1, name);

            List<Product> products = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getInt("product_amount"), rs.getDouble("product_price"), rs.getString("category")));
                }
            }

            return products.isEmpty() ? null : products.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete all products", e);
        }
    }
}
