package com.example.tellymobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;

public class BackupSettingsActivity extends BaseActivity {

    public static final String PREFS_NAME = "BackupPrefs";
    public static final String KEY_SCHEDULE = "backup_schedule";
    public static final String SCHEDULE_NONE = "none";
    public static final String SCHEDULE_DAILY = "daily";
    public static final String SCHEDULE_WEEKLY = "weekly";
    public static final String SCHEDULE_MONTHLY = "monthly";

    private GoogleSignInClient mGoogleSignInClient;
    private TextView tvSignedInUser;
    private Button btnSignIn;
    private Button btnSignOut;
    private RadioGroup rgSchedule;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_settings);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSignedInUser = findViewById(R.id.tvSignedInUser);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignOut = findViewById(R.id.btnSignOut);
        rgSchedule = findViewById(R.id.rgSchedule);
        Button btnSaveSettings = findViewById(R.id.btnSaveSettings);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignIn.setOnClickListener(v -> signIn());
        btnSignOut.setOnClickListener(v -> signOut());
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        loadCurrentSettings();
        updateUI(GoogleSignIn.getLastSignedInAccount(this));
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    updateUI(null);
                    Toast.makeText(BackupSettingsActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
            Toast.makeText(this, "Signed in as " + account.getEmail(), Toast.LENGTH_SHORT).show();
        } catch (ApiException e) {
            Toast.makeText(this, "Sign in failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            updateUI(null);
        }
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            tvSignedInUser.setText(account.getEmail());
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
        } else {
            tvSignedInUser.setText("Not signed in");
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
        }
    }

    private void loadCurrentSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String schedule = prefs.getString(KEY_SCHEDULE, SCHEDULE_NONE);

        switch (schedule) {
            case SCHEDULE_DAILY:
                ((RadioButton) findViewById(R.id.rbDaily)).setChecked(true);
                break;
            case SCHEDULE_WEEKLY:
                ((RadioButton) findViewById(R.id.rbWeekly)).setChecked(true);
                break;
            case SCHEDULE_MONTHLY:
                ((RadioButton) findViewById(R.id.rbMonthly)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.rbNone)).setChecked(true);
                break;
        }
    }

    private void saveSettings() {
        if (GoogleSignIn.getLastSignedInAccount(this) == null && rgSchedule.getCheckedRadioButtonId() != R.id.rbNone) {
            Toast.makeText(this, "Please sign in to Google Drive first to schedule backups.", Toast.LENGTH_LONG).show();
            return;
        }

        String schedule = SCHEDULE_NONE;
        int checkedId = rgSchedule.getCheckedRadioButtonId();
        if (checkedId == R.id.rbDaily) {
            schedule = SCHEDULE_DAILY;
        } else if (checkedId == R.id.rbWeekly) {
            schedule = SCHEDULE_WEEKLY;
        } else if (checkedId == R.id.rbMonthly) {
            schedule = SCHEDULE_MONTHLY;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_SCHEDULE, schedule).apply();

        // Update WorkManager
        DriveBackupWorker.schedulePeriodicBackup(this, schedule);

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
