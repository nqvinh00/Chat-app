package com.group24.chatapp.util

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.group24.chatapp.MenuActivity
import com.group24.chatapp.R
import com.group24.chatapp.messages.ChatLogActivity
import com.group24.chatapp.models.message.ImageMessage
import com.group24.chatapp.models.whiteboard.PaintView
import java.io.File
import java.util.*

class WhiteBoard : AppCompatActivity() {
    private var paintView: PaintView? = null
    private var file : File? = null
    private var REQUEST_WRITE_STORAGE_REQUEST_CODE = 122

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whiteboard)
        supportActionBar?.hide()
        val target = intent.getStringExtra("target")
        paintView = findViewById<View>(R.id.paintView) as PaintView
        requestAppPermissions()
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        paintView!!.init(metrics)
    }

    fun cancelDraw(view: View) {
        finish()
    }

    fun send(view: View) {
        getBitmapFromView(view)
        finish()
    }

    private fun getBitmapFromView(view : View ) {
        val bitmap = Bitmap.createBitmap(paintView!!.width, paintView!!.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap!!)
        val drawable =paintView!!.background
        drawable?.draw(canvas)
        paintView!!.draw(canvas)
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        val uuid = UUID.randomUUID().toString()
        file = File(path, "$uuid.png")
        file!!.writeBitmap(bitmap, Bitmap.CompressFormat.PNG, 85)
        sendAsMessage(Uri.fromFile(file))
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun sendAsMessage(uri: Uri) {
        val currentUser = MenuActivity.currentUser ?: return
        val fromId = currentUser.uid
        val to : String? = if (ChatLogActivity.user != null) {
            ChatLogActivity.user!!.uid
        } else {
            ChatLogActivity.group!!.groupName
        }
        val uuid = UUID.randomUUID().toString()
        val storagePath = "https://firebasestorage.googleapis.com/v0/b/kotlinchatapp-group24.appspot.com/o/images%2F$fromId%2F$to%2F$uuid?alt=media"
        val reference = FirebaseStorage.getInstance().getReference("/images/$fromId/$to/$uuid")
        val toReference = FirebaseStorage.getInstance().getReference("/images/$to/$fromId/$uuid")
        reference.putFile(uri)
                .addOnSuccessListener { it ->
                    Log.d("Image message", "Upload avatar to firebase successfully: ${it.metadata?.path}")
                    reference.downloadUrl.addOnSuccessListener {
                        val db = FirebaseDatabase.getInstance().getReference("/message-between/$fromId/$to").push()
                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, to!!, System.currentTimeMillis() / 1000)
                        db.setValue(imageMessage)
                                .addOnSuccessListener {

                                }
                    }
                }
                .addOnFailureListener {
                }
        toReference.putFile(uri)
                .addOnSuccessListener {
                    Log.d("Image message", "Upload avatar to firebase successfully: ${it.metadata?.path}")
                    toReference.downloadUrl.addOnSuccessListener {
                        val db = FirebaseDatabase.getInstance().getReference("/message-between/$to/$fromId").push()
                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, to!!, System.currentTimeMillis() / 1000)
                        db.setValue(imageMessage)
                                .addOnSuccessListener {

                                }
                    }
                }
        if (to!!.contains(currentUser.username)) {
            for (u in ChatLogActivity.group!!.users!!) {
                if (u.username != currentUser.username) {
                    val uid = u.uid
                    val toReference = FirebaseStorage.getInstance().getReference("/images/$uid/$to/$uuid")
                    toReference.putFile(uri)
                            .addOnSuccessListener {
                                toReference.downloadUrl.addOnSuccessListener {
                                    val db = FirebaseDatabase.getInstance().getReference("/message-between/$uid/$to").push()
                                    val imageMessage = ImageMessage(db.key!!, storagePath, fromId, uid, System.currentTimeMillis() / 1000)
                                    db.setValue(imageMessage)
                                            .addOnSuccessListener {
                                            }
                                }
                            }
                }
            }
        }
    }

    private fun requestAppPermissions() {
        if (hasReadPermissions() && hasWritePermissions()) {
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), REQUEST_WRITE_STORAGE_REQUEST_CODE) // your request code
    }

    private fun hasReadPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}