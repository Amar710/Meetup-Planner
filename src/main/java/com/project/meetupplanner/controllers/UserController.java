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

import com.project.meetupplanner.models.email.EmailService;
import com.project.meetupplanner.models.users.User;
import com.project.meetupplanner.models.users.UserRepository;
import com.project.meetupplanner.models.users.UserService;

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
        if ("name".equalsIgnoreCase(type)) {
            List<User> nameList = userRepo.findByName(value);
            return !nameList.isEmpty();
        } else if ("email".equalsIgnoreCase(type)) {
            User existingUser = userRepo.findByEmail(value);
            return existingUser != null;
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
        User profile = userRepo.findByUid(userId).orElseThrow(() -> new RuntimeException("User not found"));
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
       User user = userRepo.findByUid(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setAdmin(true);
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("adminGranted", true);
        return "redirect:/adminView";
    }

    @PostMapping("/grantConfirm")
    public String GrantConfirm(@RequestParam("userId") Integer userId, RedirectAttributes redirectAttributes) {
        System.out.println("granting confirm access to user with ID: " + userId);
        User user = userRepo.findByUid(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
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
        User profile = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("profile", profile);
        
        List<User> friends = userService.getUserFriends(profile);
        model.addAttribute("users", friends);
        model.addAttribute("user", user);
    
        // Check if the logged-in user has sent a friend request to the profile user
        boolean sentFriendRequest = profile.getReceivedFriendRequests().contains(user);
    
        // Check if the profile user has sent a friend request to the logged-in user
        boolean receivedFriendRequest = profile.getSentFriendRequests().contains(user);
    
        model.addAttribute("sentFriendRequest", sentFriendRequest);
        model.addAttribute("receivedFriendRequest", receivedFriendRequest);
    
        return "users/userPages/friendView";
    }
    
    @PostMapping("/sendFriendRequest")
    public String sendFriendRequest(@RequestParam("friendName") String friendName, HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            // Redirect or handle the case where the user is not logged in
            return "redirect:/login";
        }

        List<User> friends = userRepo.findByName(friendName);
            if (friends.isEmpty()) {
                model.addAttribute("error", "Friend not found"); 
            return friendView(model, session); 
        }

        User friend = friends.get(0); 
        user.sendFriendRequest(friend);
        userRepo.save(user);
        return "redirect:/otherFriendView?userId=" + friend.getUid();
}


    @PostMapping("/acceptFriendRequest")
    public String acceptFriendRequest(@RequestParam("userId") Integer userId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            // Redirect or handle the case where the user is not logged in
            return "redirect:/login";
        }

        User friend = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the friend request exists before accepting
        if (!friend.getReceivedFriendRequests().contains(user)) {
            model.addAttribute("error", "Friend request not found");
            return friendView(model, session);
        }
        user.acceptFriendRequest(friend);
        userRepo.save(user);
        return "redirect:/otherFriendView?userId=" + userId;
    
    }
 
    @PostMapping("/unfriend")
    public String removeFriend(@RequestParam("userId") Integer friendId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            // Redirect or handle the case where the user is not logged in
            return "redirect:/login";
        }
        userService.removeFriend(user.getUid(), friendId);
    
        // Fetch the updated user object from the database
        User updatedUser = userRepo.findById(user.getUid()).orElseThrow(() -> new RuntimeException("User not found"));
    
        // Update the session with the updated user information
        session.setAttribute("session_user", updatedUser);
    
        // Add the 'user' object to the model (optional, but can be useful in the view)
        model.addAttribute("user", updatedUser);
    
        // Redirect to the friendView method with the updated friend list
        return friendView(model, session);
    }
    
    

    
    
}
