package com.parking.system;


import jakarta.servlet.http.HttpSession;
import java.util.*;

import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;
import java.io.*;

@Controller
public class LoginController {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/dashboard";

        List<String[]> historyList = new ArrayList<>();
        long totalEarnings = 0, todayEarnings = 0;
        String todayDate = java.time.LocalDate.now().toString();

        Map<String, Long> dailyEarnings = new LinkedHashMap<>();
        Map<String, Long> monthlyEarnings = new LinkedHashMap<>();

        try {
            File file = new File("history.txt");
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] data = line.split(",");
                    if (data.length >= 8) {
                        historyList.add(data);
                        try {
                            long fee = Long.parseLong(data[3].trim());
                            totalEarnings += fee;
                            String date = data[4].trim().split(" ")[0];
                            String month = date.substring(0, 7);
                            if (date.equals(todayDate)) todayEarnings += fee;
                            dailyEarnings.put(date, dailyEarnings.getOrDefault(date, 0L) + fee);
                            monthlyEarnings.put(month, monthlyEarnings.getOrDefault(month, 0L) + fee);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
                br.close();
            }
        } catch (IOException e) { e.printStackTrace(); }

        model.addAttribute("history", historyList);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("todayEarnings", todayEarnings);
        model.addAttribute("dailyEarnings", dailyEarnings);
        model.addAttribute("monthlyEarnings", monthlyEarnings);
        return "history";
    }




}