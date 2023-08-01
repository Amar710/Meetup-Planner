package com.project.meetupplanner.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

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
    public boolean userExists(@RequestParam String type, @RequestParam String value) {
        if ("email".equalsIgnoreCase(type)) {
            User existingUser = userRepo.findByEmail(value);
            return existingUser != null;
    }   else if ("username".equalsIgnoreCase(type)) {
            List<User> nameList = userRepo.findByName(value);
            return !nameList.isEmpty();
    }
    return false; 
}

    @GetMapping("/users/add")
    public String getSignup(Model model) {
    model.addAttribute("newUser", new User());
    return "users/signUp/signup";
    }   
   
    @PostMapping("/users/add")
    public String addUser(@RequestParam Map<String, String> newuser, Model model, HttpServletResponse response) {
        try {
            String newName = newuser.get("name");
            String newPwd = newuser.get("password");
            String newEmail = newuser.get("email");
            
            // Generate confirmation code
            String confirmationCode = UUID.randomUUID().toString();

            // Save the user with the confirmation code
            User newUser = new User(newName, newEmail, newPwd);
            newUser.setConfirmationCode(confirmationCode);
            userRepo.save(newUser);
            model.addAttribute("newUser", newUser);

            // Send confirmation email
            String subject = "Confirm your email address";
            String message = "Dear " + newName + ",\n\n"
                    + "Thank you for signing up for MeetUp Planner! To complete your registration, please click the link below to confirm your email address:\n\n"
                    + "https://meetup-planner.onrender.com/confirm?code=" + confirmationCode + "\n\n"
                    + "If the above link doesn't work, you can also copy and paste the above link into your browser:\n\n"
                    + "If you did not sign up for MeetUp Planner, please ignore this email.\n\n"
                    + "Thank you,\n"
                    + "The MeetUp Planner Team";

            // Get the user's email from the form and pass it to the EmailService instance
            String recipientEmail = newuser.get("email");
            emailService.sendEmail(recipientEmail, subject, message);
            response.setStatus(201);
            return "users/signUp/signupSuccess";
        }   catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
            return "users/signUp/signupError";
     }
}

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("code") String confirmationCode, HttpServletResponse response) {
        User user = userRepo.findByConfirmationCode(confirmationCode);
        if (user != null) {
            user.setConfirmed(true);
            userRepo.save(user);
            return "users/signUp/confirmSuccess";
        } else {
            return "users/signUp/confirmError";
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
                return "users/userPages/userProfile";
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
        return "users/userPages/userProfile";
    }

     @PostMapping("/ViewUser")
    public String ViewUser(@RequestParam("userId") Integer userId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("View user with ID: " + userId);
        List<User> userList = userRepo.findByUid(userId);
        User profile = userList.get(0);
        model.addAttribute("profile", profile);

        User user = (User) session.getAttribute("session_user");
        model.addAttribute("user", user);
        return "users/userPages/userProfile";
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
        return "users/userPages/adminView";
    }

//
 // below is admin control functions
 //
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("userId") Integer userId, RedirectAttributes redirectAttributes) {
        System.out.println("DELETE user with ID: " + userId);
    
        // Fetch the user to be deleted
        Optional<User> userToDeleteOpt = userRepo.findById(userId);
        if (!userToDeleteOpt.isPresent()) {
            // handle this case, maybe return an error message
            // let's assume for this example we just return
            return "redirect:/adminView";
        }
        User userToDelete = userToDeleteOpt.get();
    
        // Fetch all users from the database
        List<User> allUsers = userRepo.findAll();
    
        // Iterate through each user
        for (User user : allUsers) {
            // If the user's friends set contains the user being deleted, remove it
            if (user.getFriends().contains(userToDelete)) {
                user.removeFriend(userToDelete);
                // Save the changes made to the user
                userRepo.save(user);
            }
        }
    
        // Delete the user
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

    @PostMapping("/grantConfirm")
    public String GrantConfirm(@RequestParam("userId") Integer userId, RedirectAttributes redirectAttributes) {
        System.out.println("granting confirm access to user with ID: " + userId);
        List<User> userList = userRepo.findByUid(userId);
        User user = userList.get(0);
        user.setConfirmed(true);
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("confirmGranted", true);
        return "redirect:/adminView";
    }

    
    @GetMapping("/calendar")
    public String Calendar(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        model.addAttribute("user", user);

        User profile = (User) session.getAttribute("session_user");
        model.addAttribute("profile", profile);
        return "users/userPages/index";
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
        return "users/userPages/friendView";
    }

    @PostMapping("/otherFriendView")
    public String otherFriendView(@RequestParam("userId") Integer userId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            // Redirect or handle the case where the user is not logged in
            return "redirect:/login";
        }
        List<User> userList = userRepo.findByUid(userId);
        
        User profile = userList.get(0);
        model.addAttribute("profile", profile);
    
        
        List<User> friends = userService.getUserFriends(profile);
        
        model.addAttribute("users", friends);
        model.addAttribute("user", user);
        return "users/userPages/friendView";
    }
    
    @PostMapping("/addFriending")
    public String friending(@RequestParam("friendName") String friendsName, HttpSession session, Model model){
        System.out.println("friend user with name: " + friendsName);
        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            // Redirect or handle the case where the user is not logged in
            return "redirect:/login";
        }

        List<User> findUserfriend = userRepo.findByName(friendsName);

        // check if the user exist in the database
        if (findUserfriend.isEmpty()){
            model.addAttribute("confirmation", "That user doesn't exist. Ensure the name is properly added!");
            return "redirect:/users/userPages/friendView";
        }

        User friendingUser = findUserfriend.get(0);
        user.addFriend(friendingUser);
        userRepo.save(user);
        model.addAttribute("confirmation", "User have been added");

        return "redirect:/users/userPages/friendView";
    }

    @PostMapping("/unfriend")
    public String unfriending(@RequestParam("userId") Integer userid, HttpSession session, Model model) {
        System.out.println("Unfriending user with ID: " + userid);
        User user = (User) session.getAttribute("session_user");



        if (user == null) {
            // Redirect or handle the case where the user is not logged in
            return "redirect:/login";
        }

        // unfriending both the user and the unfriended user
        List<User> findUserUnfriend = userRepo.findByUid(userid);

        if (findUserUnfriend.isEmpty()){
            System.out.println("test1");
            return "redirect:/users/userPages/friendView";
        }

        User unfriendUser = findUserUnfriend.get(0);
        user.removeFriend(unfriendUser);
        userRepo.save(user);
        System.out.println("test");
        return "redirect:/users/userPages/friendView";
    }

    
}
