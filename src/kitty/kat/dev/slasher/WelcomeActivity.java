package kitty.kat.dev.slasher;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

        // Request all permissions on first app open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAllRuntimePermissions();
            requestOverlayPermission();
        }

        signInButton.setOnClickListener(v -> signInWithGoogle());
    }

    /** Request all runtime permissions immediately on startup. */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestAllRuntimePermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Manifest.permission.FOREGROUND_SERVICE
        };

        List<String> toRequest = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(p);
            }
        }

        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    toRequest.toArray(new String[0]),
                    REQ_RUNTIME_PERMISSIONS
            );
        }
    }

    /** Request SYSTEM_ALERT_WINDOW separately. */
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_SYSTEM_ALERT);
        }
    }

    /** Launch the Google account picker instead of GET_ACCOUNTS. */
    private void signInWithGoogle() {
        try {
            Intent intent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    new String[]{"com.google"},
                    false,
                    null,
                    null,
                    null,
                    null
            );
            startActivityForResult(intent, REQ_ACCOUNT_PICKER);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Google Account picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Handle results from overlay and account picker. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SYSTEM_ALERT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQ_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            if (accountName != null) {
                Toast.makeText(this, "Signed in as " + accountName, Toast.LENGTH_SHORT).show();

                boolean hasConfig = checkAccountConfiguration(accountName);

                if (hasConfig) {
                    startActivity(new Intent(this, MainActivity.class));
                } else {
                    startActivity(new Intent(this, ProfileSetupActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "No account selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Dummy configuration check placeholder — replace with real data lookup. */
    private boolean checkAccountConfiguration(String accountName) {
        // TODO: replace with actual check (e.g., SharedPreferences, remote DB)
        return false;
    }

    /** Handle permission request results. */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_RUNTIME_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission denied: " + permissions[i], Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
