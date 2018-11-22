package pitman.co.za.readerforreddit.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pitman.co.za.readerforreddit.R;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;
import pitman.co.za.readerforreddit.room.SubredditSubmissionDao;
import pitman.co.za.readerforreddit.room.SubredditSubmissionDatabase;
import pitman.co.za.readerforreddit.room.SubredditSubmissionViewModel;

public class ReaderForRedditWidgetRemoteViewsFactory implements RemoteViewsFactory {

    private static final String LOG_TAG = ReaderForRedditWidgetRemoteViewsFactory.class.getSimpleName();
    private List<SubredditSubmission> mSubmissionsList;
    private SubredditSubmissionViewModel mSubredditSubmissionViewModel;
    private Context context;
    private int appWidgetId;
    private static String SHARED_PREFERENCES_SUBREDDITS_PREF = "sharedPreferences_selectedSubreddits";
    private static String SHARED_PREFERENCES_SUBREDDITS_LIST_KEY = "sharedPreferences_subredditsKey";


    public ReaderForRedditWidgetRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        SubredditSubmissionDatabase db = SubredditSubmissionDatabase.getDatabase(context);
        SubredditSubmissionDao dao = db.mSubredditSubmissionDao();

        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_SUBREDDITS_PREF, Context.MODE_PRIVATE);
        if (preferences != null) {
            Set<String> mSelectedSubreddits = preferences.getStringSet(SHARED_PREFERENCES_SUBREDDITS_LIST_KEY, null);
            if (mSelectedSubreddits == null || mSelectedSubreddits.isEmpty()) {
                mSubmissionsList = new ArrayList<SubredditSubmission>();
            } else {
                mSubmissionsList = dao.getSubredditSubmissions(mSelectedSubreddits.toArray(new String[mSelectedSubreddits.size()])).getValue();
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
        remoteView.setTextViewText(R.id.widget_list_item_score, submission.getSubmissionScore().toString());
        remoteView.setTextViewText(R.id.widget_list_item_subreddit, submission.getSubreddit());
        remoteView.setTextViewText(R.id.widget_list_item_title, submission.getTitle());
        remoteView.setTextViewText(R.id.widget_list_item_author, submission.getAuthor());

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
