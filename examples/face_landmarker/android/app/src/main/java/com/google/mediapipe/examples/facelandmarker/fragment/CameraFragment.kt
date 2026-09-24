package com.google.mediapipe.examples.facelandmarker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.mediapipe.examples.facelandmarker.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {
    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!
    private val viewModel: com.google.mediapipe.examples.facelandmarker.MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
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
                        }
                    }
                }
                override fun onError(error: String, errorCode: Int) {}
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentCameraBinding = null
    }
}
