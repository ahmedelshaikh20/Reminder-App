package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {
  private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
  lateinit var locationManager: LocationManager
  private lateinit var user_location: LatLng
  private lateinit var mMapView: MapView
  private lateinit var googleMap: GoogleMap
  private var marker : Marker? = null
  private lateinit var locationCallback: LocationCallback



  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    addMap(savedInstanceState)
    super.onViewCreated(view, savedInstanceState)
  }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
      locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
      fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
      binding.SaveButton.setOnClickListener {
        onLocationSelected()
      }
               locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                locationResult?.run {
                    val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)


                  googleMap.addMarker(
                    MarkerOptions().position(latLng).title("Title").snippet("Marker Description")
                  )
                  val cameraPosition = CameraPosition.Builder().target(latLng).zoom(12f).build()

                  googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                }
            }
        }

        return binding.root
    }

  private fun addMap(savedInstanceState: Bundle?) {

    // We add the map here
    mMapView = requireView().findViewById(R.id.map) as MapView
    mMapView.onCreate(savedInstanceState)
    mMapView.onResume()
mMapView.getMapAsync(this)

//    var mapFragment : SupportMapFragment?=null
//    mapFragment = SupportMapFragment().findFragmentById(R.id.map) as SupportMapFragment?
//    mapFragment?.getMapAsync(this)




  }
  private fun setMapStyle(map: GoogleMap?) {
    try {
      // Customize the styling of the base map using a JSON object defined
      // in a raw resource file.
      val success = map?.setMapStyle(
        MapStyleOptions.loadRawResourceStyle(
          requireActivity(),
          R.raw.map_style
        )
      )
      if (success==false) {
        Log.e("Maps Style", "Style parsing failed.")
      }
    }catch (e: Resources.NotFoundException) {
      Log.e("Maps Style", "Can't find style. Error: ", e)
    }
  }
  @SuppressLint("MissingPermission")
  private suspend fun getLastLocation(): LatLng {
    return suspendCoroutine {
      user_location = LatLng(-34.0, 151.0);
      fusedLocationClient.lastLocation
        .addOnSuccessListener { newlocation: Location? ->
          Log.d("LOCATION", newlocation.toString())
          if(newlocation == null){
            Toast.makeText(requireContext(), "Cannot get location.", Toast.LENGTH_SHORT).show()
          }

else {
          var location = user_location
            location = LatLng(newlocation.latitude, newlocation.longitude)

          if(newlocation==null){
            location = user_location
          }
          it.resume(location)}
        }
    }
  }
  private fun checkDeviceLocationSettings(resolve: Boolean = true) {

    val locationRequest = LocationRequest.create().apply {
      priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(requireActivity())
    val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

    locationSettingsResponseTask.addOnFailureListener { exception ->
      if (exception is ResolvableApiException && resolve) {
        startIntentSenderForResult(
                        exception.resolution.intentSender,
                        SaveReminderFragment.REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null)}
        else {
        Snackbar.make(
          requireView(),
          R.string.location_required_error, Snackbar.LENGTH_SHORT
        ).setDuration(3000)
          .setAction(android.R.string.ok) {
          checkDeviceLocationSettings()
        }.show()

      }

    }

    locationSettingsResponseTask.addOnCompleteListener {
      if ( it.isSuccessful ) {
        lifecycleScope.launch {
          zoomInUserLocation_helper()}
      }
    }
  }

  @SuppressLint("MissingPermission")
  private fun zoomInUserLocation() {
  if(!FineLoaction_Approved(requireActivity())){
    RequestFineLoactionPermission(this)
    Log.d("We got location" , "PermissionRequested")

  }
else {
checkDeviceLocationSettings(true)
    val locationRequest = LocationRequest.create().apply {
      priority = LocationRequest.PRIORITY_LOW_POWER
    }
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    //zoomInUserLocation()
    zoomInUserLocation_helper()
 }




  }
  fun zoomInUserLocation_helper(){
    lifecycleScope.launch {

      val location = getLastLocation()
      Log.d("We got location " , location.toString())
      user_location = location

      var userLat_Lng: LatLng = user_location

      googleMap.addMarker(
        MarkerOptions().position(userLat_Lng).title("Title").snippet("Marker Description")
      )
      // For zooming functionality
      val cameraPosition = CameraPosition.Builder().target(userLat_Lng).zoom(12f).build()
      googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
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
      (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE &&
        grantResults[0] ==
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
      zoomInUserLocation()

  }



  private fun onLocationSelected() {
    marker?.let {
  _viewModel.reminderSelectedLocationStr.value = it.title
  _viewModel.latitude.value = it.position.latitude
  _viewModel.longitude.value = it.position.longitude
}
    findNavController().popBackStack()

    }
  private fun setPoiClick(map: GoogleMap) {
    map.setOnPoiClickListener { poi ->
      map.clear()
       marker = map.addMarker(
        MarkerOptions()
          .position(poi.latLng)
          .title(poi.name)
      )

    }
    marker?.showInfoWindow()
  }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
      R.id.normal_map -> {
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        true
      }
      R.id.hybrid_map -> {
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        true
      }
      R.id.satellite_map -> {
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        true
      }
      R.id.terrain_map -> {
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        true
      }
      else -> super.onOptionsItemSelected(item)
    }

  override fun onMapReady(p0: GoogleMap?) {
    if (p0 != null) {
      googleMap = p0
    }
    googleMap.setOnMapClickListener {

      googleMap.clear()
      marker = googleMap.addMarker(
        MarkerOptions()
          .position(it)
          .title(getString(R.string.dropped_pin))

      )
      marker.let {
        it?.showInfoWindow()
      }

    }
setPoiClick(googleMap)
    setMapStyle(p0)
    zoomInUserLocation()


  }
  @SuppressLint("MissingPermission")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    Log.d("APP_RES" , resultCode.toString())
    if(resultCode == 0){
         checkDeviceLocationSettings(false)
      }else
    {
      val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
      }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
      //zoomInUserLocation()
    }
  }

// ...

    // ...





}
