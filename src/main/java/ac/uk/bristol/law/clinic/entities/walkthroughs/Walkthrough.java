package ac.uk.bristol.law.clinic.entities.walkthroughs;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.repositories.WalkthroughRepository;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="walkthroughs")
public class Walkthrough  implements Serializable {

    //public Iterable<Walkthrough> getCaseTypes(){ return this.walkthroughsRepository.findAll(); }

    public Iterable<Walkthrough> getCaseTypes(WalkthroughRepository walkthroughRepository){ return walkthroughRepository.findAll(); }

    @Column(name="id") @Getter @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name="name") @Getter @Setter
    private String name;

    @Column(name="description") @Getter @Setter
    private String description;

    @OneToMany(mappedBy = "walkthrough", cascade = CascadeType.ALL, fetch = FetchType.EAGER) @Getter @Setter
    private List<Case> cases = new ArrayList<>();

    public void removeCase(Case caseRemove){this.cases.remove(caseRemove);}


    @OneToMany(mappedBy = "walkthrough") @Getter @Setter //cascade =ALL;
    private List<WalkthroughStep> steps = new ArrayList<>();

    public void addStep(WalkthroughStep step)
    {
        this.steps.add(step);
    }

    public void removeStep(WalkthroughStep step)
    {
        this.steps.remove(step);
    }

    @OneToMany(mappedBy = "walkthrough") @Getter @Setter //cascade =ALL;
    private List<WalkthroughDocs> walkthroughDocs = new ArrayList<>();

    public void addDoc(WalkthroughDocs walkthroughDoc){
        this.walkthroughDocs.add(walkthroughDoc);
    }

    public Walkthrough(String name)
    {
        this.name = name;
    }

    public Walkthrough(String name, String description)
    {
        this.name = name;
        this.description = description;
    }


    public Walkthrough() {}
}
