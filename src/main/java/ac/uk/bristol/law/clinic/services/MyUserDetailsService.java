package ac.uk.bristol.law.clinic.services;

import ac.uk.bristol.law.clinic.MyUserPrincipal;
import ac.uk.bristol.law.clinic.entities.User;
import ac.uk.bristol.law.clinic.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    //This is where we load the user data from the database, current test version is just returning a single dummy user
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try{

            for (User user : userRepository.findAll())
            {
                if (user.getUsername().equals(username))
                {
                    return new MyUserPrincipal(user);
                }
            }
            throw new UsernameNotFoundException("no user exists");
        }
        catch (UsernameNotFoundException e)
        {
            throw e;
        }
    }
    //implement this

//    @Override
//    public User registerNewUserAccount(UserCreation dto)
//    {
//        User user = new User(dto.getFirstName(), dto.getLastName(), dto.getEmail(), dto.getPassword(), Enum.valueOf(User.PermissionLevel.class, dto.getPermissionLevel()));
//        return user;
//    }
}
