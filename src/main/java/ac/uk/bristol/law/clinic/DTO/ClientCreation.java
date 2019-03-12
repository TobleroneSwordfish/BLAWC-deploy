package ac.uk.bristol.law.clinic.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

public class ClientCreation {
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String[] getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String[] fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getAddress1() {
        return address1;
    }
    public String getAddress2() {
        return address2;
    }
    public String getCity() {
        return city;
    }
    public String getCounty() {
        return county;
    }
    public String getPostCode() {
        return postCode;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }
    public void setAddress2(String address2) {
        this.address2 = address2;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setCounty(String county) {
        this.county = county;
    }
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    private String firstName;
    private String lastName;
    //ew
    @Pattern(regexp="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "Email invalid")
    private String email;
    private String fullAddress[];
    private String address1;
    private String address2;
    private String city;
    private String county;
    private String postCode;
    private String telephone;


    @Getter
    @Setter
    private String gender;

    @Getter
    @Setter
    private String age;
    @Getter
    @Setter
    private String ethnicity;
    @Getter
    @Setter
    private String disibility;

    @Getter
    @Setter
    private Long caseID;
}
