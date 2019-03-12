package ac.uk.bristol.law.clinic.entities.cases;

import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStep;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/* concrete steps are separate from regular steps in that they correspond o cases rather than walkthroughs
We must generae concrete steps from regular steps when a case which uses a walkthrough is created.
 */

@Entity
@Table(name="casesteps")
public class CaseStep {

    @Column(name="id") @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name") @Getter @Setter
    private String name;


    @Column(name="step_number") @Getter @Setter
    private int stepNumber;

    @Column(name="date_due") @Getter @Setter
    private LocalDate dateDue;

    public String getDateDueString(){
        if(dateDue == null){
            return "";
        }
        return this.dateDue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Column(name="date_complete") @Getter @Setter
    private LocalDate dateComplete;

    public String getDateCompleteString(){
        if(dateComplete == null){
            return "";
        }
        return this.dateComplete.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @ManyToOne(targetEntity = Case.class)
    @JoinColumn(name="case_id") @Getter @Setter
    Case concreteCase;

    @OneToMany(mappedBy = "step") @Getter @Setter //cascade =ALL;
    private List<StepDocs> docs = new ArrayList<>();
    public void addDoc(StepDocs doc){
        this.docs.add(doc);
    }

    public boolean isComplete(){return (this.dateComplete != null);}



    public CaseStep(String name, int stepNumber, Case concreteCase)
    {
        this.name = name;
        this.stepNumber = stepNumber;
        this.concreteCase = concreteCase;
        this.concreteCase.addStep(this);
    }

    public CaseStep(WalkthroughStep step, Case concreteCase)
    {
        this.name = step.getName();
        this.stepNumber = step.getStepNumber();
        this.concreteCase = concreteCase;
        this.concreteCase.addStep(this);
//        this.dateComplete.format(Date)
    }


    public CaseStep() {}

}
