package com.group24.chatapp.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.group24.chatapp.R
import com.group24.chatapp.messages.ChatLogActivity
import com.group24.chatapp.messages.LatestMessages
import com.group24.chatapp.models.message.ImageMessage
import com.group24.chatapp.models.whiteboard.PaintView
import java.io.File
import java.util.*

class WhiteBoard : AppCompatActivity() {
    private var paintView: PaintView? = null
    private var file : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whiteboard)
        supportActionBar?.hide()
        paintView = findViewById<View>(R.id.paintView) as PaintView
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
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
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
        val currentUser = LatestMessages.currentUser ?: return
        val fromId = currentUser.uid
        val toId = ChatLogActivity.user!!.uid
        val uuid = UUID.randomUUID().toString()
        val storagePath = "https://firebasestorage.googleapis.com/v0/b/kotlinchatapp-group24.appspot.com/o/images%2F$fromId%2F$toId%2F$uuid?alt=media"
        val reference = FirebaseStorage.getInstance().getReference("/images/$fromId/$toId/$uuid")
        val toReference = FirebaseStorage.getInstance().getReference("/images/$toId/$fromId/$uuid")
        reference.putFile(uri)
                .addOnSuccessListener { it ->
                    Log.d("Image message", "Upload avatar to firebase successfully: ${it.metadata?.path}")
                    reference.downloadUrl.addOnSuccessListener {
                        val db = FirebaseDatabase.getInstance().getReference("/message-between/$fromId/$toId").push()
                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, toId, System.currentTimeMillis() / 1000)
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
                        val db = FirebaseDatabase.getInstance().getReference("/message-between/$toId/$fromId").push()
                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, toId, System.currentTimeMillis() / 1000)
                        db.setValue(imageMessage)
                                .addOnSuccessListener {

                                }
                    }
                }
    }
}