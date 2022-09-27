package com.tugcearas.artbooksample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.tugcearas.artbooksample.databinding.ActivityArtBookDetailsBinding
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class ArtBookDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtBookDetailsBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var db : SQLiteDatabase
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBookDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
        registerLauncher()

        val intent= intent
        changeScreenStatus()

    }

    private fun changeScreenStatus(){

        val info = intent.getStringExtra("info")
        if (info.equals("newArt")){
            binding.artNameTextView.setText("")
            binding.artistNameTextView.setText("")
            binding.yearTextView.setText("")
            binding.saveButton.visibility = View.VISIBLE
            binding.selectImageView.setImageResource(R.drawable.selectimage)

            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.selectimage)
            binding.selectImageView.setImageBitmap(selectedImageBackground)
        }
        else{
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)
            val cursor = db.rawQuery("SELECT * FROM arts WHERE id=?", arrayOf(selectedId.toString()))
            val artNameIndex = cursor.getColumnIndex("artName")
            val artistNameIndex = cursor.getColumnIndex("artistName")
            val yearIndex = cursor.getColumnIndex("year")
            val imageIndex = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.artNameTextView.setText(cursor.getString(artNameIndex))
                binding.artistNameTextView.setText(cursor.getString(artistNameIndex))
                binding.yearTextView.setText(cursor.getString(yearIndex))

                val byteArray = cursor.getBlob(imageIndex)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.selectImageView.setImageBitmap(bitmap)

            }
            cursor.close()
        }
    }

    fun selectedImageOnClick(view: View) {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission",View.OnClickListener {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }).show()
            }
            else{
                // request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        else{
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    fun saveOnClick(view: View) {

        val artName = binding.artNameTextView.text.toString()
        val artistName = binding.artistNameTextView.text.toString()
        val year = binding.yearTextView.text.toString()

        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray() //image converted to ones and zeros

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artName VARCHAR,artistName VARCHAR, year VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO arts (artName,artistName,year,image) VALUES (?,?,?,?)"
                val statement = db.compileStatement(sqlString)
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)
                statement.execute()
            }
            catch (e:Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@ArtBookDetailsActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }

    }

    private fun makeSmallerBitmap(image:Bitmap, maximumSize:Int):Bitmap{

        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1){
            //landscape image
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }
        else{
            // portrait image
            height = maximumSize
            val scaledWidth = height / bitmapRatio
            width = scaledWidth.toInt()

        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    // launcher initialize process
    private fun registerLauncher(){

        // go to gallery and select image
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    val imageUri = intentFromResult.data
                    //binding.selectImageView.setImageURI(imageUri)
                    if (imageUri != null){
                        try {
                            if (Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@ArtBookDetailsActivity.contentResolver,imageUri)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.selectImageView.setImageBitmap(selectedBitmap)
                            }
                            else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
                                binding.selectImageView.setImageBitmap(selectedBitmap)
                            }
                        }
                        catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ resultPermission ->
            if(resultPermission){
                //  permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
            else{
                // permission denied
                Toast.makeText(this@ArtBookDetailsActivity,"Permission needed!",Toast.LENGTH_LONG).show()
            }
        }



    }
}