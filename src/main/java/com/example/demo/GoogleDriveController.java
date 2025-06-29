package com.example.demo;

import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/drive")
@CrossOrigin(origins = "*")
public class GoogleDriveController {

    @Autowired
    private GoogleDriveService googleDriveService;
                                            
    @Autowired
    private UserCsvService userCsvService;

    /**
     * List all files in Google Drive
     */
    @GetMapping("/files")
    public ResponseEntity<List<File>> listFiles() {
        try {
            List<File> files = googleDriveService.listFiles();
            return ResponseEntity.ok(files);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search files by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<File>> searchFiles(@RequestParam String fileName) {
        try {
            List<File> files = googleDriveService.searchFilesByName(fileName);
            return ResponseEntity.ok(files);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get file metadata by file ID
     */
    @GetMapping("/files/{fileId}/metadata")
    public ResponseEntity<File> getFileMetadata(@PathVariable String fileId) {
        try {
            File file = googleDriveService.getFileMetadata(fileId);
            return ResponseEntity.ok(file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download file content as bytes
     */
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileId) {
        try {
            byte[] fileContent = googleDriveService.downloadFile(fileId);
            File metadata = googleDriveService.getFileMetadata(fileId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getName() + "\"");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get file content as text (for text files)
     */
    @GetMapping("/files/{fileId}/content")
    public ResponseEntity<String> getFileContent(@PathVariable String fileId) {
        try {
            String content = googleDriveService.getFileContentAsString(fileId);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download file to local server path
     */
    @PostMapping("/files/{fileId}/download-local")
    public ResponseEntity<String> downloadFileToLocal(@PathVariable String fileId, @RequestParam String localPath) {
        try {
            googleDriveService.downloadFileToLocal(fileId, localPath);
            return ResponseEntity.ok("File downloaded successfully to: " + localPath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download file: " + e.getMessage());
        }
    }
    
    /**
     * Import users from a CSV file in Google Drive
     */
    @PostMapping("/files/{fileId}/import-users")
    public ResponseEntity<String> importUsersFromGoogleDrive(@PathVariable String fileId) {
        try {
            userCsvService.importUsersFromGoogleDrive(fileId);
            return ResponseEntity.ok("Users imported successfully from Google Drive file: " + fileId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import users: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV file: " + e.getMessage());
        }
    }

    /**
     * Diagnostic endpoint to check OAuth configuration
     */
    @GetMapping("/diagnostic/oauth-config")
    public ResponseEntity<String> checkOAuthConfiguration() {
        try {
            org.springframework.core.io.ClassPathResource resource = 
                new org.springframework.core.io.ClassPathResource("client_secret_212810218755-77nlm7lshqot74a4mrhqg94u4n5al2lo.apps.googleusercontent.com.json");
            
            com.google.api.client.json.JsonFactory jsonFactory = com.google.api.client.json.gson.GsonFactory.getDefaultInstance();
            com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets clientSecrets = 
                com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.load(jsonFactory, new java.io.InputStreamReader(resource.getInputStream()));
            
            StringBuilder diagnostic = new StringBuilder();
            diagnostic.append("OAuth Client Configuration Diagnostic:\n");
            diagnostic.append("=====================================\n");
            
            if (clientSecrets.getWeb() != null) {
                diagnostic.append("❌ OAuth client is configured as 'WEB' application\n");
                diagnostic.append("   This is causing the 'Access blocked' error!\n");
                diagnostic.append("   Client ID: ").append(clientSecrets.getWeb().getClientId()).append("\n");
                diagnostic.append("\nSOLUTION:\n");
                diagnostic.append("1. Go to Google Cloud Console\n");
                diagnostic.append("2. Navigate to APIs & Services > Credentials\n");
                diagnostic.append("3. Edit your OAuth 2.0 Client ID\n");
                diagnostic.append("4. Change Application type from 'Web application' to 'Desktop application'\n");
                diagnostic.append("5. Download the new client secret file\n");
                diagnostic.append("6. Replace the existing client_secret_*.json file\n");
            } else if (clientSecrets.getInstalled() != null) {
                diagnostic.append("✅ OAuth client is correctly configured as 'DESKTOP' application\n");
                diagnostic.append("   Client ID: ").append(clientSecrets.getInstalled().getClientId()).append("\n");
            } else {
                diagnostic.append("❓ Unknown OAuth client configuration\n");
            }
            
            return ResponseEntity.ok(diagnostic.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking OAuth configuration: " + e.getMessage());
        }
    }
} 