package ac.uk.bristol.law.clinic.entities.walkthroughs;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="walkthroughsteps")
public class WalkthroughStep implements Serializable {

    @Column(name="id") @Getter @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //this is the same as "get name"
    @Column(name="name") @Getter @Setter
    private String name;


    @Column(name="step_number") @Getter @Setter
    private int stepNumber;

    @ManyToOne(targetEntity = Walkthrough.class)
    @JoinColumn(name="walkthrough") @Getter @Setter
    Walkthrough walkthrough;

    @OneToMany(mappedBy = "step") @Getter @Setter //cascade =ALL;
    private List<WalkthroughStepDocs> docs = new ArrayList<>();
    public void addDoc(WalkthroughStepDocs doc){
        this.docs.add(doc);
    }


    public WalkthroughStep(String name, int stepNumber, Walkthrough walkthrough)
    {
        this.name = name;
        this.stepNumber = stepNumber;
        this.walkthrough = walkthrough;
        this.walkthrough.addStep(this);//NOTE
    }

    //todoq: where does our step description come from? is it just the same as its name or not?
//    public String getDescription()
//    {
//    }

    public WalkthroughStep() {}


}
