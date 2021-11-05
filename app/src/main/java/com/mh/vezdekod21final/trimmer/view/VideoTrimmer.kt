package com.mh.vezdekod21final.trimmer.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Typeface
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.graphics.times
import com.give.vezdekodmh.utils.L
import com.give.vezdekodmh.utils.dp2px
import com.give.vezdekodmh.utils.screenRectPx
import com.mh.vezdekod21final.R
import com.mh.vezdekod21final.databinding.ViewTrimmerBinding
import com.mh.vezdekod21final.trimmer.interfaces.OnProgressVideoListener
import com.mh.vezdekod21final.trimmer.interfaces.OnRangeSeekBarListener
import com.mh.vezdekod21final.trimmer.interfaces.OnTrimVideoListener
import com.mh.vezdekod21final.trimmer.interfaces.OnVideoListener
import com.mh.vezdekod21final.trimmer.utils.BackgroundExecutor
import com.mh.vezdekod21final.trimmer.utils.TrimVideoUtils
import com.mh.vezdekod21final.trimmer.utils.UiThreadExecutor
import java.io.*
import java.lang.ref.WeakReference
import java.util.*


class VideoTrimmer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var mSrc: Uri
    private var mFinalPath: String? = null

    private var mMaxDuration: Int = -1
    private var mMinDuration: Int = -1
    private var mListeners: ArrayList<OnProgressVideoListener> = ArrayList()

    private var mOnTrimVideoListener: OnTrimVideoListener? = null
    private var mOnVideoListener: OnVideoListener? = null

    private var mDuration = 0f
    private var mTimeVideo = 0f
    private var mStartPosition = 0f

    private var mEndPosition = 0f
    private var mResetSeekBar = true
    private val mMessageHandler = MessageHandler(this)
    val binding = ViewTrimmerBinding.inflate(LayoutInflater.from(context), this, true)
    private var destinationPath: String
        get() {
            if (mFinalPath == null) {
                val folder = Environment.getExternalStorageDirectory()
                mFinalPath = folder.path + File.separator
            }
            return mFinalPath ?: ""
        }
        set(finalPath) {
            mFinalPath = finalPath
        }

    init {
        init(context)
    }

    private fun init(context: Context) {
        setUpListeners()
        setUpMargins()
    }

    private fun setUpListeners() {
        mListeners = ArrayList()
        mListeners.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Float, max: Float, scale: Float) {
                updateVideoProgress(time)
            }
        })


        binding.videoLoader.setOnErrorListener { _, what, _ ->
            mOnTrimVideoListener?.onError("Something went wrong reason : $what")
            false
        }

        binding.videoLoader.setOnTouchListener { _, event ->
            mGestureDetector.handleTouchEvents(event)
            true
        }


        binding.saveButton.setOnClickListener {
            onSaveClicked()
        }

        binding.handlerTop.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onPlayerIndicatorSeekChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStart()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStop(seekBar)
            }
        })

        binding.timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
                binding.handlerTop.visibility = View.GONE
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
                onStopSeekThumbs()
            }
        })

        binding.videoLoader.setOnPreparedListener { mp -> onVideoPrepared(mp) }
        binding.videoLoader.setOnCompletionListener { onVideoCompleted() }
    }

    private fun onPlayerIndicatorSeekChanged(progress: Int, fromUser: Boolean) {
        val duration = (mDuration * progress / 1000L)
        if (fromUser) {
            if (duration < mStartPosition) setProgressBarPosition(mStartPosition)
            else if (duration > mEndPosition) setProgressBarPosition(mEndPosition)
        }
    }

    private fun onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        binding.videoLoader.pause()
        binding.iconVideoPlay.visibility = View.VISIBLE
        notifyProgressUpdate(false)
    }

    private fun onPlayerIndicatorSeekStop(seekBar: SeekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        binding.videoLoader.pause()
        binding.iconVideoPlay.visibility = View.VISIBLE

        val duration = (mDuration * seekBar.progress / 1000L).toInt()
        binding.videoLoader.seekTo(duration)
        notifyProgressUpdate(false)
    }

    private fun setProgressBarPosition(position: Float) {
        if (mDuration > 0) binding.handlerTop.progress = (1000L * position / mDuration).toInt()
    }

    private fun setUpMargins() {
        val marge = binding.timeLineBar.thumbs[0].widthBitmap
        val lp = binding.timeLineView.layoutParams as LayoutParams
        lp.setMargins(marge, 0, marge, 0)
        binding.timeLineView.layoutParams = lp
    }



    fun onSaveClicked() {
        L.d("onSaveClicked()")
        mOnTrimVideoListener?.onTrimStarted()
        binding.iconVideoPlay.visibility = View.VISIBLE
        binding.videoLoader.pause()

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)
        val metaDataKeyDuration =
            java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))


        if (mTimeVideo < MIN_TIME_FRAME) {
            if (metaDataKeyDuration - mEndPosition > MIN_TIME_FRAME - mTimeVideo) mEndPosition += MIN_TIME_FRAME - mTimeVideo
            else if (mStartPosition > MIN_TIME_FRAME - mTimeVideo) mStartPosition -= MIN_TIME_FRAME - mTimeVideo
        }


        mOnTrimVideoListener?.getResult(TrimVideoUtils.stringForTime(mStartPosition),
            TrimVideoUtils.stringForTime(mEndPosition))

    }

    private fun onClickVideoPlayPause() {
        if (binding.videoLoader.isPlaying) {
            binding.iconVideoPlay.visibility = View.VISIBLE
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            binding.videoLoader.pause()
        } else {
            binding.iconVideoPlay.visibility = View.GONE
            if (mResetSeekBar) {
                mResetSeekBar = false
                binding.videoLoader.seekTo(mStartPosition.toInt())
            }
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            binding.videoLoader.start()
        }
    }

    fun onCancelClicked() {
        binding.videoLoader.stopPlayback()
        mOnTrimVideoListener?.cancelAction()
    }

    private fun onVideoPrepared(mp: MediaPlayer) {
        L.d("onVideoPrepared")
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        val videoProportion = videoWidth.dp2px.toFloat() / videoHeight.dp2px.toFloat()
        val screenWidth = binding.layoutSurfaceView.width
        val screenHeight = binding.layoutSurfaceView.height
        val screenProportion = screenWidth.dp2px.toFloat() / screenHeight.dp2px.toFloat()
        val lp = binding.videoLoader.layoutParams
        L.d("videoWidth = $videoWidth")
        L.d("videoHeight = $videoHeight")
        L.d("videoProportion = $videoProportion")
        L.d("screenProportion = $screenProportion")
        L.d("screenWidth = $screenWidth")
        L.d("screenProportion = $screenHeight")
        L.d("lp.width = ${lp.width}")
        L.d("lp.height = ${lp.height}")
        if (videoProportion < screenProportion) {
            L.d("videoProportion > screenProportion")
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() / videoProportion).toInt()
        } else {

            lp.width = (videoProportion * screenHeight.toFloat()).toInt()
            lp.height = screenHeight

        }
        L.d("lp.width = ${lp.width}")
        L.d("lp.height = ${lp.height}")
        binding.videoLoader.layoutParams = lp

        binding.iconVideoPlay.visibility = View.VISIBLE

        mDuration = binding.videoLoader.duration.toFloat()
        setSeekBarPosition()
        timeFrames()

        mOnVideoListener?.onVideoPrepared()
    }

    private fun setSeekBarPosition() {
        when {
            mDuration >= mMaxDuration && mMaxDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMaxDuration / 2
                mEndPosition = mDuration / 2 + mMaxDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))
            }
            mDuration <= mMinDuration && mMinDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMinDuration / 2
                mEndPosition = mDuration / 2 + mMinDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))
            }
            else -> {
                mStartPosition = 0f
                mEndPosition = mDuration
            }
        }
        binding.videoLoader.seekTo(mStartPosition.toInt())
        mTimeVideo = mDuration
        binding.timeLineBar.initMaxWidth()
    }

    private fun timeFrames() {
        val seconds = context.getString(R.string.short_seconds)
        binding.textTimeSelection.text = String.format(
            "%s %s - %s %s",
            TrimVideoUtils.stringForTime(mStartPosition),
            seconds,
            TrimVideoUtils.stringForTime(mEndPosition),
            seconds
        )
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            Thumb.LEFT -> {
                mStartPosition = (mDuration * value / 100L)
                binding.videoLoader.seekTo(mStartPosition.toInt())
            }
            Thumb.RIGHT -> {
                mEndPosition = (mDuration * value / 100L)
            }
        }
        timeFrames()
        mTimeVideo = mEndPosition - mStartPosition
    }

    private fun onStopSeekThumbs() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        binding.videoLoader.pause()
        binding.iconVideoPlay.visibility = View.VISIBLE
    }

    private fun onVideoCompleted() {
        binding.videoLoader.seekTo(mStartPosition.toInt())
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (mDuration == 0f) return
        val position = binding.videoLoader.currentPosition
        if (all) {
            for (item in mListeners) {
                item.updateProgress(position.toFloat(), mDuration, (position * 100 / mDuration))
            }
        } else {
            mListeners[0].updateProgress(
                position.toFloat(),
                mDuration,
                (position * 100 / mDuration)
            )
        }
    }

    private fun updateVideoProgress(time: Float) {
        if (time <= mStartPosition && time <= mEndPosition){
            binding.handlerTop.visibility = View.GONE
        }
        else {
            binding.handlerTop.visibility = View.VISIBLE
        }
        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            binding.videoLoader.pause()
            binding.iconVideoPlay.visibility = View.VISIBLE
            mResetSeekBar = true
            return
        }
        setProgressBarPosition(time)
    }

    fun setVideoInformationVisibility(visible: Boolean): VideoTrimmer {
        binding.timeFrame.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    fun setOnTrimVideoListener(onTrimVideoListener: OnTrimVideoListener): VideoTrimmer {
        mOnTrimVideoListener = onTrimVideoListener
        return this
    }

    fun setOnVideoListener(onVideoListener: OnVideoListener): VideoTrimmer {
        mOnVideoListener = onVideoListener
        return this
    }

    fun destroy() {
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")
    }

    fun setMaxDuration(maxDuration: Int): VideoTrimmer {
        mMaxDuration = maxDuration * 1000
        return this
    }

    fun setMinDuration(minDuration: Int): VideoTrimmer {
        mMinDuration = minDuration * 1000
        return this
    }

    fun setDestinationPath(path: String): VideoTrimmer {
        destinationPath = path
        return this
    }

    fun setVideoURI(videoURI: Uri): VideoTrimmer {
        mSrc = videoURI
        binding.videoLoader.setVideoURI(mSrc)
        binding.videoLoader.requestFocus()
        binding.timeLineView.setVideo(mSrc)
        return this
    }

    fun setTextTimeSelectionTypeface(tf: Typeface?): VideoTrimmer {
        if (tf != null) binding.textTimeSelection.typeface = tf
        return this
    }

    private class MessageHandler internal constructor(view: VideoTrimmer) : Handler() {
        private val mView: WeakReference<VideoTrimmer> = WeakReference(view)
        override fun handleMessage(msg: Message) {
            val view = mView.get() ?: return
            view.notifyProgressUpdate(true)
            if (view.binding.videoLoader.isPlaying) sendEmptyMessageDelayed(0, 10)
        }
    }

    companion object {
        private const val MIN_TIME_FRAME = 1000
        private const val SHOW_PROGRESS = 2
    }

    //private var lastScale = 0f

    private var videoWidth = 0
    private var videoHeight = 0
    private val mGestureDetector = CustomGestureDetector(context, CustomGestureDetector.Listener(
        onScale = { scaleFactor ->
            L.d("scaleFactor = $scaleFactor")
            //L.d("lastScale = $lastScale")
            //L.d("binding.videoLoader.scaleX = ${binding.videoLoader.scaleX}")
            if(videoWidth == 0 ){
                videoWidth = binding.videoLoader.width
                videoHeight = binding.videoLoader.height
            }
            L.d("(width*scaleFactor).toInt() = ${(videoWidth*scaleFactor).toInt()}")
            L.d("(height*scaleFactor).toInt() = ${(videoWidth*scaleFactor).toInt()}")
            if(scaleFactor >= 1f) {
                val layoutParams = binding.videoLoader.layoutParams
                layoutParams.width = (videoWidth * scaleFactor).toInt()
                layoutParams.height = (videoHeight * scaleFactor).toInt()
                binding.videoLoader.layoutParams = layoutParams
            }

            //lastScale = scaleFactor
        },
        onScroll = { tx, ty ->
            L.d("gestureDetector tx = $tx")
            L.d("gestureDetector ty = $ty")
            binding.videoLoader.x -= tx
            binding.videoLoader.y -= ty
            //binding.videoLoader.rotationY =
            //("use tx and ty")
        },
        onCLickVideo = {
            onClickVideoPlayPause()
            L.d("onCLickVideo " )
        },
        onScaleBegin = {

        }
    ))

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        L.d("onConfigurationChanged newConfig = $newConfig")
        /*getWindowManager().getDefaultDisplay().getMetrics(dm);
        MIN_WIDTH = dm.widthPixels;
        mVodView.setFixedVideoSize(dm.widthPixels,dm.heightPixels);
        mRootParam.width = dm.widthPixels;
        mRootParam.height = dm.heightPixels;


        getWindowManager().getDefaultDisplay().getMetrics(dm);
        MIN_WIDTH = dm.widthPixels;
        mVodView.setFixedVideoSize(dm.widthPixels,dm.heightPixels);
        mRootParam.width = dm.widthPixels;*/

    }

    class CustomGestureDetector(
        context: Context,
        listener: Listener
    ) {

        data class Listener(
            val onScroll: (tx: Float, ty: Float) -> Unit,
            val onScale: (scaleFactor: Float) -> Unit,
            val onCLickVideo: () -> Unit,
            val onScaleBegin: () -> Unit
        )

        private val gestureDetector = GestureDetector(
            context,
            CustomDetectorListener ({ tx, ty -> listener.onScroll(tx, ty) },{
                    listener.onCLickVideo()
            })

        )

        private val scaleGestureDetector = ScaleGestureDetector(
            context,
            CustomScaleDetectorListener ({ listener.onScale(it) },{
                listener.onScaleBegin()
            })
        )

        fun handleTouchEvents(event: MotionEvent): Boolean {
            val scaleEventResult = scaleGestureDetector.onTouchEvent(event)
            return if (scaleEventResult == scaleGestureDetector.isInProgress) true
            else gestureDetector.onTouchEvent(event)
        }

        private class CustomDetectorListener(val onScroll: (tx: Float, ty: Float) -> Unit,
                                             val onCLickVideo: () -> Unit) :
            GestureDetector.SimpleOnGestureListener() {

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                onScroll(distanceX, distanceY)
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onCLickVideo()
                return true
            }

        }

        private class CustomScaleDetectorListener(val onScale: (scaleFactor: Float) -> Unit,
                                                  val onScaleBegin: () -> Unit) :
            ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                onScale(detector.scaleFactor)
                return super.onScale(detector)
            }

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                onScaleBegin()
                return super.onScaleBegin(detector)
            }
        }
    }
}
