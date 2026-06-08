package Tools;

import lombok.Data;

@Data
public class Product {
    private Integer id;
    private String name;
    private Integer amount;
    private Double price;
    private String category;

    public Product(String name, Integer amount, Double price, String category) {
        this(null, name, amount, price, category);
    }

    public Product(Integer id, String name, Integer amount, Double price, String category) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.category = category;
    }
}
