package com.example.safetyalarmapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EmergencyContacts : AppCompatActivity() {

    private lateinit var edtName1: EditText
    private lateinit var edtEmail1: EditText
    private lateinit var edtPhone1: EditText
    private lateinit var edtName2: EditText
    private lateinit var edtEmail2: EditText
    private lateinit var edtPhone2: EditText
    private lateinit var edtName3: EditText
    private lateinit var edtEmail3: EditText
    private lateinit var edtPhone3: EditText
    private lateinit var btnSubmit: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emergency_contacts)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        edtName1 = findViewById(R.id.edt_name1)
        edtEmail1 = findViewById(R.id.edt_email1)
        edtPhone1 = findViewById(R.id.edt_phone1)
        edtName2 = findViewById(R.id.edt_name2)
        edtEmail2 = findViewById(R.id.edt_email2)
        edtPhone2 = findViewById(R.id.edt_phone2)
        edtName3 = findViewById(R.id.edt_name3)
        edtEmail3 = findViewById(R.id.edt_email3)
        edtPhone3 = findViewById(R.id.edt_phone3)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val eName1 = edtName1.text.toString()
            val ePhone1 = edtPhone1.text.toString()
            val eEmail1 = edtEmail1.text.toString()
            val eName2 = edtName2.text.toString()
            val ePhone2 = edtPhone2.text.toString()
            val eEmail2 = edtEmail2.text.toString()
            val eName3 = edtName3.text.toString()
            val ePhone3 = edtPhone3.text.toString()
            val eEmail3 = edtEmail3.text.toString()

            eContact(eName1, ePhone1, eEmail1, eName2, ePhone2, eEmail2, eName3, ePhone3, eEmail3);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun eContact(
        eName1: String, ePhone1: String, eEmail1: String,
        eName2: String, ePhone2: String, eEmail2: String,
        eName3: String, ePhone3: String, eEmail3: String
    ) {
        val currentUser = mAuth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            val userRef = mDbRef.child("user").child(uid)
            val userData = HashMap<String, Any>()
            userData["ename1"] = eName1
            userData["ephone1"] = ePhone1
            userData["eemail1"] = eEmail1
            userData["ename2"] = eName2
            userData["ephone2"] = ePhone2
            userData["eemail2"] = eEmail2
            userData["ename3"] = eName3
            userData["ephone3"] = ePhone3
            userData["eemail3"] = eEmail3

            userRef.updateChildren(userData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@EmergencyContacts,
                            "Emergency contacts added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@EmergencyContacts, MainActivity::class.java)
                        finish()
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@EmergencyContacts,
                            "Failed to add emergency contacts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

}