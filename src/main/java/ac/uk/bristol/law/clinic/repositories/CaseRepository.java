package ac.uk.bristol.law.clinic.repositories;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.entities.User;
import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CaseRepository extends CrudRepository<Case, Long> {
    Set<Case> findAllByStatusEquals(Case.CaseStatus status);
    Set<Case> findAllBySupervisorEquals(User supervisor);
    Set<Case> findAllByWalkthroughEquals(Walkthrough walk);
    @Override
    Set<Case> findAll();



}
