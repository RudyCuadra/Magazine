package com.example.magazine

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.magazine.Objects.Magazin
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import com.google.firebase.storage.OnProgressListener
import com.example.magazine.MainActivity as M

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    val NAME: String = "Nombre"
    val EDITION: String = "Edición"
    val MONTH: String = "Mes"
    val AGE: String = "Año"
    val TAG: String = "MagazinCool"
    //val db: DocumentReference = FirebaseFirestore.getInstance().document("Guía/
    val refMagazn = FirebaseDatabase.getInstance()
    lateinit var btnLoandingImage:Button
    lateinit var imageView: ImageView
    lateinit var btnLoadingPDF:Button
    var storage= FirebaseStorage.getInstance()
    var storageReference = storage.reference

    val PICK_IMAGE_REQUEST:Int = 71
    val FILE_PDF_REQUEST:Int = 86
    lateinit var path: Uri
    lateinit var downloadUri: Uri
    lateinit var uriPDFL:Uri
    lateinit var downloadUriPDF:Uri
    lateinit var progressDialog:ProgressDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSave: Button = findViewById(R.id.btnSaveMagazine)
        btnLoandingImage = findViewById(R.id.btnLCover)
        imageView = findViewById(R.id.imgView)
        btnLoadingPDF = findViewById(R.id.btnLPdf)

        btnSave.setOnClickListener {
            uploadPDF()
            //uploadImage()
            //saveMagazineBS()
        }

        btnLoandingImage.setOnClickListener {
            chooseImage()
        }

        btnVerLista.setOnClickListener {
            val intent = Intent(this,MagazineListActivity::class.java)
            startActivity(intent)
        }

        btnLoadingPDF.setOnClickListener {
            choosePDF()
        }
    }



    private fun choosePDF() {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED){
                selectPDF()
        } else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),9)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            selectPDF()
        }else{
            Toast.makeText(this,"please provide permission..", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectPDF() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        if(intent.resolveActivity(packageManager)!=null){
            startActivityForResult(Intent.createChooser(intent,"Select PDF"),FILE_PDF_REQUEST)
        }
    }

    private fun uploadPDF() {
        progressDialog = ProgressDialog(this)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setTitle("Uploading Magazine...")
        progressDialog.progress = 0

        progressDialog.show()

        val ref: StorageReference = storageReference.child("FILES PDF/*"+ System.currentTimeMillis()+"")
        val uploadTask = ref.putFile(uriPDFL)

        uploadTask.addOnFailureListener {

            Toast.makeText(this, "unsuccessful uploads", Toast.LENGTH_SHORT).show();
        }.addOnSuccessListener {
            Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
        }.addOnProgressListener { taskSnapshot ->
            val currentProgress = 100*taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount
            progressDialog.progress= currentProgress.toInt()

        }

        //Obteniedo URL

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUriPDF = task.result!!
                Log.i("URL",downloadUriPDF.toString())

                uploadImage()
            } else {
                // Handle failures
                // ...
            }
        }

    }

    fun uploadImage() {
        /*val progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Uploading...")
        progresDialog.show()*/

        val ref: StorageReference = storageReference.child("images/*"+ UUID.randomUUID().toString())
        val uploadTask = ref.putFile(path)

        uploadTask.addOnFailureListener {
            Toast.makeText(this, "unsuccessful uploads", Toast.LENGTH_SHORT).show();
        }.addOnSuccessListener {
            Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
        }

        //OBTENER URL DE ARCHIVO

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUri = task.result!!

                saveMagazineBS()
                progressDialog.dismiss()
                Log.i("URL",downloadUri.toString())
            } else {
                // Handle failures
                // ...
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/"
        if(intent.resolveActivity(packageManager)!=null){
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null)
        {
            path = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,path)
                imageView.setImageBitmap(bitmap)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }else if(requestCode == FILE_PDF_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null) {
                uriPDFL = data.data
                txtpdfseleccionado.text = data.data.lastPathSegment.toString()
            //data.data.lastPathSegment
        }else
            Toast.makeText(this,"Please select a file",Toast.LENGTH_SHORT).show()
    }

     fun saveMagazineBS() {
        val refMagaznD = refMagazn.getReference("magazines")
        val editTextName: EditText = findViewById(R.id.etNombre)
        val editTextEdition: EditText = findViewById(R.id.etEdition)
        val editTextMonth: EditText = findViewById(R.id.etMonth)
        val editTextAge: EditText = findViewById(R.id.etAge)

        val name = editTextName.text.toString()
        //.trim()  método elimina tanto los espacios en blanco finales como los anteriores.
        val edition = editTextEdition.text.toString().toInt()
        val month = editTextMonth.text.toString()
        val age = editTextAge.text.toString().toInt()
         val magazinId = refMagaznD.push().key

        if (!name.isEmpty() && edition > 0 && !month.isEmpty() && age > 0 ) {
            if (magazinId != null) {
                val magazine = Magazin(magazinId, name, edition, month, age,downloadUri.toString(),downloadUriPDF.toString())
                refMagaznD.child(magazinId).setValue(magazine).addOnCompleteListener {
                    Toast.makeText(applicationContext, "Magazine saved successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill up the fields :c", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
