package ac.uk.bristol.law.clinic.entities.walkthroughs;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name="walkthroughstepdocs")
public class WalkthroughStepDocs {

    @Column(name="id") @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="name") @Getter @Setter
    String name;


    @ManyToOne(targetEntity = WalkthroughStep.class)
    @JoinColumn(name="step_id") @Getter
    WalkthroughStep step;

    public void setCasestep(WalkthroughStep step){
        this.step = step;
        step.addDoc(this);
    }

    @Column(name="URL") @Getter @Setter
    String url;

    public WalkthroughStepDocs(String name, WalkthroughStep step)
    {
        this.name = name;
        this.setCasestep(step);
    }

    public WalkthroughStepDocs() {}

}
