package com.gz.jey.go4lunch.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.api.UserHelper
import com.gz.jey.go4lunch.models.Contact
import com.gz.jey.go4lunch.models.Details
import com.gz.jey.go4lunch.models.User
import com.gz.jey.go4lunch.utils.ApiStreams
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NotificationReceiver : BroadcastReceiver() {

    private val NOTIFICATION_CHANNEL_ID = "4565"
    private val NOTIFICATION_CHANNEL_NAME = "GO4L"

    // Activity
    private var user : User? = null
    private var disposable: Disposable? = null
    private var contacts : ArrayList<Contact>? = null
    private var lang : Int = 0
    private var today : Calendar? = null
    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)

    /**
     * @param context Context
     * @param intent Intent
     * to request article with articleSearch parameters
     */
    override fun onReceive(context: Context, intent: Intent) {
        val extra = intent.extras!!
        lang = extra.getInt("LANG")
        Log.d("NOTIF", "STARTING")
        if (this.getCurrentUser() != null){
            val uid = getCurrentUser()!!.uid
            Log.d("UID", uid)

            UserHelper.getUser(uid).addOnSuccessListener {
                if(it["uid"].toString()==uid && it["whereEatID"].toString().isNotEmpty())
                    loadData(it, context)
            }
        }
    }

    /**
     * @param data DocumentSnapshot
     * @param context Context
     * to build models results with the returned request
     */
    @Suppress("UNCHECKED_CAST")
    private fun loadData(data : DocumentSnapshot, context:Context){
        Log.d("LOAD","DATA")
        user= User(
                data["uid"] as String,
                data["username"] as String,
                data["mail"] as String,
                data["urlPicture"] as String,
                data["whereEatID"] as String,
                data["whereEatName"] as String,
                data["whereEatDate"] as String,
                data["restLiked"] as ArrayList<String> )

        today = Calendar.getInstance()
        today!!.set(Calendar.HOUR_OF_DAY, 0)
        today!!.set(Calendar.MINUTE, 0)
        today!!.set(Calendar.SECOND, 0)
        today!!.set(Calendar.MILLISECOND, 0)

        val eatTime : Calendar = Calendar.getInstance()
        eatTime.time = df.parse(user!!.whereEatDate)

        if(eatTime.after(today)){

        contacts = ArrayList()
        contacts!!.clear()
        UserHelper.getUsersCollection().get().addOnSuccessListener {
            for(contact in it.documents){

                if(contact.get("uid").toString() != getCurrentUser()!!.uid) {
                    val username = contact.get("username").toString()
                    val urlPicture = contact.get("urlPicture").toString()
                    val whereEatID= contact.get("whereEatID").toString()
                    val whereEatName= contact.get("whereEatName").toString()
                    val whereEatDate= if(contact.get("whereEatDate").toString().isEmpty()) "0000-00-00 00:00:00"
                    else contact.get("whereEatDate").toString()
                    val restLiked= contact.get("restLiked") as ArrayList<String>

                    val cntc = Contact(username, urlPicture, whereEatID, whereEatName, whereEatDate, restLiked)

                    contacts!!.add(cntc)
                }
            }

            disposable = ApiStreams.streamFetchDetails(context.getString(R.string.google_maps_key), user!!.whereEatID, lang)
                    .subscribeWith(object : DisposableObserver<Details>() {
                        override fun onNext(details: Details) {
                            buildNotification(details, context)
                        }

                        override fun onError(e: Throwable) {
                            Log.e("CODE_DETAILS NOTIF", e.toString())
                        }

                        override fun onComplete() {disposeWhenDestroy()}
                    })

            }
        }
    }

    /**
     * @param context Context
     * build the notification and pop it if results resturned a new article
     */
    private fun buildNotification(det: Details, context: Context) {

        val coming : ArrayList<String> = ArrayList()
        coming.clear()
        for (c in contacts!!){
            val eatTime : Calendar = Calendar.getInstance()
            eatTime.time = df.parse(c.whereEatDate)

            if(eatTime.after(today) && c.whereEatID == det.result.place_id)
                coming.add(c.username)
        }

        val notificationManager: NotificationManager?
        //Notification Channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)

            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            assert(true)
            notificationManager.createNotificationChannel(notificationChannel)
        } else {
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        val repeatingIntent = Intent(context, MainActivity::class.java)
        repeatingIntent.putExtra("NotiClick", true)
        repeatingIntent.putExtra("RestaurantId", det.result.place_id)
        repeatingIntent.putExtra("RestaurantName", det.result.name)
        repeatingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(context, 987,
                repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val sc = "-"
        val joining = context.getString(R.string.is_joining)

        val sb : StringBuilder = StringBuilder()
                .append(det.result.name).append("\r\n")
                for (c in coming)
                     sb.append("\r\n").append("   $sc $c $joining")


        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_g4l)
                .setContentTitle("Go4Lunch - " + context.getString(R.string.will_eat))
                .setContentText(det.result.name)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(sb.toString()))
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        assert(true)
        notificationManager.notify(987, builder.build())
    }

    private fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }


    /**
     * to destroy the disposable and avoid memoryLeaks
     */
    private fun disposeWhenDestroy() {
        if (disposable != null && !disposable!!.isDisposed)
            disposable!!.dispose()
    }
}
