package ac.uk.bristol.law.clinic.controllers;

import ac.uk.bristol.law.clinic.*;
import ac.uk.bristol.law.clinic.DTO.*;
import ac.uk.bristol.law.clinic.entities.*;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.entities.cases.CaseStep;
import ac.uk.bristol.law.clinic.entities.Documents;
import ac.uk.bristol.law.clinic.entities.cases.StepDocs;
import ac.uk.bristol.law.clinic.entities.walkthroughs.Walkthrough;
import ac.uk.bristol.law.clinic.entities.walkthroughs.WalkthroughStep;
import ac.uk.bristol.law.clinic.repositories.*;
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
            List<Case> cases = new ArrayList<>(user.getMyCases());
            cases.sort((o1, o2) -> new Long(o1.getId() - o2.getId()).intValue());
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
    public String accountPost(@ModelAttribute UserEdit userEdit)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            user.setEmail(userEdit.getEmail());
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
            model.addAttribute("caseDocs", currentCase.getDocs());
            model.addAttribute("assignees", currentCase.getUsers());
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

    @PostMapping("/confirm-delete")
    public String deleteCasePost(@RequestParam("caseID") Long caseID) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            Case casedelete = caseRepository.findById(caseID).get();
            casedelete.Close(stepDocsRepository, actionRepository, documentsRepository, caseStepRepository);
            caseRepository.delete(casedelete);
        }
        for(Case r : caseRepository.findAll()){
            System.out.println("!!!!!");
        }
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
    public String clientMap(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        System.out.print(walktest.getName());
        if (authentication.isAuthenticated())
        {
            User user = controllerUtils.getUserFromUsername(authentication.getName());


            Set<Case> caseSet = (Set<Case>) model.asMap().get("caseCreationGathers");
            boolean seshattribute = caseSet.isEmpty();
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

    @PostMapping("/create-client")
    public RedirectView clientCreationPost(HttpServletRequest request, @ModelAttribute("caseCreationGathers") Set<Case> caseCreationGathers, @Valid ClientCreation createClient, BindingResult bindingResult, RedirectAttributes attributes)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer i = 0;
        Long newCaseID = Long.valueOf(i);
        if (authentication.isAuthenticated()) {
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
            Case newcase = caseCreationGathers.iterator().next();//COMMENTED
            newcase.addClient(client);
            caseRepository.save(newcase);
            newCaseID = newcase.getId();
            //attributes.addFlashAttribute("user-overview");
        }
        //return "redirect: /user-overview"; //for some reason returns a 404??
        return new RedirectView("case-display?caseID=" + newCaseID) ;
        //return new RedirectView("/");
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

            Case newCase = new Case(caseName, caseWalkthrough, clientSet, userSet, supervisor);
            this.caseRepository.save(newCase);
            Action action = new Action(Action.ActionType.CREATE_CASE, user, newCase);
            actionRepository.save(action);
            caseCreationGathers.add(newCase);
            attributes.addFlashAttribute("caseCreationGathers", caseCreationGathers);
            System.out.println(supervisor);
//            request.getSession().setAttribute("testkey", "value");
        }

        return new RedirectView("create-client");//COMMENTED
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

    @PostMapping("/casetype-creation")
    public RedirectView caseTypeCreationPost(HttpServletRequest request, @ModelAttribute WalkthroughCreation walkthrough, BindingResult bindingResult, @ModelAttribute("currentSteps") List<WalkthroughStep> currentSteps, RedirectAttributes attributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {

            //System.out.println("\u001B[31m" + "1." + "\u001B[0m");
            if(bindingResult.hasErrors()) {
                return new RedirectView("casetype-creation");
            }
            request.getSession().setAttribute("name", walkthrough.getName());
            request.getSession().setAttribute("description", walkthrough.getDescription());
            //System.out.println("\u001B[31m" + "casetype postmap" + walkthrough.getName() + "\u001B[0m");
            Walkthrough newWalkthrough = new Walkthrough(walkthrough.getName());
            walkthroughRepository.save(newWalkthrough);

            request.getSession().setAttribute("walkthroughId", newWalkthrough);

            attributes.addFlashAttribute("currentSteps", currentSteps);
            return new RedirectView("addsteps?walkthroughid=" + newWalkthrough.getId());
        }
        return new RedirectView("/");
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("/addsteps")
    public String addstepMap(@RequestParam(name="walkthroughid") Integer walkthroughid, HttpServletRequest request, Model model, @ModelAttribute("currentSteps") List<WalkthroughStep> currentSteps, RedirectAttributes attributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //System.out.println("\u001B[31m" + "get addsteps" + "\u001B[0m");
        if (authentication.isAuthenticated()) {
            //String string = (String)request.getSession().getAttribute("walkthroughId");
            Walkthrough newwalk = walkthroughRepository.findById(walkthroughid.longValue()).get();//(Walkthrough)request.getSession().getAttribute("walkthroughId");//walkthroughRepository.findById(Long.getLong(string)).get();

            //System.out.println(newwalk.getSteps().size());
            model.addAttribute("walkthroughId", newwalk.getId());
            model.addAttribute("name", request.getSession().getAttribute("name"));
            model.addAttribute("description", request.getSession().getAttribute("description"));
            model.addAttribute("currentSteps", newwalk.getSteps());
            model.addAttribute("nosteps", Integer.toString(newwalk.getSteps().size()+1));
            model.addAttribute("stepCreation", new stepCreation());

            //attributes.addFlashAttribute("currentSteps", currentSteps);
        }
        return "addsteps";
    }

    @PostMapping("/addsteps")
    public RedirectView addstepPost(@RequestParam(name="walkthroughid") Integer walkthroughid, HttpServletRequest request, @ModelAttribute stepCreation newstep, BindingResult bindingResult, @ModelAttribute("currentSteps") List<WalkthroughStep> currentSteps, RedirectAttributes attributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            //System.out.println("\u001B[31m" + "addsteps postmap" + Integer.parseInt(newstep.getNumber()) + "\u001B[0m");


            //System.out.println("\u001B[31m" + currentSteps.size() + "\u001B[0m");
            //currentSteps.add(new WalkthroughStep(newstep.getName(), Integer.parseInt(newstep.getNumber()), (Walkthrough) request.getSession().getAttribute("walkthroughId")));
            //Walkthrough newwalk = (Walkthrough)request.getSession().getAttribute("walkthroughId");//walkthroughRepository.findById((Long)request.getSession().getAttribute("walkthroughId")).get();
            Walkthrough newwalk = walkthroughRepository.findById(walkthroughid.longValue()).get();

            WalkthroughStep newstepConcrete = new WalkthroughStep(newstep.getName(), Integer.parseInt(newstep.getNumber()), newwalk);
            walkthroughStepRepository.save(newstepConcrete);
            //System.out.println("\u001B[31m" + currentSteps.size() + "\u001B[0m");

            attributes.addFlashAttribute("currentSteps", currentSteps);
            return new RedirectView("addsteps?walkthroughid=" + newwalk.getId());
        }
        return new RedirectView("addsteps");
    }

    @ModelAttribute("currentSteps")
    public List<WalkthroughStep> currentSteps(){
        return new ArrayList<WalkthroughStep>();
    }

//    @GetMapping("favicon.ico")
//    public String favicon()
//    {
//        System.out.println("favicon requested");
//        return "https://mybristol.bris.ac.uk/favicon.ico";
//    }
}
