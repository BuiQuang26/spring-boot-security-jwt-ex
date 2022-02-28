package com.example.demo.Controllers;

import com.example.demo.Models.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/users")
    public List<?> getUsers(){
        List<User> userList = userRepository.findAll();
        return userList;
    }
}
