package com.example.safetyalarmapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*
import java.io.IOException
import java.util.Locale
import java.util.Objects
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var location_manager: LocationManager? = null
    private var geocoder: Geocoder? = null
    private var addresses: List<Address>? = null
    private var getloc: Button? = null
    private var userName: TextView? = null
    private var userEmail: TextView? = null
    private var userPhone: TextView? = null
    private var userName1: TextView? = null
    private var userName2: TextView? = null
    private var userName3: TextView? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    // Location variables
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvlatitude: TextView
    private lateinit var tvlongitude: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        getloc = findViewById(R.id.sendAlert)
        userName = findViewById(R.id.userName)
        userPhone = findViewById(R.id.userPhone)
        userEmail = findViewById(R.id.userEmail)
        userName1 = findViewById(R.id.userName1)
        userName2 = findViewById(R.id.userName2)
        userName3 = findViewById(R.id.userName3)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        fetchUserData()

        // Location initialization
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        tvlatitude = findViewById(R.id.latitudes)
        tvlongitude = findViewById(R.id.longitudes)


        // Request location updates when getloc button is clicked
        getloc?.setOnClickListener {
            getCurrentLocation()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        val currentUser = mAuth.currentUser
        currentUser?.let { user ->
            mDbRef.child("user").child(user.uid).get().addOnSuccessListener { dataSnapshot ->
                val userData = dataSnapshot.getValue(User::class.java)
                // Now you have userData containing all user information including emergency contacts
            }.addOnFailureListener { exception ->
                Log.e("MainActivity", "Error getting user data", exception)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.logout){
            //write the logic for  logout
            mAuth.signOut()
            val intent = Intent(this@MainActivity, LogIn::class.java)
            finish()
            startActivity(intent)
            return true
        }
        return true
    }

    private fun fetchUserData() {
        // Step 1: Retrieve the User's UID
        val currentUserUid = mAuth.currentUser?.uid ?: ""

        // Step 2: Fetch Data from Database
        val database = FirebaseDatabase.getInstance()
        val mDbRef = database.getReference("user").child(currentUserUid)

        mDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Step 3: Update UI with User Information
                val user = dataSnapshot.getValue(User::class.java)
                Log.d("MainActivity", "User data: $user")
                updateUserInterface(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.e("MainActivity", "Error fetching user data", databaseError.toException())
            }
        })
    }

    private fun updateUserInterface(user: User?) {
        // Update UI with user information
        userName?.text = user?.name
        userEmail?.text = user?.email
        userPhone?.text = user?.phone

        // Update UI with emergency contact information
        userName1?.text = "${user?.eName1}: ${user?.ePhone1}, ${user?.eEmail1}"
        userName2?.text = "${user?.eName2}: ${user?.ePhone2}, ${user?.eEmail2}"
        userName3?.text = "${user?.eName3}: ${user?.ePhone3}, ${user?.eEmail3}"
    }




    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            // Fetching x,y,z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            // Display a Toast message if
            // acceleration value is over 12
            if (acceleration > 12) {
                Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    private fun getCurrentLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                //final latitude and longitude
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Null Location", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Location found successfully", Toast.LENGTH_SHORT).show()
                        tvlatitude.text = "${location.latitude}"
                        tvlongitude.text = "${location.longitude}"

                        // Send alert SMS to emergency contacts
                        sendAlertSMS(location)
                    }
                }
            } else {
                //settings activity open here
                Toast.makeText(this, "Turn on Location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            //request permission here
            requestPermission()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private val PERMISSION_REQUEST_SEND_SMS = 123
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION  || requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
                requestSmsPermission()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST_SEND_SMS
            )
        } else {
            // Permission has already been granted
            // You can proceed with sending SMS messages here
        }
    }

    private fun sendAlertSMS(location: Location) {
        val currentUserUid = mAuth.currentUser?.uid ?: ""
        val dbRef = mDbRef.child("user").child(currentUserUid)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                val message = "Emergency! I need your help. My current location is: " +
                        "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                val smsManager = this@MainActivity.getSystemService(SmsManager::class.java)
                user?.let {
                    try {
                        smsManager.sendTextMessage(it.ePhone1, null, message, null, null)
                        smsManager.sendTextMessage(it.ePhone2, null, message, null, null)
                        smsManager.sendTextMessage(it.ePhone3, null, message, null, null)
                        Toast.makeText(applicationContext, "Alert SMS sent successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error sending SMS: ${e.message}", e)
                        Toast.makeText(applicationContext, "Failed to send alert SMS", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity", "Error fetching user data", databaseError.toException())
            }
        })
    }
}