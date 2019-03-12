package ac.uk.bristol.law.clinic;

import ac.uk.bristol.law.clinic.repositories.CaseRepository;
import ac.uk.bristol.law.clinic.repositories.UserRepository;
import ac.uk.bristol.law.clinic.repositories.WalkthroughRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.web.bind.annotation.RestController;

//what does this do?
@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@RestController
public class LawClinicApplication {

	@Autowired
	public CaseRepository casesRepo;

	@Autowired
	WalkthroughRepository walkthroughRepository;

	@Autowired
	UserRepository userRepository;

	public static void main(String[] args) { SpringApplication.run(LawClinicApplication.class, args); }
}
