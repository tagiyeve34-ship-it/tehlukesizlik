package az.familytracker.child;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int REQ_LOCATION = 1001;
    private static final int REQ_NOTIFICATIONS = 1002;

    private static final String PREFS = "family_tracker_prefs";
    private static final String KEY_SETUP_DONE = "setup_done";

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isSetupDone()) {
            buildSimpleScreen();
        } else {
            buildSetupScreen();
        }
    }

    private boolean isSetupDone() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        return sp.getBoolean(KEY_SETUP_DONE, false);
    }

    private void setSetupDone(boolean value) {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        sp.edit().putBoolean(KEY_SETUP_DONE, value).apply();
    }

    private void buildSetupScreen() {

        int pad = dp(22);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Təhlükəsizlik");
        title.setTextSize(27);
        title.setTextColor(Color.rgb(24, 34, 53));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(28), 0, dp(10));

        TextView info = new TextView(this);
        info.setText(
                "Bu cihazın lokasiyası valideyn paneli ilə paylaşılır. " +
                "Aktivləşdirmə üçün aşağıdakı düyməyə toxunun və " +
                "Android-in tələb etdiyi icazələri verin."
        );
        info.setTextSize(15);
        info.setTextColor(Color.rgb(99, 113, 133));
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 0, 0, dp(22));

        status = new TextView(this);
        status.setTextSize(14);
        status.setTextColor(Color.rgb(45, 94, 68));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, dp(18));

        updateStatus();

        Button activate = makeButton("Lokasiya paylaşımını aktiv et");

        activate.setOnClickListener(v -> activate());

        Button settings = makeSecondaryButton("Tətbiq icazələrini aç");

        settings.setOnClickListener(v -> {

            Intent i = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            );

            i.setData(
                    android.net.Uri.parse(
                            "package:" + getPackageName()
                    )
            );

            startActivity(i);
        });

        root.addView(title);
        root.addView(info);
        root.addView(status);
        root.addView(activate);
        root.addView(space());
        root.addView(settings);

        setContentView(root);
    }

    private void buildSimpleScreen() {

        int pad = dp(22);

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);

        title.setText("Təhlükəsizlik");
        title.setTextSize(28);
        title.setTextColor(Color.rgb(24, 34, 53));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(14));

        TextView info = new TextView(this);

        info.setText(
                "Cihaz təhlükəsizliyi hazırdır.\n" +
                "Təhlükəsizlik və valideyn nəzarəti ayarları aktivdir. " +
                "Cihaz konfiqurasiyası tamamlanıb."
        );

        info.setTextSize(17);
        info.setTextColor(Color.rgb(88, 101, 122));
        info.setGravity(Gravity.CENTER);
        info.setLineSpacing(0f, 1.18f);

        root.addView(title);
        root.addView(info);

        setContentView(root);
    }

    private View space() {

        View v = new View(this);

        v.setLayoutParams(
                new LinearLayout.LayoutParams(
                        1,
                        dp(10)
                )
        );

        return v;
    }

    private Button makeButton(String text) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextSize(15);
        b.setAllCaps(false);

        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(29, 78, 216));

        b.setMinHeight(dp(52));

        b.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return b;
    }

    private Button makeSecondaryButton(String text) {

        Button b = makeButton(text);

        b.setTextColor(Color.rgb(29, 78, 216));
        b.setBackgroundColor(Color.rgb(239, 244, 255));

        return b;
    }

    private void activate() {

        if (
                checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

                &&

                checkSelfPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQ_LOCATION
            );

            return;
        }

        requestNotificationThenStart();
    }

    private void requestNotificationThenStart() {

        if (
                Build.VERSION.SDK_INT >= 33
                        &&
                checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    REQ_NOTIFICATIONS
            );

            return;
        }

        startTracker();
    }

    private void startTracker() {

        Intent service = new Intent(
                this,
                LocationService.class
        );

        if (Build.VERSION.SDK_INT >= 26) {

            startForegroundService(service);

        } else {

            startService(service);
        }

        setSetupDone(true);

        buildSimpleScreen();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQ_LOCATION) {

            if (
                    grantResults.length > 0
                            &&
                    (
                            checkSelfPermission(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            ||

                            checkSelfPermission(
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                    )
            ) {

                requestNotificationThenStart();

            } else if (status != null) {

                status.setText(
                        "Lokasiya icazəsi verilməyib."
                );
            }

        } else if (requestCode == REQ_NOTIFICATIONS) {

            startTracker();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!isSetupDone()) {
            updateStatus();
        }
    }

    private void updateStatus() {

        if (status == null) {
            return;
        }

        boolean loc =
                checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                ||

                checkSelfPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        status.setText(
                loc
                        ? "Lokasiya icazəsi verilib."
                        : "Lokasiya icazəsi gözlənilir."
        );
    }

    private int dp(int value) {

        return Math.round(
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
