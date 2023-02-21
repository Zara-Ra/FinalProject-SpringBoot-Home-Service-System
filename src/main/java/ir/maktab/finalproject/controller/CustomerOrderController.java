package ir.maktab.finalproject.controller;

import ir.maktab.finalproject.controller.enums.SortType;
import ir.maktab.finalproject.data.dto.*;
import ir.maktab.finalproject.data.entity.CustomerOrder;
import ir.maktab.finalproject.data.entity.ExpertOffer;
import ir.maktab.finalproject.data.enums.PaymentType;
import ir.maktab.finalproject.data.mapper.OfferMapper;
import ir.maktab.finalproject.data.mapper.OrderMapper;
import ir.maktab.finalproject.data.mapper.ReviewMapper;
import ir.maktab.finalproject.data.mapper.UserMapper;
import ir.maktab.finalproject.service.exception.NotExistsException;
import ir.maktab.finalproject.service.impl.CustomerOrderService;
import ir.maktab.finalproject.util.exception.ValidationException;
import ir.maktab.finalproject.util.sort.SortExpertOffer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/order")
@Validated
public class CustomerOrderController extends MainController {
    private final CustomerOrderService customerOrderService;

    public CustomerOrderController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @PostMapping("/request-order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String requestOrder(@Valid @RequestBody CustomerOrderDto customerOrderDto, Principal principal) {
        log.info("*** Request Order for: {} ***", customerOrderDto);
        customerOrderDto.setCustomerEmail(principal.getName());
        CustomerOrder customerOrder = customerOrderService.requestOrder(OrderMapper.INSTANCE.convertOrder(customerOrderDto));
        log.info("*** Order Saved: {} ***", customerOrder);
        return "Order Has Been Saved";
    }

    @GetMapping("/all-orders-for-sub")
    @PreAuthorize("hasAnyRole('EXPERT','ADMIN')")
    public List<CustomerOrderDto> findAllBySubService(@RequestParam String subServiceName) {
        log.info("*** Find All Orders For Sub Service: {} ***", subServiceName);
        List<CustomerOrderDto> customerOrderDtos = OrderMapper.INSTANCE
                .convertOrderList(customerOrderService.findAllBySubServiceAndTwoStatus(subServiceName));
        log.info("*** All Orders For {} : {}***", subServiceName, customerOrderDtos);
        return customerOrderDtos;
    }

    @GetMapping("/all-offers-for-order")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public List<ExpertOfferDto> getAllOffersForOrder(@RequestParam @Min(1) Integer orderId) {
        log.info("*** Find All Offers For Order: {} ***", orderId);
        List<ExpertOfferDto> expertOfferDtos = OfferMapper.INSTANCE.convertOfferList
                (customerOrderService.getAllOffersForOrder(orderId, SortExpertOffer.SortByPriceAcs));
        log.info("*** All Offers For {} : {}***", orderId, expertOfferDtos);
        return expertOfferDtos;
    }

    @GetMapping("/all-offers-for-order-sort")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public List<ExpertOfferDto> getAllOffersForOrder(@RequestParam @Min(1) Integer orderId, @RequestParam String sortBy) {
        log.info("*** Find All Offers For Order {}, Sorted By {} ***", orderId, sortBy);
        Comparator<ExpertOffer> comparator;
        try {
            comparator = SortType.valueOf(sortBy).getComparator();
        } catch (IllegalArgumentException e) {
            log.error("*** Invalid Sort Type Entered: {} ***", sortBy);
            throw new ValidationException(messageSource.getMessage("errors.message.invalid_sort"));
        }
        List<ExpertOfferDto> expertOfferDtos = OfferMapper.INSTANCE.convertOfferList
                (customerOrderService.getAllOffersForOrder(orderId, comparator));
        log.info("*** All Offers For Order {}, Sorted By {} ***", orderId, expertOfferDtos);
        return expertOfferDtos;
    }

