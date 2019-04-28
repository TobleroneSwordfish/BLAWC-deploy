package ac.uk.bristol.law.clinic.repositories;
import ac.uk.bristol.law.clinic.entities.Client;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Component @Repository
public interface ClientRepository extends CrudRepository<Client, Long> {
    Set<Client> findAll();
}
