package az.familytracker.child;

import android.location.Location;
import android.os.BatteryManager;
import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Http {
    private Http() {}

    public static void sendLocationAsync(Context context, Location loc) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject body = new JSONObject();
                body.put("device_id", Config.DEVICE_ID);
                body.put("device_name", Config.DEVICE_NAME);
                body.put("lat", loc.getLatitude());
                body.put("lng", loc.getLongitude());
                body.put("accuracy", loc.hasAccuracy() ? loc.getAccuracy() : JSONObject.NULL);
                body.put("altitude", loc.hasAltitude() ? loc.getAltitude() : JSONObject.NULL);
                body.put("speed", loc.hasSpeed() ? loc.getSpeed() : JSONObject.NULL);
                body.put("bearing", loc.hasBearing() ? loc.getBearing() : JSONObject.NULL);
                body.put("provider", loc.getProvider());

                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                if (bm != null) {
                    int pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    if (pct >= 0 && pct <= 100) body.put("battery", pct);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
                sdf.setTimeZone(TimeZone.getDefault());
                body.put("device_time", sdf.format(new Date()));

                URL url = new URL(Config.LOCATION_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("X-Device-Token", Config.API_TOKEN);
                conn.setDoOutput(true);

                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                }

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(),
                            StandardCharsets.UTF_8))) {
                        while (br.readLine() != null) { /* consume */ }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {
                // Növbəti location callback zamanı yenidən cəhd ediləcək.
            } finally {
                if (conn != null) conn.disconnect();
            }
        }, "location-upload").start();
    }
}
