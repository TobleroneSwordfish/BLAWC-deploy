package ac.uk.bristol.law.clinic.entities;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.repositories.CaseRepository;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity @Table(name="clients")
public class Client  implements Serializable {

    @Column(name = "id")
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    @Getter
    @Setter
    private String firstName;

    @Column(name = "last_name")
    @Getter
    @Setter
    private String lastName;

    @Email
    @Column(name = "email")
    @Getter
    @Setter
    private String email;

    @Column(name = "address1")
    @Getter
    @Setter
    private String address1;

    @Column(name = "address2")
    @Getter
    @Setter
    private String address2;

    @Column(name = "city")
    @Getter
    @Setter
    private String city;

    @Column(name = "county")
    @Getter
    @Setter
    private String county;

    @Column(name = "post_code")
    @Getter
    @Setter
    private String postCode;

    @Column(name = "telephone")
    @Getter
    @Setter
    private String telephone;

    @Getter
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL},
            mappedBy = "clients")
    private Set<Case> cases = new HashSet<>();

    //we add the other way since this does not own many to many relationship
    public void addCase(Case newCase, CaseRepository repo) {
        this.cases.add(newCase);
        newCase.addClient(this);

        repo.save(newCase);
    }



    public void removeCase(Case cases)
    {
        cases.removeClient(this);

        Set<Case> newCases = new HashSet<>();
        for(Case c : this.cases)
        {
            if(!c.getId().equals(cases.getId()))
            {
                newCases.add(c);
            }
        }

        this.cases = newCases;
    }


    //this is called getUsers in the API, but should we call this getLawyers for consistency?
    public List<User> getUsers()
    {
        List<User> lawyersList = new ArrayList<>();
        for(Case c : cases)
        {
            for(User lawyer : c.getUsers())
            {
                lawyersList.add(lawyer);
            }
        }

        return lawyersList;
    }

    public String[] getAddress()
    {
        String[] addressArray = new String[]{address1, address2, city, county};
        return addressArray;
    }


    //---------------------IMPORTANT: Note on enums-------------------------
    //this is stored as a string in the database so don't modify any of the enum names unless you're sure you know what you're doing
    //adding new enumerations is fine, just make sure they're handled in getDescription below

    //PNTS = prefer not to say
    public enum Genders {MALE, FEMALE, NON_BIN, PNTS}
    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private Genders gender;

    public enum Ages { U18, INCLUSIVE_18_65, ABOVE_65, PNTS}
    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private Ages age;

    public enum Ethnicities {WHITE, MINORITIES, PNTS}
    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private Ethnicities ethnicity;

    //PNTS = prefer not to say
    public enum Disibilities {YES, NO, PNTS}
    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private Disibilities disability;


    //returning enums as strigns to display
    public String genderString()
    {
        if(this.gender == null){
            return "Not specified";
        }else {
            switch (this.gender) {
                case MALE:
                    return "Male";
                case FEMALE:
                    return "Female";
                case NON_BIN:
                    return "non-binary";
                case PNTS:
                    return "Prefer not to say";
                default:
                    return "Not specified";
            }
        }
    }
    public String ageString()
    {
        if(this.age == null){
            return "Not specified";
        }else {
            switch (this.age) {
                case U18:
                    return "Under 18";
                case INCLUSIVE_18_65:
                    return "18 - 65";
                case ABOVE_65:
                    return "65+";
                case PNTS:
                    return "Prefer not to say";
                default:
                    return "Not specified";
            }
        }
    }
    public String disibilityString()
    {
        if(this.disability == null){
            return "Not specified";
        }else {
            switch (this.disability) {
                case YES:
                    return "Yes";
                case NO:
                    return "No";
                case PNTS:
                    return "Prefer not to say";
                default:
                    return "Not specified";
            }
        }
    }
    public String ethnicityString() {
        if (this.ethnicity == null) {
            return "Not specified";
        } else {
            switch (this.ethnicity) {
                case WHITE:
                    return "White";
                case MINORITIES:
                    return "Black or ethnic minority";
                case PNTS:
                    return "Prefer not to say";
                default:
                    return "Not specified";
            }
        }
    }


    public Client(String firstName, String lastName, String email, String[] address, String telephone)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address1 = address[0];
        this.address2 = address[1];
        this.city = address[2];
        this.county = address[3];
        this.postCode = address[4];
        this.telephone = telephone;
    }

    public Client(String firstName, String lastName, String email, String[] address, String telephone, Ages age, Ethnicities ethnicity, Disibilities disability, Genders gender)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address1 = address[0];
        this.address2 = address[1];
        this.city = address[2];
        this.county = address[3];
        this.postCode = address[4];
        this.telephone = telephone;
        this.age = age;
        this.ethnicity = ethnicity;
        this.gender = gender;
        this.disability = disability;
    }

    public Client() {}
}
