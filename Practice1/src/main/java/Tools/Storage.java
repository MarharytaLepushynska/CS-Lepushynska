package Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {
    HashMap<String, Double> productPrice;
    HashMap<String, Integer> productCount;
    HashMap<String, List<String>> categoryProducts;

    public Storage() {
        productPrice = new HashMap<>();
        productCount = new HashMap<>();
        categoryProducts = new HashMap<>();
    }

    public synchronized int getProductCount(String productName) {
        return productCount.getOrDefault(productName, 0);
    }

    public synchronized String reduceProductCount(String productName, int count) {
        int curentCount = productCount.getOrDefault(productName, 0);
        int newCount = curentCount - count;
        if (curentCount >= count) {
            productCount.put(productName, curentCount - count);
        } else {
            throw new ArithmeticException("Not enough products on storage");
        }
        return "Now on storage " + newCount + " of " + productName ;
    }

    public synchronized String increaseProductCount(String productName, int count) {
        int curentCount = productCount.getOrDefault(productName, 0);
        int newCount = curentCount + count;
        productCount.put(productName, curentCount + count);
        return "Now on storage " + newCount + " of " + productName ;
    }

    public synchronized String addCategory(String categoryName) {
        categoryProducts.put(categoryName, new ArrayList<>());
        return categoryName + " was added";
    }

    public synchronized String addProduct(String category, String product) {
        categoryProducts.computeIfAbsent(category, k -> new ArrayList<>()).add(product);
        return product + " was added to category " + category;
    }

    public synchronized String addProductPrice(String productName, double price) {
        productPrice.put(productName, price);
        return "Price: " + price + " was added to product " + productName;
    }

    public void addTestProduct(String name, int count) {
        productCount.put(name, count);
    }

    public boolean isCategoryPresent(String category) {
        return categoryProducts.containsKey(category);
    }

    public boolean isProductPresentInCategory(String category, String product) {
        return categoryProducts.get(category).contains(product);
    }

    public boolean isPricePresentInProduct(String product, double price) {
        return productPrice.containsKey(product) && productPrice.get(product) >= price;
    }
}
