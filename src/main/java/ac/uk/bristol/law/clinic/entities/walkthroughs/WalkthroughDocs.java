package ac.uk.bristol.law.clinic.entities.walkthroughs;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name="walkthroughdocs")
public class WalkthroughDocs {

    @Column(name="id") @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="name") @Getter @Setter
    String name;


    @ManyToOne(targetEntity = WalkthroughStep.class)
    @JoinColumn(name="walkthrough_id") @Getter
    Walkthrough walkthrough;

    public void setWalkthrough(Walkthrough walkthrough){
        this.walkthrough = walkthrough;
        walkthrough.addDoc(this);
    }

    @Column(name="URL") @Getter @Setter
    String url;

    public WalkthroughDocs(String name, Walkthrough walkthrough)
    {
        this.name = name;
        this.setWalkthrough(walkthrough);
    }

    public WalkthroughDocs() {}

}
