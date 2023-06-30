package com.project.meetupplanner.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.meetupplanner.models.User;
import com.project.meetupplanner.models.UserRespository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UserController {
    
    @Autowired
    private UserRespository userRepo;

    @PostMapping("/users/add")
    public String addUser(@RequestParam Map<String,String> newUser, HttpServletResponse response)
    {
        System.out.println("Adding User");

        // get the users' inserted element from the webpage
        String newEmail = newUser.get("email");
        String newName = newUser.get("name");
        String newPassword = newUser.get("password");

        // add the user to the database
        userRepo.save(new User(newEmail, newName, newPassword));
        response.setStatus(201);
        return "/users/success.html";
    }
}
