package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.utils.Action_GeoFence_Event

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
Log.d("GeofenceBroadcastReceiv" , "We are here in on Recieve")
      if(intent.action == Action_GeoFence_Event)
        GeofenceTransitionsJobIntentService.enqueueWork(context , intent)

    }


  fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
      GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
        R.string.geofence_not_available
      )
      GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
        R.string.geofence_too_many_geofences
      )
      GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
        R.string.geofence_too_many_pending_intents
      )
      else -> resources.getString(R.string.geofence_unknown_error)
    }
  }
}
