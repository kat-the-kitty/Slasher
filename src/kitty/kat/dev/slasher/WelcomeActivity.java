package kitty.kat.dev.slasher;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity {
    
    private static final int REQUEST_PERMISSIONS = 1001;
    private static final int REQUEST_OVERLAY = 1002;
    private static final int REQUEST_BATTERY = 1003;
    private static final String PREFS_NAME = "SlasherPrefs";
    private static final String KEY_FIRST_RUN = "isFirstRun";
    private static final String KEY_GOOGLE_ACCOUNT = "googleAccount";
    
    private String selectedGoogleAccount = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if this is first run
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);
        
        if (!isFirstRun) {
            // Not first run, go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        
        // Create UI
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);
        layout.setBackgroundColor(Color.parseColor("#3A3A3A")); // Medium dark grey
        
        // Welcome Text
        TextView welcomeText = new TextView(this);
        welcomeText.setText("Welcome");
        welcomeText.setTextSize(48);
        welcomeText.setTextColor(Color.parseColor("#9D4EDD")); // Midnight purple
        welcomeText.setGravity(Gravity.CENTER);
        welcomeText.setPadding(0, 0, 0, 100);
        
        // Sign In Button
        Button signInButton = new Button(this);
        signInButton.setText("Sign in with Google");
        signInButton.setTextSize(18);
        signInButton.setTextColor(Color.WHITE);
        signInButton.setPadding(40, 30, 40, 30);
        
        // Button background with rounded corners
        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setColor(Color.parseColor("#9D4EDD")); // Midnight purple
        buttonBg.setCornerRadius(25);
        signInButton.setBackground(buttonBg);
        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        signInButton.setLayoutParams(buttonParams);
        
        // Button click listener
        signInButton.setOnClickListener(v -> signInWithGoogle());
        
        layout.addView(welcomeText);
        layout.addView(signInButton);
        
        setContentView(layout);
    }
    
    private void signInWithGoogle() {
        // Request GET_ACCOUNTS permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSIONS);
                return;
            }
        }
        
        // Get Google accounts on device
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        
        if (accounts.length == 0) {
            Toast.makeText(this, "No Google account found. Please add one in Settings.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Use first Google account (or show picker if multiple)
        selectedGoogleAccount = accounts[0].name;
        
        Toast.makeText(this, "Signed in as: " + selectedGoogleAccount, Toast.LENGTH_SHORT).show();
        
        // Save account
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_GOOGLE_ACCOUNT, selectedGoogleAccount).apply();
        
        // TODO: Check Gmail for existing profile data
        // For now, assume no data found and go to profile setup
        Toast.makeText(this, "No profile found. Let's create one!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        intent.putExtra("googleAccount", selectedGoogleAccount);
        startActivity(intent);
        finish();
    }
    
    private void requestRuntimePermissions() {
        // Request dangerous permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WAKE_LOCK
            }, REQUEST_PERMISSIONS);
        }
        
        // Request overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
                return;
            }
        }
        
        // Request battery optimization exemption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_BATTERY);
            return;
        }
        
        // Request accessibility service (user must enable manually)
        Toast.makeText(this, "Please enable Accessibility Service in Settings", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        
        // Go to main activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            // Continue with other permission requests
            requestRuntimePermissions();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY || requestCode == REQUEST_BATTERY) {
            // Continue with next permission
            requestRuntimePermissions();
        }
    }
}