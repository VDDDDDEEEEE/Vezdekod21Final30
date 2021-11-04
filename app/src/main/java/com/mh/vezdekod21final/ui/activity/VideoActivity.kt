package com.mh.vezdekod21final.ui.activity

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.give.vezdekodmh.utils.Const
import com.give.vezdekodmh.utils.L
import com.mh.vezdekod21final.databinding.ActivityVideoBinding
import com.video.trimmer.interfaces.OnTrimVideoListener
import com.video.trimmer.interfaces.OnVideoListener
import java.io.File

class VideoActivity : AppCompatActivity(), OnTrimVideoListener, OnVideoListener {
    private lateinit var binding: ActivityVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val uri = intent.getStringExtra(Const.VIDEOURI)
        setTrimmer(Uri.parse(uri))

    }

    private fun setTrimmer(path: Uri){
        binding.videoTrimmer
            //.setTextTimeSelectionTypeface(FontsHelper[this, FontsConstants.SEMI_BOLD])
            .setOnTrimVideoListener(this)
            .setOnVideoListener(this)
            .setVideoURI(path)
            //.setVideoInformationVisibility(true)
            //.setMaxDuration(10)
            //.setMinDuration(2)
            .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "temp" + File.separator + "Videos" + File.separator)
    }

    override fun cancelAction() {
        L.d("cancelAction")
    }

    override fun getResult(uri: Uri) {
        L.d("getResult uri = $uri")
    }

    override fun onError(message: String) {
        L.d("onError message = $message" )
    }

    override fun onTrimStarted() {
        L.d("onTrimStarted")
    }

    override fun onVideoPrepared() {
        L.d("onVideoPrepared")
    }
}