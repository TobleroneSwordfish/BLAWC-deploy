package ac.uk.bristol.law.clinic.entities.cases;

import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStepDocs;
import ac.uk.bristol.law.clinic.services.FileStorageService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.io.File;


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

    public StepDocs(WalkthroughStepDocs stepDoc, CaseStep step, FileStorageService fileStorageService)
    {
        this.name = stepDoc.getName();
        String newPath = "case/" + step.concreteCase.getId() + "/step/" + step.getStepNumber() + "/";
        this.url = "/" + newPath + stepDoc.getName();
        fileStorageService.cloneFile(FileStorageService.cutFilename(stepDoc.getUrl().substring(1)), stepDoc.getName(), newPath, name);
        this.setCasestep(step);
    }

    public StepDocs() {}

}
