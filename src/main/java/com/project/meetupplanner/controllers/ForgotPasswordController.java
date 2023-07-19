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

import com.project.meetupplanner.models.User;
import com.project.meetupplanner.models.UserRepository;
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

    @PostMapping("/forgotPassword")
    public String sendForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String token = RandomString.make(45);
        User sendUser = userRepo.findByEmail(email);
        if (sendUser == null)
        {
            return "users/resetError";
        }
        User user = sendUser;
        user.setResetPasswordToken(token);
        userRepo.save(user);

        try {
            String resetPasswordLink = EmailUtil.getSiteUrl(request) + "/reset_password?token=" + token;
            sendEmail(email,resetPasswordLink);

        } catch (UnsupportedEncodingException e) {
            return "users/resetError";
        }
        return "users/resetMailSent";
        
    }

    private void sendEmail(String email, String resetPasswordLink) throws UnsupportedEncodingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
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

    @GetMapping("/reset_password")
    public String showResetPassword(@Param(value = "token") String token, Model model) {
        List<User> targetUser = userRepo.findByResetPasswordToken(token);
        User user = targetUser.get(0);

        if (user == null){
            model.addAttribute("message", "Invalid Token");
            return "users/invalidToken";
        }
        model.addAttribute("token", token);
        return "users/resetPassword";
    }
    
    @PostMapping("/resetPassword")
    public String changePassword(HttpServletRequest request, Model model, HttpServletResponse response) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");
        List<User> targetUser = userRepo.findByResetPasswordToken(token);
        User user = targetUser.get(0);
        if (user == null){
            model.addAttribute("message", "Invalid Token");
            return "users/invalidToken";
        }
        
        else {
            user.setPassword(password);
            user.setResetPasswordToken(null);
            userRepo.save(user);
            response.setStatus(201);
            return "/users/resetSuccess";
        }
    }
    
}
