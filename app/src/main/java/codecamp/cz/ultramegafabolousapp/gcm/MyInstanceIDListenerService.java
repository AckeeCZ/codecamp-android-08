package codecamp.cz.ultramegafabolousapp.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {
    public static final String TAG = MyInstanceIDListenerService.class.getName();


    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).

        Log.d(TAG, "onTokenRefresh() called with: " + "");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
