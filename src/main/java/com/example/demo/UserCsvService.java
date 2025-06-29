package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

@Service
public class UserCsvService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GoogleDriveService googleDriveService;

    @Transactional
    public void importUsersFromCsv(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirst = true;
            while ((line = br.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; } // skip header
                String[] parts = line.split(",");
                if (parts.length != 4) continue;
                User user = new User();
                user.setId(parts[0].trim());
                user.setFullName(parts[1].trim());
                user.setBirthday(parts[2].trim());
                user.setExperience(Integer.parseInt(parts[3].trim()));
                userRepository.save(user);
            }
        }
    }
    
    /**
     * Import users from a CSV file stored in Google Drive
     * @param fileId The Google Drive file ID
     * @throws IOException if there's an error reading the file
     */
    @Transactional
    public void importUsersFromGoogleDrive(String fileId) throws IOException {
        // Get the CSV content from Google Drive
        String csvContent = googleDriveService.getFileContentAsString(fileId);
        
        try (BufferedReader br = new BufferedReader(new StringReader(csvContent))) {
            String line;
            boolean isFirst = true;
            while ((line = br.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; } // skip header
                String[] parts = line.split(",");
                if (parts.length != 4) continue;
                User user = new User();
                user.setId(parts[0].trim());
                user.setFullName(parts[1].trim());
                user.setBirthday(parts[2].trim());
                user.setExperience(Integer.parseInt(parts[3].trim()));
                userRepository.save(user);
            }
        }
    }
} 