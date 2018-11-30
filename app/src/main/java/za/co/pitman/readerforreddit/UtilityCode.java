package za.co.pitman.readerforreddit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;

public class UtilityCode {

    public String formatSubredditSubmissionsScore(Integer submissionScore) {
        String formattedSubmissionScore = submissionScore.toString();
        if (submissionScore > 999 && submissionScore < 10000) {
            // https://stackoverflow.com/questions/5195837/format-float-to-n-decimal-places
            formattedSubmissionScore = String.format(java.util.Locale.US, "%.1f", (float) submissionScore / 1000);
            formattedSubmissionScore += 'k';
        } else if (submissionScore > 9999) {
            formattedSubmissionScore = String.valueOf(submissionScore / 1000);
            formattedSubmissionScore += 'k';
        }
        return formattedSubmissionScore;
    }

    public String getCommentAge(Context mContext, String whenLogged) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(mContext.getString(R.string.submission_comment_date_format));    // reddit timestamp format is painful!
        LocalDateTime timestamp = formatter.parseLocalDateTime(whenLogged);
        Period commentAge = Period.fieldDifference(timestamp, new LocalDateTime());

        if (commentAge.getMonths() > 0) {
            return String.format(commentAge.getMonths() + mContext.getString(R.string.submission_comment_months),
                    commentAge.getMonths() > 1 ? mContext.getString(R.string.submission_comment_plural) : mContext.getString(R.string.submission_comment_blank));

        } else if (commentAge.getWeeks() > 0) {
            return String.format(commentAge.getWeeks() + mContext.getString(R.string.submission_comment_weeks),
                    commentAge.getWeeks() > 1 ? mContext.getString(R.string.submission_comment_plural) : mContext.getString(R.string.submission_comment_blank));

        } else if (commentAge.getDays() > 0) {
            return String.format(commentAge.getDays() + mContext.getString(R.string.submission_comment_days),
                    commentAge.getDays() > 1 ? mContext.getString(R.string.submission_comment_plural) : mContext.getString(R.string.submission_comment_blank));

        } else if (commentAge.getHours() > 0) {
            return String.format(commentAge.getHours() + mContext.getString(R.string.submission_comment_hours),
                    commentAge.getHours() > 1 ? mContext.getString(R.string.submission_comment_plural) : mContext.getString(R.string.submission_comment_blank));

        } else if (commentAge.getMinutes() > 0) {
            return String.format(commentAge.getMinutes() + mContext.getString(R.string.submission_comment_minutes),
                    commentAge.getMinutes() > 1 ? mContext.getString(R.string.submission_comment_plural) : mContext.getString(R.string.submission_comment_blank));
        }

        return mContext.getString(R.string.submission_comment_indeterminate_period);
    }

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