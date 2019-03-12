package ac.uk.bristol.law.clinic.entities.cases;
import ac.uk.bristol.law.clinic.entities.Action;
import ac.uk.bristol.law.clinic.entities.Client;
import ac.uk.bristol.law.clinic.entities.Documents;
import ac.uk.bristol.law.clinic.entities.User;
import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import ac.uk.bristol.law.clinic.repositories.ActionRepository;
import ac.uk.bristol.law.clinic.repositories.CaseStepRepository;
import ac.uk.bristol.law.clinic.repositories.DocumentsRepository;
import ac.uk.bristol.law.clinic.repositories.StepDocsRepository;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Entity @Table(name="cases")
public class Case implements Serializable {

    public enum CaseStatus {Active, Closed}

    @Column(name="id") @Getter @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name") @Getter @Setter
    private String name;

    @Column(name="case_status") @Getter @Setter
    private CaseStatus status;


    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="supervisor") @Getter
    User supervisor;


    //
    @OneToMany(mappedBy = "concreteCase") @Getter @Setter //cascade =ALL;
    private List<CaseStep> steps = new ArrayList<>();

    public void addStep(CaseStep step)
    {
        this.steps.add(step);
    }
    //

//    public List<WalkthroughStep> getCaseSteps(){
//        return this.walkthrough.getSteps();
//    }
//    DEPRICATED

    @ManyToOne(targetEntity = Walkthrough.class, fetch = FetchType.EAGER) @JoinColumn(name="walkthroughs") @Getter @Setter
    private Walkthrough walkthrough; //this used to be called "walkthroughIndex" //changed type to walkthrough

    //I presume this is our version of the API's "getCurrentStepNumber" and "setCurrentStepNumber"
    @Column(name="step") @Getter @Setter
    private int step;

    @Getter @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cases_clients",
            joinColumns = { @JoinColumn(name="case_id") },
            inverseJoinColumns = { @JoinColumn(name = "client_id") }
    )
    private Set<Client> clients = new HashSet<>();

    public void addClient(Client client)
    {
        this.clients.add(client);
    }

    public void removeClient(Client client)
    {
        this.clients.remove(client);
    }

    @Getter
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL},
            mappedBy = "myCases")
    private Set<User> Users = new HashSet<>();//Note:Users are not clients here
    public void addLawyer(User newLawyer){
        this.Users.add(newLawyer);
        newLawyer.addCase(this);

        //repo.save(newLawyer);
    }


    public void removeUser(User lawyer)
    {
        lawyer.removeCase(this);

        Set<User> newLawyers = new HashSet<>();
        for(User l : this.Users)
        {
            if(!l.getId().equals(lawyer.getId()))
            {
                newLawyers.add(l);
            }
        }
        this.Users = newLawyers;
    }

    public List<String> getUserEmails(){
        return getUsers().stream().map(User::getEmail).collect(Collectors.toList());
    }
    public List<String> getUserNames(){
        return getUsers().stream().map(User::getEmail).collect(Collectors.toList());
    }


    //TODOQ: Does this mean the files (URLs) of the walkthrough steps, AND/OR the shared files of the lawyers? They each have different return types though. I gave it type void for now
//    public void getFiles()
//    {
//        //returns all files associated with case
//
//        //this is how i'd return the URLs of the case itself
//        List<String> URLlist = new ArrayList<>();
//        for( WalkthroughStep step : this.walkthrough.getSteps())
//        {
//            URLlist.add(step.getURL());
//        }
//
//        //this is how i'd return the lawyer's docs. The nested for loop is because the lawyer.getDocuments() returns a Set of documents. The code below extracts the docs from those sets to package them more neatly into one list List<Documents> instead of List<Set<Documents>>
//        List<Documents> documentsList = new ArrayList<>();
//
//        for( User lawyer : this.getLawyers())
//        {
//            for(Documents document : lawyer.getDocuments())
//            {
//                documentsList.add(document);
//            }
//        }
//    }

////    //NOTE:Error here
    @OneToMany(mappedBy = "caseowner") @Getter @Setter //cascade =ALL;
    private List<Documents> docs = new ArrayList<>();

    public void addDoc(Documents doc){
        this.docs.add(doc);
    }


    //TODOQ: Not sure what this means at all mate, let me know if you do
    public void addFileFromTemplate(String UUID)
    {
        //adds a template file to the case and populates it with case values
    }

    public List<Action> getActions(ActionRepository repository)
    {
        return repository.findAllByParentCaseOrderByTimeDesc(this);
    }

    public Date lastModified(ActionRepository repository)
    {
        return repository.findTopByOrderByTimeDesc().getTime();
    }

    //create new case
    public Case(String name, Walkthrough walkthrough, Set<Client> clients, Set<User> lawyers)
    {
        this.name = name;
        this.walkthrough = walkthrough;
        this.status = CaseStatus.Active;
        this.setClients(clients);
        for(User l : lawyers){
            l.addCase(this);
        }
        this.step = 0;
    }

    //overload with supervisor
    public Case(String name, Walkthrough walkthrough, Set<Client> clients, Set<User> lawyers, User supervisor)
    {
        this.name = name;
        this.walkthrough = walkthrough;
        this.status = CaseStatus.Active;
        this.setClients(clients);
        for(User l : lawyers){
            l.addCase(this);
        }
        this.step = 0;
        this.supervisor = supervisor;
    }

    //retrieve case
//    public Case retrieveCase(Long id)
//    {
//        return this.casesRepository.findById(id).get();
//    }
//
//    public Case retrieveCase(Long id, CaseRepository caseRepository)
//    {
//        return casesRepository.findById(id).get();
//    }

    public Case() {}
    
    
    //Call when deleting a case to sort out all relevant DB clean up.
    public void Close(StepDocsRepository stepDocsRepository, ActionRepository actionRepository, DocumentsRepository documentsRepository, CaseStepRepository caseStepRepository){
        for(CaseStep step: this.getSteps()){
            for(StepDocs doc: step.getDocs()){

                stepDocsRepository.delete(doc);
            }
            caseStepRepository.delete(step);
        }
        for(Documents doc: this.getDocs()){
            documentsRepository.delete(doc);
        }
        for(Action action : this.getActions(actionRepository)){
            actionRepository.delete(action);
        }
        for(User user : this.getUsers()){
            user.removeCase(this);
        }
        this.getSupervisor().removeSupervisedCase(this);
        for(Client client: this.getClients()){
            client.removeCase(this);
        }
        this.getWalkthrough().removeCase(this);
    }
}
