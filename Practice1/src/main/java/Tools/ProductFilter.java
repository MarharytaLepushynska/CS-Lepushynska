package Tools;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductFilter {
    private String name;

    private Integer amountFrom;
    private Integer amountTo;

    private Double priceFrom;
    private Double priceTo;

    private String category;

    private Integer pageNumber;
    private Integer pageSize;
}
