package ac.uk.bristol.law.clinic.DTO;

import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import lombok.Getter;
import lombok.Setter;

public class CaseCreation {

    public String getCaseName() { return caseName;}
    public void setCaseName(String caseName) {this.caseName = caseName;}

    public Walkthrough getCaseWalkthrough() {
        return caseWalkthrough;
    }
    public void setCaseWalkthrough(Walkthrough caseWalkthrough) {this.caseWalkthrough = caseWalkthrough;}

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public Long getCaseSupervisor() { return caseSupervisor;}
    public void setCaseSupervisor(Long caseSupervisor) {this.caseSupervisor = caseSupervisor;}

    private String caseType;
    private String caseName;
    @Getter
    @Setter
    private Long caseSupervisor;
    private Walkthrough caseWalkthrough;
    @Getter
    @Setter
    private Integer ID;

}
