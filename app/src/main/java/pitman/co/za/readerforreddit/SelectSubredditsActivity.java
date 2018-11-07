package pitman.co.za.readerforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SelectSubredditsActivity extends AppCompatActivity {//implements MainActivityFragment.Callbacks {

    private static String LOG_TAG = SelectSubredditsActivity.class.getSimpleName();
//    private FirebaseAnalytics mFirebaseAnalytics;

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_select_subreddits;      // returns de-referenced value, for different screen sizes - not strictly necessary here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//
//        Bundle firebaseBundle = new Bundle();
//        firebaseBundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, "Nokia7plus");
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, firebaseBundle);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.activity_select_subreddits_container);

        if (fragment == null) {
            fragment = new SelectSubredditsActivityFragment();

            Bundle fragmentBundle = new Bundle();
//            fragmentBundle.putBoolean("isTablet", mIsTablet);
            fragment.setArguments(fragmentBundle);

            fm.beginTransaction().add(R.id.activity_select_subreddits_container, fragment).commit();
        }

//        // Fragment for displaying subreddit cards
//        new QuerySubscribedSubredditsListAsyncTask(this).execute();
    }

    /*@Override
    public void onSubredditSelected(SubredditSubmission subredditSubmission) {
        // create intent for new activity, to display the posts of the selected subreddit
        Log.d(LOG_TAG, "subreddit was selected: " + subredditSubmission.getTitle());
        Intent viewSubredditIntent = new Intent(this, ViewSubredditActivity.class);
        viewSubredditIntent.putExtra("selectedSubreddit", subredditSubmission.getSubreddit());
        startActivity(viewSubredditIntent);
        // Once recipe is selected, update the widget with ingredients for the newly selected recipe
        // https://stackoverflow.com/questions/3455123/programmatically-update-widget-from-activity-service-receiver
//        Intent updateWidgetIntent = new Intent(this, RecipeWidgetProvider.class);
//        updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//
//        SharedPreferences sharedPreferences = this.getSharedPreferences("pitman.co.za.bakingapp", Context.MODE_PRIVATE);
//        sharedPreferences.edit().putString("selectedRecipe", recipe.getName()).apply();
//
//        this.sendBroadcast(updateWidgetIntent);
    }*/

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Activity lifecycle methods for debugging/understanding/etc //////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent()");
    }

    @Override
    public void onResume () {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
    }

    @Override
    public void onPause () {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }

    @Override
    public void onStop () {
        super.onStop();
        Log.d(LOG_TAG, "onStop()");
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
    }
}