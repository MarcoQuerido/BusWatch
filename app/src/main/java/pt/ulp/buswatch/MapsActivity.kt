package pt.ulp.easybus2_testes

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
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
import pt.ulp.easybus2_testes.databinding.ActivityMapsBinding
import java.io.IOException
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
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
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obter o SupportMapFragment e ser avisado de quando o mapa está pronto a utilizar.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                calculateTime(lastLocation,currentLatLng)
                // Se a distância entre locations for mínima, não marca o ponto no mapa
                if (distanceTime<0.2)
                {
                    Log.i("Distância", "$distanceTime")
                } else {
                    val currentLatLng2 = currentLatLng
                    val zoom = map.cameraPosition.zoom
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), zoom))
                    updateDatabaseLocation(lastLocation,currentLatLng2)
                }
            }
        }

        createLocationRequest()

        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String

        findViewById<FloatingActionButton>(R.id.fab_BackToMS).setOnClickListener {
            deletePartilha(username,id)
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("Username",username)
            intent.putExtra("ID",id)
            startActivity(intent)
            finish()
        }

        findViewById<ExtendedFloatingActionButton>(R.id.fabAlerts).setOnClickListener {
            showAlerts()
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

    override fun onDestroy() {
        super.onDestroy()
        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String
        deletePartilha(username,id)
    }

    override fun onStop() {
        super.onStop()
        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String
        deletePartilha(username,id)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap    //variável do tipo googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
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
        map.mapType = GoogleMap.MAP_TYPE_HYBRID

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Vai buscar a última localização conhecida
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                // Envia para a base de dados as locations
                //updateDatabaseLocation(lastLocation,currentLatLng)
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        val titleStr = getAddress(location)
        markerOptions.title(titleStr)
        markerOptions.alpha(0.5f)
        map.addMarker(markerOptions)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
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
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun calculateDistance(markerLoc: LatLng, lastLocation: Location){
        val currentLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
        distance = SphericalUtil.computeDistanceBetween(markerLoc, currentLocation)
    }

    private fun calculateTime(lastLocation: Location, currentLocation: LatLng){
        val lastLoc = LatLng(lastLocation.latitude,lastLocation.longitude)
        distanceTime = SphericalUtil.computeDistanceBetween(lastLoc, currentLocation)
        velocity = (distanceTime / 5) // 5 sec de intervalo entre updates da location
    }

    private fun updateDatabaseLocation(location: Location, currentLocation: LatLng){
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Partilhas")
        val id = intent?.extras?.get("ID") as String
        Log.e("id",id)

        reference.child(id).get().addOnSuccessListener {
            if (it.exists()){
                val loc2Lat = it.child("loc1Lat").getValue<Double>()!!
                val loc2Lng = it.child("loc1Lng").getValue<Double>()!!
                reference.child(id).child("loc2Lat").setValue(loc2Lat)
                reference.child(id).child("loc2Lng").setValue(loc2Lng)
            } else {
                Log.e("Leitura", "Sem sucesso")
            }
        }.addOnFailureListener {
            Log.e("Leitura", "Erro na leitura dos dados")
        }

        val loc1Lat = location.latitude
        val loc1Lng = location.longitude
        reference.child(id).child("loc1Lat").setValue(loc1Lat)
        reference.child(id).child("loc1Lng").setValue(loc1Lng)

    }

    private fun showAlerts() {
        var alertEscolhidoIndex = 0
        val alerts = arrayOf("Avaria de camioneta", "Acidente na estrada", "Acidente com a camioneta",
            "Trânsito na estrada", "Lotação excedida na camioneta", "Nenhum")

        var alertEscolhido = alerts[alertEscolhidoIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Escolha um alert a enviar")
            .setSingleChoiceItems(alerts, alertEscolhidoIndex) { dialog_, which ->
                alertEscolhidoIndex = which
                alertEscolhido = alerts[which]
            }
            .setPositiveButton("Enviar") { dialog, which ->
                Toast.makeText(this, "$alertEscolhido Selecionado", Toast.LENGTH_SHORT)
                    .show()
                sendAlert(alertEscolhido)
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendAlert(alert: String){
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Partilhas")
        val id = intent?.extras?.get("ID") as String
        reference.child(id).child("alert").setValue(alert)
    }

    private fun deletePartilha(username: String, id: String){
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Partilhas")
        reference.child(id).removeValue().addOnSuccessListener {
            // Write was successful!
            // ...
            Log.e("Base de Dados", "Passou para a base de dados")
        }
            .addOnFailureListener {
                // Write failed
                // ...
                Log.e("Base de Dados", "Não passou para a base de dados")
            }
    }
}