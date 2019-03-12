package ac.uk.bristol.law.clinic.entities;
import ac.uk.bristol.law.clinic.entities.User;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.repositories.UserRepository;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name="documents")
public class Documents {

    @Column(name="id") @Getter @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="client_access") @Getter @Setter
    boolean clientAccess;

    @Column(name="name") @Getter @Setter
    String name;

    @Column(name="URL") @Getter @Setter
    String url;

    //one to many, a case has many documents (but it doesnt have to, or a document doesnt have to be part of a case)
//    @JoinColumn(name="case_id") @Getter @Setter
//    Long caseId;
//   or optional case (if it is associated to a case,
    //many docs to one case


    @ManyToOne(targetEntity = Case.class)
    @JoinColumn(name="case_id") @Getter
    Case caseowner;

    public void setCaseowner(Case cases){
        this.caseowner = cases;
        cases.addDoc(this);
    }


    @ManyToOne(targetEntity = User.class) @JoinColumn(name="originator") @Getter @Setter
    User originator;

    @Getter
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL},
            mappedBy = "documents")
    private Set<User> lawyers = new HashSet<>();


    public void removeLawyer(User lawyer)
    {
        lawyer.removeDocument(this);

        Set<User> newLawyers = new HashSet<>();
        for(User l : this.lawyers)
        {
            if(!l.getId().equals(lawyer.getId()))
            {
                newLawyers.add(l);
            }
        }
        this.lawyers = newLawyers;
    }

    public void addLawyer(User newLawyer, UserRepository repo){
        this.lawyers.add(newLawyer);
        newLawyer.addDocument(this);

        repo.save(newLawyer);
    }

    public Documents(String name, boolean clientAccess, User originator, String url)
    {
        this.name = name;
        this.url = url;
        this.clientAccess = clientAccess;
        this.originator = originator;
    }

    public Documents() {}

}
