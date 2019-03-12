package ac.uk.bristol.law.clinic.repositories;
import ac.uk.bristol.law.clinic.entities.cases.CaseStep;
import ac.uk.bristol.law.clinic.entities.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Component @Repository
public interface CaseStepRepository extends CrudRepository<CaseStep, Long> {

    //oh lord I apologise for my sins, please forgive me as I have forgiven
    //the fuckers that didn't add the ability to do contains and joins on the same
    //method name built query

    //yeah just don't worry about below, it works and it's efficient
    @Query(value = "SELECT casesteps.id FROM casesteps " +
            "INNER JOIN cases ON casesteps.case_id = cases.id " +
            "INNER JOIN cases_users ON cases_users.case_id = cases.id " +
            "WHERE cases_users.user_id = :#{#user.getId()} AND date_complete IS NULL " +
            "ORDER BY casesteps.date_due ASC ", nativeQuery = true)
    List<BigInteger> findAllByUserOrderByDateDuePageable(@Param("user") User user, Pageable pageable);

    default List<BigInteger> findAllByUserOrderByDateDue(User user, int n)
    {
        return findAllByUserOrderByDateDuePageable(user, PageRequest.of(0, n));
    }
}
