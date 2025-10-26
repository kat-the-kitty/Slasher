package kitty.kat.dev.slasher;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private static final int REQ_RUNTIME_PERMISSIONS = 101;
    private static final int REQ_SYSTEM_ALERT = 102;
    private static final int REQ_ACCOUNT_PICKER = 103;

    private Button signInButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        signInButton = findViewById(R.id.sign_in_button);

        // Request all runtime permissions on first launch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAllRuntimePermissions();
        }

        // Request SYSTEM_ALERT_WINDOW separately if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_SYSTEM_ALERT);
        }

        // Google sign-in button
        signInButton.setOnClickListener(v -> signInWithGoogle());
    }

    /** Request all dangerous permissions immediately. */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestAllRuntimePermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        };

        List<String> toRequest = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(p);
            }
        }

        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toArray(new String[0]), REQ_RUNTIME_PERMISSIONS);
        }
    }

    /** Use AccountPicker to show Google account selection. */
    private void signInWithGoogle() {
        try {
            Intent intent = AccountManager.newChooseAccountIntent(
                    null, null,
                    new String[]{"com.google"},
                    false, null, null, null, null);
            startActivityForResult(intent, REQ_ACCOUNT_PICKER);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening account picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                // TODO: check saved configuration for this account
                Toast.makeText(this, "Signed in as: " + accountName, Toast.LENGTH_SHORT).show();

                boolean hasConfig = false; // Replace with your check logic

                if (hasConfig) {
                    startActivity(new Intent(this, MainActivity.class));
                } else {
                    startActivity(new Intent(this, ProfileSetupActivity.class));
                }
                finish();
            }
        }
    }
}
