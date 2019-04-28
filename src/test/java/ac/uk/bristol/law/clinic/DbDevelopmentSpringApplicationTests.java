package ac.uk.bristol.law.clinic;

import ac.uk.bristol.law.clinic.entities.*;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.entities.cases.CaseStep;
import ac.uk.bristol.law.clinic.entities.Documents;
import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughDocs;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStep;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStepDocs;
import ac.uk.bristol.law.clinic.repositories.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes=DbDevelopmentSpringApplicationTests.class)
@SpringBootTest
public class DbDevelopmentSpringApplicationTests {

    @Autowired
    CaseRepository casesRepository;

    @Autowired
    WalkthroughRepository walkthroughRepository;

    @Autowired
    WalkthroughDocsRepository walkthroughDocsRepository;

    @Autowired
    ClientRepository clientsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DocumentsRepository documentsRepository;

    @Autowired
    WalkthroughStepRepository stepsRepository;

    @Autowired
    WalkthroughStepDocsRepository stepdocsRepositoryWalkthrough;

    @Autowired
    CaseStepRepository concretecasestepreopository;

    @Autowired
    ActionRepository actionRepository;

    @Test
    public void trueCase() {    // test saving to the database
        assert(true);
    }


	@Test
    public void caseTest() {    // test saving to the database
        Walkthrough walkthrough1 = new Walkthrough("Walkthrough1");
        walkthroughRepository.save(walkthrough1);
        User johnPeake = new User("John", "Peake", "john@Peake.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(johnPeake);
        Case dummyCase = new Case("dummyCase", walkthrough1, new HashSet<Client>(), new HashSet<User>(), Optional.of(johnPeake));
        casesRepository.save(dummyCase);
//        List<Case> cases = new ArrayList<>();
//        cases.add(dummyCase);
//        walkthrough1.setCases(cases);
        walkthroughRepository.save(walkthrough1);

        assert(!walkthroughRepository.findById(walkthrough1.getId()).get().getCases().isEmpty());
		assert(casesRepository.existsById(dummyCase.getId()));
		assert(casesRepository.findById(dummyCase.getId()).get().getSupervisor().getName().equals("John Peake"));
    }

    @Test
    public void lawyersTest() {    // test saving to the database
        User helena = new User("Helena", "Peake", "helena@email.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(helena);

        assert(userRepository.existsById(helena.getId()));
    }

    @Test
    public void clientsTest() {    // test saving to the database
        Client client = new Client("Amin", "Lessan", "aminlessan@email.com", new String[]{"UK", "Bristol", "Bristol", "Avon", "fd"}, "07712222222");
        clientsRepository.save(client);


        assert(clientsRepository.existsById(client.getId()));
    }

    @Test
    public void documentsTest() {    // test saving to the database
        User helena = new User("Helena", "Peake", "helena@email.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(helena);

        Documents documents = new Documents("Document1", false, helena, "www...");
        documentsRepository.save(documents);


        assert(documentsRepository.existsById(documents.getId()));
    }

    @Test
    public void stepsTest(){
        Walkthrough walkthroughA = new Walkthrough("WalkthroughA");
        walkthroughRepository.save(walkthroughA);

        WalkthroughStep stepA = new WalkthroughStep("Step1", 3, walkthroughA);
        stepsRepository.save(stepA);

        assert(stepsRepository.existsById(stepA.getId()));
    }


//    @Test
//    public void stepdocsTest(){
//        User law = new User("hel", "lno", "@email", "1234", User.PermissionLevel.Admin);
//        Walkthrough walker = new Walkthrough("walk");
//
//
//        Case caseex = new Case("case1", walk, new HashSet<Client>(), new HashSet<User>());
//        Documents doc = new Documents("doc", true, law);
//        doc.setCaseowner(caseex);
//        casesRepository.save(caseex);
//        documentsRepository.save(doc);
//        assert(!caseex.getDocs().isEmpty());
//        //-
//        WalkthroughStep astep = new WalkthroughStep("astep", "URL", 2, walker)
//        WalkthroughStepDocs astepdoc = new WalkthroughStepDocs("astedoc", )
//    }

    @Test
    public void walkthroughTest(){
        Walkthrough walkthroughA = new Walkthrough("WalkthroughA","A nice and potentially long description about the walkthrough");
        walkthroughRepository.save(walkthroughA);

        assert(walkthroughRepository.existsById(walkthroughA.getId()));
        assert(walkthroughRepository.findById(walkthroughA.getId()).get().getDescription().equals("A nice and potentially long description about the walkthrough"));
    }


    @Test
    public void caseToWalkthroughManyToOneTest(){
        Walkthrough walkthroughA = new Walkthrough("WalkthroughA");
        walkthroughRepository.save(walkthroughA);

        Case caseA = new Case("RobberyCase", walkthroughA, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        casesRepository.save(caseA);

//        System.out.println(casesRepository.findById(caseA.getId()).get().getWalkthrough().getName());
        assert(casesRepository.findById(caseA.getId()).get().getWalkthrough().getName().equals("WalkthroughA"));
        assert(!walkthroughRepository.findById(walkthroughA.getId()).get().getCases().isEmpty());
    }

    //Case owns relationship with clients so myCases.setClients persists as expected
	@Test
    public void clientCaseManyToManyRelationshipTest()
    {
        Walkthrough walkthrough3 = new Walkthrough("Walkthrough3");
        walkthroughRepository.save(walkthrough3);

        Case CaseA = new Case("dummyCase", walkthrough3, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        casesRepository.save(CaseA);

        Client ClientA = new Client("John", "Peake", "johnpeake@email.com", new String[]{"UK", "Bristol", "Bristol", "Avon", "h"}, "07712222222");
        clientsRepository.save(ClientA);


        CaseA.addClient(ClientA);

        casesRepository.save(CaseA);


        assert(!clientsRepository.findById(ClientA.getId()).get().getCases().isEmpty());
    }

    //User owns relationship with myCases so lawyer.setMyCases persists as expected
    @Test
    public void caseLawyerManyToManyRelationshipTest2()
    {
        Walkthrough Walkthrough3 = new Walkthrough("Walthrough");
        walkthroughRepository.save(Walkthrough3);

        User LawyerB = new User("Tim", "Peake", "timpeake@email.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(LawyerB);
        Case CaseB = new Case("MuderCase", Walkthrough3, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        casesRepository.save(CaseB);


        LawyerB.addCase(CaseB);

        userRepository.save(LawyerB);
        casesRepository.save(CaseB);

        assert(userRepository.existsById(LawyerB.getId()));
        assert(!casesRepository.findById(CaseB.getId()).get().getUsers().isEmpty());
    }

    //lawyer owns relationship with documents so lawyer.setTDocuments persists as expected
    @Test
    public void documentLawyerManyToManyRelationshipTest()
    {
        Walkthrough Walkthrough3 = new Walkthrough("Walthrough");
        walkthroughRepository.save(Walkthrough3);

        User LawyerB = new User("Tim", "Peake", "timpeake@email.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(LawyerB);

        Documents documents = new Documents("Document1", false, LawyerB, "www...");
        documentsRepository.save(documents);


        LawyerB.addDocument(documents);

        userRepository.save(LawyerB);
        documentsRepository.save(documents);

        assert(!userRepository.findById(LawyerB.getId()).get().getDocuments().isEmpty());
        assert(!documentsRepository.findById(documents.getId()).get().getLawyers().isEmpty());
    }

    @Test
    public void documentLawyerManyToManyRelationshipTest2()
    {
        Walkthrough Walkthrough3 = new Walkthrough("Walthrough");
        walkthroughRepository.save(Walkthrough3);

        User LawyerB = new User("Tim", "Peake", "timpeake@email.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(LawyerB);

        Documents documents = new Documents("Document1", false, LawyerB, "WWW...");
        documentsRepository.save(documents);

        documents.addLawyer(LawyerB, userRepository);

        userRepository.save(LawyerB);

        assert(!userRepository.findById(LawyerB.getId()).get().getDocuments().isEmpty());
        assert(!documentsRepository.findById(documents.getId()).get().getLawyers().isEmpty());
    }

    //Case owns relationship with clients so clients.setMyCases does not persist properly unless we do it manually
    @Test
    public void clientCaseManyToManyRelationshipTest2()
    {
        Walkthrough somewalkthrough = new Walkthrough("somewalkthrough");
        walkthroughRepository.save(somewalkthrough);

        Case alsHousing = new Case("als Housing case", somewalkthrough, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        casesRepository.save(alsHousing);

        Client Al = new Client("Al", "zing", "alzing@email.com", new String[]{"UK", "Bristol", "Bristol", "Avon", "y"}, "07712222222");
        clientsRepository.save(Al);

        Al.addCase(alsHousing, casesRepository);


        casesRepository.save(alsHousing);
        clientsRepository.save(Al);


        assert(!casesRepository.findById(alsHousing.getId()).get().getClients().isEmpty());
    }

    @Test
    public void lawyerCaseManyToManyRelationshipTest2()
    {
        Walkthrough Walkthrough3 = new Walkthrough("Walthrough");
        walkthroughRepository.save(Walkthrough3);

        User LawyerB = new User("Tim", "Peake", "timpeake@email.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(LawyerB);
        Case CaseB = new Case("MuderCase", Walkthrough3, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        casesRepository.save(CaseB);


        CaseB.addLawyer(LawyerB);

        userRepository.save(LawyerB);

        assert(userRepository.existsById(LawyerB.getId()));
        assert(!userRepository.findById(LawyerB.getId()).get().getMyCases().isEmpty());
//        assert(!casesRepository.findById(CaseB.getId()).get().getLawyers().isEmpty());
    }

    @Test
    public void testRemoveLawyer()
    {
        Walkthrough Walkthrough3 = new Walkthrough("Walthrough");
        walkthroughRepository.save(Walkthrough3);

        User LawyerB = new User("Tim", "Peake", "timpeake@email.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(LawyerB);
        Case CaseB = new Case("MuderCase", Walkthrough3, new HashSet<Client>(), new HashSet<User>(),Optional.empty());
        casesRepository.save(CaseB);

        Case CasesC = new Case("CaseC", Walkthrough3, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        casesRepository.save(CasesC);


        LawyerB.addCase(CaseB);
        LawyerB.addCase(CasesC);
//        CaseB.addLawyer(LawyerB, userRepository);
        userRepository.save(LawyerB);



        //CaseB.removeLawyer(LawyerB); sf
        LawyerB.removeCase(CaseB);
        userRepository.save(LawyerB);
//        casesRepository.save(CaseB);

        assert(!userRepository.findById(LawyerB.getId()).get().getMyCases().isEmpty());
        assert(casesRepository.findById(CaseB.getId()).get().getUsers().isEmpty());

//        assert(userRepository.findById(LawyerB.getId()).get().getMyCases().isEmpty());
//        assert(!casesRepository.findById(CaseB.getId()).get().getLawyers().isEmpty());
    }

    @Test@Transactional//solved with manually adding to walkthroughs steps n in steps constructor and @Transactional
    public void walkthroughStepsOneToMany(){
        Walkthrough walkthroughexample = new Walkthrough("some_walkthrough");
        walkthroughRepository.save(walkthroughexample);

        WalkthroughStep stepexample = new WalkthroughStep("step_name", 1, walkthroughexample);
        stepsRepository.save(stepexample);

        assert(stepsRepository.findById(stepexample.getId()).get().getWalkthrough().getName().equals("some_walkthrough"));
        //NOTE: I added this assert below which makes it fail.
        assert(!walkthroughRepository.findById(walkthroughexample.getId()).get().getSteps().isEmpty());
    }

    @Test@Transactional
    public void caseStepsTest()
    {

        Walkthrough walkthroughexample = new Walkthrough("some_walkthrough");
//        walkthroughRepository.save(walkthroughexample);

        WalkthroughStep stepA = new WalkthroughStep("step_name", 4, walkthroughexample);
        walkthroughexample.addStep(stepA);
        walkthroughRepository.save(walkthroughexample);
        stepsRepository.save(stepA);

        Case CaseA = new Case("Toolbox_Stolen_Case", walkthroughexample, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        casesRepository.save(CaseA);


//        assert(CaseA.getCaseSteps().contains(stepA));
        assert(!walkthroughexample.getSteps().isEmpty());
        assert(!walkthroughRepository.findById(walkthroughexample.getId()).get().getSteps().isEmpty());

//        assert(!casesRepository.findById(CaseA.getId()).get().getCaseSteps().isEmpty());
//        assert(casesRepository.findById(CaseA.getId()).get().getCaseSteps().contains(stepA));
    }

    @Test
    public void lawyersGetClientsTest()
    {
        Client clientA = new Client("John", "Peake", "johnpeake@email.com", new String[]{"UK", "Bristol", "Bristol", "Avon", "h"}, "07712222222");
        clientsRepository.save(clientA);

        User helena = new User("Helena", "Peake", "helena@email.com", "1234", User.PermissionLevel.Admin);

        Walkthrough walkthroughExample = new Walkthrough("Example");
        walkthroughRepository.save(walkthroughExample);

        Case caseA = new Case("John_stole_toolbox", walkthroughExample, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        caseA.addClient(clientA);
        casesRepository.save(caseA);

        helena.addCase(caseA);
        userRepository.save(helena);

        assert(helena.getMyClients().contains(clientA));//taken away cascade ALL

//        assert(userRepository.findById(helena.getId()).get().getMyClients().contains(clientA));

        assert(helena.getMyClients().contains(clientA));//taken away cascade ALL
        assert(!userRepository.findById(helena.getId()).get().getMyClients().isEmpty());
    }
    @Test@Transactional
    public void casesDocsOnetoMany(){
        User law = new User("hel", "lno", "@email", "1234", User.PermissionLevel.Admin);
        Walkthrough walk = new Walkthrough("walk");
        Case caseex = new Case("case1", walk, new HashSet<Client>(), new HashSet<User>(), Optional.empty());
        Documents doc = new Documents("doc", true, law, "www...");
        doc.setCaseowner(caseex);
        casesRepository.save(caseex);
        documentsRepository.save(doc);
        assert(!caseex.getDocs().isEmpty());
    }

    @Test@Transactional
    public void stepDocsOnetoMany(){
        Walkthrough walker = new Walkthrough("walker");
        WalkthroughStep astep = new WalkthroughStep("astep", 1, walker);
        WalkthroughStepDocs astepdoc = new WalkthroughStepDocs("astepdoc", astep);
        stepdocsRepositoryWalkthrough.save(astepdoc);
        stepsRepository.save(astep);
        assert(!astep.getDocs().isEmpty());
    }


    @Test@Transactional
    public void walkthroughDocsOnetoMany(){
        Walkthrough walkthroughA = new Walkthrough("walker");
        WalkthroughDocs walkthroughDocs = new WalkthroughDocs("walkthroughTemplate1", "/walkthrough/walker/walkthroughTemplate1");
        walkthroughRepository.save(walkthroughA);
        walkthroughDocsRepository.save(walkthroughDocs);

        assert(!walkthroughA.getWalkthroughDocs().isEmpty());
    }

    @Test@Transactional
    public void SimonTest() {
        User dummyUser = new User("Simon", "Lock", "Email@Simon", "Password", User.PermissionLevel.Admin);
        userRepository.save(dummyUser);
        Set<User> dummyLawyers = new HashSet<>();
        dummyLawyers.add(dummyUser);

        Walkthrough housingWalkthrough = new Walkthrough("Housing");
        walkthroughRepository.save(housingWalkthrough);

        Client dummyClient = new Client("Dan", "Lock", "Email@Dan", new String[]{"Bristol", "MVB", "BS1", "Avon", "5"}, "077");
        clientsRepository.save(dummyClient);
        Set<Client> dummyClients = new HashSet<>();
        dummyClients.add(dummyClient);

        Case dummyCase = new Case("Dan's Housing Case", housingWalkthrough, dummyClients, dummyLawyers);
        casesRepository.save(dummyCase);

        userRepository.save(dummyUser);

        assert (userRepository.findById(dummyUser.getId()).get().getName().equals("Simon Lock"));
        assert (!userRepository.findById(dummyUser.getId()).get().getMyCases().isEmpty());
    }
//    @Test
//    public void testRemoveCaseSteps()
//    {
//        WalkthroughStep stepB = new WalkthroughStep();
//
//        Walkthrough walkthroughA = new Walkthrough("WalkthroughA");
//        walkthroughRepository.save(walkthroughA);
//
//        Case caseA = new Case("RobberyCase", walkthroughA);
//        casesRepository.save(caseA);
//
//        assert(casesRepository.findById(caseA.getId()).get().getWalkthrough().getName().equals("WalkthroughA"));
//        assert(!walkthroughRepository.findById(walkthroughA.getId()).get().getMyCases().isEmpty());
//    }


    @Test
    public void concreteStepsTest(){
        Walkthrough walkthroughA = new Walkthrough("walka");
        walkthroughRepository.save(walkthroughA);
        User johnPeake = new User("John", "Peake", "john@Peake.com", "1234", User.PermissionLevel.Admin);
        userRepository.save(johnPeake);
        Case caseA = new Case("casea",walkthroughA , new HashSet<Client>(), new HashSet<User>(), Optional.of(johnPeake));
        casesRepository.save(caseA);

        CaseStep stepA = new CaseStep("Step1", 3, caseA);
        concretecasestepreopository.save(stepA);

        stepA.setDateComplete(LocalDate.now());
        concretecasestepreopository.save(stepA);
        assert(concretecasestepreopository.findById(stepA.getId()).get().getDateComplete() != null);
        assert(concretecasestepreopository.findById(stepA.getId()).get().getDateDue() == null);
        assert(concretecasestepreopository.existsById(stepA.getId()));
    }

    @Test
    public void  actionCaseTest(){
        User user = new User();
        userRepository.save(user);
        Case newCase = new Case();
        casesRepository.save(newCase);
        Action action = new Action(Action.ActionType.ADD_STEP, user, newCase);
        actionRepository.save(action);
//        assert(casesRepository.findById(newCase.getId()).get().getActions(actionRepository).contains(action));
    }

}
