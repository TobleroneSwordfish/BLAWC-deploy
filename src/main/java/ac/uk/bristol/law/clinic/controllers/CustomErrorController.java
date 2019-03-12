package ac.uk.bristol.law.clinic.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

//does this do anything?
public class CustomErrorController implements ErrorController
{
    @GetMapping("/error")
    public String errorMap(HttpServletRequest httpRequest, Model model)
    {
        System.out.println("ERROR");
        Integer errorCode = (Integer)httpRequest.getAttribute("javax.servlet.error.status_code");
        System.out.println("Error code: " + errorCode);
        model.addAttribute("errorCode", errorCode);
        return "error";
    }
    @Override
    public String getErrorPath()
    {
        return "/error";
    }
}
