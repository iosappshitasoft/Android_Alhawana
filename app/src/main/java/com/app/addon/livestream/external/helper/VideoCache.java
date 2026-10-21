package com.app.addon.livestream.external.helper;

import android.content.Context;



import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class VideoCache {
    private static SimpleCache sDownloadCache;

    public static SimpleCache getInstance(Context context) {
        if (sDownloadCache == null) {
/*            DatabaseProvider databaseProvider = new ExoDatabaseProvider(context);
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor((100 * 1024 * 1024));
            sDownloadCache = new SimpleCache(new File(StorageManager.getInstance(context).getExtCachesDir(), "exoCache"),
                    evictor, databaseProvider);*/
        }
        return sDownloadCache;
    }
}