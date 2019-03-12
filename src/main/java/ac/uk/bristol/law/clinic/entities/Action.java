package ac.uk.bristol.law.clinic.entities;

import ac.uk.bristol.law.clinic.entities.cases.Case;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "actions")
public class Action implements Serializable
{
    public Action()
    {
        time = new Date();
    }

    public Action(ActionType type, User user, Case parentCase)
    {
        this();
        this.type = type;
        this.user = user;
        this.parentCase = parentCase;
    }

    //---------------------IMPORTANT-------------------------
    //this is stored as a string in the database so don't modify any of the enum names unless you're sure you know what you're doing
    //adding new enumerations is fine, just make sure they're handled in getDescription below
    public enum ActionType
    {
        UPLOAD_FILE, SET_STATUS, ADD_STEP, CREATE_CASE, SET_DATE_DUE, SET_DATE_COMPLETED
    }

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private ActionType type;

    @Column(name="id") @Getter @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="user_id")
    @Getter
    @Setter
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date time;

    @ManyToOne(targetEntity = Case.class)
    @JoinColumn(name="case_id")
    @Getter
    @Setter
    private Case parentCase;

    //general info stored about the action, varies based on type eg. filename, step name etc.
    @Column(name="body")
    @Getter @Setter
    private String body;

    public String getDescription()
    {
        switch (type)
        {
            case UPLOAD_FILE:
                return "Uploaded file: " + body;
            case SET_STATUS:
                return "Set status to " + body;
            case ADD_STEP:
                return "Added step " + body;
            case CREATE_CASE:
                return "Case created";
            case SET_DATE_DUE:
                return "Set due date for step: " + body;
            case SET_DATE_COMPLETED:
                return "Set completed date for step: " + body;
            default:
                return "unknown step type: " + body;
        }
    }

    @Override
    public String toString()
    {
        return time + ": " + user.getName() + " " + getDescription();
    }
}
