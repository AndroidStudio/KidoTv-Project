package mp.agencja.apsik.kidotv.main;


public class RetainCache {
    private static RetainCache sSingleton;
    public BitmapCache mRetainedCache;

    public static RetainCache getOrCreateRetainableCache() {
        if (sSingleton == null) {
            sSingleton = new RetainCache();
        }
        return sSingleton;
    }

}