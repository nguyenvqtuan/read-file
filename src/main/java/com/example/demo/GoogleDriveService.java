package com.example.demo;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "ReadFile API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_READONLY);

    @Value("${google.drive.oauth.port:8888}")
    private int oauthPort;

    @Value("${google.drive.oauth.redirect.uri:http://localhost:8888/Callback}")
    private String redirectUri;

    private Drive driveService;

    public GoogleDriveService() {
        try {
            this.driveService = getDriveService();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize Google Drive service", e);
        }
    }

    /**
     * Creates an authorized Credential object.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        ClassPathResource resource = new ClassPathResource("client_secret_212810218755-u9vpuae3cdk1i2dqjeqagbr3m5hphpq2.apps.googleusercontent.com.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(resource.getInputStream()));

        // Check if we have web client configuration by looking at the JSON structure
        if (clientSecrets.getWeb() != null && clientSecrets.getInstalled() == null) {
            throw new IOException("This OAuth client is configured as 'web' application. " +
                    "Please either: 1) Change the OAuth client type to 'desktop' in Google Cloud Console, " +
                    "or 2) Use a different authentication method for web applications.");
        }

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(oauthPort)
                .build();
        
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Build and return an authorized Drive client service.
     */
    private Drive getDriveService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Download a file from Google Drive by file ID
     */
    public byte[] downloadFile(String fileId) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Download a file from Google Drive by file ID and save to local path
     */
    public void downloadFileToLocal(String fileId, String localPath) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(localPath)) {
            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
        }
    }

    /**
     * Get file metadata by file ID
     */
    public File getFileMetadata(String fileId) throws IOException {
        return driveService.files().get(fileId).execute();
    }

    /**
     * List files in Google Drive (limited to 10 files for demo)
     */
    public List<File> listFiles() throws IOException {
        return driveService.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, mimeType, size)")
                .execute()
                .getFiles();
    }

    /**
     * Search for files by name
     */
    public List<File> searchFilesByName(String fileName) throws IOException {
        String query = "name contains '" + fileName + "'";
        return driveService.files().list()
                .setQ(query)
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, mimeType, size)")
                .execute()
                .getFiles();
    }

    /**
     * Get file content as string (for text files)
     */
    public String getFileContentAsString(String fileId) throws IOException {
        byte[] fileContent = downloadFile(fileId);
        return new String(fileContent);
    }
} 