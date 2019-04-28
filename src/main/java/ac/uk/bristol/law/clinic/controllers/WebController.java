package ac.uk.bristol.law.clinic.controllers;

import ac.uk.bristol.law.clinic.ControllerUtils;
import ac.uk.bristol.law.clinic.DTO.*;
import ac.uk.bristol.law.clinic.entities.Action;
import ac.uk.bristol.law.clinic.entities.Client;
import ac.uk.bristol.law.clinic.entities.Documents;
import ac.uk.bristol.law.clinic.entities.User;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.entities.cases.CaseStep;
import ac.uk.bristol.law.clinic.entities.cases.StepDocs;
import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughDocs;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStep;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStepDocs;
import ac.uk.bristol.law.clinic.repositories.*;
import ac.uk.bristol.law.clinic.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
//@RequestMapping("/sessionattributes")//COMMENTED
@SessionAttributes("caseCreationGathers")
public class WebController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private WalkthroughStepRepository walkthroughStepRepository;

    @Autowired
    private WalkthroughRepository walkthroughRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private CaseStepRepository caseStepRepository;

    @Autowired
    private StepDocsRepository stepDocsRepository;

    @Autowired
    private DocumentsRepository documentsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender javaMailSender;

    @GetMapping("/")
    public String indexMap(@RequestParam(name="status", required = false) String status,@RequestParam(name="walkthrough", required = false) Long walkthrough, @RequestParam(name="supervisor", required = false) Long supervisor, Model model)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.isAuthenticated())
        {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            model.addAttribute("fullname", user.getName());
            List<Case> casesx = new ArrayList<>(user.getMyCases());
            casesx.sort((o1, o2) -> new Long(o1.getId() - o2.getId()).intValue());
            Set<Case> cases = new HashSet<Case>(casesx);
            model.addAttribute("userCases", cases);
            addClientNames(user, model);
            Map<Long, Integer> caseCompletion = new HashMap<>();
            //System.out.println("User has " + user.getMyCases().size() + " cases");
            for (Case c: user.getMyCases())
            {
                int completeSteps = 0;
                for (CaseStep caseStep : c.getSteps())
                {
                    if(caseStep.isComplete())
                    {
                        completeSteps++;
                    }
                }
                Double ratio = new Double(completeSteps) / new Double(c.getSteps().stream().count());
                //System.out.println("Case has " + completeSteps + " complete steps with " + ratio +" ratio");
                caseCompletion.put(c.getId(), (int)(ratio * 100));
            }
            model.addAttribute("caseCompletion", caseCompletion);
            model.addAttribute("alertSteps", user.nextNSteps(10, caseStepRepository));

            //Filters web controller//

            List<String> statuses = new ArrayList<>();
            for(Case.CaseStatus s : Case.CaseStatus.values()){
                statuses.add(s.name());
            }
            model.addAttribute("statuses", statuses);

            List<User> supervisors = new ArrayList<>();
            for(User x : userRepository.findAllByPermissionLevelEquals(User.PermissionLevel.Admin)) {
                supervisors.add(x);
            }
            model.addAttribute("supervisors", supervisors);

            List<Walkthrough> walkthroughs = new ArrayList<>();
            for(Walkthrough y : walkthroughRepository.findAll()) {
                walkthroughs.add(y);
            }
            model.addAttribute("walkthroughs", walkthroughs);




            Set<Case> casesSorted;

            if (status == null) {
                casesSorted = cases;
            }
            else {
                //System.out.println("Status received: " + Case.CaseStatus.valueOf(status));

                casesSorted = caseRepository.findAllByStatusEqualsAndUsersContains(Case.CaseStatus.valueOf(status), user);
                
            }

            if(supervisor != null) {
                Optional<User> supers = userRepository.findById(supervisor);
                if (supers.isPresent())
                {
                    casesSorted = caseRepository.findAllBySupervisorEqualsAndUsersContains(supers.get(), user);
                }
                else {
                    casesSorted = cases;

                }
            }

            if(walkthrough != null) {
                Optional<Walkthrough> walkies = walkthroughRepository.findById(walkthrough);
                if (walkies.isPresent()) {
                    casesSorted = caseRepository.findAllByWalkthroughEqualsAndUsersContains(walkies.get(), user);
                }
                else {
                    casesSorted = cases;
                }
            }

            model.addAttribute("sorted", casesSorted);

            //

        }
        model.addAttribute("username", authentication.getName());
        return "user-overview";
    }

    private void addClientNames(User user, Model model)
    {
        Map<Long, String> clientNames = new HashMap<>();
        for(Case userCase : user.getMyCases())
        {
            String s = "";
            for (Client client : userCase.getClients())
            {
                s += client.getFirstName() + " " + client.getLastName() + ", ";
            }
            s = s.replaceAll(", $", "");
            //System.out.println("Case names: " + s);
            clientNames.put(userCase.getId(), s);
        }
        model.addAttribute("clientNames", clientNames);
    }

    @GetMapping("/account")
    public String accountMap(Model model)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            UserEdit userEdit = new UserEdit(user);
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userEdit", userEdit);
            model.addAttribute("fullname", user.getName());
            model.addAttribute("email", user.getEmail());
        }
        return "account";
    }

    @PostMapping("/account")
    public String accountPost(@Valid UserEdit userEdit, BindingResult bindingResult)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            if (passwordEncoder.matches(userEdit.getOldPassword(), user.getPassword()))
            {
                user.setEmail(userEdit.getEmail());
                if (!userEdit.getPassword().isEmpty())
                {
                    user.setPassword(passwordEncoder.encode(userEdit.getPassword()));
                }
            }
            else
            {
                bindingResult.addError(new FieldError("userEdit", "oldPassword", "Wrong password"));
            }
        }
        return "account";
    }

    @GetMapping("/case-display")
    public String caseMap(@RequestParam(name="caseID") Integer caseID, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            model.addAttribute("fullname", user.getName());
            model.addAttribute("userCases", user.getMyCases());
            Case currentCase = caseRepository.findById((long)caseID).get();
            String currentCaseName = currentCase.getName();
            List<CaseStep> caseSteps = currentCase.getSteps();
            Set<Client> currentClientDetails = currentCase.getClients();
            model.addAttribute("caseSteps", caseSteps);
            model.addAttribute("currentCase", currentCaseName);
            model.addAttribute("caseID", currentCase.getId());
            model.addAttribute("currentClient", currentClientDetails);
            model.addAttribute("supervisor", currentCase.getSupervisor());
            model.addAttribute("caseDocs", currentCase.getDocs());
            model.addAttribute("assignees", currentCase.getUsers());
            Set<User> lawyers = currentCase.getUsers();

            model.addAttribute("lawyerID", lawyers.iterator().next().getId());

            model.addAttribute("status", currentCase.getStatus().name());
            model.addAttribute("actions", currentCase.getActions(actionRepository));
            List<String> statuses = new ArrayList<>();
            for(Case.CaseStatus s : Case.CaseStatus.values()){
                statuses.add(s.name());
            }
            //nyaha!
            String clientEmails = currentCase.getClients().stream().map(c -> c.getEmail()).collect(Collectors.joining("&address="));
            model.addAttribute("clientEmails", clientEmails);
            String lawyerEmails = currentCase.getUsers().stream().map(u -> u.getEmail()).collect(Collectors.joining("&address="));
            model.addAttribute("lawyerEmails", lawyerEmails);
            model.addAttribute("statuses", statuses);
        }
        model.addAttribute("username", authentication.getName());
        return "case-display";
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/case-display")
    public String caseStatusPost(@RequestParam(name="caseID") Long caseID, @RequestParam(name="setStatus") String setStatus,  HttpServletRequest request)
    {
        Optional<Case> optCase = caseRepository.findById(caseID);
        if(optCase.isPresent() && optCase.get().getStatus() != Case.CaseStatus.valueOf(setStatus)) {
            Case tempCase = optCase.get();
            tempCase.setStatus(Case.CaseStatus.valueOf(setStatus));
            caseRepository.save(tempCase);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = controllerUtils.getUserFromUsername(authentication.getName());

            Action action = new Action(Action.ActionType.SET_STATUS, user, tempCase);
            action.setBody(setStatus);
            actionRepository.save(action);
        }
        return "redirect:" + request.getHeader("Referer");
    }

    @GetMapping("/client-details")
    public String clientMap(@RequestParam(name="clientID") Integer clientID, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            Client client = clientRepository.findById((long)clientID).get();
            model.addAttribute("client", client);
        }
        return "client-details";
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/confirm-delete")
    public String deleteCaseMap(@RequestParam(name="caseID") Integer caseID, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            Case casedelete = caseRepository.findById((long)caseID).get();
            model.addAttribute("caseName", casedelete.getName());
            model.addAttribute("caseID", caseID);
        }
        return "confirm-delete";
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/confirm-delete")
    public String deleteCasePost(@RequestParam("caseID") Long caseID) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            Case casedelete = caseRepository.findById(caseID).get();
            casedelete.Close(stepDocsRepository, actionRepository, documentsRepository, caseStepRepository);
            caseRepository.delete(casedelete);
        }
