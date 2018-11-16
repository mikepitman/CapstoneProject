package pitman.co.za.readerforreddit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class UtilityCode {

    public String formatSubredditSubmissionsScore(Integer submissionScore) {
        String formattedSubmissionScore = submissionScore.toString();
        if (submissionScore > 999 && submissionScore < 10000) {
            // https://stackoverflow.com/questions/5195837/format-float-to-n-decimal-places
            formattedSubmissionScore = String.format(java.util.Locale.US,"%.1f", (float) submissionScore/1000);
            formattedSubmissionScore += 'k';
        } else if (submissionScore > 9999) {
            formattedSubmissionScore = String.valueOf(submissionScore/1000);
            formattedSubmissionScore += 'k';
        }
        return formattedSubmissionScore;
    }

    // determines whether network connection is available.
    public boolean isNetworkAvailable(FragmentActivity fragmentActivity) {
        /* http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html */
        ConnectivityManager cm = (ConnectivityManager) fragmentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return ((activeNetwork != null) && (activeNetwork.isConnectedOrConnecting()));
    }

    // https://materialdoc.com/components/snackbars-and-toasts/#with-code
    public void showSnackbar(CoordinatorLayout mCoordinatorLayout, int message, Context context) {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.furtherOffWhite));
        snackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }
}