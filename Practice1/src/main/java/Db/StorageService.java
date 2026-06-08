package Db;

import Tools.Product;
import Tools.ProductFilter;
import Tools.ProductUpdate;

import java.util.List;

public class StorageService{
    private StorageDb storageDb;

    public StorageService(){
        storageDb = new StorageDb("jdbc:mysql://localhost:3306/store_db", "root", "root");
    }

    public String getById(Integer id){
        Product product = storageDb.getById(id);
        if(product == null){
            return "Product not found";
        }

        return product.toString();
    }

    public String create(Product product){
        int create = storageDb.insert(product);
        if(create > 0) {
            return "Product created with id: " + create;
        } else {
            return "Product not created";
        }
    }

    public String update(ProductUpdate productUpdate){
        Product existing = storageDb.getById(productUpdate.getId());
        if(existing == null) return "Product not found";

        if(productUpdate.getName() != null){
            existing.setName(productUpdate.getName());
        }
        if(productUpdate.getAmount() != null){
            existing.setAmount(productUpdate.getAmount());
        }
        if(productUpdate.getPrice() != null){
            existing.setPrice(productUpdate.getPrice());
        }
        if(productUpdate.getCategory() != null){
            existing.setCategory(productUpdate.getCategory());
        }

        Product updated = storageDb.update(existing);
        if(updated != null) {
            return "Product updated " + updated;
        }
        return "Updating failed";
    }

    public String deleteById(Integer id){
        int delete = storageDb.deleteById(id);
        if(delete < 1) {
            return "Deleting failed";
        }
        return "Product deleted successfully";
    }

    public String deleteAll(){
        int delete = storageDb.deleteAll();
        if(delete < 1) {
            return "Deleting failed";
        }
        return "All products deleted successfully";
    }

    public String findAndFilter(ProductFilter productFilter){
        List<Product> products = storageDb.getAll(productFilter);
        StringBuilder sb = new StringBuilder();

        if(products.isEmpty()){
            return "No products found";
        }

        for(Product product : products){
            sb.append(product + "\n");
        }

        return sb.toString();
    }

    //for my tests

    public int getAmountByName(String productName){
        Product product = storageDb.getProductByName(productName);
        return product.getAmount();
    }

    public double getPriceByName(String productName){
        Product product = storageDb.getProductByName(productName);
        return product.getPrice();
    }

    public String getCategoryByName(String productName){
        Product product = storageDb.getProductByName(productName);
        return product.getCategory();
    }

    public int getIdByName(String productName){
        Product product = storageDb.getProductByName(productName);
        return product.getId();
    }
}
