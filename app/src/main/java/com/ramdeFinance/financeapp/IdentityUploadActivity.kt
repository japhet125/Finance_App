package com.ramdefinance.financeapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage

class IdentityUploadActivity : AppCompatActivity() {

    private var userLanguage = "en"
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_upload)
        val previewImage =
            findViewById<ImageView>(R.id.imgIdPreview)

        val backButton = findViewById<Button>(R.id.btnBack)
        val titleText = findViewById<TextView>(R.id.txtIdentityTitle)
        val idTypeSpinner = findViewById<Spinner>(R.id.spIdType)
        val idNumberInput = findViewById<EditText>(R.id.etIdNumber)
        val idExpirationInput = findViewById<EditText>(R.id.etIdExpiration)
        val idCountrySpinner = findViewById<Spinner>(R.id.spIdCountry)
        val uploadButton = findViewById<Button>(R.id.btnUploadIdPicture)
        val submitButton = findViewById<Button>(R.id.btnSubmitIdentity)

        backButton.setOnClickListener {
            finish()
        }

        val idTypeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.id_types,
            android.R.layout.simple_spinner_item
        )
        uploadButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        idTypeAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        idTypeSpinner.adapter = idTypeAdapter

        val countryAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.supported_countries,
            android.R.layout.simple_spinner_item
        )

        countryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        idCountrySpinner.adapter = countryAdapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    userLanguage =
                        document.getString("language") ?: "en"

                    if (userLanguage == "fr") {
                        backButton.text = "Retour"
                        titleText.text = "Vérification d'identité"
                        idNumberInput.hint = "Numéro d'identité"
                        idExpirationInput.hint = "Date d'expiration MM/JJ/AAAA"
                        uploadButton.text = "Choisir une photo de la pièce d'identité"
                        submitButton.text = "Soumettre la vérification"
                    } else {
                        backButton.text = "Back"
                        titleText.text = "Identity Verification"
                        idNumberInput.hint = "ID Number"
                        idExpirationInput.hint = "Expiration Date MM/DD/YYYY"
                        uploadButton.text = "Choose ID Picture"
                        submitButton.text = "Submit Verification"
                    }
                }
        }

        submitButton.setOnClickListener {

            val idTypeText = idTypeSpinner.selectedItem.toString()
            val idNumberText = idNumberInput.text.toString().trim()
            val idExpirationText = idExpirationInput.text.toString().trim()
            val idCountryText = idCountrySpinner.selectedItem.toString()

            if (idNumberText.isBlank() || idExpirationText.isBlank()) {
                Toast.makeText(
                    this,
                    if (userLanguage == "fr")
                        "Veuillez remplir tous les champs"
                    else
                        "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(
                    this,
                    if (userLanguage == "fr")
                        "Utilisateur introuvable"
                    else
                        "User not found",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }
            val imageUri = selectedImageUri

            if (imageUri == null) {
                Toast.makeText(
                    this,
                    if (userLanguage == "fr")
                        "Choisir une photo de votre pièce d'identité"
                    else
                        "Choose ID Picture",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val storageRef = FirebaseStorage.getInstance()
                .reference
                .child("identity_documents/$userId/id_${System.currentTimeMillis()}.jpg")

            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl
                        .addOnSuccessListener { downloadUrl ->

                            val updates = mapOf(
                                "idType" to idTypeText,
                                "idNumber" to idNumberText,
                                "idExpirationDate" to idExpirationText,
                                "idCountry" to idCountryText,
                                "identityDocumentUrl" to downloadUrl.toString(),
                                "identityStatus" to "pending",
                                "identityVerified" to false,
                                "identitySubmittedAt" to System.currentTimeMillis()
                            )

                            db.collection("users")
                                .document(userId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        if (userLanguage == "fr")
                                            "Vérification soumise"
                                        else
                                            "Verification submitted",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    finish()
                                }
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }


        }
    }
    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {
                selectedImageUri = uri

                val preview =
                    findViewById<ImageView>(R.id.imgIdPreview)

                preview.setImageURI(uri)
            }
        }
}