package ac.uk.bristol.law.clinic.repositories;
import ac.uk.bristol.law.clinic.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.Set;
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    //Set<User> findAllByLevelEquals(User.PermissionLevel level);
    Set<User> findAllByPermissionLevelEquals(User.PermissionLevel permissionLevel);
}
