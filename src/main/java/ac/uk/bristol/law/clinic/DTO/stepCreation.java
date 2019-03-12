package ac.uk.bristol.law.clinic.DTO;

import lombok.Getter;
import lombok.Setter;

public class stepCreation {
    @Getter
    @Setter
    private String number;

    @Getter
    @Setter
    private String name;

    public stepCreation(String name, String number){this.number = number; this.name = name;}

    public stepCreation(){};
}