//        for(Case r : caseRepository.findAll()){
//            System.out.println("!!!!!");
//        }
        return "redirect:/";
    }

    @PostMapping("/set-step-date")
    public String stepDatePost(@RequestParam(value = "dueDate", required = false) String due, @RequestParam(value = "completionDate", required = false) String complete, @RequestParam("stepID") Long stepID, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()){
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            Optional<CaseStep> optstep = caseStepRepository.findById(stepID);
            if (optstep.isPresent()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                CaseStep step = optstep.get();

                //COMPLETE
                if (!complete.isEmpty()) {
                    LocalDate date = LocalDate.parse(complete, dtf);
                    if (!date.equals(step.getDateComplete())) {
                        step.setDateComplete(date);
                        Action action = new Action(Action.ActionType.SET_DATE_COMPLETED, user, step.getConcreteCase());
                        action.setBody("(" + stepID + ") " + step.getName() + ": " + date.toString());
                        actionRepository.save(action);
                    }
                } else {
                    step.setDateComplete(null);
                    Action action = new Action(Action.ActionType.SET_DATE_COMPLETED, user, step.getConcreteCase());
                    action.setBody("(" + step.getId() + ") " + step.getName() + ": date reset");
                    actionRepository.save(action);
                }

                //DUE
                if (!due.isEmpty()) {
                    LocalDate date = LocalDate.parse(due, dtf);
                    if (!date.equals(step.getDateDue())) {
                        step.setDateDue(date);
                        Action action = new Action(Action.ActionType.SET_DATE_DUE, user, step.getConcreteCase());
                        action.setBody("(" + step.getId() + ") " + step.getName() + ": " + date.toString());
                        actionRepository.save(action);
                        step.setDateDue(LocalDate.parse(due, dtf));
                    }
                } else {
                    step.setDateDue(null);
                    Action action = new Action(Action.ActionType.SET_DATE_DUE, user, step.getConcreteCase());
                    action.setBody("(" + step.getId() + ") " + step.getName() + ": date reset");
                    actionRepository.save(action);
                }

                caseStepRepository.save(step);
            }
        }
        return "redirect:" + request.getHeader("Referer");
    }


    @Secured({"ROLE_ADMIN"})
    @PostMapping("/case-update")
    public String caseStatusPost(@RequestParam("dueDate") String dueDate, @RequestParam("completionDate") String completionDate, @RequestParam(name="stepID") Long stepID, HttpServletRequest request)
    {
        CaseStep step = caseStepRepository.findById(stepID).get();
        if(checkDate(dueDate)){
            step.setDateDue(toDate(dueDate));
        }
        if(checkDate(completionDate)){
            step.setDateComplete(toDate(completionDate));
        }
//        System.out.println(step.getDateComplete());
        return "redirect:" + request.getHeader("Referer");
    }

    //date helper functions
    private LocalDate toDate(String date){
        return LocalDate.parse(date);
    }
    private boolean checkDate(String date){
        if (date.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})"))
            return true;
        else
            return false;
    }

    @GetMapping("/login")
    public String loginMap(Model model) { return "login"; }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/search")
    public String searchGet(@RequestParam(name="searchQuery") String searchQuery, Model model)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            List<Case> cases = user.findCases(searchQuery, caseRepository);
            if (cases.isEmpty())
            {
                return "search-empty";
            }
            model.addAttribute("cases", cases);
            addClientNames(user, model);
        }
        model.addAttribute("searchQuery", searchQuery);
        return "search";
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/admin")
    public String adminMap(Model model) {
        return "admin";
    }



    @Secured({"ROLE_ADMIN"})
    @GetMapping("/allcases")
    public String allcasesMap(@RequestParam(name="status", required = false) String status, @RequestParam(name="walkthrough", required = false) Long walkthrough, @RequestParam(name="supervisor", required = false) Long supervisor, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            model.addAttribute("fullname", user.getName());
            model.addAttribute("allCases", user.getAllCases(caseRepository));
            Map<Long, String> nameLists = new HashMap<>();
            for (Case c : user.getAllCases(caseRepository))
            {
                nameLists.put(c.getId(), StringUtils.arrayToDelimitedString(c.getUserNames().toArray(), ", "));
            }
            model.addAttribute("namesMap", nameLists);

            //Case currentCase = caseRepository.findById((long)caseID).get();
            //String currentCaseName = currentCase.getName();
            //model.addAttribute("status", currentCase.getStatus().name());
            List<String> statuses = new ArrayList<>();
            for(Case.CaseStatus s : Case.CaseStatus.values()){
                statuses.add(s.name());
            }

            List<User> supervisors = new ArrayList<>();
            for(User x : userRepository.findAllByPermissionLevelEquals(User.PermissionLevel.Admin)) {
                supervisors.add(x);
            }
            model.addAttribute("supervisors", supervisors);
            //System.out.println(supervisors);

            List<Walkthrough> walkthroughs = new ArrayList<>();
            for(Walkthrough y : walkthroughRepository.findAll()) {
                walkthroughs.add(y);
            }
            model.addAttribute("walkthroughs", walkthroughs);



            model.addAttribute("statuses", statuses);
            Set<Case> casesSorted;
            if (status == null) {
               casesSorted = caseRepository.findAll();
            }
            else {
                //System.out.println("Status received: " + Case.CaseStatus.valueOf(status));
                casesSorted = caseRepository.findAllByStatusEquals(Case.CaseStatus.valueOf(status));
            }

            if(supervisor != null) {
                Optional<User> supers = userRepository.findById(supervisor);
                if (supers.isPresent())
                {
                    casesSorted = caseRepository.findAllBySupervisorEquals(supers.get());
                }
                else {
                    casesSorted = caseRepository.findAll();
                }
            }

            if(walkthrough != null) {
                Optional<Walkthrough> walkies = walkthroughRepository.findById(walkthrough);
                if (walkies.isPresent()) {
                    casesSorted = caseRepository.findAllByWalkthroughEquals(walkies.get());
                }
                else {
                    casesSorted = caseRepository.findAll();
                }
            }

            model.addAttribute("sorted", casesSorted);
            Map<Long, Date> dates = new HashMap<>();
            for (Case c : casesSorted)
            {
                dates.put(c.getId(), actionRepository.getLastModified(c));
            }
            model.addAttribute("lastModifiedMap", dates);
        }
        model.addAttribute("username", authentication.getName());

        return "allcases";
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/allcases")
    public String allcasesStatusPost(@RequestParam(name="status", required = false) String status,  HttpServletRequest request, Model model)
    {
        //Optional<Case> optCase = caseRepository.findById(caseID);
        //if(optCase.isPresent() && optCase.get().getStatus() != Case.CaseStatus.valueOf(setStatus)) {

            //Case tempCase = optCase.get();
            //tempCase.setStatus(Case.CaseStatus.valueOf(setStatus));

            //caseRepository.save(tempCase);


            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = controllerUtils.getUserFromUsername(authentication.getName());

            //Action action = new Action(Action.ActionType.SET_STATUS, user, tempCase);
            //action.setBody(setStatus);
            //actionRepository.save(action);
        List<Case> cases = new ArrayList<>();
        for(Case s : user.getAllCases(caseRepository)){
            if(s.getStatus().equals(status)) {
                cases.add(s);
            }
        }



        model.addAttribute("caseStatus", status);
        model.addAttribute("allCases", cases);

        //}
        return "redirect:" + request.getHeader("Referer");
    }


    @ModelAttribute("caseCreationGathers")
    public Set<Case> caseCreationGathers() {
        return new HashSet<Case>();
    }

    @GetMapping("/create-client")
    public String clientMap(Model model, @RequestParam(value="caseID", required=false) Long caseID) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            model.addAttribute("caseID", caseID);
            model.addAttribute("fullname", user.getName());
            model.addAttribute("allCases", user.getMyCases());
            model.addAttribute("clientCreation", new ClientCreation());
        }
        model.addAttribute("username", authentication.getName());
        return "create-client";
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/create-user")
    public String createUserMap(Model model)
    {
        model.addAttribute("userCreation", new UserCreation());
        model.addAttribute("permissionEnum", Arrays.stream(User.PermissionLevel.values()).filter(p -> !(p == User.PermissionLevel.Client)).toArray());
        return "create-user";
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/create-user")
    public RedirectView createUserPost(Model model, @Valid UserCreation userCreation, BindingResult bindingResult)
    {
        if (bindingResult.hasErrors())
        {
            return new RedirectView("/create-user");
        }
        User newUser = new User();
        newUser.setEmail(userCreation.getEmail());
        newUser.setPassword(passwordEncoder.encode(userCreation.getPassword()));
        newUser.setFirstName(userCreation.getFirstName());
        newUser.setLastName(userCreation.getLastName());
        newUser.setPermissionLevel(userCreation.getPermissionLevel());
        userRepository.save(newUser);
        return new RedirectView("/admin");
    }

    @GetMapping("/add-lawyer")
    public String addLawerMap(@RequestParam(name="caseID") Integer caseID, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            model.addAttribute("caseCreation", new CaseCreation());
            model.addAttribute("caseID", caseID);
            List<User> allUsers = new ArrayList<>();
            for(User x : userRepository.findAll()) {
                allUsers.add(x);
            }
            model.addAttribute("allUsers", allUsers);
        }
        return "add-lawyer";
    }

    @PostMapping("/add-lawyer")
    public RedirectView addLawyerPost(Model model, @RequestParam("caseID") Long caseID, @Valid CaseCreation createCase, BindingResult bindingResult) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {

            User lawyerToBeAdded = userRepository.findById(createCase.getCaseSupervisor()).get();
            Case theCase = caseRepository.findById(caseID).get();
            theCase.addLawyer(lawyerToBeAdded);
        }
        //return "redirect:/";
        return new RedirectView("case-display?caseID=" + caseID) ;
    }

    @GetMapping("/confirm-delete-lawyer")
    public String deleteLawyerMap(@RequestParam("caseID") Long caseID, @RequestParam(name="lawyerID") Integer lawyerID, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            User lawyerRemove = userRepository.findById((long)lawyerID).get();
            Case caseRemove = caseRepository.findById(caseID).get();
            model.addAttribute("caseName", caseRemove.getName());
            model.addAttribute("lawyerName", lawyerRemove.getName());
            model.addAttribute("lawyerID", lawyerID);
            model.addAttribute("caseID", caseID);
        }
        return "confirm-delete-lawyer";
    }

    @PostMapping("/confirm-delete-lawyer")
    public RedirectView deleteLawyerPost(@RequestParam("caseID") Long caseID, @RequestParam("lawyerID") Long lawyerID) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            User lawyerRemove = userRepository.findById(lawyerID).get();
            Case caseRemove = caseRepository.findById(caseID).get();
            caseRemove.removeUser(lawyerRemove);
        }
        //return "confirm-delete-lawyer";
        return new RedirectView("case-display?caseID=" + caseID) ;
    }

    @GetMapping("/add-client")
    public String addClientMap(@RequestParam(name="caseID") Integer caseID, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            model.addAttribute("caseCreation", new CaseCreation());
            model.addAttribute("caseID", caseID);
            List<Client> allClients = new ArrayList<>();
            for(Client x : clientRepository.findAll()) {
                allClients.add(x);
            }
            model.addAttribute("allClients", allClients);
        }
        return "add-client";
    }

    @PostMapping("/add-client")
    public RedirectView addClientPost(Model model, @RequestParam("caseID") Long caseID, @Valid CaseCreation createCase, BindingResult bindingResult) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            Client clientToBeAdded = clientRepository.findById(createCase.getCaseSupervisor()).get();
            return AddClientToCase(clientToBeAdded, caseID);
        }
        //return "redirect:/";
        return new RedirectView("/") ;
    }

    private RedirectView AddClientToCase(Client client, Long caseID)
    {
        if (caseID != null)
        {
            Optional<Case> opt = caseRepository.findById(caseID);
            if (opt.isPresent())
            {
                Case theCase = opt.get();
                theCase.addClient(client);
                caseRepository.save(theCase);
                return new RedirectView("case-display?caseID=" + caseID);
            }
        }
        return new RedirectView("/");
    }

    @PostMapping("/create-client")
    public RedirectView clientCreationPost(@RequestParam(name="caseID", required = false) Long caseID, HttpServletRequest request, @Valid ClientCreation createClient, BindingResult bindingResult, RedirectAttributes attributes)
    {
        if(bindingResult.hasErrors()) {
            return new RedirectView("create-client");
        }
        String[] fullAddress = {createClient.getAddress1(), createClient.getAddress2(), createClient.getCity(), createClient.getCounty(), createClient.getPostCode()};
        Client client = new Client(createClient.getFirstName(), createClient.getLastName(), createClient.getEmail(), fullAddress, createClient.getTelephone());

        //set some fields not set in constructor
        client.setAge(Client.Ages.valueOf(createClient.getAge()));
        client.setGender(Client.Genders.valueOf(createClient.getGender()));
        client.setEthnicity(Client.Ethnicities.valueOf(createClient.getEthnicity()));
        client.setDisability(Client.Disibilities.valueOf(createClient.getDisibility()));

        clientRepository.save(client);
        return AddClientToCase(client, caseID);
    }

    @GetMapping("/case-creation")
    public String creationMap(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //request.getSession().setAttribute("testkey", "vaue");
        //System.out.println("Setting in " + request.getSession());
        //attributes.addFlashAttribute("testwalk", new Walkthrough("test") );
        if (authentication.isAuthenticated())
        {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            model.addAttribute("fullname", user.getName());
            Set<String> walkthroughs = new HashSet<>();
            for (Walkthrough walkthrough: user.getAllWalkthroughs(walkthroughRepository))
            {
                walkthroughs.add(walkthrough.getName());
            }
            model.addAttribute("caseTypes", user.getAllWalkthroughs(walkthroughRepository).iterator());
            model.addAttribute("caseCreation", new CaseCreation());

            List<User> supervisors = new ArrayList<>();
            for(User x : userRepository.findAllByPermissionLevelEquals(User.PermissionLevel.Admin)) {
                supervisors.add(x);
            }
            model.addAttribute("supers", supervisors);

        }
        model.addAttribute("username", authentication.getName());

        return "case-creation";
    }

    @PostMapping("/case-creation")
    public RedirectView caseCreationPost(@ModelAttribute("caseCreationGathers") Set<Case> caseCreationGathers, RedirectAttributes attributes, @Valid CaseCreation createCase, BindingResult bindingResult, @RequestParam(value = "walkthroughid", required = false) Long walkthroughID, @RequestParam(value = "supervisorid", required = false) Long supervisorID)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            String caseName = createCase.getCaseName();

            Walkthrough caseWalkthrough = walkthroughRepository.findById(walkthroughID).get();
            Set<Client> clientSet = new HashSet<>();
            Set<User> userSet = new HashSet<>();
            userSet.add(user);

            User supervisor = userRepository.findById(createCase.getCaseSupervisor()).get();

            //User supervisor = user;

            Case newCase = new Case(caseName, caseWalkthrough, clientSet, userSet, Optional.of(supervisor));
            caseRepository.save(newCase);
            for (WalkthroughDocs doc : caseWalkthrough.getWalkthroughDocs())
            {
                Documents caseDoc = new Documents(doc, false, user, fileStorageService, newCase);
                newCase.addDoc(caseDoc);
                documentsRepository.save(caseDoc);
            }
            for (WalkthroughStep step : caseWalkthrough.getSteps())
            {
                CaseStep newStep = new CaseStep(step, newCase);
                caseStepRepository.save(newStep);
                for (WalkthroughStepDocs doc : step.getDocs())
                {
                    StepDocs stepDoc = new StepDocs(doc, newStep, fileStorageService);
                    newStep.addDoc(stepDoc);
                    stepDocsRepository.save(stepDoc);
                }
                newCase.addStep(newStep);
                caseStepRepository.save(newStep);
            }
            caseRepository.save(newCase);
            Action action = new Action(Action.ActionType.CREATE_CASE, user, newCase);
            actionRepository.save(action);
