package kitty.kat.dev.slasher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ProfileSetupActivity:
 * - Receives googleAccount (email) extra from WelcomeActivity.
 * - If provided, saves created profile to shared preferences keyed with the email:
 *   key = "config_" + email
 * - After saving, navigates to MainActivity.
 */
public class ProfileSetupActivity extends Activity {

    private static final String PREFS_NAME = "SlasherPrefs";
    private String googleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read passed account email
        Intent i = getIntent();
        if (i != null && i.hasExtra("googleAccount")) {
            googleAccount = i.getStringExtra("googleAccount");
        }

        // Simple profile UI
        ScrollView sv = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#3A3A3A"));

        TextView title = new TextView(this);
        title.setText("Create your profile");
        title.setTextSize(20f);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        EditText nameField = new EditText(this);
        nameField.setHint("Display name");
        nameField.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(nameField);

        EditText phoneField = new EditText(this);
        phoneField.setHint("Phone (optional)");
        phoneField.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(phoneField);

        // Add more profile fields as desired...
        Button saveButton = new Button(this);
        saveButton.setText("Save profile");
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(18f);
        saveButton.setBackground(bg);
        saveButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a display name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save profile to SharedPreferences keyed by account email
            if (googleAccount == null || googleAccount.isEmpty()) {
                Toast.makeText(this, "No Google account specified", Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String key = "config_" + googleAccount;

            // We'll keep a simple JSON-ish string for demo; change to structured serialization as needed
            String value = "name=" + name + ";phone=" + phone;
            prefs.edit().putString(key, value).apply();

            Toast.makeText(this, "Profile saved for " + googleAccount, Toast.LENGTH_SHORT).show();

            // Continue to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("googleAccount", googleAccount);
            startActivity(intent);
            finish();
        });
        layout.addView(saveButton);

        sv.addView(layout);
        setContentView(sv);
    }
}
