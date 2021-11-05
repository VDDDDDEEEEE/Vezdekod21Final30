package com.mh.vezdekod21final.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import br.com.onimur.handlepathoz.HandlePathOz
import br.com.onimur.handlepathoz.HandlePathOzListener
import br.com.onimur.handlepathoz.model.PathOz
import com.give.vezdekodmh.utils.Const
import com.mh.vezdekod21final.R
import com.mh.vezdekod21final.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity(), HandlePathOzListener.SingleUri {
    private lateinit var binding: ActivityMainBinding
    private lateinit var handlePathOz: HandlePathOz
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        handlePathOz = HandlePathOz(this, this)
        setContentView(binding.root)
        setOnClickListeners()
        //setTrimmer()
    }
    private fun setOnClickListeners(){
        binding.chooseVideo.setOnClickListener{
            checkWritePermission()
        }
    }

    fun checkWritePermission(){
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA),
                Const.REQUEST_PERMISSIONS_CODE_WRITE_STORAGE
            )
        }else{
            //getImageFromGallery(true)
            getChooseCameraOrGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if( requestCode == Const.REQUEST_PERMISSIONS_CODE_WRITE_STORAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getChooseCameraOrGallery()
                //getImageFromGallery(true)
            }else{
                getImageFromGallery(false)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getImageFromGallery(allowed: Boolean) {
        if(allowed){
            val videoIntent = Intent()
            videoIntent.setTypeAndNormalize("video/*")
            videoIntent.action = Intent.ACTION_GET_CONTENT
            videoIntent.addCategory(Intent.CATEGORY_OPENABLE)
            resultLauncher.launch(videoIntent)
        }else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    fun getChooseCameraOrGallery(){
        val galleryintent = Intent(Intent.ACTION_GET_CONTENT, null)
        galleryintent.type = "video/*"

        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, galleryintent)
        chooser.putExtra(Intent.EXTRA_TITLE, "Select from:")

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        resultLauncher.launch(chooser)
    }


    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // There are no request codes
            val data: Intent? = result.data

            val selectedPhotoUri = data!!.data
            try {
                selectedPhotoUri?.let {
                    handlePathOz.getRealPath(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }







    override fun onRequestHandlePathOz(pathOz: PathOz, tr: Throwable?) {
        val uri = Uri.fromFile(File(pathOz.path))
        val videoActivity = Intent(this, VideoActivity::class.java)
        videoActivity.putExtra(Const.VIDEOURI , uri.toString())
        videoActivity.putExtra(Const.VIDEOPATH , pathOz.path)
        startActivity(videoActivity)
        //generateTagsByPhoto(uri)
        //Glide.with(requireContext()).load(uri)
        //.into(b.userPhotoIv)
    }
}