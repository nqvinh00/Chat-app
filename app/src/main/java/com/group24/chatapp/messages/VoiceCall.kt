package com.group24.chatapp.messages

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.speech.tts.Voice
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import java.util.*
import com.group24.chatapp.R


class VoiceCall : AppCompatActivity() {
    private var mRtcEngine : RtcEngine? = null
    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft(uid, reason) }
        }

        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVoiceMuted(uid, muted) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.voice_call)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel()
        }
    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine() // Tutorial Step 1
        joinChannel() // Tutorial Step 2
    }

    fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(LOG_TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(this,
                        permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission),
                    requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)
        when (requestCode) {
            PERMISSION_REQ_ID_RECORD_AUDIO -> {
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel()
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                    finish()
                }
            }
        }
    }

    fun showLongToast(msg: String?) {
        runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    fun onSwitchSpeakerphoneClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        mRtcEngine!!.setEnableSpeakerphone(view.isSelected())
    }

    fun onEncCallClicked(view: View?) {
        finish()
    }

    private fun initializeAgoraEngine() {
        mRtcEngine = try {
            RtcEngine.create(baseContext, getString(R.string.app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e(LOG_TAG, Log.getStackTraceString(e))
            throw RuntimeException("""
            NEED TO check rtc sdk init fatal error
            ${Log.getStackTraceString(e)}
            """.trimIndent())
        }
    }

    private fun joinChannel() {
        val reference = FirebaseDatabase.getInstance().getReference("/voice-call")
        reference.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
                Log.d(LOG_TAG, snapshot.value.toString())
                mRtcEngine!!.joinChannel(null, snapshot.value.toString(), "Extra Optional Data", 0) // if you do not specify the uid, we will generate the uid for you
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
        })
    }

    private fun leaveChannel() {
        mRtcEngine!!.leaveChannel()
    }

    private fun onRemoteUserLeft(uid: Int, reason: Int) {
        showLongToast(String.format(Locale.US, "user %d left %d", uid and 0xFFFFFFFFL.toInt(), reason))
        val tipMsg = findViewById<View>(R.id.quick_tips_when_use_agora_sdk) // optional UI
        tipMsg.visibility = View.VISIBLE
    }

    private fun onRemoteUserVoiceMuted(uid: Int, muted: Boolean) {
        showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", uid and 0xFFFFFFFFL.toInt(), muted))
    }

    companion object {
        private val LOG_TAG = VoiceCall::class.java.simpleName
        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
    }
}