package com.example.tellymobile;

import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class DriveServiceHelper {

    private final Drive mDriveService;
    private static final String FOLDER_NAME = "TellyMobileBackups";
    private static final String APP_DATA_FOLDER = "appDataFolder";
    public static final String BACKUP_FILE_NAME = "TellyMobile_Backup.db";

    public DriveServiceHelper(Context context, GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        mDriveService = new Drive.Builder(new NetHttpTransport(), new GsonFactory(), credential)
                .setApplicationName("TellyMobile")
                .build();
    }

    public void uploadDatabaseFile(java.io.File file) throws IOException {
        String folderId = getOrCreateFolder("appDataFolder", FOLDER_NAME);

        // Check if backup file already exists
        String existingFileId = getFileId(folderId, BACKUP_FILE_NAME);

        if (existingFileId != null) {
            // Update existing file
            FileContent mediaContent = new FileContent("application/x-sqlite3", file);
            File body = new File(); // Empty body for update
            mDriveService.files().update(existingFileId, body, mediaContent).execute();
        } else {
            // Create new file
            File fileMetadata = new File();
            fileMetadata.setName(BACKUP_FILE_NAME);
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("application/x-sqlite3", file);

            mDriveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    public void downloadDatabaseFile(java.io.File destination) throws IOException {
        String folderId = getOrCreateFolder("appDataFolder", FOLDER_NAME);
        String fileId = getFileId(folderId, BACKUP_FILE_NAME);

        if (fileId == null) {
            throw new IOException("Backup file not found on Google Drive.");
        }

        try (OutputStream outputStream = new FileOutputStream(destination)) {
            mDriveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
        }
    }
    
    // Check if the backup exists on Drive
    public boolean backupExists() throws IOException {
        String folderId = getOrCreateFolder("appDataFolder", FOLDER_NAME);
        return getFileId(folderId, BACKUP_FILE_NAME) != null;
    }

    private String getOrCreateFolder(String space, String name) throws IOException {
        String query = String.format("name = '%s' and mimeType = 'application/vnd.google-apps.folder' and '%s' in parents and trashed = false", name, space);
        FileList result = mDriveService.files().list()
                .setSpaces(space)
                .setQ(query)
                .setFields("nextPageToken, files(id, name)")
                .execute();

        for (File file : result.getFiles()) {
            if (file.getName().equals(name)) {
                return file.getId(); // Folder exists
            }
        }

        // Folder doesn't exist, create it
        File folderMetadata = new File();
        folderMetadata.setName(name);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        if (!space.equals("drive")) {
             folderMetadata.setParents(Collections.singletonList(space));
        }

        File folder = mDriveService.files().create(folderMetadata)
                .setFields("id")
                .execute();
        return folder.getId();
    }

    private String getFileId(String parentId, String name) throws IOException {
        String query = String.format("name = '%s' and '%s' in parents and trashed = false", name, parentId);
        FileList result = mDriveService.files().list()
                .setSpaces(APP_DATA_FOLDER)
                .setQ(query)
                .setFields("nextPageToken, files(id, name)")
                .execute();

        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }
        return null;
    }
}
