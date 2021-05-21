package com.example.ustchat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MessagingService extends Service {

    private FirebaseAuth mAuth;
    private DatabaseReference mDataRef;
    private NotificationManager mNotificationManager;
    private int Counter;
    public MessagingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        mAuth = FirebaseAuth.getInstance();
        mDataRef = FirebaseDatabase.getInstance().getReference();
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Log.d("My Service", "created");
        if(mAuth!= null){
            Query query = mDataRef.child("users/"+mAuth.getUid()+"/notiMessage/");
            query.addValueEventListener(new ValueEventListener()
            {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(mAuth.getCurrentUser() == null){
                        stopSelf();
                    }else{
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            NotiMessage msg = postSnapshot.getValue(NotiMessage.class);
                            if(Utility.enableNotification) {
                                pushNotification(msg);
                            }
                        }
                        mDataRef.child("users/"+mAuth.getUid()+"/notiMessage/").removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    Log.d("My Service", "cancel"+databaseError);
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void pushNotification(NotiMessage msg){
        Log.d("My Service", "pushNotification");
        String channelId = "0";
        int requestID = (int) System.currentTimeMillis();
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "USTCHAT-messaging",
                NotificationManager.IMPORTANCE_HIGH);
        Intent resultIntent = new Intent(this, PrivateMessageActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestID,resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n  = new Notification.Builder(this)
                .setContentTitle(msg.getTitle())
                .setContentText(msg.getFrom()+" : "+msg.getContent())
                .setSmallIcon(R.drawable.ic_bell)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setChannelId(channelId).build();

        mNotificationManager.createNotificationChannel(channel);
        mNotificationManager.notify(++Counter, n);
    }

}