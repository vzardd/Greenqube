package com.vzardd.greenqube;

import android.util.Log;
import android.widget.Toast;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class sendNotification {

    public sendNotification(String title,String message,String userId) {
        try {
            OneSignal.postNotification(new JSONObject("{'contents': {'en':'"+title+": "+message+"'}, 'include_player_ids': ['" + userId + "']}"), null);
            Log.i("Notification","Sent");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
