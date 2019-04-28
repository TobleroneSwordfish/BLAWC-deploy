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


    @ManyToOne(targetEntity = Walkthrough.class)
    @JoinColumn(name="walkthrough_id") @Getter @Setter
    Walkthrough walkthrough;

    @Column(name="URL") @Getter @Setter
    String url;

    public WalkthroughDocs(String name, String url)
    {
        this.name = name;
        this.url = url;
    }

    public WalkthroughDocs() {}

}
