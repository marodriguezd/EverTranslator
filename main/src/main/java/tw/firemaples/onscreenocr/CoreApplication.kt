package tw.firemaples.onscreenocr

import android.app.Application
import com.google.android.material.color.DynamicColors
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.log.UserInfoUtils
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.AdManager

class CoreApplication : Application() {
    companion object {
        lateinit var instance: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Apply Material 3 Dynamic Colors across the application where supported (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(this)

        FirebaseEvent.validateSignature()
        UserInfoUtils.setClientInfo()
        RemoteConfigManager.tryFetchNew()
        AdManager.init()
    }
}
