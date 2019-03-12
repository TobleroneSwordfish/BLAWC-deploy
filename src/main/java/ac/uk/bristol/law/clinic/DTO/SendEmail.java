package ac.uk.bristol.law.clinic.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class SendEmail
{
    @Getter
    @Setter
    public String body;
    @Getter
    @Setter
    public ArrayList<String> addresses;
    @Getter
    @Setter
    public String subject;
}
