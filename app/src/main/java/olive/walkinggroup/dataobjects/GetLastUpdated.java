package olive.walkinggroup.dataobjects;

import android.app.Activity;
import android.util.Log;

import java.security.acl.LastOwnerException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import olive.walkinggroup.R;

/**
 *  GetLastUpdated class provides pattern and locale for generating timestamps, and generating a last updated
 *  string from a timestamp.
 */


public class GetLastUpdated {
    public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final Locale LOCALE = Locale.CANADA;

    private static final Double MS_PER_S = 1000.0;
    private static final Double ONE_MIN_IN_S = 60.0;
    private static final Double ONE_HR_IN_S = ONE_MIN_IN_S*60;
    private static final Double ONE_DAY_IN_S = ONE_HR_IN_S*24;
    private static final Double ONE_MONTH_IN_S = ONE_DAY_IN_S*30.4375;
    private static final Double ONE_YEAR_IN_S = ONE_MONTH_IN_S*12;
    
    private Activity activity;
    
    public GetLastUpdated(Activity activity) {
        this.activity = activity;
    }

    public String getLastUpdatedString(String timestamp) {
        Date currentDate = new Date();
        String lastUpdatedString;
        if (timestamp != null) {
            SimpleDateFormat format = new SimpleDateFormat(PATTERN, LOCALE);
            ParsePosition parsePosition = new ParsePosition(0);
            Date timestampDate = format.parse(timestamp, parsePosition);

            long timeDifference = currentDate.getTime() - timestampDate.getTime();


            Double timeDifferenceSecs = timeDifference / MS_PER_S;

            lastUpdatedString = activity.getString(R.string.last_updated_about);

            if (timeDifferenceSecs < 0) {
                lastUpdatedString = activity.getString(R.string.last_updated_error);
            } else if (timeDifferenceSecs < 1) {
                lastUpdatedString += " " + activity.getString(R.string.last_updated_less_secs);
            } else if (timeDifferenceSecs < ONE_MIN_IN_S) {
                lastUpdatedString += " " + timeDifferenceSecs.intValue() + " " + activity.getString(R.string.last_updated_secs_ago);
            } else if (timeDifferenceSecs < ONE_HR_IN_S) {
                Double minutes = timeDifferenceSecs / ONE_MIN_IN_S;
                lastUpdatedString += " " + minutes.intValue() + " " + activity.getString(R.string.last_updated_mins_ago);
            } else if (timeDifferenceSecs < ONE_DAY_IN_S) {
                Double hours = timeDifferenceSecs / ONE_HR_IN_S;
                lastUpdatedString += " " + hours.intValue() + " " + activity.getString(R.string.last_updated_hours_ago);
            } else if (timeDifferenceSecs < ONE_MONTH_IN_S) {
                Double days = timeDifferenceSecs / ONE_DAY_IN_S;
                lastUpdatedString += " " + days.intValue() + " " + activity.getString(R.string.last_updated_days_ago);
            } else if (timeDifferenceSecs < ONE_YEAR_IN_S) {
                Double months = timeDifferenceSecs / ONE_MONTH_IN_S;
                lastUpdatedString += " " + months.intValue() + " " + activity.getString(R.string.last_updated_months_ago);
            } else {
                Double years = timeDifferenceSecs / ONE_YEAR_IN_S;
                lastUpdatedString += " " + years.intValue() + " " + activity.getString(R.string.last_updated_years_ago);
            }
        } else {
            lastUpdatedString = activity.getString(R.string.last_updated_never);
        }
        return lastUpdatedString;
    }
}
