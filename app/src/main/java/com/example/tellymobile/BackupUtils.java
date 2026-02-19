package com.example.tellymobile;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupUtils {
    
    private static void showNotification(Context context, String message, boolean isError) {
        if (context instanceof android.app.Activity) {
            NotificationUtils.showTopNotification((android.app.Activity) context, new DatabaseHelper(context), message, isError);
        } else {
            new DatabaseHelper(context).addNotification(isError ? "Error" : "Success", message, isError ? "Error" : "Success");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void backupDatabase(Context context) {
        try {
            File sd = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.tellymobile//databases//TellyMobile.db";
                String backupDBPath = "TellyMobile_Backup.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    showNotification(context, "Backup Successful: " + backupDB.getAbsolutePath(), false);
                } else {
                     showNotification(context, "Database Not Found", true);
                }
            }
        } catch (Exception e) {
            showNotification(context, "Backup Failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    public static void restoreDatabase(Context context) {
        try {
            File sd = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File data = Environment.getDataDirectory();

            if (sd.canRead()) {
                String currentDBPath = "//data//com.example.tellymobile//databases//TellyMobile.db";
                String backupDBPath = "TellyMobile_Backup.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (backupDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    showNotification(context, "Restore Successful! Restart App.", false);
                } else {
                    showNotification(context, "Backup File Not Found", true);
                }
            }
        } catch (Exception e) {
            showNotification(context, "Restore Failed: " + e.getMessage(), true);
        }
    }

    public static void backupToUri(Context context, android.net.Uri uri) {
        try {
            File data = Environment.getDataDirectory();
            String currentDBPath = "//data//com.example.tellymobile//databases//TellyMobile.db";
            File currentDB = new File(data, currentDBPath);

            if (currentDB.exists()) {
                try (java.io.InputStream in = new FileInputStream(currentDB);
                     java.io.OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                    
                    if (out != null) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        showNotification(context, "Backup Successful!", false);
                    } else {
                         throw new java.io.IOException("Output stream is null");
                    }
                }
            } else {
                showNotification(context, "Database Not Found", true);
            }
        } catch (Exception e) {
            showNotification(context, "Backup Failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    public static void restoreFromUri(Context context, android.net.Uri uri) {
        try {
            File data = Environment.getDataDirectory();
            String currentDBPath = "//data//com.example.tellymobile//databases//TellyMobile.db";
            File currentDB = new File(data, currentDBPath);

            try (java.io.InputStream in = context.getContentResolver().openInputStream(uri);
                 java.io.OutputStream out = new FileOutputStream(currentDB)) {
                 
                if (in != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    showNotification(context, "Restore Successful! Restart App.", false);
                } else {
                     throw new java.io.IOException("Input stream is null");
                }
            }
        } catch (Exception e) {
            showNotification(context, "Restore Failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
}
