package ac.uk.bristol.law.clinic.entities.cases;

import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStepDocs;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name="stepdocs")
public class StepDocs {

    @Column(name="id") @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="name") @Getter @Setter
    String name;


    @ManyToOne(targetEntity = CaseStep.class)
    @JoinColumn(name="step_id") @Getter
    CaseStep step;

    public void setCasestep(CaseStep step){
        this.step = step;
        step.addDoc(this);
    }

    @Column(name="URL") @Getter @Setter
    String url;

    public StepDocs(String name, CaseStep step)
    {
        this.name = name;
        this.setCasestep(step);
    }

    public StepDocs(WalkthroughStepDocs stepDoc, CaseStep step)
    {
        this.name = stepDoc.getName();
        this.setCasestep(step);
        this.setUrl(stepDoc.getUrl());
    }

    public StepDocs() {}

}
