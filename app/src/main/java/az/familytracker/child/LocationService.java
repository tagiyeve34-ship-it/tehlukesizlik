package az.familytracker.child;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

public class LocationService extends Service implements LocationListener {

    public static final String CHANNEL_ID = "family_safety_location";
    public static final int NOTIFICATION_ID = 4101;

    private LocationManager lm;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        Config.MIN_TIME_MS,
                        Config.MIN_DISTANCE_M,
                        this
                );
            }
        } catch (Exception ignored) {}

        try {
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        Config.MIN_TIME_MS,
                        Config.MIN_DISTANCE_M,
                        this
                );
            }
        } catch (Exception ignored) {}

        sendBestLastKnown();
        return START_STICKY;
    }

    private void sendBestLastKnown() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        try {
            Location best = null;
            for (String provider : lm.getProviders(true)) {
                Location l = lm.getLastKnownLocation(provider);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            }
            if (best != null) Http.sendLocationAsync(this, best);
        } catch (Exception ignored) {}
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) Http.sendLocationAsync(this, location);
    }

    @Override
    public void onDestroy() {
        try {
            if (lm != null) lm.removeUpdates(this);
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Təhlükəsizlik xidməti",
                    NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("Lokasiya paylaşımı aktiv olduqda göstərilir.");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return b.setSmallIcon(az.familytracker.child.R.drawable.ic_location)
                .setContentTitle("Təhlükəsizlik aktivdir")
                .setContentText("Lokasiya valideyn paneli ilə paylaşılır.")
                .setOngoing(true)
                .setContentIntent(pi)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
