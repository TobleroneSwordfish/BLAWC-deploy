package ac.uk.bristol.law.clinic.entities;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.entities.cases.CaseStep;
import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import ac.uk.bristol.law.clinic.repositories.CaseRepository;
import ac.uk.bristol.law.clinic.repositories.CaseStepRepository;
import ac.uk.bristol.law.clinic.repositories.WalkthroughRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.PermissionDeniedDataAccessException;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Entity @Table(name="users")
public class User implements Serializable {

    public enum PermissionLevel {Admin, Student, Client}

    public Iterable<Case> getAllCases(CaseRepository caseRepository){
        if(!this.permissionLevel.equals(PermissionLevel.Admin)){
            throw new PermissionDeniedDataAccessException("only Admin users can access all myCases!", new Exception());
        }
        return caseRepository.findAll();
    }

//    public Iterable<Case> getAllCases(WalkthroughStepRepository walkthroughStepRepository){
//        if(!this.permissionLevel.equals(PermissionLevel.Admin)){
//            throw new PermissionDeniedDataAccessException("only Admin users can access all myCases!", new Exception());
//        }
//        return caseRepository.findAll();
//    }

    public String getUsername(){
        return this.email;
    }

//    public Iterable<Walkthrough> getAllWalkthroughs(){
//        return this.walkthroughRepository.findAll();
//    }

    public Iterable<Walkthrough> getAllWalkthroughs(WalkthroughRepository walkthroughRepository){
        return walkthroughRepository.findAll();
    }

    @Column(name="id") @Getter @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="first_name") @Getter @Setter
    private String firstName;

    @Column(name="last_name") @Getter @Setter
    private String lastName;

    @Column(name="email") @Getter @Setter
    private String email;

    @Column(name = "password") @Getter @Setter
    private String password;

    //The API defines this as an enum of PermissonLevel {Admin, Student, Client}, but I think this version is better anyway
    @Column(name="permission") @Getter @Setter
    private PermissionLevel permissionLevel;



    //This class is the owner, so it'll only be persisted this way
    @Getter @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cases_users",
            joinColumns = { @JoinColumn(name="user_id") },
            inverseJoinColumns = { @JoinColumn(name = "case_id") }
    )
    private Set<Case> myCases = new HashSet<>();

    @OneToMany(mappedBy = "supervisor") @Getter @Setter //cascade =ALL;
    private List<Case> supervisedCases = new ArrayList<>();
    public void addSupervisedCase(Case caseAdded){
        this.supervisedCases.add(caseAdded);
    }
    public void removeSupervisedCase(Case caseRemoved){this.supervisedCases.remove(caseRemoved);}

    public void addCase(Case cases)
    {
        this.myCases.add(cases);
    }

    public void removeCase(Case cases)
    {
        Set<Case> newCases = new HashSet<>();
        for(Case c : this.myCases)
        {
            if(!c.getId().equals(cases.getId()))
            {
                newCases.add(c);
            }
        }

        this.myCases = newCases;
//        this.myCases.remove(myCases);
    }


    @Getter @Setter
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(
            name = "documents_users",
            joinColumns = { @JoinColumn(name="user_id") },
            inverseJoinColumns = { @JoinColumn(name = "document") }
    )
    private Set<Documents> documents = new HashSet<>();


    public void addDocument(Documents document)
    {
        this.documents.add(document);
    }

    public void removeDocument(Documents document)
    {
        this.documents.remove(document);
    }

    //returns all files in myCases this user is assigned to
    public Set<Documents> getFiles()
    {
        return this.getDocuments();
    }

    //create new lawyer/user
    public User(String firstName, String lastName, String email, String password, PermissionLevel permissionLevel)//
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.permissionLevel = permissionLevel;
        this.password = password;
    }


//    //retrieve an existing lawyer
//    static public User retrieveUser(Long id)
//    {
//        Optional<User> user = this.lawyersRepository.findById(id);
//        if(user.isPresent()){
//            System.out.println("exists");
//        }else{
//            System.out.println("no");
//        }
//        return user.get();
//        }
//
//    //retrieve an existing lawyer
//    static public User retrieveUser(String username)//NOTE:username ==email right now :(
//    {
//        Iterator<User> it = lawyersRepository.findAll().iterator();
//        while(it.hasNext()){
//            User user = it.next();
//            if(user.email.equals(username)){
//                return user;
//            }
//        }
//        throw new UsernameNotFoundException("no user exists");
//    }

    //gets the next n steps assigned to the user in date order
    public List<CaseStep> nextNSteps(int n, CaseStepRepository repo)
    {
        List<CaseStep> steps = new ArrayList<>();
        List<BigInteger> values = repo.findAllByUserOrderByDateDue(this, n);
        for (BigInteger i : values)
        {
            long l = i.longValue();
            steps.add(repo.findById(l).get());
        }
        return steps;
    }

    public String getName(){
        return this.getFirstName().concat(" ").concat(this.getLastName());
    }

    public List<Client> getMyClients()
    {
        List<Client> clientsList = new ArrayList<>();
        for(Case cases : this.myCases)
        {
            clientsList.addAll(cases.getClients());
        }
        return clientsList;
    }

    //TODO: do efficiently in DB isntead of like this
    public List<Case> findCases(String query, CaseRepository caseRepository)
    {
        Iterable<Case> allCases = caseRepository.findAll();
        final String lowerQuery = query.toLowerCase();
        List<Case> list = new ArrayList<>();
        if (allCases.iterator().hasNext())
        {
            for (Case c : allCases)
            {
                List<String> props = new ArrayList<>();
                props.add(Long.toString(c.getId()));
                props.add(c.getStatus().toString());
                props.add(c.getWalkthrough().getName());
                props.add(c.getName());
                for (Client client : c.getClients())
                {
                    props.add(client.getFirstName());
                    props.add(client.getLastName());
                }
                props = props.stream().map(p -> p.toLowerCase()).collect(Collectors.toList());
                if (props.stream().anyMatch(p -> p.contains(lowerQuery)))
                {
                    list.add(c);
                }
            }
        }
        return list;
    }

    //TODOq: time_spent is apparently a column in our join table for cases_lawyers, but I don't recall doing anything with it
    //returns the number of minutes for this user on this case
//    public int getTimings(Case caseObject)
//    {
//    }
//
//    //NOTE: The value timing passed in should be added to the case's current timing to increment


    //NOTE: The value timing passed in should be added to the case's current timing to increment
//    public void setTimings(Case caseObject, int timing)
//    {
//    }


    //q: not done yet, but isn't this the same as just getMyCases that we have?
//    return all myCases user is allowed to see (the same as getMyCases for non-Admin users)
//    public List<Case> findCases(String caseName, String clientName, String userName, String caseType)
//    {
//    }

//
//    //TODOq: again isn't this the same as just getClients that we have?
//    public List<Client> findClients(String clientName, String caseName, String clientAddress)
//    {
//    }


    public User() {}
}
