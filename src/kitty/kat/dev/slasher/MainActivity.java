package kitty.kat.dev.slasher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Note: Wake lock and screen unlock will be triggered by a specific feature later
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#3A3A3A"));
        layout.setPadding(40, 40, 40, 40);
        
        TextView titleView = new TextView(this);
        titleView.setText("Slasher");
        titleView.setTextSize(32);
        titleView.setTextColor(Color.parseColor("#9D4EDD"));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 0, 0, 40);
        
        TextView statusView = new TextView(this);
        statusView.setTextSize(18);
        statusView.setTextColor(Color.WHITE);
        statusView.setGravity(Gravity.CENTER);
        
        // Load profile data
        SharedPreferences prefs = getSharedPreferences("SlasherPrefs", MODE_PRIVATE);
        String products = prefs.getString("products", "None");
        String stores = prefs.getString("stores", "None");
        boolean hasPayPal = prefs.getBoolean("hasPayPalDebit", false);
        String account = prefs.getString("googleAccount", "None");
        
        String profileInfo = String.format(
            "Google Account: %s\n\nProducts: %s\n\nStores: %s\n\nPayPal Debit: %s\n\n(Main functionality not implemented yet)",
            account, products, stores, hasPayPal ? "Yes" : "No"
        );
        
        statusView.setText(profileInfo);
        
        layout.addView(titleView);
        layout.addView(statusView);
        
        setContentView(layout);
    }
}
