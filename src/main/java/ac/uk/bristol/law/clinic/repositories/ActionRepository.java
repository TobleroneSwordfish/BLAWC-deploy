package ac.uk.bristol.law.clinic.repositories;
import ac.uk.bristol.law.clinic.entities.Action;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Component
@Repository
public interface ActionRepository extends CrudRepository<Action, Long>
{
    List<Action> findByParentCase(Case parent);
    Action findTopByOrderByTimeDesc();
    Action findTopByParentCaseOrderByTimeDesc(Case parent);
    List<Action> findAllByParentCaseOrderByTimeDesc(Case parent);
    default Date getLastModified(Case c)
    {
        Action topAct = findTopByParentCaseOrderByTimeDesc(c);
        if (topAct != null)
        {
            return topAct.getTime();
        }
        return null;
    }
}
