package com.example.tellymobile;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class DriveBackupWorker extends Worker {

    private static final String WORK_NAME = "DriveBackupWork";

    public DriveBackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        if (account == null) {
            return Result.failure();
        }

        try {
            DriveServiceHelper helper = new DriveServiceHelper(context, account);
            
            // Current database path
            File data = Environment.getDataDirectory();
            String currentDBPath = "//data//" + context.getPackageName() + "//databases//TellyMobile.db";
            File currentDB = new File(data, currentDBPath);

            if (currentDB.exists()) {
                helper.uploadDatabaseFile(currentDB);
                new DatabaseHelper(context).addNotification("Success", "Scheduled Google Drive Backup Successful", "Success");
                return Result.success();
            } else {
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    public static void schedulePeriodicBackup(Context context, String schedule) {
        WorkManager workManager = WorkManager.getInstance(context);

        if (BackupSettingsActivity.SCHEDULE_NONE.equals(schedule)) {
            workManager.cancelUniqueWork(WORK_NAME);
            return;
        }

        long repeatInterval;
        TimeUnit timeUnit = TimeUnit.DAYS;

        switch (schedule) {
            case BackupSettingsActivity.SCHEDULE_DAILY:
                repeatInterval = 1;
                break;
            case BackupSettingsActivity.SCHEDULE_WEEKLY:
                repeatInterval = 7;
                break;
            case BackupSettingsActivity.SCHEDULE_MONTHLY:
                repeatInterval = 30;
                break;
            default:
                return;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(DriveBackupWorker.class, repeatInterval, timeUnit)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, // Update the schedule if it changed
                request
        );
    }
}
