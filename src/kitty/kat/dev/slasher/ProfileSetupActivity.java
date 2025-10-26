package kitty.kat.dev.slasher;

import android.Manifest;
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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class ProfileSetupActivity extends Activity {
    
    private static final int REQUEST_PERMISSIONS = 1001;
    private static final int REQUEST_OVERLAY = 1002;
    private static final int REQUEST_BATTERY = 1003;
    private static final String PREFS_NAME = "SlasherPrefs";
    private static final String KEY_FIRST_RUN = "isFirstRun";
    
    private ArrayList<String> selectedProducts = new ArrayList<>();
    private ArrayList<String> selectedStores = new ArrayList<>();
    private boolean hasPayPalDebit = false;
    private String googleAccount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        googleAccount = getIntent().getStringExtra("googleAccount");
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#3A3A3A")); // Medium dark grey
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 60);
        
        // Title
        TextView title = new TextView(this);
        title.setText("Build Your Profile");
        title.setTextSize(36);
        title.setTextColor(Color.parseColor("#9D4EDD")); // Midnight purple
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        layout.addView(title);
        
        // Question 1: Products
        TextView q1Label = new TextView(this);
        q1Label.setText("What kind of products do you like?");
        q1Label.setTextSize(20);
        q1Label.setTextColor(Color.WHITE);
        q1Label.setPadding(0, 20, 0, 20);
        layout.addView(q1Label);
        
        String[] products = {"Electronics", "Clothing", "Home Goods", "Groceries", "Beauty Products"};
        for (String product : products) {
            CheckBox cb = new CheckBox(this);
            cb.setText(product);
            cb.setTextColor(Color.WHITE);
            cb.setTextSize(16);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedProducts.add(product);
                } else {
                    selectedProducts.remove(product);
                }
            });
            layout.addView(cb);
        }
        
        // Question 2: Stores
        TextView q2Label = new TextView(this);
        q2Label.setText("What stores do you shop at frequently?");
        q2Label.setTextSize(20);
        q2Label.setTextColor(Color.WHITE);
        q2Label.setPadding(0, 40, 0, 20);
        layout.addView(q2Label);
        
        String[] stores = {"Walmart", "Target", "Amazon", "Best Buy", "Costco"};
        for (String store : stores) {
            CheckBox cb = new CheckBox(this);
            cb.setText(store);
            cb.setTextColor(Color.WHITE);
            cb.setTextSize(16);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedStores.add(store);
                } else {
                    selectedStores.remove(store);
                }
            });
            layout.addView(cb);
        }
        
        // Question 3: PayPal Debit
        TextView q3Label = new TextView(this);
        q3Label.setText("Do you have a PayPal Debit Card?");
        q3Label.setTextSize(20);
        q3Label.setTextColor(Color.WHITE);
        q3Label.setPadding(0, 40, 0, 20);
        layout.addView(q3Label);
        
        RadioGroup paypalGroup = new RadioGroup(this);
        paypalGroup.setOrientation(RadioGroup.VERTICAL);
        
        RadioButton yesButton = new RadioButton(this);
        yesButton.setText("Yes");
        yesButton.setTextColor(Color.WHITE);
        yesButton.setTextSize(16);
        yesButton.setId(View.generateViewId());
        
        RadioButton noButton = new RadioButton(this);
        noButton.setText("No");
        noButton.setTextColor(Color.WHITE);
        noButton.setTextSize(16);
        noButton.setId(View.generateViewId());
        
        paypalGroup.addView(yesButton);
        paypalGroup.addView(noButton);
        
        paypalGroup.setOnCheckedChangeListener((group, checkedId) -> {
            hasPayPalDebit = (checkedId == yesButton.getId());
        });
        
        layout.addView(paypalGroup);
        
        // Next Button
        Button nextButton = new Button(this);
        nextButton.setText("Next");
        nextButton.setTextSize(18);
        nextButton.setTextColor(Color.WHITE);
        nextButton.setPadding(60, 30, 60, 30);
        
        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setColor(Color.parseColor("#9D4EDD")); // Midnight purple
        buttonBg.setCornerRadius(25);
        nextButton.setBackground(buttonBg);
        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 60, 0, 0);
        buttonParams.gravity = Gravity.CENTER;
        nextButton.setLayoutParams(buttonParams);
        
        nextButton.setOnClickListener(v -> saveProfileAndContinue());
        
        layout.addView(nextButton);
        
        scrollView.addView(layout);
        setContentView(scrollView);
    }
    
    private void saveProfileAndContinue() {
        // Validate at least one selection
        if (selectedProducts.isEmpty() || selectedStores.isEmpty()) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save data locally
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Convert ArrayList to comma-separated string manually
        StringBuilder productsStr = new StringBuilder();
        for (int i = 0; i < selectedProducts.size(); i++) {
            productsStr.append(selectedProducts.get(i));
            if (i < selectedProducts.size() - 1) {
                productsStr.append(",");
            }
        }
        
        StringBuilder storesStr = new StringBuilder();
        for (int i = 0; i < selectedStores.size(); i++) {
            storesStr.append(selectedStores.get(i));
            if (i < selectedStores.size() - 1) {
                storesStr.append(",");
            }
        }
        
        editor.putString("products", productsStr.toString());
        editor.putString("stores", storesStr.toString());
        editor.putBoolean("hasPayPalDebit", hasPayPalDebit);
        editor.apply();
        
        // TODO: Save to Google account (Gmail) in background
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        
        // Mark first run complete
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
        
        // Request runtime permissions
        requestRuntimePermissions();
    }
    
    private void requestRuntimePermissions() {
        // Request dangerous permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WAKE_LOCK
            }, REQUEST_PERMISSIONS);
        } else {
            continuePermissionRequests();
        }
    }
    
    private void continuePermissionRequests() {
        // Request overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
                return;
            }
        }
        
        requestBatteryOptimization();
    }
    
    private void requestBatteryOptimization() {
        // Request battery optimization exemption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "Please allow app to run in background", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_BATTERY);
            return;
        }
        
        requestAccessibilityService();
    }
    
    private void requestAccessibilityService() {
        // Request accessibility service (user must enable manually)
        Toast.makeText(this, "Please enable Accessibility Service for full functionality", Toast.LENGTH_LONG).show();
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
            continuePermissionRequests();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY) {
            requestBatteryOptimization();
        } else if (requestCode == REQUEST_BATTERY) {
            requestAccessibilityService();
        }
    }
}