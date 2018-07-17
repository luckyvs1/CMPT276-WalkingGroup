package olive.walkinggroup.dataobjects;

import android.util.Log;

import java.security.acl.LastOwnerException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GetLastUpdated {
    public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final Locale LOCALE = Locale.CANADA;

    private static final Double MS_PER_S = 1000.0;
    private static final Double ONE_MIN_IN_S = 60.0;
    private static final Double ONE_HR_IN_S = ONE_MIN_IN_S*60;
    private static final Double ONE_DAY_IN_S = ONE_HR_IN_S*24;
    private static final Double ONE_MONTH_IN_S = ONE_DAY_IN_S*30.4375;
    private static final Double ONE_YEAR_IN_S = ONE_MONTH_IN_S*12;

    public String getLastUpdatedString(String timestamp) {
        Date currentDate = new Date();
        String lastUpdatedString;
        if (timestamp != null) {
            SimpleDateFormat format = new SimpleDateFormat(PATTERN, LOCALE);
            ParsePosition parsePosition = new ParsePosition(0);
            Date timestampDate = format.parse(timestamp, parsePosition);

            long timeDifference = currentDate.getTime() - timestampDate.getTime();


            Double timeDifferenceSecs = timeDifference / MS_PER_S;

            lastUpdatedString = "Last updated: about ";

            if (timeDifferenceSecs < 0) {
                lastUpdatedString = "Error";
            } else if (timeDifferenceSecs < 1) {
                lastUpdatedString += "less than a second ago";
            } else if (timeDifferenceSecs < ONE_MIN_IN_S) {
                lastUpdatedString += timeDifferenceSecs.intValue() + " second(s) ago";
            } else if (timeDifferenceSecs < ONE_HR_IN_S) {
                Double minutes = timeDifferenceSecs / ONE_MIN_IN_S;
                lastUpdatedString += minutes.intValue() + " minute(s) ago";
            } else if (timeDifferenceSecs < ONE_DAY_IN_S) {
                Double hours = timeDifferenceSecs / ONE_HR_IN_S;
                lastUpdatedString += hours.intValue() + " hour(s) ago";
            } else if (timeDifferenceSecs < ONE_MONTH_IN_S) {
                Double days = timeDifferenceSecs / ONE_DAY_IN_S;
                lastUpdatedString += days.intValue() + " day(s) ago";
            } else if (timeDifferenceSecs < ONE_YEAR_IN_S) {
                Double months = timeDifferenceSecs / ONE_MONTH_IN_S;
                lastUpdatedString += months.intValue() + " month(s) ago";
            } else {
                Double years = timeDifferenceSecs / ONE_YEAR_IN_S;
                lastUpdatedString += years.intValue() + " year(s) ago";
            }
        } else {
            lastUpdatedString = "Last updated: never";
        }
        return lastUpdatedString;
    }
}
