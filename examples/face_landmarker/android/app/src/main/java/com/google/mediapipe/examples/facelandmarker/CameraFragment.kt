package com.google.mediapipe.examples.facelandmarker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.mediapipe.examples.facelandmarker.databinding.FragmentCameraBinding
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class CameraFragment : Fragment() {
    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!
    private val viewModel: MainViewModel by activityViewModels()
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var isStreaming = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Thread {
            try {
                serverSocket = ServerSocket(8085)
                isStreaming = true
                while (isStreaming) {
                    clientSocket = serverSocket?.accept()
                }
            } catch (e: IOException) { e.printStackTrace() }
        }.start()

        viewModel.faceLandmarkerHelper.setResultsListener(
            object : FaceLandmarkerHelper.LandmarkerListener {
                override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
                    activity?.runOnUiThread {
                        if (_fragmentCameraBinding != null) {
                            fragmentCameraBinding.overlay.setResults(
                                resultBundle.results,
                                resultBundle.inputImageHeight,
                                resultBundle.inputImageWidth,
                                MainViewModel.RunningMode.LIVE_STREAM
                            )
                            
                            val blurredBitmap = fragmentCameraBinding.overlay.getBitmap()
                            if (blurredBitmap != null && clientSocket?.isConnected == true) {
                                Thread {
                                    try {
                                        val outputStream = clientSocket?.getOutputStream()
                                        blurredBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                                        outputStream?.flush()
                                    } catch (e: Exception) { e.printStackTrace() }
                                }.start()
                            }
                        }
                    }
                }
                override fun onError(error: String) {}
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isStreaming = false
        try {
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) { e.printStackTrace() }
        _fragmentCameraBinding = null
    }
}
