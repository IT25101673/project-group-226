package com.parking.system;


import java.util.*;

import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;
import java.io.*;

@Controller
public class LoginController {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<String[]> vehicleList = new ArrayList<>();
        int totalSlots = getTotalSlots();
        try {
            File vFile = new File("vehicles.txt");
            if (vFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(vFile));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if(data.length >= 7) vehicleList.add(data);
                }
                br.close();
            }
        } catch (IOException e) { e.printStackTrace(); }

        model.addAttribute("totalSlots", totalSlots);
        model.addAttribute("vehicles", vehicleList);
        model.addAttribute("availableSlots", totalSlots - vehicleList.size());
        return "dashboard";
    }

    private int getTotalSlots() {
        try {
            File file = new File("settings.txt");
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                br.close();
                if (line != null) return Integer.parseInt(line.trim());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 10; // Default slots
    }

}