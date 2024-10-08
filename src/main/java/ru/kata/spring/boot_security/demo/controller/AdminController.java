package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleRepository roleRepository;

public AdminController(@Qualifier ("userServiceImplRepo") UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String getUsers(Model model) {
        model.addAttribute("usersList", userService.getUsers());
        if (model.containsAttribute("searchedUser")) {
            model.addAttribute("showSearchResult", true);
        }

        return "admin";
    }

    @GetMapping("/add")
    public String showAddUserForm(@ModelAttribute("user") User user, Model model) {
        model.addAttribute("rolesList", roleRepository.findAll());
        return "save-user-form";
    }

    @PostMapping("/save")
    public String addUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        boolean userAdded = userService.addUser(user);
        if (userAdded) {
            return "redirect:/admin";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User with username '" + user.getUsername() + "' already exists.");
            return "redirect:/admin/add";
        }
    }

    @GetMapping("/find")
    public String searchUser(@RequestParam("username") String name, Model model) {
        if (name != null && !name.isEmpty()) {
            User user;
            if ((user = userService.loadUserByUsername(name)) != null) {
                model.addAttribute("user", user);
                return "find-result";
            }
        }
        return "user-not-found";
    }

    @GetMapping("/user-not-found")
    public String showUserNotFound() {
        return "user-not-found";
    }

    @GetMapping("/getInfo")
    public String getUserByName(@RequestParam("name") String name, Model model) {
        model.addAttribute("user", userService.loadUserByUsername(name));
        model.addAttribute("highLev", true);
        return "user-page";
    }

    @PostMapping("/update")
    public String updateUser(@RequestParam("password") String password, @ModelAttribute("user") User user) {
        if (user != null) {
            User existingUser = userService.loadUserByUsername(user.getUsername());
            existingUser.setPassword(password);
            userService.updateUser(existingUser);
            return "redirect:/admin/getInfo?name=" + existingUser.getUsername();
        } else {
            return "user-not-found";
        }
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("name") String name) {
        userService.deleteUser(name);
        return "redirect:/admin";
    }
}
