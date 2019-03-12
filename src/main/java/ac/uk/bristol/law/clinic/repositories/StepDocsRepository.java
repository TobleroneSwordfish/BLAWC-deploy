package ac.uk.bristol.law.clinic.repositories;
import ac.uk.bristol.law.clinic.entities.cases.StepDocs;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component @Repository
public interface StepDocsRepository extends CrudRepository<StepDocs, Long> {
}
