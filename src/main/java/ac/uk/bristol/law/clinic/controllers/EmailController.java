package ac.uk.bristol.law.clinic.controllers;

import ac.uk.bristol.law.clinic.DTO.SendEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class EmailController
{
    @Autowired
    JavaMailSender mailSender;

    @GetMapping("/send-email")
    public String getMapping(Model model, @RequestParam(value = "address", required = false) String[] addresses)
    {
        System.out.println("Addresses received: " + addresses);
        model.addAttribute("addresses", addresses);
        SendEmail dto = new SendEmail();
        dto.addresses = new ArrayList<>();
        if (addresses != null)
        {
            dto.addresses.addAll(Arrays.asList(addresses));
        }
        model.addAttribute("sendEmail", dto);
        return "send-email";
    }

    @PostMapping("/send-email")
    public void postMapping(@ModelAttribute SendEmail sendEmail)
    {
        System.out.println("Post mapping called");
        System.out.println(sendEmail.getBody());
        MimeMessage msg = mailSender.createMimeMessage();
        //SimpleMailMessage msg = new SimpleMailMessage();
        try
        {
            msg.setContent(sendEmail.getBody(), "text/html");
        }
        catch (MessagingException e)
        {
            System.out.println("Error setting mime content of message: ");
            System.out.println(e);
        }
        MimeMailMessage mailMsg = new MimeMailMessage(msg);
        mailMsg.setTo((String[])sendEmail.getAddresses().toArray(new String[sendEmail.getAddresses().size()]));
        mailMsg.setFrom("blawctestemail@gmail.com");
        mailMsg.setSubject(sendEmail.getSubject());
        System.out.println(msg);
        mailSender.send(msg);
    }
}
