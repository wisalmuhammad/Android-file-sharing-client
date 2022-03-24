package com.wisal.android.practices.filesharing.client

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var selectButton: Button
    private lateinit var imageView: ImageView
    private lateinit var infoView: TextView
    private lateinit var requestingIntent: Intent
    private lateinit var inputPFD: ParcelFileDescriptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectButton = findViewById(R.id.select_view)
        imageView = findViewById(R.id.image_view)
        infoView = findViewById(R.id.textView)

        selectButton.setOnClickListener {
            requestingIntent = Intent(Intent.ACTION_PICK).apply {
                type = "image/jpg"
            }
            startActivityForResult(requestingIntent,0)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)
        if(resultCode != Activity.RESULT_OK) {
            Log.e(TAG,"File selection did not work")
            return
        }
        returnIntent?.data?.also { uri ->
            contentResolver.query(uri,
                null,
                null,
                null,
                null
            )?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                it.moveToFirst()
                val name = it.getString(nameIndex)
                val size = it.getLong(sizeIndex).div(1024).toFloat()
                infoView.text = name
                    .plus("\n")
                    .plus("Size: ${String.format("%.2f KB",size)}")
            }

            inputPFD = try {
                contentResolver.openFileDescriptor(uri,"r")!!
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Log.e(TAG, "File not found.")
                return
            }

            val fd = inputPFD.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fd)
            inputPFD.close()
            Glide.with(this)
                .load(image)
                .centerCrop()
                .into(imageView)
        }

    }



}