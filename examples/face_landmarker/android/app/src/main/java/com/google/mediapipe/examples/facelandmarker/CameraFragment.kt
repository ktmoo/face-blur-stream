package com.google.mediapipe.examples.facelandmarker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.mediapipe.examples.facelandmarker.databinding.FragmentCameraBinding
import de.jcm.ndi.NDI

class CameraFragment : Fragment() {
    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!
    private val viewModel: com.google.mediapipe.examples.facelandmarker.MainViewModel by activityViewModels()
    
    private var ndiSource1: Long = 0
    private var ndiSource2: Long = 0
    private var frameCounter = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        NDI.initialize()
        ndiSource1 = NDI.createSource("FaceBlurCamera_1")
        ndiSource2 = NDI.createSource("FaceBlurCamera_2")
        
        viewModel.faceLandmarkerHelper.setResultsListener(
            object : com.google.mediapipe.examples.facelandmarker.FaceLandmarkerHelper.LandmarkerListener {
                override fun onResults(resultBundle: com.google.mediapipe.examples.facelandmarker.FaceLandmarkerHelper.ResultBundle) {
                    activity?.runOnUiThread {
                        if (_fragmentCameraBinding != null) {
                            fragmentCameraBinding.overlay.setResults(
                                resultBundle.results,
                                resultBundle.inputImageHeight,
                                resultBundle.inputImageWidth,
                                com.google.mediapipe.examples.facelandmarker.MainViewModel.RunningMode.LIVE_STREAM
                            )
                            
                            frameCounter++
                            if (frameCounter % 2 == 0) {
                                val blurredBitmap = fragmentCameraBinding.overlay.getBitmap()
                                if (blurredBitmap != null) {
                                    NDI.sendFrame(ndiSource1, blurredBitmap)
                                    NDI.sendFrame(ndiSource2, blurredBitmap)
                                }
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
        if (ndiSource1 != 0L) NDI.destroySource(ndiSource1)
        if (ndiSource2 != 0L) NDI.destroySource(ndiSource2)
        _fragmentCameraBinding = null
    }
}
