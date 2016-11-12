package com.bazz_techtronics.codefind.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CodeFindSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static CodeFindSyncAdapter sCodeFindSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("CodeFindSyncService", "onCreate - CodeFindSyncService");
        synchronized (sSyncAdapterLock) {
            if (sCodeFindSyncAdapter == null) {
                sCodeFindSyncAdapter = new CodeFindSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sCodeFindSyncAdapter.getSyncAdapterBinder();
    }
}