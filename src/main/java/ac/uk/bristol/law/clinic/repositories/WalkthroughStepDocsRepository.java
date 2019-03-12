package ac.uk.bristol.law.clinic.repositories;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStepDocs;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component @Repository
public interface WalkthroughStepDocsRepository extends CrudRepository<WalkthroughStepDocs, Long> {
}
