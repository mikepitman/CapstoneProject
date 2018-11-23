package pitman.co.za.readerforreddit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

public class UtilityCode {

//    public String formatSubredditSubmissionsScore(Integer submissionScore) {
//        String formattedSubmissionScore = submissionScore.toString();
//        if (submissionScore > 999 && submissionScore < 10000) {
//            // https://stackoverflow.com/questions/5195837/format-float-to-n-decimal-places
//            formattedSubmissionScore = String.format(java.util.Locale.US,"%.1f", (float) submissionScore/1000);
//            formattedSubmissionScore += 'k';
//        } else if (submissionScore > 9999) {
//            formattedSubmissionScore = String.valueOf(submissionScore/1000);
//            formattedSubmissionScore += 'k';
//        }
//        return formattedSubmissionScore;
//    }

    // Collect the top-scored submissions for each subreddit for display on 'home screen' from list of all returned subreddit submissions
    public List<SubredditSubmission> parseTopSubredditSubmissions(List<SubredditSubmission> subredditSubmissions) {
        List<SubredditSubmission> topSubredditSubmissions = new ArrayList<>();
        if (subredditSubmissions != null) {     // NPE results if code is executed before the asyncTask returns
            Set<String> subredditsParsed = new HashSet<>();
            SubredditSubmission topSubmissionForSubreddit = null;
            for (SubredditSubmission submission : subredditSubmissions) {
                if (!subredditsParsed.contains(submission.getSubreddit())) {
                    subredditsParsed.add(submission.getSubreddit());
                    topSubmissionForSubreddit = submission;
                    topSubredditSubmissions.add(topSubmissionForSubreddit);
                } else {
                    if (submission.getSubmissionScore() > topSubmissionForSubreddit.getSubmissionScore()) {
                        topSubmissionForSubreddit = submission;
                    }
                }
            }
        }
        return topSubredditSubmissions;
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