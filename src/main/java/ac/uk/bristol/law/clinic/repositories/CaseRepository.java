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
    Set<Case> findAllByStatusEqualsAndUsersContains(Case.CaseStatus status, User user);
    Set<Case> findAllBySupervisorEquals(User supervisor);
    Set<Case> findAllBySupervisorEqualsAndUsersContains(User supervisor, User user);
    //Case findByCaseIDEquals(Case theCase);
    Set<Case> findAllByWalkthroughEquals(Walkthrough walk);
    Set<Case> findAllByWalkthroughEqualsAndUsersContains(Walkthrough walk, User user);
    @Override
    Set<Case> findAll();



}
