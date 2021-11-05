package com.mh.vezdekod21final.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.give.vezdekodmh.utils.Const
import com.give.vezdekodmh.utils.L
import com.mh.vezdekod21final.databinding.ActivityVideoBinding
import com.mh.vezdekod21final.trimmer.interfaces.OnTrimVideoListener
import com.mh.vezdekod21final.trimmer.interfaces.OnVideoListener
import com.simform.videooperations.*
import java.io.File

class VideoActivity : AppCompatActivity(), OnTrimVideoListener, OnVideoListener {


    private lateinit var binding: ActivityVideoBinding
    private var filePath = ""
    val ffmpegQueryExtension = FFmpegQueryExtension()
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val uri = intent.getStringExtra(Const.VIDEOURI)
        setTrimmer(Uri.parse(uri))
        filePath = intent.getStringExtra(Const.VIDEOPATH)?:""
        L.d("filePatch = $filePath")

        /*binding.videoTrimmer.setOnTouchListener { view, motionEvent ->
            mGestureDetector.handleTouchEvents(motionEvent)
        }*/
    }

    private fun setTrimmer(path: Uri){

        binding.videoTrimmer
            .setOnTrimVideoListener(this)
            .setOnVideoListener(this)
            .setVideoURI(path)

            //.setVideoInformationVisibility(true)
            //.setMaxDuration(10)
            //.setMinDuration(2)
            //.setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "temp" + File.separator + "Videos" + File.separator)
            //.setDestinationPath(filePatch)


    }

    override fun cancelAction() {
        L.d("cancelAction")
    }

    override fun getResult(startTimeString:String, endTimeString:String) {
        val fileName = "${System.currentTimeMillis()}MH.mp4"
        val videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val outputPath = File(videoPath, fileName)
        L.d("outputPath = $outputPath")
        L.d("outputPath  absolutePath = ${outputPath.absolutePath}")
        val query = ffmpegQueryExtension.cutVideo(filePath, startTimeString, endTimeString, outputPath.absolutePath)
        CallBackOfQuery().callQuery(this, query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                L.d("process logMessage = logMessage")
            }
            override fun success() {
                L.d("success ")
                toastSuccess()
            }

            override fun cancel() {
                L.d("cancel")
                toastFail()
            }
            override fun failed() {
                L.d("failed")
                toastFail()
            }
        })

    }
    private fun toastFail(){
        binding.progress = false
        Toast.makeText(this, getText(com.mh.vezdekod21final.R.string.error), Toast.LENGTH_SHORT).show()
    }
    private fun toastSuccess(){
        binding.progress = false
        Toast.makeText(this, "Cохранено успешно", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        L.d("onError message = $message" )
        binding.progress = false
    }

    override fun onTrimStarted() {
        L.d("onTrimStarted")
        binding.progress = true
    }

    override fun onVideoPrepared() {
        L.d("onVideoPrepared")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

    }




    /*inner class MySimpleOnGestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (mVodView.isPlaying()) mVodView.pause() else mVodView.start()
            return true
        }
    }

    inner class MyScaleGestureListener : OnScaleGestureListener {
        private var mW = 0
        private var mH = 0
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // scale our video view
            mW *= detector.scaleFactor.toInt()
            mH *= detector.scaleFactor.toInt()
            if (mW < MIN_WIDTH) { // limits width
                mW = mVodView.getWidth()
                mH = mVodView.getHeight()
            }
            L.d("onScal scale=" + detector.scaleFactor + ", w=" + mW + ", h=" + mH)
            mVodView.setFixedVideoSize(mW, mH) // important
            mRootParam.width = mW
            mRootParam.height = mH
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mW = mVodView.getWidth()
            mH = mVodView.getHeight()
            L.d("onScaleBegin scale=" + detector.scaleFactor + ", w=" + mW + ", h=" + mH)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            L.d("onScaleEnd scale=" + detector.scaleFactor + ", w=" + mW + ", h=" + mH)
        }
    }*/
}