    @GetMapping("/find-order")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','EXPERT')")
    public CustomerOrderDto findOrder(@RequestParam @Min(1) Integer orderId) {
        log.info("*** Find Order: {} ***", orderId);
        CustomerOrderDto customerOrderDto = OrderMapper.INSTANCE.convertOrder(customerOrderService.findById(orderId).orElseThrow(
                () -> new NotExistsException(messageSource.getMessage("errors.message.order_not_exists"))
        ));
        log.info("*** Order Found: {} ***", customerOrderDto);
        return customerOrderDto;
    }

    @GetMapping("/find-accepted-order") //TODO REMOVE
    @PreAuthorize("hasRole('CUSTOMER')")
    public AcceptedOrderDto findAcceptedOrder(@RequestParam @Min(1) Integer orderId) {
        log.info("*** Find Accepted Order: {} ***", orderId);
        AcceptedOrderDto acceptedOrderDto = OrderMapper.INSTANCE.convertAcceptedOrder(customerOrderService.findById(orderId).orElseThrow(
                () -> new NotExistsException(messageSource.getMessage("errors.message.order_not_exists"))
        ));
        log.info("*** Accepted Order: {} ***", acceptedOrderDto);
        return acceptedOrderDto;
    }

    @CrossOrigin
    @PostMapping("/pay-online")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String payOnline(@Valid @ModelAttribute PaymentDto paymentDto, HttpServletRequest request) {
        log.info("*** Pay Online For: {} ***", paymentDto);
        PaymentUserDto paymentUserDto = new PaymentUserDto();
        paymentUserDto.setCustomerEmail(request.getUserPrincipal().getName());
        paymentUserDto.setOrderId(paymentDto.getOrderId());
        paymentUserDto.setPaymentType(PaymentType.ONLINE);

        try {
            Date expirationDate = new SimpleDateFormat("yyyy-MM").parse(paymentDto.getExpirationDate());
            if (expirationDate.before(new Date())) {
                log.error("*** Card Expired {}: ***", paymentDto.getExpirationDate());
                throw new ValidationException(messageSource.getMessage("errors.message.invalid_card_date"));
            }
        } catch (ParseException e) {
            log.error("*** Invalid Date Format: {} ***", paymentDto.getExpirationDate());
            throw new ValidationException(messageSource.getMessage("errors.message.invalid_date"));
        }
        if (!paymentDto.getCaptcha().equals(request.getSession().getAttribute("captcha")))
            throw new ValidationException(messageSource.getMessage("errors.message.invalid_captcha"));
        //call bank
        customerOrderService.pay(paymentUserDto);
        log.info("*** Paid Online for: {} ***", paymentDto.getOrderId());
        return "Order Payed Online";
    }

    @GetMapping("/pay-credit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String payFromCredit(@RequestBody PaymentUserDto paymentUserDto,Principal principal) {
        log.info("*** Pay Online For: {} ***", paymentUserDto.getOrderId());
        paymentUserDto.setCustomerEmail(principal.getName());
        paymentUserDto.setPaymentType(PaymentType.CREDIT);
        customerOrderService.pay(paymentUserDto);
        log.info("*** Paid From Credit for: {} ***", paymentUserDto.getOrderId());
        return "Order Payed By Credit";
    }

    @PostMapping("/add-review")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String addReview(@Valid @RequestBody ReviewDto reviewDto,Principal principal) {
        log.info("*** Add Review For Order: {} ,Review: {} ***", reviewDto.getOrderId(), reviewDto);
        reviewDto.setCustomerEmail(principal.getName());
        customerOrderService.addReview(ReviewMapper.INSTANCE.convertReview(reviewDto));
        log.info("*** Review Added ***");
        return "Review Added";
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public Iterable<AcceptedOrderDto> search(@RequestParam String search) {
        log.info("*** Search for: {} ***", search);
        Iterable<AcceptedOrderDto> customerOrderDtos = OrderMapper.INSTANCE.convertCustomerOrderIterator(customerOrderService.findAll(search));
        log.info("*** : Search Results: {} ***", customerOrderDtos);
        return customerOrderDtos;
    }
}
