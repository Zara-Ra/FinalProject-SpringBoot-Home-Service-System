package ir.maktab.finalproject.data.dto;

import ir.maktab.finalproject.data.enums.OrderStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptedOrderDto {

    private Integer id;

    @NotNull
    @Email
    private String customerEmail;

    @NotNull
    private String subServiceName;

    @NotNull
    private String offerPrice;

    private String description;

    private OrderStatus status;

    @NotNull
    private AddressDto address;

    private Integer offerId;

    private String startDate;

    private String finishDate;
}