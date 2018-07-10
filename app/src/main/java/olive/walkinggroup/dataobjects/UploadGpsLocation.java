package olive.walkinggroup.dataobjects;

import android.app.Activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class UploadGpsLocation {
    private Activity activity;
    private Model instance;
    private User user;
    private Timer uploadTimer;
    private Timer autoStopTimer;
    private boolean hasArrivedAtDestLocation;

    private static final int NUM_MS_IN_S = 1000;
    private static final int NUM_S_IN_MIN = 60;

    private static final int UPLOAD_RATE_S = 30;
    private static final int UPLOAD_DELAY_S = 0;
    private static final int STOP_UPLOAD_DELAY_MIN = 10;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    public UploadGpsLocation(Activity activity) {
        hasArrivedAtDestLocation = false;
        uploadTimer = new Timer();
        autoStopTimer = new Timer();
        this.activity = activity;
        instance = Model.getInstance();
        user = instance.getCurrentUser();
    }

    public void start() {
        uploadTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (hasArrivedAtDestLocation) {
                    startAutoStopTimer();
                }
                uploadGpsLocationToServer();
            }
        },UPLOAD_DELAY_S, UPLOAD_RATE_S*NUM_MS_IN_S);


    }

    private void startAutoStopTimer() {
        autoStopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stop();
            }
        },STOP_UPLOAD_DELAY_MIN*NUM_S_IN_MIN*NUM_MS_IN_S);
    }


    public void stop() {
        uploadTimer.cancel();
    }

    private void uploadGpsLocationToServer() {
        String timeStamp = new SimpleDateFormat(TIMESTAMP_PATTERN, Locale.CANADA).format(new Date());
        GpsLocation lastGpsLocation = new GpsLocation(1.23, 1.23, timeStamp);
        Call<GpsLocation> caller = instance.getProxy().setLastGpsLocation(user.getId(), lastGpsLocation);
        ProxyBuilder.callProxy(activity, caller, returnedGpsLocation -> setLastGpsLocationReturned(returnedGpsLocation));

    }

    private void setLastGpsLocationReturned(GpsLocation returnedGpsLocation) {
        // callback
    }

    public void setHasArrivedAtDestLocation(boolean hasArrivedAtDestLocation) {
        this.hasArrivedAtDestLocation = hasArrivedAtDestLocation;
    }
}
