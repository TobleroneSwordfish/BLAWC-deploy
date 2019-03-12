package ac.uk.bristol.law.clinic.DTO;

import ac.uk.bristol.law.clinic.entities.User;

public class UserEdit
{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserEdit()
    {

    }

    public UserEdit(User user)
    {
        this.email = user.getEmail();
        this.name = user.getFirstName().concat(" ").concat(user.getLastName());
    }

    private String name;
    private String email;
}
