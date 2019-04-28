package ac.uk.bristol.law.clinic.DTO;

import lombok.Getter;
import lombok.Setter;

public class StepCreation {
    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String name;

    public StepCreation(String name, String description){this.description = description; this.name = name;}

    public StepCreation(){};
}
