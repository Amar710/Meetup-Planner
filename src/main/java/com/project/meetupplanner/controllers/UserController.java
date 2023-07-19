package com.project.meetupplanner.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.project.meetupplanner.models.User;
import com.project.meetupplanner.models.UserRepository;
import com.project.meetupplanner.models.EmailService;
import com.project.meetupplanner.models.UserService;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    private final UserRepository userRepo;
    private final UserService userService;
    private final EmailService emailService;
    

    @Autowired
    public UserController(UserRepository userRepo, UserService userService, EmailService emailService) {
        this.userRepo = userRepo;
        this.userService = userService;
        this.emailService = emailService;
    }
    

    @GetMapping("/")
    public RedirectView process() {
        return new RedirectView("homepage.html");
    }
       
    @GetMapping("/users/exists")
    @ResponseBody
    public boolean userExists(@RequestParam String name) {
        List<User> nameList = userRepo.findByName(name);
        return !nameList.isEmpty();
    }

    @GetMapping("/users/add")
    public String getSignup(Model model) {
    model.addAttribute("user", new User());
    return "users/signup";
    }   
   
    @PostMapping("/users/add")
    public String addUser(@RequestParam Map<String, String> newuser, HttpServletResponse response) {
        try {
            String newName = newuser.get("name");
            String newPwd = newuser.get("password");
            String newEmail = newuser.get("email");

            // Check if user with the given email already exists
            User existingUser = userRepo.findByEmail(newEmail);
            if (existingUser != null) {
                response.setStatus(400); // Bad Request
                return "users/signupError";
        }

            // Generate confirmation code
            String confirmationCode = UUID.randomUUID().toString();

            // Save the user with the confirmation code
            User newUser = new User(newName, newEmail, newPwd);
            newUser.setConfirmationCode(confirmationCode);
            userRepo.save(newUser);

            // Send confirmation email
            String subject = "Confirm your email address";
            String message = "Dear " + newName + ",\n\n"
                    + "Thank you for signing up for MeetUp Planner! To complete your registration, please click the link below to confirm your email address:\n\n"
                    + "http://localhost:8080/confirm?code=" + confirmationCode + "\n\n"
                    + "If the above link doesn't work, you can also copy and paste the above link into your browser:\n\n"
                    + "If you did not sign up for MeetUp Planner, please ignore this email.\n\n"
                    + "Thank you,\n"
                    + "The MeetUp Planner Team";

            // Get the user's email from the form and pass it to the EmailService instance
            String recipientEmail = newuser.get("email");
            emailService.sendEmail(recipientEmail, subject, message);
            response.setStatus(201);
            return "users/signupSuccess";
        }   catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
            return "users/signupError";
     }
}

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("code") String confirmationCode, HttpServletResponse response) {
        User user = userRepo.findByConfirmationCode(confirmationCode);
        if (user != null) {
            user.setConfirmed(true);
            userRepo.save(user);
            return "users/confirmSuccess";
        } else {
            return "users/confirmError";
        }
    }

    @GetMapping("/login")
    public String getLogin(Model model, HttpServletResponse request, HttpSession session){
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            return "users/login";
        }
        else {
            model.addAttribute("user", user);
            return "users/protected";
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam Map<String, String> formData, Model model, HttpServletRequest request, HttpSession session) {
        // Processing the login
        String name = formData.get("name");
        String pwd = formData.get("password");
        List<User> userList = userRepo.findByNameAndPassword(name, pwd);
        
        if (userList.isEmpty()) {
            return "users/login";
        } 
        
        else {
            // Successful login
            User user = userList.get(0);
            request.getSession().setAttribute("session_user", user);
            model.addAttribute("user", user);

            User profile = (User) session.getAttribute("session_user");
            model.addAttribute("profile", profile);


            if(user.isAdmin()) 
                return adminView(model, session);
            else
                return "users/userProfile";
        }
    }


    @GetMapping("/logout")
    public String destroySession(HttpServletRequest request) {
        request.getSession().invalidate();
        return "users/login";
    }

    

    // user profile and admin link pather
    @GetMapping("/userProfile")
    public String userProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        model.addAttribute("user", user);

        User profile = (User) session.getAttribute("session_user");
        model.addAttribute("profile", profile);
        return "users/userProfile";
    }

     @PostMapping("/ViewUser")
    public String ViewUser(@RequestParam("userId") Integer userId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("View user with ID: " + userId);
        List<User> userList = userRepo.findByUid(userId);
        User profile = userList.get(0);
        model.addAttribute("profile", profile);

        User user = (User) session.getAttribute("session_user");
        model.addAttribute("user", user);
        return "users/userProfile";
    }


    @GetMapping("/adminView")
    public String adminView(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            // Redirect or handle the case where the user is not an admin
            return "redirect:/homepage";
        }
        
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", user);
        return "users/adminView";
    }

 // below is delete user functions
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("userId") Integer userId, RedirectAttributes redirectAttributes) {
        System.out.println("DELETE user with ID: " + userId);
        userRepo.deleteById(userId);
        redirectAttributes.addFlashAttribute("deletedUser", true);
        return "redirect:/adminView";
    }


    @PostMapping("/grantAdmin")
    public String grantAdmin(@RequestParam("userId") Integer userId, RedirectAttributes redirectAttributes) {
        System.out.println("granting admin access to user with ID: " + userId);
        List<User> userList = userRepo.findByUid(userId);
        User user = userList.get(0);
        user.setAdmin(true);
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("adminGranted", true);
        return "redirect:/adminView";
    }
    
        @GetMapping("/calendar")
    public String Calendar(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        model.addAttribute("user", user);

        User profile = (User) session.getAttribute("session_user");
        model.addAttribute("profile", profile);
        return "users/index";
    }

    // friend view code

    @GetMapping("/friendView")
    public String friendView(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            // Redirect or handle the case where the user is not logged in
            return "redirect:/login";
        }
        
        List<User> friends = userService.getUserFriends(user);
        
        model.addAttribute("users", friends);
        model.addAttribute("user", user);
        return "users/friendView";
    }
    
    
}
