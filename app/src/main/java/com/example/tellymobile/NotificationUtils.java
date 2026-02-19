package com.example.tellymobile;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;

public class NotificationUtils {

    private static View activeNotification = null;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void showTopNotification(Activity activity, String message, boolean isError) {
        showTopNotification(activity, null, message, isError);
    }

    public static void showTopNotification(Activity activity, DatabaseHelper db, String message, boolean isError) {
        if (activity == null || activity.isFinishing()) return;

        // Save to database if db is provided
        if (db != null) {
            db.addNotification(isError ? "Error" : "Success", message, isError ? "Error" : "Success");
        }

        // Remove previous if exists
        dismissNotification();

        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        LayoutInflater inflater = LayoutInflater.from(activity);
        activeNotification = inflater.inflate(R.layout.layout_top_notification, decorView, false);

        MaterialCardView card = activeNotification.findViewById(R.id.cardNotification);
        TextView tvMessage = activeNotification.findViewById(R.id.tvNotificationMessage);
        ImageView ivIcon = activeNotification.findViewById(R.id.ivNotificationIcon);
        ImageView btnClose = activeNotification.findViewById(R.id.btnDismissNotification);

        tvMessage.setText(message);
        
        // Colors and Icons based on type
        if (isError) {
            card.setCardBackgroundColor(activity.getResources().getColor(R.color.error));
            ivIcon.setImageResource(R.drawable.ic_close);
        } else {
            card.setCardBackgroundColor(activity.getResources().getColor(R.color.primary));
            ivIcon.setImageResource(R.drawable.ic_receipt);
        }

        btnClose.setOnClickListener(v -> dismissNotification());

        // Add to decor view
        decorView.addView(activeNotification);

        // Slide Down Animation - account for status bar potentially
        // We set a slightly larger -Y and move to a positive Y if needed, 
        // but 16dp margin in XML + 0 translation might be okay.
        // Let's use 50f for translationY to push it down from the very top.
        activeNotification.setTranslationY(-300f);
        activeNotification.animate()
                .translationY(20f) // Push it down a bit from the very top
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Auto-dismiss after 3.5 seconds
        handler.removeCallbacksAndMessages(null); // Clear any pending dismissals
        handler.postDelayed(() -> dismissNotification(), 3500);
    }

    private static void dismissNotification() {
        if (activeNotification != null) {
            final View currentNotification = activeNotification;
            activeNotification = null;

            currentNotification.animate()
                    .translationY(-250f)
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        ViewGroup parent = (ViewGroup) currentNotification.getParent();
                        if (parent != null) {
                            parent.removeView(currentNotification);
                        }
                    })
                    .start();
        }
    }
}
