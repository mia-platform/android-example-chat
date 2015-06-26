package eu.makeitapp.meetup;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

/**
 * ${PROJECT}
 * <p/>
 * Created by Federico Oldrini (federico.oldrini@makeitapp.eu) on 08/04/2015.
 */
public class MTPPushIntentService extends IntentService {
    private static final String PUSH_INTENT_SERVICE_NAME = "dcn_intent_service";

    public MTPPushIntentService() {
        super(PUSH_INTENT_SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("mia", "PUSH RECEIVED");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("PUSH");
        broadcastIntent.putExtras(intent);

        sendBroadcast(broadcastIntent);
    }
}
