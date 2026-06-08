package Tools;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductUpdate {
    private Integer id;
    private String name;
    private Integer amount;
    private Double price;
    private String category;
}
