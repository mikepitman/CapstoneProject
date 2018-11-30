package za.co.pitman.readerforreddit;
// Rename package: https://www.youtube.com/watch?v=FhBreC33LOU
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
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Set;

import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callbacks, ViewSubredditActivityFragment.Callbacks {

    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean mIsTablet;

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
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        mIsTablet = getResources().getBoolean(R.bool.is_tablet);

        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.shared_prefs_subreddits_pref), Context.MODE_PRIVATE);
        if (preferences != null) {
            Set<String> mSelectedSubreddits = preferences.getStringSet(getString(R.string.shared_prefs_subreddits_list_key), null);
            if (mSelectedSubreddits == null || mSelectedSubreddits.isEmpty()) {
                // launch intent for action to select subreddits
                Intent updateSubredditSelectionIntent = new Intent(this, SelectSubredditsActivity.class);
                startActivity(updateSubredditSelectionIntent);
            } else {
                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.activity_main_coordinatorLayout);

                if (fragment == null) {
                    fragment = new MainActivityFragment();

                    Bundle mainActivityFragmentBundle = new Bundle();
                    mainActivityFragmentBundle.putBoolean(getString(R.string.bundle_key_is_tablet), mIsTablet);
                    mainActivityFragmentBundle.putStringArrayList(getString(R.string.bundle_key_selected_subreddits_list), new ArrayList<>(mSelectedSubreddits));
                    fragment.setArguments(mainActivityFragmentBundle);

                    fm.beginTransaction().add(R.id.activity_main_coordinatorLayout, fragment).commit();
                }

                if (mIsTablet) {
                    Fragment subredditFragment = new ViewSubredditActivityFragment();

                    Bundle viewSubredditfragmentBundle = new Bundle();
                    viewSubredditfragmentBundle.putString(getString(R.string.bundle_key_selected_subreddit), new ArrayList<String>(mSelectedSubreddits).get(0));
                    subredditFragment.setArguments(viewSubredditfragmentBundle);

                    fm.beginTransaction().replace(R.id.selected_subreddits_frame, subredditFragment, getString(R.string.subreddit_listing_tag)).commit();
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

                Bundle firebaseBundle = new Bundle();
                firebaseBundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, getString(R.string.firebase_analytics_search_term));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, firebaseBundle);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Callbacks ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override   // callback from MainActivityFragment
    public void onSubredditSelected(SubredditSubmission subredditSubmission) {

        if (mIsTablet) {
            Fragment subredditFragment = new ViewSubredditActivityFragment();

            Bundle viewSubredditfragmentBundle = new Bundle();
            viewSubredditfragmentBundle.putString(getString(R.string.bundle_key_selected_subreddit), subredditSubmission.getSubreddit());
            subredditFragment.setArguments(viewSubredditfragmentBundle);

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.selected_subreddits_frame, subredditFragment).commit();

        } else {
            Intent viewSubredditIntent = new Intent(this, ViewSubredditActivity.class);
            viewSubredditIntent.putExtra(getString(R.string.intent_extra_key_selected_submission), subredditSubmission);
            viewSubredditIntent.putExtra(getString(R.string.intent_extra_key_is_tablet), mIsTablet);
            startActivity(viewSubredditIntent);
        }
    }

    @Override   // callback from ViewSubredditActivityFragment
    public void onSubmissionSelected(SubredditSubmission subredditSubmission) {

        /* Display the list of subreddit submissions on left pane, and the selected submission on the right pane
         * Hence, create an intent to ViewSubredditActivity, with the mIsTablet parameter passed in as true
         * */
        Intent viewSubredditIntent = new Intent(this, ViewSubredditActivity.class);
        viewSubredditIntent.putExtra(getString(R.string.intent_extra_key_selected_submission), subredditSubmission);
        viewSubredditIntent.putExtra(getString(R.string.intent_extra_key_is_tablet), mIsTablet);
        startActivity(viewSubredditIntent);
    }
}
