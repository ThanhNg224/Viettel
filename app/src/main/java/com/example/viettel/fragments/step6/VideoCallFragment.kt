package com.example.viettel.fragments.step6

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.fragments.step7.PaymentFragment
import com.example.viettel.stringee.Common
import com.example.viettel.stringee.SensorManagerUtils
import com.example.viettel.stringee.Utils
import com.example.viettel.utils.ProgressUtils
import com.stringee.call.StringeeCall2
import com.stringee.common.StringeeAudioManager
import com.stringee.common.StringeeAudioManager.AudioDevice
import com.stringee.common.StringeeAudioManager.AudioManagerEvents
import com.stringee.listener.StatusListener
import com.stringee.video.StringeeVideoTrack
import org.json.JSONObject

class VideoCallFragment : Fragment(R.layout.fragment_video_call) {

    private var vRemote: FrameLayout? = null
    private var vLocal: FrameLayout? = null
    private var stringeeCall: StringeeCall2? = null
    private var sensorManagerUtils: SensorManagerUtils? = null
    private var audioManager: StringeeAudioManager? = null
    private var mMediaState: StringeeCall2.MediaState? = null
    private var mSignalingState: StringeeCall2.SignalingState? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bước 6
        ProgressUtils.animateProgressToStep(view, 6)

        vRemote = view.findViewById(R.id.v_remote)
        vLocal = view.findViewById(R.id.v_local)

        sensorManagerUtils = SensorManagerUtils.getInstance(requireContext())
        sensorManagerUtils!!.acquireProximitySensor(requireActivity().localClassName)

        // Thay thế disableKeyguard bằng window flags
        requireActivity().window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        Common.isInCall = true

        makeCall()
    }

    private fun makeCall() {
        //create audio manager to control audio device
        audioManager = StringeeAudioManager.create(requireContext())
        audioManager!!.start { audioDevice, set -> }
        audioManager!!.setSpeakerphoneOn(true)

        //make a call
        stringeeCall = StringeeCall2(Common.client, Common.client?.userId, "+testcall")
        stringeeCall!!.setVideoCall(true)

        stringeeCall!!.setCallListener(object : StringeeCall2.StringeeCallListener {
            override fun onSignalingStateChange(
                stringeeCall2: StringeeCall2?,
                signalingState: StringeeCall2.SignalingState,
                reason: String?,
                sipCode: Int,
                sipReason: String?
            ) {
                requireActivity().runOnUiThread {
                    Log.d("Stringee", "onSignalingStateChange: " + signalingState)
                    mSignalingState = signalingState
                    when (signalingState) {
                        StringeeCall2.SignalingState.CALLING -> requireContext().toast("Outgoing call")
                        StringeeCall2.SignalingState.RINGING -> requireContext().toast("Ringing")
                        StringeeCall2.SignalingState.ANSWERED -> {
                            requireContext().toast("Starting")
                            if (mMediaState == StringeeCall2.MediaState.CONNECTED) {
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
                requireActivity().runOnUiThread{
                    Log.d("Stringee", "onError: " + desc)
                    requireContext().toast("Ended")
                    dismissLayout()
                }
            }

            override fun onHandledOnAnotherDevice(
                stringeeCall2: StringeeCall2?,
                signalingState: StringeeCall2.SignalingState?,
                desc: String?
            ) {
            }

            override fun onMediaStateChange(
                stringeeCall2: StringeeCall2?,
                mediaState: StringeeCall2.MediaState?
            ) {
                requireActivity().runOnUiThread {
                    Log.d("Stringee", "onMediaStateChange: " + mediaState)
                    mMediaState = mediaState
                    if (mediaState == StringeeCall2.MediaState.CONNECTED) {
                        if (mSignalingState == StringeeCall2.SignalingState.ANSWERED) {
                            requireContext().toast("Started")
                        }
                    }
                }
            }

            override fun onLocalStream(stringeeCall2: StringeeCall2) {
                requireActivity().runOnUiThread {
                    Log.d("Stringee", "onLocalStream")
                    if (stringeeCall2.isVideoCall()) {
                        vLocal!!.removeAllViews()
                        vLocal!!.addView(stringeeCall2.getLocalView())
                        stringeeCall2.renderLocalView(true)
                    }
                }
            }

            override fun onRemoteStream(stringeeCall2: StringeeCall2) {
                requireActivity().runOnUiThread {
                    Log.d("Stringee", "onRemoteStream")
                    if (stringeeCall2.isVideoCall()) {
                        vRemote!!.removeAllViews()
                        vRemote!!.addView(stringeeCall2.getRemoteView())
                        stringeeCall2.renderRemoteView(false)
                    }
                }
            }

            override fun onVideoTrackAdded(stringeeVideoTrack: StringeeVideoTrack?) {
            }

            override fun onVideoTrackRemoved(stringeeVideoTrack: StringeeVideoTrack?) {
            }

            override fun onCallInfo(stringeeCall2: StringeeCall2?, jsonObject: JSONObject?) {
            }

            override fun onTrackMediaStateChange(
                from: String?,
                mediaType: StringeeVideoTrack.MediaType?,
                enable: Boolean
            ) {
            }
        })

        stringeeCall!!.makeCall(object : StatusListener() {
            override fun onSuccess() {
            }
        })
    }

    private fun endCallSuccess() {
        //Chuyển tới fragment thanh toán
        val paymentFragment = PaymentFragment()
        parentFragmentManager.beginTransaction()
        .replace(R.id.fragmentContainer, paymentFragment)  // fragment_container là id của FrameLayout trong activity
        .addToBackStack(null)  // thêm vào backstack để có thể nhấn nút back quay lại
        .commit()
    }

    private fun endCall() {
        stringeeCall!!.hangup(object : StatusListener() {
            override fun onSuccess() {
                //Chuyển tới fragment thanh toán
                val paymentFragment = PaymentFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, paymentFragment)  // fragment_container là id của FrameLayout trong activity
                    .addToBackStack(null)  // thêm vào backstack để có thể nhấn nút back quay lại
                    .commit()
            }
        })
        dismissLayout()
    }

    private fun dismissLayout() {
        if (audioManager != null) {
            audioManager!!.stop()
            audioManager = null
        }
        sensorManagerUtils!!.releaseSensor()
        Utils.postDelay({
            Common.isInCall = false
        }, 1000)
    }

    fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}