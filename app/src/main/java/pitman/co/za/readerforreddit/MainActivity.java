package pitman.co.za.readerforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Set;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callbacks {

    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;
    private Set<String> mSelectedSubreddits;
    private static String SHARED_PREFERENCES_SUBREDDITS_PREF = "sharedPreferences_selectedSubreddits";
    private static String SHARED_PREFERENCES_SUBREDDITS_LIST_KEY = "sharedPreferences_subredditsKey";

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_main;      // returns de-referenced value, for different screen sizes - not strictly necessary here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle firebaseBundle = new Bundle();
        firebaseBundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, "Nokia7plus");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, firebaseBundle);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        SharedPreferences preferences = this.getSharedPreferences(SHARED_PREFERENCES_SUBREDDITS_PREF, Context.MODE_PRIVATE);
        if (preferences != null) {
            mSelectedSubreddits = preferences.getStringSet(SHARED_PREFERENCES_SUBREDDITS_LIST_KEY, null);
            if (mSelectedSubreddits == null || mSelectedSubreddits.isEmpty()) {
                // launch intent for action to select subreddits
                Intent updateSubredditSelectionIntent = new Intent(this, SelectSubredditsActivity.class);
                startActivity(updateSubredditSelectionIntent);
            } else {
                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.activity_main_coordinatorLayout);

                if (fragment == null) {
                    fragment = new MainActivityFragment();

                    Bundle fragmentBundle = new Bundle();
//            fragmentBundle.putBoolean("isTablet", mIsTablet);
                    fragmentBundle.putStringArrayList("selectedSubredditsBundleForFragment", new ArrayList<>(mSelectedSubreddits));
                    fragment.setArguments(fragmentBundle);

                    fm.beginTransaction().add(R.id.activity_main_coordinatorLayout, fragment).commit();
                }
            }
        }

        Toolbar fragmentToolbar = (Toolbar) findViewById(R.id.toolbar_mainActivity);
        setSupportActionBar(fragmentToolbar);
        ActionBar newBar = getSupportActionBar();
        newBar.setTitle(R.string.app_name);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Initialise menu, and launch activity for changing the selection of subreddits
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_subreddits, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.select_subreddits:
                Intent updateSubredditSelectionIntent = new Intent(this, SelectSubredditsActivity.class);
                startActivity(updateSubredditSelectionIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onSubredditSelected(SubredditSubmission subredditSubmission) {
        // create intent for new activity, to display the posts of the selected subreddit
        Log.d(LOG_TAG, "subreddit was selected: " + subredditSubmission.getTitle());

        Intent viewSubredditIntent = new Intent(this, ViewSubredditActivity.class);
        viewSubredditIntent.putExtra("selectedSubredditIntentExtra", subredditSubmission.getSubreddit());
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
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Activity lifecycle methods for debugging/understanding/etc //////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
    }
}
