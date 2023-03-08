package net.montoyo.wd.utilities;

import net.montoyo.wd.net.Messages;

public class SyncedUrl {
    private static String url;
    
    public static String getUrl() {
        return url;
    }
    
    public static void setUrl(String newUrl) {
        url = newUrl;
        Messages.sendUrlUpdate(newUrl);
    }
    
    public static void updateUrl(String newUrl) {
        url = newUrl;
    }
}