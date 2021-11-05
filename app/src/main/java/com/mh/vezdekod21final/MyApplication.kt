package com.mh.vezdekod21final

import android.app.Application
import android.content.Context
import com.give.vezdekodmh.utils.L

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
       /* val fingerprints =
            getCertificateFingerprint(this, this.packageName)
        L.d("fingerprint = " + (fingerprints?.get(0) ?: ""))*/

    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        //MultiDex.install(this)
    }

}