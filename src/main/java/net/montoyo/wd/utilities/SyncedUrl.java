package net.montoyo.wd.utilities;

public class SyncedUrl {
    private static String url;
    
    public static String getUrl() {
        return url;
    }
    
    public static void setUrl(String newUrl) {
        url = newUrl;
    }
    
    public static void updateUrl(String newUrl) {
        url = newUrl;
    }
}