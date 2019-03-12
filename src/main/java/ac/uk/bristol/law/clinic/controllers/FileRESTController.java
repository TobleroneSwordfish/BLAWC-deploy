package ac.uk.bristol.law.clinic.controllers;

import ac.uk.bristol.law.clinic.*;
import ac.uk.bristol.law.clinic.entities.*;
import ac.uk.bristol.law.clinic.entities.cases.Case;
import ac.uk.bristol.law.clinic.entities.cases.CaseStep;
import ac.uk.bristol.law.clinic.entities.Documents;
import ac.uk.bristol.law.clinic.entities.cases.StepDocs;
import ac.uk.bristol.law.clinic.repositories.*;
import ac.uk.bristol.law.clinic.services.FilePermissionService;
import ac.uk.bristol.law.clinic.services.FileStorageService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


@Controller
public class FileRESTController
{
    @Autowired
    FileStorageService fileStore;

    @Autowired
    CaseRepository caseRepository;

    @Autowired
    FilePermissionService permissionService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DocumentsRepository documentsRepository;

    @Autowired
    StepDocsRepository stepDocsRepository;

    @Autowired
    CaseStepRepository caseStepRepository;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    //java doesn't have optional params
    private ResponseEntity tryGetFile(Long caseID, String filename)
    {
        return tryGetFile(caseID, -1l, filename);
    }

    //try to fetch the file from storage, returning a response based on whether it was found
    private ResponseEntity tryGetFile(Long caseID, Long stepID, String filename)
    {
        try
        {
            String stringPath;
            //is a stepID specified
            if (stepID > 0)
            {
                stringPath = "case/" + caseID + "/step/" + stepID + "/" + filename;
            }
            else
            {
                stringPath = "case/" + caseID + "/" + filename;
            }
            if (fileStore.fileExists(stringPath))
            {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication.isAuthenticated())
                {
                    User user = controllerUtils.getUserFromUsername(authentication.getName());
                    if (permissionService.userHasRead(user, caseRepository.findById(caseID).get()))
                    {
                        File file = fileStore.getFile(stringPath);
                        InputStream inputStream = new FileInputStream(file);
                        byte[] bytes = new byte[inputStream.available()];
//                        ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(HttpStatus.FOUND);
                        IOUtils.readFully(inputStream, bytes);
                        HttpHeaders headers = new HttpHeaders();
                        if (filename.endsWith(".pdf"))
                        {
                            headers.setContentType(MediaType.APPLICATION_PDF);
                        }
                        else if (filename.endsWith(".png"))
                        {
                            headers.setContentType(MediaType.IMAGE_PNG);
                        }
                        else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
                        {
                            headers.setContentType(MediaType.IMAGE_JPEG);
                        }
                        headers.add("Content-Disposition","inline; filename= " + file.getName());
                        return ResponseEntity.ok().contentLength(bytes.length)
                                .contentType(MediaType.parseMediaType("application/octet-stream")).headers(headers).body(bytes);
                    }
                    else
                    {
                        return new ResponseEntity(HttpStatus.FORBIDDEN);
                    }
                }
                else
                {
                    return new ResponseEntity(HttpStatus.FORBIDDEN);
                }
            }
            else
            {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/files/case/{caseID}/step/{stepID}/{filename}")
    public ResponseEntity getFile(@PathVariable("caseID") Long caseID, @PathVariable("stepID") Long stepID, @PathVariable("filename") String filename)
    {
//        System.out.println("Received request for file " + filename);
        return tryGetFile(caseID, stepID, filename);
    }

    @GetMapping("/files/case/{caseID}/{filename}")
    public ResponseEntity getFile(@PathVariable("caseID") Long caseID,
                                  @PathVariable("filename") String filename)
    {
        return tryGetFile(caseID, filename);
    }

    private ResponseEntity tryStoreFile(MultipartFile file, Long caseID)
    {
        return tryStoreFile(file, caseID, -1l);
    }

    private ResponseEntity tryStoreFile(MultipartFile file, Long caseID, Long stepID)
    {
        String stringPath;
        if (stepID > 0)
        {
            stringPath = "case/" + caseID + "/step/" + stepID + "/";
        }
        else
        {
            stringPath = "case/" + caseID + "/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            User user = controllerUtils.getUserFromUsername(authentication.getName());
            if (permissionService.userHasWrite(user, caseRepository.findById(caseID).get())) {
                try
                {
                    //actually store the file
                    InputStream inStream = file.getInputStream();
                    String filename = file.getOriginalFilename();
                    if (filename != "")
                    {
                        try
                        {
                            fileStore.storeFile(stringPath, inStream, filename);
                            Case theCase = caseRepository.findById(caseID).get();
                            boolean fileExists;
                            if (stepID > 0)
                            {
                                fileExists = !storeStepDoc(file, theCase.getSteps().get(stepID.intValue() - 1), stringPath, filename, user);
                            }
                            else
                            {
                                fileExists = !storeCaseDoc(file, theCase, stringPath, filename, user);
                            }
                            Action action = new Action(Action.ActionType.UPLOAD_FILE, user, theCase);
                            action.setBody(filename);
                            actionRepository.save(action);
                        }
                        catch(Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                    }
                    inStream.close();
                }
                catch (Exception e)
                {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else
            {
                System.out.println("User does not have permission");
                return new ResponseEntity(HttpStatus.FORBIDDEN);
            }
        }
        else
        {
            System.out.println("User is not logged in");
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean storeCaseDoc(MultipartFile file, Case theCase, String path, String filename, User user)
    {
        if (!theCase.getDocs().stream().anyMatch(d -> d.getUrl() == path))
        {
            //System.out.println("Adding new doc entry");
            Documents doc = new Documents(filename,true, user, "/" + path + filename);
            theCase.addDoc(doc);
            doc.setCaseowner(theCase);
            documentsRepository.save(doc);
            caseRepository.save(theCase);
            return true;
        }
        return false;
    }

    private boolean storeStepDoc(MultipartFile file, CaseStep step, String path, String filename, User user)
    {
        if (!step.getDocs().stream().anyMatch(d -> d.getName() == filename))
        {
            StepDocs doc = new StepDocs(filename, step);
            step.addDoc(doc);
            doc.setCasestep(step);
            doc.setUrl("/" + path + filename);
            stepDocsRepository.save(doc);
            caseStepRepository.save(step);
            return true;
        }
        return false;
    }

    @PostMapping("/files/case/{caseID}")
    public String uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file, @PathVariable("caseID") Long caseID)
    {
        tryStoreFile(file, caseID);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/files/case/{caseID}/step/{stepID}")
    public String uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file, @PathVariable("caseID") Long caseID, @PathVariable("stepID") Long stepID)
    {
        tryStoreFile(file, caseID, stepID);
        return "redirect:" + request.getHeader("Referer");
    }

    //TODO: delet this
    @GetMapping("upload-test")
    public String test()
    {
        return "upload-test";
    }

}
