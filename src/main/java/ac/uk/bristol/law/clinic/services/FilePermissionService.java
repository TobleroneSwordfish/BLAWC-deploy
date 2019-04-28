package ac.uk.bristol.law.clinic.services;

import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.entities.User;
import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import org.springframework.stereotype.Service;

@Service
public class FilePermissionService
{
    public boolean userHasWrite(User user, Case c)
    {
        //is admin
        if (user.getPermissionLevel() == User.PermissionLevel.Admin)
        {
            return true;
        }
        //assigned to case
        if (c.getUserNames().contains(user.getUsername()))
        {
            return true;
        }
        return false;
    }
    public boolean userHasWrite(User user, Walkthrough walkthrough)
    {
        return user.getPermissionLevel() == User.PermissionLevel.Admin;
    }
    //currently the same
    public boolean userHasRead(User user, Case c)
    {
        return userHasWrite(user, c);
    }
    public boolean userHasRead(User user, Walkthrough walkthrough)
    {
        return userHasWrite(user, walkthrough);
    }
}
