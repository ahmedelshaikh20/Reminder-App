package com.udacity.project4.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions


const val Action_GeoFence_Event="Geofence";

 val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_REQUEST_CODE = 23
 val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE=24
val REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE=25

 val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
  android.os.Build.VERSION_CODES.Q

 fun RequestLoactionPermission(activity: Activity) {
  if(Fine_BackgroundLoaction_Approved(activity)) return
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
fun RequestBackgroundLoactionPermission(fragment: Fragment) {
  if(BackgroundLoaction_Approved(fragment.requireActivity())) return
  if(runningQOrLater) {
    val permissionArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    var result_code = REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE

    fragment.requestPermissions(
      permissionArray,
      result_code
    )

  }
}
fun RequestFineLoactionPermission(fragment: Fragment) {
  if(FineLoaction_Approved(fragment.requireActivity())) return
  var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

  var result_code = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

  fragment.requestPermissions(
    permissionArray,
    result_code
  )


}


fun FineLoaction_Approved(activity: Activity):Boolean{
  val foregroundLocationApproved = (
    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity , Manifest.permission.ACCESS_FINE_LOCATION)
    )



  return foregroundLocationApproved


}
fun BackgroundLoaction_Approved(activity: Activity):Boolean{
  val BackGroundLocationApproved = (
    if(runningQOrLater)
      PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity , Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    else
      true
    )


  return BackGroundLocationApproved


}
fun Fine_BackgroundLoaction_Approved(activity: Activity):Boolean{
  val BackGroundLocationApproved = (
    if(runningQOrLater)
      PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity , Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    else
      true
    )
  val foregroundLocationApproved = (
    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity , Manifest.permission.ACCESS_FINE_LOCATION)
    )


  return BackGroundLocationApproved && foregroundLocationApproved


}
