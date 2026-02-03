package com.example.tellymobile;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupUtils {

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
                    Toast.makeText(context, "Backup Successful: " + backupDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    new DatabaseHelper(context).addNotification("Backup Successful", "Database backed up to: " + backupDB.getAbsolutePath(), "Success");
                } else {
                     Toast.makeText(context, "Database Not Found", Toast.LENGTH_SHORT).show();
                     new DatabaseHelper(context).addNotification("Backup Failed", "Original database file not found.", "Error");
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Backup Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, "Restore Successful! Restart App.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Backup File Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Restore Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
