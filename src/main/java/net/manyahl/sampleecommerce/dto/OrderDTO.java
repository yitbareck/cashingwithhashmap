package net.manyahl.sampleecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.manyahl.sampleecommerce.model.Orders;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDTO {
    private String customerEmail;
    private String productCode;
    private int quantity;
    public Orders convertToModel(){
        return Orders.builder()
                .email(customerEmail)
                .code(productCode)
                .quantity(quantity)
                .build();
    }
}
