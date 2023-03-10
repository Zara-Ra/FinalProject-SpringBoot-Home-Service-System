package ir.maktab.finalproject.data.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponseDto {

    private Integer id;

    @Email
    private String email;

    @Pattern(regexp = "^[a-zA-Z ]{2,}")
    private String firstName;

    @Pattern(regexp = "^[a-zA-Z ]{2,}")
    private String lastName;
}
