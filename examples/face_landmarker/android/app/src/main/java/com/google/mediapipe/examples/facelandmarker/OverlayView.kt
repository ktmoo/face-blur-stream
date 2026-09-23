package com.google.mediapipe.examples.facelandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.BlurMaskFilter
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.pedro.rtmp.rtmp.RtmpClient

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: FaceLandmarkerResult? = null
    private var rtmpClient: RtmpClient? = null
    private var isStreaming = false

    private var blurPaint = Paint().apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(120f, BlurMaskFilter.Blur.NORMAL)
        color = Color.argb(230, 80, 80, 80)
    }

    init {
        // Initialize the local background network connector
        rtmpClient = RtmpClient(object : com.pedro.rtmp.utils.ConnectCheckerRtmp {
            override fun onConnectionSuccessRtmp() { isStreaming = true }
            override fun onConnectionFailedRtmp(reason: String) { isStreaming = false }
            override fun onNewBitrateRtmp(bitrate: Long) {}
            override fun onDisconnectRtmp() { isStreaming = false }
            override fun onAuthErrorRtmp() {}
            override fun onAuthSuccessRtmp() {}
        })
        
        // Start streaming locally automatically
        Thread {
            try {
                rtmpClient?.connect("rtmp://127.0.0.1:1935/live/blur")
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    fun clear() {
        results = null
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val currentResults = results ?: return
        
        for (landmarks in currentResults.faceLandmarks()) {
            var minX = canvas.width.toFloat()
            var maxX = 0f
            var minY = canvas.height.toFloat()
            var maxY = 0f
            
            for (landmark in landmarks) {
                val x = landmark.x() * canvas.width
                val y = landmark.y() * canvas.height
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
            
            // Render the real-time privacy blur overlay tracking your face coordinates
            canvas.drawOval(minX - 40f, minY - 80f, maxX + 40f, maxY + 40f, blurPaint)
        }
    }

    fun setResults(
        faceLandmarkerResult: FaceLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: MainViewModel.RunningMode
    ) {
        results = faceLandmarkerResult
        invalidate()
    }
}
