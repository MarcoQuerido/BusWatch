package pt.ulp.easybus2_testes

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import pt.ulp.easybus2_testes.databinding.ActivityMapPartilhaBinding
import java.io.IOException
import android.graphics.Color
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlin.math.roundToInt

class ActivityMapPartilha : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapPartilhaBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var currentLatLng: LatLng = LatLng(0.0,0.0)
    private var locationUpdateState = false
    private var cameraPosition: CameraPosition? = null
    private var distance: Double = 0.0
    private var distanceTime: Double = 0.0
    private var time: Double = 0.0
    private var velocity: Double = 0.0
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedLocation: LatLng
    private var loc1Lat: Double = 0.0
    private var loc1Lng: Double = 0.0
    private var loc2Lat: Double = 0.0
    private var loc2Lng: Double = 0.0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1  // Código para comparar com o CODE no manifest, para dar permissão de uso da localização
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        // Recuperar o estado anterior do mapa
        if (savedInstanceState != null) {
            lastLocation = savedInstanceState.getParcelable(KEY_LOCATION)!!
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        // Dar inflate do layout do mapa
        binding = ActivityMapPartilhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obter o SupportMapFragment e ser avisado de quando o mapa está pronto a utilizar.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation

                val id = intent?.extras?.get("ID") as String
                getDatabaseLocations(id)
                getDatabaseLocations2(id)

                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                val zoom = map.cameraPosition.zoom
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), zoom))
            }
        }
        createLocationRequest()

        val id = intent?.extras?.get("ID") as String
        val uname = intent?.extras?.get("Username") as String
        findViewById<FloatingActionButton>(R.id.fab_BackToMS_MapsPartilha).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("ID",id)
            intent.putExtra("Username",uname)
            startActivity(intent)
            finish()
        }

        findViewById<FloatingActionButton>(R.id.fab_instructions).setOnClickListener { view ->
            val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
                .create()
            val view = layoutInflater.inflate(R.layout.instrucoes,null)
            builder.setView(view)
            val button = view.findViewById<Button>(R.id.button_confirmOK)
            button.setOnClickListener {
                builder.dismiss()
            }
            builder.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastLocation)
        }
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.setOnMapLongClickListener(this)
        setUpMap()
    }

    override fun onMarkerClick(p0: Marker): Boolean = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_HYBRID     // tipo do mapa (vista satélite)

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Vai buscar a última localização conhecida
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                // Envia para a base de dados as locations
                //placeMarkerOnMap(currentLatLng)

               //map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
               //map.addMarker(MarkerOptions().position(currentLatLng).title("Marcador em $currentLatLng"))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        val titleStr = getAddress(location)
        markerOptions.title(titleStr)
        markerOptions.alpha(0.5f)
        map.addMarker(markerOptions)
        Log.e("MinhaLocation",location.toString())
    }

    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && addresses.isNotEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }
        return addressText
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!! /* Looper */)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@ActivityMapPartilha,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onMapLongClick(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.draggable(true)

        calculateDistance(location, loc1Lat,loc1Lng)
        calculateTime(loc1Lat,loc1Lng,loc2Lat,loc2Lng)

        time = (distance / velocity)
        time.roundToInt()

        showDistanceTime(markerOptions)

        map.addMarker(markerOptions)

        /*val polyline1 = map.addPolyline(
            PolylineOptions()
                .clickable(false)
                .add(location,LatLng(loc1Lat,loc1Lng)))
        // Store a data object with the polyline, used here to indicate an arbitrary type.
        polyline1.tag = "Distância"
        polyline1.color = (Color.RED)
        polyline1.isGeodesic = true*/
    }

    private fun calculateDistance(markerLoc: LatLng, loc1Lat: Double, loc1Lng: Double){
        val currentLocation = LatLng(loc1Lat,loc1Lng)
        distance = SphericalUtil.computeDistanceBetween(markerLoc, currentLocation)
    }

    private fun calculateTime(loc1Lat: Double,loc1Lng: Double,loc2Lat: Double,loc2Lng: Double){
        val currentLoc = LatLng(loc1Lat,loc1Lng)
        val lastLoc = LatLng(loc2Lat,loc2Lng)
        distanceTime = SphericalUtil.computeDistanceBetween(lastLoc, currentLoc)
        velocity = (distanceTime / 5) // 5 sec de intervalo entre updates da location
    }

    private fun showDistanceTime(markerOptions: MarkerOptions){
        val segundos = time
        val minutos = time / 60
        val horas = time / 3600
        val showSegundos = time % 60
        val showMinutos = time % 60

        if (distance < 1000.0){
            if (segundos < 60.0) {
                markerOptions.title("Distância: " + String.format("%.1f", distance) + " metros" + " / Tempo: " + segundos.roundToInt() + " seg")
            }
            if (segundos >= 60.0 || minutos < 60.0) {
                markerOptions.title("Distância: " + String.format("%.1f", distance) + " metros" + " / Tempo: " + String.format("%.0f", minutos) + " min e " + showSegundos.roundToInt() + " seg")
            }
            if (horas >= 1.0) {
                markerOptions.title("Distância: " + String.format("%.1f", distance) + " metros" + " / Tempo: " + horas.roundToInt() + " hora/s e " + showMinutos.roundToInt() + " min")
            }
        } else
            if (distance >= 1000.0){
                if (segundos < 60.0) {
                    markerOptions.title("Distância: " + String.format("%.1f", distance / 1000) + "km" + " / Tempo: " + segundos.roundToInt() + " seg")
                }
                if (segundos >= 60.0 || minutos < 60.0) {
                    markerOptions.title("Distância: " + String.format("%.1f", distance / 1000) + "km" + " / Tempo: " + String.format("%.0f", minutos) + " min e " + showSegundos.roundToInt() + " seg")
                }
                if (horas >= 1.0) {
                    markerOptions.title("Distância: " + String.format("%.1f", distance / 1000) + "km" + " / Tempo: " + horas.roundToInt() + " hora/s e " + showMinutos.roundToInt() + " min")
                }
            }
    }

    private fun getDatabaseLocations(id: String){
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Partilhas")
        reference.child(id).get().addOnSuccessListener {
            if (it.exists()){
                loc1Lat = it.child("loc1Lat").getValue<Double>()!!
                loc1Lng = it.child("loc1Lng").getValue<Double>()!!
                if (loc1Lat != null) {
                    if (loc1Lng != null) {
                        placeMarkerOnMapLocationShared(loc1Lat,loc1Lng)
                        sharedLocation = LatLng(loc1Lat,loc1Lng)
                        val zoom = map.cameraPosition.zoom
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc1Lat,loc1Lng), zoom))
                    }
                }
            } else {
                Log.e("Leitura", "Sem sucesso")
            }
        }.addOnFailureListener {
            Log.e("Leitura", "Erro na leitura dos dados")
        }
    }

    private fun placeMarkerOnMapLocationShared(val1: Double, val2: Double){
        val coordinates = LatLng(val1, val2)
        val markerOptions = MarkerOptions().position(coordinates)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)))
        val titleStr = getAddress(coordinates)
        markerOptions.title(titleStr)
        markerOptions.alpha(1.0f)
        val zoom = map.cameraPosition.zoom
        map.addMarker(markerOptions)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoom))
    }

    private fun getDatabaseLocations2(id: String){
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Partilhas")
        reference.child(id).get().addOnSuccessListener {
            if (it.exists()){
                loc2Lat = it.child("loc2Lat").getValue<Double>()!!
                loc2Lng = it.child("loc2Lng").getValue<Double>()!!
            } else {
                Log.e("Leitura", "Sem sucesso")
            }
        }.addOnFailureListener {
            Log.e("Leitura", "Erro na leitura dos dados")
        }
    }

    override fun onMarkerDrag(p0: Marker) {}

    override fun onMarkerDragEnd(p0: Marker) {
        val newLatLng = LatLng(p0.position.latitude,p0.position.longitude)
        val markerOptions = MarkerOptions().position(newLatLng)
        calculateDistance(newLatLng, loc1Lat,loc1Lng)
        calculateTime(loc1Lat,loc1Lng,loc2Lat,loc2Lng)

        time = (distance / velocity)
        time.roundToInt()

        placeMarkerOnMap(newLatLng)
        showDistanceTime(markerOptions)
    }

    override fun onMarkerDragStart(p0: Marker) {}
}