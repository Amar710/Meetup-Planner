package com.project.meetupplanner.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.project.meetupplanner.models.User;
import com.project.meetupplanner.models.UserRespository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserRespository userRepo;


    @GetMapping("/")
    public RedirectView process() {
        return new RedirectView("homepage.html");
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam Map<String, String> newuser, HttpServletResponse response) {
        System.out.println("ADD user");
        String newName = newuser.get("name");
        String newPwd = newuser.get("password");
        String newEmail = newuser.get("email");
        userRepo.save(new User(newName, newEmail, newPwd)); 
        response.setStatus(201);
        return "users/addedUser";
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
        } else {
            // Successful login
            User user = userList.get(0);
            request.getSession().setAttribute("session_user", user);
            model.addAttribute("user", user);

            if(user.getAdmin()) 
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

}