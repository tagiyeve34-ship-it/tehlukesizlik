package az.familytracker.child;

public final class Config {
    private Config() {}

    public static final String DEVICE_ID = "fatime";
    public static final String DEVICE_NAME = "Emilia";
    public static final String LOCATION_URL = "https://hesabat.site/g/api/location.php";
    public static final String API_TOKEN = "577cdb9cd3a8ca8008d81ee58bebb40434127905b89fc128d9203bd5bc204c30";

    // 5 dəqiqə və ya 100 metr dəyişiklikdən sonra yeniləmə.
    public static final long MIN_TIME_MS = 5L * 60L * 1000L;
    public static final float MIN_DISTANCE_M = 100f;
}
