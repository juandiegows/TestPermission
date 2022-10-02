package com.example.testpermission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.testpermission.databinding.ActivityMainBinding
import com.example.testpermission.databinding.SelectPhotoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    enum class CODE_RESULT {
        CAMARA,
        GPS,
        GALLERY
    }

    var code = CODE_RESULT.CAMARA

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnCamera.setOnClickListener {
            var bindingBottom = SelectPhotoBinding.bind(
                LayoutInflater.from(this).inflate(R.layout.select_photo, null)
            )

            var alert = BottomSheetDialog(this, R.style.BottomSheetDialogTheme).apply {
                setContentView(bindingBottom.root)
            }
            bindingBottom.btncamera.setOnClickListener {
                TakePhoto()
                alert.dismiss()
            }
            bindingBottom.btnGallery.setOnClickListener {
                getGallery()
                alert.dismiss()
            }
            alert.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getGallery() {
        code = CODE_RESULT.GALLERY
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED -> {
                activityResultLauncher.launch(Intent.createChooser(Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                }, "Imagen"))
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                activityResultLauncher.launch(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", this@MainActivity.packageName, null)
                })
            }
            else -> {
                getPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun TakePhoto() {
        code = CODE_RESULT.CAMARA
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                activityResultLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                activityResultLauncher.launch(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", this@MainActivity.packageName, null)
                })

            }
            else -> {
                getPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (code) {
                CODE_RESULT.CAMARA -> {
                    binding.imageView.setImageBitmap(it.data!!.extras!!.get("data") as Bitmap?)
                }
                CODE_RESULT.GALLERY -> {
                    binding.imageView.setImageBitmap(
                        MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            it.data?.data
                        )
                    )
                }
                else -> {

                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    private val getPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
                when (code) {
                    CODE_RESULT.CAMARA -> {
                        TakePhoto()
                    }
                    CODE_RESULT.GALLERY -> {
                        getGallery()
                    }
                    else -> {}
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

}