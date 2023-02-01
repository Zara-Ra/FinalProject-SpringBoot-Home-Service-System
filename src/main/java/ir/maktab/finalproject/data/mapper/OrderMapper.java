package ir.maktab.finalproject.data.mapper;

import ir.maktab.finalproject.data.dto.CustomerOrderDto;
import ir.maktab.finalproject.data.entity.CustomerOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    
    @Mapping(source = "customerEmail", target = "customer.email")
    @Mapping(source = "subServiceName", target = "subService.subName")
    @Mapping(target = "preferredDate", source = "preferredDate", dateFormat = "yyyy-MM-dd hh:mm")
    CustomerOrder convertOrder(CustomerOrderDto customerOrderDto);

    @Mapping(source = "customer.email" , target = "customerEmail")
    @Mapping(source = "subService.subName" , target = "subServiceName")
    @Mapping(target = "preferredDate", source = "preferredDate", dateFormat = "yyyy-MM-dd hh:mm")
    CustomerOrderDto convertOrder(CustomerOrder customerOrder);

    List<CustomerOrderDto> convertOrderList(List<CustomerOrder> orders);

}
