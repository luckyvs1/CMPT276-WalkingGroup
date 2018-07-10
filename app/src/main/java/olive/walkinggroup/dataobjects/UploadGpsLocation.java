package olive.walkinggroup.dataobjects;

import android.app.Activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import olive.walkinggroup.app.DashBoardActivity;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class UploadGpsLocation {
    private Activity activity;
    private Model instance;
    private User user;
    private Timer timer;

    private static final int UPLOAD_RATE_S = 30;
    private static final int UPLOAD_DELAY_S = 0;
    private static final int NUM_MS_IN_S = 1000;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    public UploadGpsLocation(Activity activity) {
        timer = new Timer();
        this.activity = activity;
        instance = Model.getInstance();
        user = instance.getCurrentUser();
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String timeStamp = new SimpleDateFormat(TIMESTAMP_PATTERN, Locale.CANADA).format(new Date());
                GpsLocation lastGpsLocation = new GpsLocation(1.23, 1.23, timeStamp);
                Call<GpsLocation> caller = instance.getProxy().setLastGpsLocation(user.getId(), lastGpsLocation);
                ProxyBuilder.callProxy(activity, caller, returnedGpsLocation -> setLastGpsLocationReturned(returnedGpsLocation));

            }
        },UPLOAD_DELAY_S, UPLOAD_RATE_S*NUM_MS_IN_S);


    }

    private void setLastGpsLocationReturned(GpsLocation returnedGpsLocation) {
        // callback
    }
}
