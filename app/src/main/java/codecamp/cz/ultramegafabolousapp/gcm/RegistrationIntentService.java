package codecamp.cz.ultramegafabolousapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import codecamp.cz.ultramegafabolousapp.R;

/**
 * Register for push notifications
 *
 * @author Michal Kučera [michal.kucera@ackee.cz]
 * @since {28/04/16}
 **/
public class RegistrationIntentService extends IntentService {
    public static final String TAG = RegistrationIntentService.class.getName();


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RegistrationIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: " + "intent = [" + intent + "]");
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            String token = instanceID.getToken(getString(R.string.gcm_sender_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(TAG, "GCM Registration Token: " + token);

            // Poslat token na server aby vedel komu poslat pushky

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
