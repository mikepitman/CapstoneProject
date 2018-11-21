package pitman.co.za.readerforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

public class ViewSubredditActivity extends AppCompatActivity implements ViewSubredditActivityFragment.Callbacks {

    private static String LOG_TAG = ViewSubredditActivity.class.getSimpleName();
    private String mSelectedSubreddit;
    private SubredditSubmission mSelectedSubmission;
    private boolean mIsTablet;

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_view_subreddit;      // returns de-referenced value, for different screen sizes - not strictly necessary here
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("selectedSubredditSaveInstanceState", mSelectedSubreddit);
        outState.putBoolean("isTabletSaveInstanceState", mIsTablet);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.mSelectedSubreddit = savedInstanceState.getString("selectedSubredditSaveInstanceState");
            this.mIsTablet = savedInstanceState.getBoolean("isTabletSaveInstanceState");
        } else {
            Intent intent = getIntent();
            mSelectedSubmission = intent.getParcelableExtra(getString(R.string.intent_extra_key_selected_submission));
            mIsTablet = intent.getBooleanExtra(getString(R.string.intent_extra_key_is_tablet), false);
            mSelectedSubreddit = mSelectedSubmission.getSubreddit();

            Log.d(LOG_TAG, "selected subreddit " + mSelectedSubreddit);
            Log.d(LOG_TAG, "is device a tablet?: " + mIsTablet);
        }

        setContentView(getLayoutResId());

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//
//        Bundle firebaseBundle = new Bundle();
//        firebaseBundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, "Nokia7plus");
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, firebaseBundle);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.view_subreddit_submissions_coordinator_layout);

        if (fragment == null) {
            fragment = new ViewSubredditActivityFragment();

            Bundle viewSubredditsListFragmentBundle = new Bundle();
            viewSubredditsListFragmentBundle.putString(getString(R.string.bundle_key_selected_subreddit), mSelectedSubreddit);
            fragment.setArguments(viewSubredditsListFragmentBundle);

            fm.beginTransaction().add(R.id.view_subreddit_submissions_coordinator_layout, fragment).commit();
        }

//      if device is tablet, initiate tablet view with subreddit submissions on the left, selected submission on the right
        if (mIsTablet) {
            Fragment submissionFragment = new ViewSubmissionActivityFragment();

            Bundle viewSubmissionFragmentBundle = new Bundle();
            viewSubmissionFragmentBundle.putParcelable(getString(R.string.bundle_key_selected_submission), mSelectedSubmission);
            submissionFragment.setArguments(viewSubmissionFragmentBundle);

            fm.beginTransaction().replace(R.id.selected_subreddit_submission_frame, submissionFragment, "SUBREDDIT_LISTING_TAG").commit();
        }

        Toolbar fragmentToolbar = (Toolbar) findViewById(R.id.toolbar_viewSubreddit);
        setSupportActionBar(fragmentToolbar);
        ActionBar newBar = getSupportActionBar();
        String formattedTitleString = "r/" + mSelectedSubreddit;
        newBar.setTitle(formattedTitleString);
        newBar.setDisplayShowHomeEnabled(true);
        newBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSubmissionSelected(SubredditSubmission subredditSubmission) {
        if (mIsTablet) {
            Fragment submissionFragment = new ViewSubmissionActivityFragment();

            Bundle viewSubmissionFragmentBundle = new Bundle();
            viewSubmissionFragmentBundle.putParcelable(getString(R.string.bundle_key_selected_submission), subredditSubmission);
            submissionFragment.setArguments(viewSubmissionFragmentBundle);

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.selected_subreddit_submission_frame, submissionFragment, "SUBREDDIT_LISTING_TAG").commit();
        } else {
            Intent viewSubmissionIntent = new Intent(this, ViewSubmissionActivity.class);
            viewSubmissionIntent.putExtra(getString(R.string.intent_extra_key_selected_submission), subredditSubmission);
            startActivity(viewSubmissionIntent);
        }
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