package za.co.pitman.readerforreddit.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import za.co.pitman.readerforreddit.R;
import za.co.pitman.readerforreddit.UtilityCode;
import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;
import za.co.pitman.readerforreddit.room.SubredditSubmissionDao;
import za.co.pitman.readerforreddit.room.SubredditSubmissionDatabase;

public class ReaderForRedditWidgetRemoteViewsFactory implements RemoteViewsFactory {

    private static final String LOG_TAG = ReaderForRedditWidgetRemoteViewsFactory.class.getSimpleName();
    private static UtilityCode sUtilityCode;
    private List<SubredditSubmission> mSubmissionsList;
    private Context context;
    private int appWidgetId;

    public ReaderForRedditWidgetRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        sUtilityCode = new UtilityCode();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        SubredditSubmissionDatabase db = SubredditSubmissionDatabase.getDatabase(context);
        SubredditSubmissionDao dao = db.mSubredditSubmissionDao();

        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.shared_prefs_subreddits_pref), Context.MODE_PRIVATE);
        if (preferences != null) {
            Set<String> mSelectedSubreddits = preferences.getStringSet(context.getString(R.string.shared_prefs_subreddits_list_key), null);
            if (mSelectedSubreddits == null || mSelectedSubreddits.isEmpty()) {
                mSubmissionsList = new ArrayList<SubredditSubmission>();
            } else {
                mSubmissionsList = dao.getStaticSubredditSubmissions(mSelectedSubreddits.toArray(new String[mSelectedSubreddits.size()]));
                mSubmissionsList = sUtilityCode.parseTopSubredditSubmissions(mSubmissionsList);
            }
        }
    }

    @Override
    public void onDestroy() { mSubmissionsList.clear(); }

    @Override
    public int getCount() { return mSubmissionsList == null ? 0 : mSubmissionsList.size(); }

    @Override
    public RemoteViews getViewAt(int position) {
        // create view
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        SubredditSubmission submission = mSubmissionsList.get(position);
        String formattedSubreddit = context.getString(R.string.subreddit_prefix) + submission.getSubreddit();
        String formattedAuthor = context.getString(R.string.user_prefix) + submission.getAuthor();

        remoteView.setTextViewText(R.id.widget_list_item_score, sUtilityCode.formatSubredditSubmissionsScore(submission.getSubmissionScore()));
        remoteView.setTextViewText(R.id.widget_list_item_subreddit, formattedSubreddit);
        remoteView.setTextViewText(R.id.widget_list_item_title, submission.getTitle());
        remoteView.setTextViewText(R.id.widget_list_item_author, formattedAuthor);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
