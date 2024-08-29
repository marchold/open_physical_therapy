package com.catglo.openphysicaltherapy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.canhub.cropper.CropImageView
import java.io.File

class ImageEditor : AppCompatActivity() {
    var cropImageView: CropImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val uri = intent.getParcelableExtra<Uri>("Uri")
        cropImageView = findViewById<CropImageView>(R.id.cropImageView)
        cropImageView?.setAspectRatio(300,400)
        cropImageView?.setImageUriAsync(uri)

        findViewById<Button>(R.id.doneButton)?.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().apply {
                val file = File(cacheDir, "cropped_image.png")
                if (file.exists()) file.delete()
                cropImageView?.getCroppedImage()?.let { bitmap ->
                    file.outputStream().use {
                        bitmap.compress(
                            android.graphics.Bitmap.CompressFormat.PNG,
                            100,
                            it
                        )
                    }
                }
                putExtra("Uri", Uri.fromFile(file))
            })
            finish()
        }
    }
}