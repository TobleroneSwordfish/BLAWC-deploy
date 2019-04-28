package ac.uk.bristol.law.clinic.DTO;

import ac.uk.bristol.law.clinic.entities.User;
import lombok.Getter;
import lombok.Setter;

public class UserEdit
{
    public UserEdit()
    {

    }

    public UserEdit(User user)
    {
        this.email = user.getEmail();
        this.name = user.getFirstName().concat(" ").concat(user.getLastName());
    }

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String oldPassword;
}
