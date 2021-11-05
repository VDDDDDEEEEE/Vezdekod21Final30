package com.mh.vezdekod21final.ui.activity


import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.give.vezdekodmh.utils.Const
import com.give.vezdekodmh.utils.L
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.mh.vezdekod21final.databinding.ActivityVideoPlayerBinding
import java.lang.Exception
import android.content.ContextWrapper
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import java.io.*


class VideoPlayer : AppCompatActivity() {
    private lateinit var uri: Uri
    private var filePatch = ""
    private lateinit var binding: ActivityVideoPlayerBinding
    private var scaleGestureDetector: ScaleGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val player = SimpleExoPlayer.Builder(this).build()
        binding.videoView.player = player
        uri = Uri.parse(intent.getStringExtra(Const.VIDEOURI))
        filePatch = intent.getStringExtra(Const.VIDEOPATH)?:""
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        //player.play()
        setListeners()
    }

    private fun setListeners(){
        binding.saveButton.setOnClickListener{
            saveVideoToInternalStorage(filePatch)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    private fun saveVideoToInternalStorage(filePath: String) {
        L.d("filePath = $filePath")
        val newfile: File
        try {
            val currentFile = File(filePath)
            val fileName = "${System.currentTimeMillis()}MH.mp4"
            val cw = ContextWrapper(applicationContext)
            val loc = Environment.getExternalStorageDirectory();
            //val directory = File(loc.getAbsolutePath()+"/FolderNameWhateverYouWant")
            val videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            //val videoPath = currentFile.parent
            L.d("videoPath = $videoPath")
            //val directory = cw.getDir("videoDir", Context.MODE_PRIVATE)
            newfile = File(videoPath, fileName)
            L.d("newfile = ${newfile.absolutePath}")
            if (currentFile.exists()) {
                val `in`: InputStream = FileInputStream(currentFile)
                val out: OutputStream = FileOutputStream(newfile)

                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
                L.d( "Video file saved successfully.")
                Toast.makeText(this, "Video file saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                L.d("Video saving failed. Source file missing.")
                Toast.makeText(this, "Video saving failed. Source file missing", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private class CustomOnScaleGestureListener(
        private val player: PlayerView
    ) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var scaleFactor = 0f

        override fun onScale(
            detector: ScaleGestureDetector
        ): Boolean {
            scaleFactor = detector.scaleFactor
            return true
        }

        override fun onScaleBegin(
            detector: ScaleGestureDetector
        ) : Boolean{
            return true
        }
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (scaleFactor > 1) {
                player.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            } else {
                player.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        scaleGestureDetector?.onTouchEvent(event)

        return true
    }




}