package com.example.viettel.fragments.step6

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.databinding.FragmentVideoCallBinding
import com.example.viettel.fragments.step7.PaymentFragment
import com.example.viettel.stringee.Common
import com.example.viettel.stringee.SensorManagerUtils
import com.example.viettel.stringee.Utils
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.navigateTo
import com.stringee.call.StringeeCall2
import com.stringee.common.StringeeAudioManager
import com.stringee.listener.StatusListener
import com.stringee.video.StringeeVideoTrack
import org.json.JSONObject

class VideoCallFragment : Fragment(R.layout.fragment_video_call) {

    private var binding: FragmentVideoCallBinding? = null
    private var stringeeCall: StringeeCall2? = null
    private var sensorManagerUtils: SensorManagerUtils? = null
    private var audioManager: StringeeAudioManager? = null
    private var mediaState: StringeeCall2.MediaState? = null
    private var signalingState: StringeeCall2.SignalingState? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVideoCallBinding.bind(view)

        ProgressUtils.animateProgressToStep(view, 6)

        sensorManagerUtils = SensorManagerUtils.getInstance(requireContext())
        sensorManagerUtils?.acquireProximitySensor(requireActivity().localClassName)

        requireActivity().window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
        )

        Common.isInCall = true

        startCall()
    }

    private fun startCall() {
        audioManager = StringeeAudioManager.create(requireContext()).apply {
            start { _: StringeeAudioManager.AudioDevice?, _: Set<StringeeAudioManager.AudioDevice?>? -> }
            setSpeakerphoneOn(true)
        }

        stringeeCall = StringeeCall2(Common.client, Common.client?.userId, "+testcall").apply {
            setVideoCall(true)
            setCallListener(object : StringeeCall2.StringeeCallListener {
                override fun onSignalingStateChange(
                    stringeeCall2: StringeeCall2?,
                    signalingState: StringeeCall2.SignalingState,
                    reason: String?,
                    sipCode: Int,
                    sipReason: String?,
                ) {
                    requireActivity().runOnUiThread {
                        Log.d("Stringee", "onSignalingStateChange: $signalingState")
                        this@VideoCallFragment.signalingState = signalingState
                        when (signalingState) {
                            StringeeCall2.SignalingState.CALLING -> requireContext().toast("Outgoing call")
                            StringeeCall2.SignalingState.RINGING -> requireContext().toast("Ringing")
                            StringeeCall2.SignalingState.ANSWERED -> {
                                requireContext().toast("Starting")
                                if (mediaState == StringeeCall2.MediaState.CONNECTED) {
                                    requireContext().toast("Started")
                                }
                            }

                            StringeeCall2.SignalingState.BUSY -> {
                                requireContext().toast("Busy")
                                endCall()
                            }

                            StringeeCall2.SignalingState.ENDED -> {
                                requireContext().toast("Ended")
                                endCall()
                                endCallSuccess()
                            }
                        }
                    }
                }

                override fun onError(stringeeCall2: StringeeCall2?, code: Int, desc: String?) {
                    requireActivity().runOnUiThread {
                        Log.d("Stringee", "onError: $desc")
                        requireContext().toast("Ended")
                        dismissLayout()
                    }
                }

                override fun onHandledOnAnotherDevice(
                    stringeeCall2: StringeeCall2?,
                    signalingState: StringeeCall2.SignalingState?,
                    desc: String?,
                ) {
                }

                override fun onMediaStateChange(stringeeCall2: StringeeCall2?, mediaState: StringeeCall2.MediaState?) {
                    requireActivity().runOnUiThread {
                        Log.d("Stringee", "onMediaStateChange: $mediaState")
                        this@VideoCallFragment.mediaState = mediaState
                        if (mediaState == StringeeCall2.MediaState.CONNECTED &&
                            signalingState == StringeeCall2.SignalingState.ANSWERED
                        ) {
                            requireContext().toast("Started")
                        }
                    }
                }

                override fun onLocalStream(stringeeCall2: StringeeCall2) {
                    requireActivity().runOnUiThread {
                        Log.d("Stringee", "onLocalStream")
                        if (stringeeCall2.isVideoCall) {
                            binding?.vLocal?.removeAllViews()
                            binding?.vLocal?.addView(stringeeCall2.localView)
                            stringeeCall2.renderLocalView(true)
                        }
                    }
                }

                override fun onRemoteStream(stringeeCall2: StringeeCall2) {
                    requireActivity().runOnUiThread {
                        Log.d("Stringee", "onRemoteStream")
                        if (stringeeCall2.isVideoCall) {
                            binding?.vRemote?.removeAllViews()
                            binding?.vRemote?.addView(stringeeCall2.remoteView)
                            stringeeCall2.renderRemoteView(false)
                        }
                    }
                }

                override fun onVideoTrackAdded(stringeeVideoTrack: StringeeVideoTrack?) = Unit

                override fun onVideoTrackRemoved(stringeeVideoTrack: StringeeVideoTrack?) = Unit

                override fun onCallInfo(stringeeCall2: StringeeCall2?, jsonObject: JSONObject?) = Unit

                override fun onTrackMediaStateChange(from: String?, mediaType: StringeeVideoTrack.MediaType?, enable: Boolean) = Unit
            })
        }

        stringeeCall?.makeCall(object : StatusListener() {
            override fun onSuccess() = Unit
        })
    }

    private fun endCallSuccess() {
        navigateTo(PaymentFragment())
    }

    private fun endCall() {
        stringeeCall?.hangup(object : StatusListener() {
            override fun onSuccess() {
                navigateTo(PaymentFragment())
            }
        })
        dismissLayout()
    }

    private fun dismissLayout() {
        audioManager?.stop()
        audioManager = null
        sensorManagerUtils?.releaseSensor()
        Utils.postDelay({
            Common.isInCall = false
        }, 1000)
    }

    fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
