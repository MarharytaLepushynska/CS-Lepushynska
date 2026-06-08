package Db;

import Tools.Product;
import Tools.ProductFilter;

import java.util.List;

public interface Db {

    int insert(Product product);

    Product update(Product product);

    int count();

    List<Product> getAll(ProductFilter filter);

    Product getById(int id);

    int deleteAll();

    int deleteById(int id);

}
