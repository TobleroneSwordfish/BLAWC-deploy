package ac.uk.bristol.law.clinic.DTO;

import ac.uk.bristol.law.clinic.entities.User;
import lombok.Setter;
import lombok.Getter;

import javax.validation.constraints.Pattern;

public class UserCreation
{
    @Getter @Setter
    //ew
    @Pattern(regexp="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "Email invalid")
    private String email;
    @Getter @Setter
    private String firstName;
    @Getter @Setter
    private String lastName;
    @Getter @Setter
    private String password;
    @Getter @Setter
    private Integer ID;
    @Getter @Setter
    private User.PermissionLevel permissionLevel;
}