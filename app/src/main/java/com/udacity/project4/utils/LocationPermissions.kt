package com.udacity.project4.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat



const val Action_GeoFence_Event="Geofence";

 val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_REQUEST_CODE = 23
 val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE=24
 val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
  android.os.Build.VERSION_CODES.Q

 fun RequestLoactionPermission(activity: Activity) {
  if(FineLoaction_BackgroundLoaction_Approved(activity)) return
  var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
  val result_code  = when {
    runningQOrLater -> {
      permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
      REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_REQUEST_CODE
    }
    else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
  }

  ActivityCompat.requestPermissions(
    activity,
    permissionArray,
    result_code
  )


}

fun FineLoaction_BackgroundLoaction_Approved(activity: Activity):Boolean{
  val foregroundLocationApproved = (
    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity , Manifest.permission.ACCESS_FINE_LOCATION)
    )
  val BackGroundLocationApproved = (
    if(runningQOrLater)
      PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity , Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    else
      true
    )

  return foregroundLocationApproved && BackGroundLocationApproved;


}
