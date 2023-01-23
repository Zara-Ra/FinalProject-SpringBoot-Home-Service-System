package ir.maktab.finalproject.service;

import ir.maktab.finalproject.data.entity.CustomerOrder;
import ir.maktab.finalproject.data.entity.roles.Customer;
import ir.maktab.finalproject.data.entity.services.SubService;
import ir.maktab.finalproject.data.enums.OrderStatus;
import ir.maktab.finalproject.service.exception.OrderRequirementException;
import ir.maktab.finalproject.service.exception.PasswordException;
import ir.maktab.finalproject.util.exception.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class CustomerServiceTest {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private SubServiceService subServiceService;
    private static Customer customer;
    private static SubService subService;
    private static Date afterNow;
    private static Date beforeNow;


    @BeforeAll
    static void beforeAll() {
        customer = Customer.builder()
                .email("customer@email.com")
                .password("customer")
                .firstName("Customer Name")
                .lastName("Customer Lastname")
                .customerOrderList(new ArrayList<>()).build();

        subService = SubService.builder()
                .subName("SubService Test")
                .basePrice(100).build();


        long now = System.currentTimeMillis();
        afterNow = new Date(now + 900000);
        beforeNow = new Date(now - 900000);
    }

    @Order(1)
    @Test
    void signUpTest() {
        Customer newCustomer = customerService.signUp(customer);
        assertAll(
                () -> assertEquals("customer@email.com", newCustomer.getEmail()),
                () -> assertEquals("customer", newCustomer.getPassword()),
                () -> assertEquals("Customer Name", newCustomer.getFirstName()),
                () -> assertEquals("Customer Lastname", newCustomer.getLastName())
        );
    }

    @Order(2)
    @ParameterizedTest
    @CsvSource(value = {
            "customer@email.com,12345678,Customer,Customer,Already Registered With This Email",
            ",12345678,Customer,Customer,Invalid Email",
            "email.email.com,12345678,Customer,Customer,Invalid Email",
            "email@email.com,NIL,Customer,Customer,Invalid Password should be 8 characters including alphanumeric values",
            "email@email.com,123456,Customer,Customer,Invalid Password should be 8 characters including alphanumeric values",
            "email@email.com,12345678,NIL,Customer,Invalid Name Only Alphabetic Characters Accepted",
            "email@email.com,12345678,Customer123,Customer,Invalid Name Only Alphabetic Characters Accepted",
            "email@email.com,12345678,Customer,NIL,Invalid Name Only Alphabetic Characters Accepted",
            "email@email.com,12345678,Customer,Customer123,Invalid Name Only Alphabetic Characters Accepted"
    }, nullValues = "NIL")
    void invalidSignUpTest(String email, String password, String firstName, String lastName, String exceptionMsg) {
        Customer invalidCustomer = Customer.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName).build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> customerService.signUp(invalidCustomer));
        assertEquals(exceptionMsg, exception.getMessage());
    }

    @Order(3)
    @Test
    void signInTest() {
        Customer signInCustomer = customerService.signIn("customer@email.com", "customer");
        assertEquals(customer, signInCustomer);
    }

    @Order(4)
    @ParameterizedTest
    @CsvSource({"invalid@email.com,12345678,No User Registered With This Email",
            "customer@email.com,11111111,Incorrect Password"
    })
    void invalidSignInTest(String email, String password, String exceptionMsg) {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.signIn(email, password));
        assertEquals(exceptionMsg, exception.getMessage());
    }

    @Order(5)
    @Test
    void changePasswordTest() {
        Customer changePasswordCustomer = customerService.changePassword(customer, "customer", "12345678");
        assertEquals("12345678", changePasswordCustomer.getPassword());
    }

    @Order(6)
    @Test
    void invalidChangePasswordTest() {
        PasswordException exception = assertThrows(PasswordException.class,
                () -> customerService.changePassword(customer, "invalidOldPassword", "newPassword"));
        assertEquals("Entered Password Doesn't Match", exception.getMessage());
    }

    @Order(7)
    @Test
    void requestOrderTest() {
        subServiceService.addSubService(subService);
        CustomerOrder order = CustomerOrder.builder()
                .customer(customer)
                .subService(subService)
                .price(200)
                .description("Order description")
                .preferredDate(afterNow).build();

        customerService.requestOrder(customer, order);

        assertAll(
                () -> assertEquals(1, customer.getCustomerOrderList().size()),
                () -> assertEquals(OrderStatus.WAITING_FOR_EXPERT_OFFER, customer.getCustomerOrderList().get(0).getStatus()));
    }
    @Test
    @Order(8)
    void invalidPriceForRequestOrderTest(){
        CustomerOrder order = CustomerOrder.builder()
                .customer(customer)
                .subService(subService)
                .price(50)
                .description("Invalid price")
                .preferredDate(afterNow).build();
        OrderRequirementException exception  = assertThrows(OrderRequirementException.class
                ,()->customerService.requestOrder(customer,order));
        assertEquals("Price Of Order Should Be Greater Than Base Price Of The Sub-Service:( "+
                order.getSubService().getSubName() + " "+order.getSubService().getBasePrice() +" )",exception.getMessage());
    }
    @Order(9)
    @Test
    void invalidPreferredDateForRequestOrderTest(){
        CustomerOrder order = CustomerOrder.builder()
                .customer(customer)
                .subService(subService)
                .price(200)
                .description("Invalid preferred date")
                .preferredDate(beforeNow).build();
        OrderRequirementException exception  = assertThrows(OrderRequirementException.class
                ,()->customerService.requestOrder(customer,order));
        assertEquals("The Preferred Date Should Be After Now",exception.getMessage());
    }
}
