package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
  //Get the view model this time as a single to be shared with the another fragment
  override val _viewModel: SaveReminderViewModel by inject()
  private lateinit var binding: FragmentSaveReminderBinding
  lateinit var reminderDataItem: ReminderDataItem
  private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
    android.os.Build.VERSION_CODES.Q
  val geofencePendingIntent: PendingIntent by lazy {
    val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
    intent.action = Action_GeoFence_Event
    PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }
  lateinit var geofencingClient: GeofencingClient;

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding =
      DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
    geofencingClient = LocationServices.getGeofencingClient(this.requireActivity())

    setDisplayHomeAsUpEnabled(true)

    binding.viewModel = _viewModel
    //////////////////////////////////////
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = this

    binding.selectLocation.setOnClickListener {
      //            Navigate to another fragment to get the user location
      _viewModel.navigationCommand.value =
        NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())

    }

    binding.saveReminder.setOnClickListener {
      val title = _viewModel.reminderTitle.value
      val description = _viewModel.reminderDescription.value
      val location = _viewModel.reminderSelectedLocationStr.value
      val latitude = _viewModel.latitude.value
      val longitude = _viewModel.longitude.value
      Log.d("SaveReminderFragment" , location.toString()  )

      reminderDataItem =
        ReminderDataItem(title, description, location, latitude, longitude)

      if (_viewModel.validateEnteredData(reminderDataItem)) {
        if (BackgroundLoaction_Approved(requireActivity())){


          checkDeviceLocationSettingsAndStartGeofence(true)}
        else
          RequestBackgroundLoactionPermission(requireActivity())      }

    }
  }



  private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {

    val locationRequest = LocationRequest.create().apply {
      priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(requireActivity())
    val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

    locationSettingsResponseTask.addOnFailureListener { exception ->
      if (exception is ResolvableApiException && resolve) {
        exception.startResolutionForResult(this.requireActivity(),
          REQUEST_TURN_DEVICE_LOCATION_ON)


      }
     else {
      Snackbar.make(
        requireView(),
        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
      ).setAction(android.R.string.ok) {
        checkDeviceLocationSettingsAndStartGeofence()
      }.show()
    }
  }

    locationSettingsResponseTask.addOnCompleteListener {
      if ( it.isSuccessful ) {
        startGeoFence()
      }
    }
  }

  @SuppressLint("MissingPermission")
  private fun startGeoFence() {
    val geofence = Geofence.Builder()
      .setRequestId(reminderDataItem.id)
      .setCircularRegion(
        reminderDataItem.latitude!!,
        reminderDataItem.longitude!!,
        GEOFENCE_RADIUS_IN_METERS)
      .setExpirationDuration(Geofence.NEVER_EXPIRE)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
      .build()

    val geofencingRequest = GeofencingRequest.Builder()
      .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
      .addGeofence(geofence)
      .build()
    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
      addOnSuccessListener {
        _viewModel.saveReminder(reminderDataItem)
      }
      addOnFailureListener {
        _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
      }
    }
  }

  override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    Log.d("onRequestPermission", "onRequestPermissionResult")

    if (
      grantResults.isEmpty() ||
      grantResults[0] == PackageManager.PERMISSION_DENIED ||
      (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_REQUEST_CODE &&
        grantResults[1] ==
        PackageManager.PERMISSION_DENIED))
    {
      Snackbar.make(
        binding.root,
        R.string.permission_denied_explanation,
        Snackbar.LENGTH_INDEFINITE
      )
        .setAction(R.string.settings) {
          startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          })
        }.show()
    }else
      checkDeviceLocationSettingsAndStartGeofence(true)

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if(resultCode == REQUEST_TURN_DEVICE_LOCATION_ON)
      checkDeviceLocationSettingsAndStartGeofence(true)
  }

  companion object{
    private const val GEOFENCE_RADIUS_IN_METERS = 100f
    private const val REQUEST_TURN_DEVICE_LOCATION_ON =35

  }
}

