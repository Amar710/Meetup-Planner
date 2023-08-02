package com.project.meetupplanner.controllers;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import net.bytebuddy.utility.RandomString;

import com.project.meetupplanner.models.users.User;
import com.project.meetupplanner.models.users.UserRepository;
import com.project.meetupplanner.Utilities.EmailUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ForgotPasswordController {
    
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JavaMailSender mailSender;

    // creates a token associated with the account we wish to change password
    @PostMapping("/forgotPassword")
    public String sendForgotPassword(HttpServletRequest request, Model model) {
        
        // gets the email from the database and attach a token to it
        String email = request.getParameter("email");
        String token = RandomString.make(45);
        User sendUser = userRepo.findByEmail(email);
        if (sendUser == null)
        {
            return "users/reset/resetError";
        }

        // save the token into the database
        User user = sendUser;
        user.setResetPasswordToken(token);
        userRepo.save(user);

        // send the email that contains a password reset link that is connected to that token
        try {
            String resetPasswordLink = EmailUtil.getSiteUrl(request) + "/reset_password?token=" + token;
            sendEmail(email,resetPasswordLink);

        } catch (UnsupportedEncodingException e) {
            return "users/reset/resetError";
        }
        return "users/reset/resetMailSent";
        
    }

    // the message the email will contain
    private void sendEmail(String email, String resetPasswordLink) throws UnsupportedEncodingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        // creates the message that will be sent
        try {
            helper.setFrom("MeetUpPlannerSupport.com", "Meetup-Planner Support");
            helper.setTo(email);
            String subject = "Password Reset";
            String content = "<p>Hello,</p>" 
                            + "<p>You have requested to reset your password.</p>"
                            + "<p><b><a href=\"" + resetPasswordLink + "\">Change my password</a><b></p>"
                            + "<p>Ignore this email if you do remember your password, or you have not made the request.</p>";
            
            helper.setSubject(subject);
            helper.setText(content,true);
            mailSender.send(message);

        } catch (MessagingException e){
            
        }
    }

    // will send the user to the website that will allow them to change their password
    @GetMapping("/reset_password")
    public String showResetPassword(@Param(value = "token") String token, Model model) {
        List<User> targetUser = userRepo.findByResetPasswordToken(token);
        
        if (targetUser.isEmpty()){
            return "users/reset/invalidToken";
        }

        model.addAttribute("token", token);
        return "users/reset/resetPassword";
    }
    
    // update the database with the new password
    @PostMapping("/resetPassword")
    public String changePassword(HttpServletRequest request, Model model, HttpServletResponse response) {
        
        // get the updated password and token associated with it
        String token = request.getParameter("token");
        String password = request.getParameter("password");
        List<User> targetUser = userRepo.findByResetPasswordToken(token);
        User user = targetUser.get(0);

        if (targetUser.isEmpty()){
            model.addAttribute("message", "Invalid Token");
            return "users/reset/invalidToken";
        }
        
        else {
            // saves the password to the associated account
            user.setPassword(password);
            user.setResetPasswordToken(null);
            userRepo.save(user);
            response.setStatus(201);
            return "/users/reset/resetSuccess";
        }
    }
    
}
