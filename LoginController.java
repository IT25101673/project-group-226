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


  
    @GetMapping("/userManagement")
    public String userManagement(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/dashboard";
        List<String[]> userList = new ArrayList<>();
        try {
            File uFile = new File("users.txt");
            if (uFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(uFile));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] uData = line.split(",");
                    if(uData.length >= 3) userList.add(uData);
                }
                br.close();
            }
        } catch (IOException e) { e.printStackTrace(); }
        model.addAttribute("allUsers", userList);
        return "userManagement";
    }

    @PostMapping("/addUser")
    public String addUser(@RequestParam String newUsername, @RequestParam String newPassword, @RequestParam String role, HttpSession session) {
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("users.txt", true)))) {
                out.println(newUsername.trim() + "," + newPassword.trim() + "," + role.trim());
            } catch (IOException e) { e.printStackTrace(); }
        }
        return "redirect:/userManagement";
    }

    @GetMapping("/deleteUser")
    public String deleteUser(@RequestParam String username, HttpSession session) {
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            try {
                List<String> lines = new ArrayList<>();
                File file = new File("users.txt");
                if(file.exists()){
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith(username + ",")) lines.add(line);
                    }
                    br.close();
                    PrintWriter pw = new PrintWriter(new FileWriter("users.txt"));
                    for (String l : lines) pw.println(l);
                    pw.close();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
        return "redirect:/userManagement";
    }
}