package codecamp.cz.ultramegafabolousapp;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * TODO add class description
 *
 * @author Michal Kučera [michal.kucera@ackee.cz]
 * @since {28/04/16}
 **/
public class App extends Application {
    public static final String TAG = App.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);
    }


}
