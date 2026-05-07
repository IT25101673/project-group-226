package com.parking.system;


import jakarta.servlet.http.HttpSession;
import java.util.*;

import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.ui.Model;
import java.io.*;

@Controller
public class LoginController {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            File userFile = new File("users.txt");
            if (userFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(userFile));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] details = line.split(",");
                    if (details[0].trim().equals(username) && details[1].trim().equals(password)) {
                        session.setAttribute("userRole", details[2].trim());
                        session.setAttribute("userName", username);
                        br.close();
                        return "redirect:/dashboard";
                    }
                }
                br.close();
            }
        } catch (IOException e) { e.printStackTrace(); }
        model.addAttribute("error", "Invalid Credentials!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }


}