//            caseCreationGathers.add(newCase);
//            attributes.addFlashAttribute("caseCreationGathers", caseCreationGathers);
            //System.out.println(supervisor);
            return new RedirectView("create-client?caseID=" + newCase.getId());
        }
        return new RedirectView("/");
    }


    @Secured({"ROLE_ADMIN"})
    @GetMapping("/casetype-creation")
    public String caseTypeCreationMap(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            model.addAttribute("WalkthroughCreation", new WalkthroughCreation());
        }
        return "casetype-creation";
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/casetype-creation")
    public RedirectView caseTypeCreationPost(HttpServletRequest request, @ModelAttribute WalkthroughCreation walkthrough, BindingResult bindingResult, @ModelAttribute("currentSteps") List<WalkthroughStep> currentSteps, RedirectAttributes attributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            if(bindingResult.hasErrors()) {
                return new RedirectView("casetype-creation");
            }
            Walkthrough newWalkthrough = new Walkthrough(walkthrough.getName());
            walkthroughRepository.save(newWalkthrough);

            attributes.addFlashAttribute("currentSteps", currentSteps);
            return new RedirectView("walkthrough-display?id=" + newWalkthrough.getId());
        }
        return new RedirectView("/");
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/walkthrough-display")
    public String addstepMap(@RequestParam(name="id") Integer walkthroughid, HttpServletRequest request, Model model, RedirectAttributes attributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Walkthrough newwalk = walkthroughRepository.findById(walkthroughid.longValue()).get();

            model.addAttribute("walkthroughId", newwalk.getId());
            model.addAttribute("name", newwalk.getName());
            model.addAttribute("description", newwalk.getDescription());
            model.addAttribute("currentSteps", newwalk.getSteps());
//            System.out.println(newwalk.getSteps().get(1).getDocs());
            model.addAttribute("nosteps", Integer.toString(newwalk.getSteps().size()+1));
            model.addAttribute("stepCreation", new StepCreation());
            model.addAttribute("walkthroughDocs", newwalk.getWalkthroughDocs());
        }
        return "walkthrough-display";
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/add-walkthrough-step")
    public RedirectView addstepPost(@RequestParam(name="id") Integer walkthroughid, HttpServletRequest request, @ModelAttribute StepCreation newstep, BindingResult bindingResult, @ModelAttribute("currentSteps") List<WalkthroughStep> currentSteps, RedirectAttributes attributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Optional<Walkthrough> opt = walkthroughRepository.findById(walkthroughid.longValue());
            if (!opt.isPresent())
            {
                return new RedirectView("error");
            }
            else
            {
                Walkthrough newwalk = walkthroughRepository.findById(walkthroughid.longValue()).get();
                WalkthroughStep newstepConcrete = new WalkthroughStep(newstep.getName(), newwalk.getSteps().size() + 1, newwalk);
                walkthroughStepRepository.save(newstepConcrete);
                attributes.addFlashAttribute("currentSteps", currentSteps);
                return new RedirectView("walkthrough-display?id=" + newwalk.getId());
            }
        }
        return new RedirectView("walkthrough-display");
    }


    @Secured({"ROLE_ADMIN"})
    @GetMapping("/allwalkthroughs")
    public String allWalkthroughs(Model model)
    {
        model.addAttribute("allWalkthroughs", walkthroughRepository.findAll());
        return "allwalkthroughs";
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/confirm-delete-walkthrough")
    public String deleteWalkthroughGet(@RequestParam(name="id") Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            Walkthrough walkthrough = walkthroughRepository.findById(id).get();
            model.addAttribute("walkthroughName", walkthrough.getName());
            model.addAttribute("walkthroughID", id);
        }
        return "confirm-delete-walkthrough";
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping("/confirm-delete-walkthrough")
    public String deleteWalkthroughPost(@RequestParam("id") Long id, @Autowired WalkthroughDocsRepository walkthroughDocsRepository, @Autowired WalkthroughStepDocsRepository walkthroughStepDocsRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            Walkthrough walkthrough = walkthroughRepository.findById(id).get();
            walkthrough.Close(walkthroughStepRepository, walkthroughDocsRepository, walkthroughStepDocsRepository);
            walkthroughRepository.delete(walkthrough);
        }
        return "redirect:/";
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/addstepscase")
    public String addcasestepMap(@RequestParam(name="caseID") Integer caseid, HttpServletRequest request, Model model, @ModelAttribute("currentSteps") List<CaseStep> currentSteps, RedirectAttributes attributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Case currentCase = caseRepository.findById(caseid.longValue()).get();
            model.addAttribute("caseID", currentCase.getId());
            model.addAttribute("name", request.getSession().getAttribute("name"));
            model.addAttribute("currentSteps", currentCase.getSteps());
            model.addAttribute("nosteps", Integer.toString(currentCase.getSteps().size()+1));
            model.addAttribute("stepCreation", new StepCreation());

        }
        return "addstepscase";
    }

    @PostMapping("/addstepscase")
    public String addcasestepPost(@RequestParam(name="caseID") Integer caseID, @ModelAttribute StepCreation newstep){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Case currentCase = caseRepository.findById(caseID.longValue()).get();
            CaseStep ConcretenewStep = new CaseStep(newstep.getName(), currentCase.getSteps().size() + 1, currentCase);
            caseStepRepository.save(ConcretenewStep);
            caseRepository.save(currentCase);
            return "redirect:case-display?caseID=" + caseID;
        }
        return "allcases";
    }

    @ModelAttribute("currentSteps")
    public List<WalkthroughStep> currentSteps(){
        return new ArrayList<WalkthroughStep>();
    }

}
