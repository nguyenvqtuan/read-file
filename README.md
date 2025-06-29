# ReadFile API

A Spring Boot application that integrates with Google Drive API to read and process CSV files.

## Features

- Google Drive API integration
- CSV file processing
- User management with MySQL database
- RESTful API endpoints

## Prerequisites

- Java 17 or higher
- Gradle
- MySQL database
- Google Cloud Platform account

## Setup Instructions

### 1. Google Cloud Platform Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing project
3. Enable Google Drive API
4. Create OAuth 2.0 credentials:
   - Go to APIs & Services > Credentials
   - Click "Create Credentials" > "OAuth client ID"
   - Choose "Desktop application" as application type
   - Download the client secret JSON file

### 2. OAuth Configuration

1. Rename your downloaded client secret file to:
   ```
   client_secret_212810218755-77nlm7lshqot74a4mrhqg94u4n5al2lo.apps.googleusercontent.com.json
   ```
2. Place it in `src/main/resources/`
3. Add your email as a test user in OAuth consent screen (if app is in testing mode)

### 3. Database Setup

1. Start MySQL database (using Docker):
   ```bash
   docker-compose up -d
   ```
2. The application will automatically create tables on startup

### 4. Application Configuration

Update `src/main/resources/application.properties` with your database credentials if needed.

## Running the Application

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## API Endpoints

- `GET /api/drive/files` - List files in Google Drive
- `GET /api/drive/search?fileName={name}` - Search files by name
- `GET /api/drive/files/{fileId}/metadata` - Get file metadata
- `GET /api/drive/files/{fileId}/download` - Download file
- `GET /api/drive/files/{fileId}/content` - Get file content as text
- `POST /api/drive/files/{fileId}/import-users` - Import users from CSV file
- `GET /api/drive/diagnostic/oauth-config` - Check OAuth configuration

## Security Notes

⚠️ **Important**: The following files are excluded from version control for security:

- `src/main/resources/client_secret_*.json` - OAuth client secrets
- `tokens/` - OAuth tokens directory
- Any files containing sensitive credentials

## Troubleshooting

### OAuth Issues

- Ensure you're using a "Desktop application" OAuth client, not "Web application"
- Add your email as a test user in Google Cloud Console OAuth consent screen
- Clear browser cache/cookies if authentication fails

### Database Issues

- Ensure MySQL is running on port 3307
- Check database credentials in `application.properties`

## Development

This project uses:

- Spring Boot 3.x
- Google Drive API
- MySQL database
- Gradle build system
