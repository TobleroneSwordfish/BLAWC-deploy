package ac.uk.bristol.law.clinic;

import ac.uk.bristol.law.clinic.entities.User;
import ac.uk.bristol.law.clinic.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ControllerUtils
{
    @Autowired
    UserRepository userRepository;
    public User getUserFromUsername(String username)
    {
        Iterable<User> users = userRepository.findAll();
        for (User user : users)
        {
            if (user.getEmail().equals(username))
            {
                return user;
            }
        }
        throw new UsernameNotFoundException("Not ere mate");
    }
}
