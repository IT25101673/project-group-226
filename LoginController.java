package com.parking.system;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import java.io.*;

@Controller
public class LoginController {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @PostMapping("/addVehicle")
    public String addVehicle(@RequestParam String vNumber, @RequestParam String owner,
                             @RequestParam String model, @RequestParam String customerType,
                             @RequestParam("vehicleImage") MultipartFile file, HttpSession session) {
        try {
            String entryUser = (String) session.getAttribute("userName");
            if (entryUser == null) entryUser = "System";

            int totalSlots = getTotalSlots();
            List<String[]> currentVehicles = new ArrayList<>();
            File vFile = new File("vehicles.txt");
            if (vFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(vFile));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if(data.length >= 5) currentVehicles.add(data);
                }
                br.close();
            }

            if (currentVehicles.size() < totalSlots) {
                String assignedSlot = "";
                for (int i = 1; i <= totalSlots; i++) {
                    String slotName = "Slot-" + String.format("%02d", i);
                    boolean isOccupied = false;
                    for (String[] v : currentVehicles) {
                        if (v[0].trim().equals(slotName)) { isOccupied = true; break; }
                    }
                    if (!isOccupied) { assignedSlot = slotName; break; }
                }

                String fileName = "default.jpg";
                if (!file.isEmpty()) {
                    String uploadDir = new File("src/main/resources/static/images/uploaded_images/").getAbsolutePath();
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();
                    fileName = vNumber.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".jpg";
                    file.transferTo(new File(dir + File.separator + fileName));
                }

                String entryTime = LocalDateTime.now().format(formatter);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter("vehicles.txt", true))) {
                    bw.write(assignedSlot + "," + vNumber.trim() + "," + owner.trim() + "," +
                            model.trim() + "," + entryTime + "," + customerType + "," + fileName + "," + entryUser);
                    bw.newLine();
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return "redirect:/dashboard";
    }

    @GetMapping("/deleteVehicle")
    public String deleteVehicle(@RequestParam String vNumber, HttpSession session, Model model) {
        String owner = "", slot = "", imageName = "default.jpg", entryTimeStr = "", entryUser = "Unknown";
        String exitUser = (String) session.getAttribute("userName");
        if (exitUser == null) exitUser = "System";

        long fee = 0, minutes = 0;
        int rate = getRatePerMinute(); // Settings වලින් rate එක ගන්නවා
        boolean isRemoved = false;
        List<String> remainingLines = new ArrayList<>();

        try {
            File file = new File("vehicles.txt");
            if(file.exists()){
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] details = line.split(",");
                    if (details[1].trim().equals(vNumber.trim())) {
                        slot = details[0];
                        owner = details[2];
                        entryTimeStr = details[4];
                        imageName = details[6];
                        if (details.length >= 8) entryUser = details[7].trim();

                        LocalDateTime entryTime = LocalDateTime.parse(entryTimeStr.trim(), formatter);
                        Duration duration = Duration.between(entryTime, LocalDateTime.now());
                        minutes = Math.max(1, duration.toMinutes());


                        fee = Math.max(50, minutes * rate);
                        isRemoved = true;
                    } else {
                        remainingLines.add(line);
                    }
                }
                br.close();
            }

            if (isRemoved) {
                PrintWriter pw = new PrintWriter(new FileWriter("vehicles.txt"));
                for (String l : remainingLines) pw.println(l);
                pw.close();

                try (BufferedWriter historyBw = new BufferedWriter(new FileWriter("history.txt", true))) {
                    String exitTime = LocalDateTime.now().format(formatter);
                    String handledBy = entryUser + " | " + exitUser;
                    historyBw.write(slot + "," + vNumber + "," + owner + "," + fee + "," + exitTime + "," + imageName + "," + entryTimeStr + "," + handledBy);
                    historyBw.newLine();
                }
            }
            model.addAttribute("vNumber", vNumber);
            model.addAttribute("owner", owner);
            model.addAttribute("minutes", minutes);
            model.addAttribute("fee", fee);
        } catch (Exception e) { e.printStackTrace(); }
        return "invoice";
    }

    @GetMapping("/adminSettings")
    public String showSettings(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/dashboard";


        model.addAttribute("totalSlots", getTotalSlots());
        model.addAttribute("currentRate", getRatePerMinute());
        return "adminSettings";
    }

    @PostMapping("/updateSettings")
    public String updateSettings(@RequestParam int newLimit, @RequestParam int newRate, HttpSession session, RedirectAttributes ra) {
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            try (PrintWriter out = new PrintWriter(new FileWriter("settings.txt"))) {
                out.println(newLimit);
                out.println(newRate);
            } catch (IOException e) { e.printStackTrace(); }
        }
        ra.addFlashAttribute("msg", "Settings Updated Successfully!");
        return "redirect:/adminSettings";
    }

    private int getRatePerMinute() {
        try {
            File file = new File("settings.txt");
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                br.readLine(); // Slots පේළිය මඟ හරින්න
                String line = br.readLine(); // Rate එක ගන්න
                br.close();
                if (line != null) return Integer.parseInt(line.trim());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 2; // Default rate
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