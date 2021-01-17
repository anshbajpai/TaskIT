package com.example.taskit;



import android.app.Application
import android.util.Log
import com.onesignal.OSSubscriptionState
import com.onesignal.OneSignal


const val ONESIGNAL_APP_ID = "2bfdec01-56ed-45d6-bb5c-da5412b25f45"

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        OneSignal.setNotificationOpenedHandler { result ->
            val actionId = result.action.actionId
            val type: String = result.action.type.name
            val title = result.notification.title
            Log.d("Notify","Title : ${title}")
        }







    }




}