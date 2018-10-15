package pitman.co.za.readerforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

public class ViewSubredditActivity extends AppCompatActivity implements ViewSubredditActivityFragment.Callbacks {

    private static String LOG_TAG = ViewSubredditActivity.class.getSimpleName();
    private String mSelectedSubreddit;

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_view_subreddit;      // returns de-referenced value, for different screen sizes - not strictly necessary here
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("selectedSubreddit", mSelectedSubreddit);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.mSelectedSubreddit = savedInstanceState.getString("selectedSubreddit");
        } else {
            Intent intent = getIntent();
            mSelectedSubreddit = intent.getStringExtra("selectedSubreddit");
            Log.d(LOG_TAG, "selected subreddit " + mSelectedSubreddit);
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
        Fragment fragment = fm.findFragmentById(R.id.view_subreddit_submissions_container);

        if (fragment == null) {
            fragment = new ViewSubredditActivityFragment();

            Bundle fragmentBundle = new Bundle();
            fragmentBundle.putString("selectedSubreddit", mSelectedSubreddit);
            fragment.setArguments(fragmentBundle);

            fm.beginTransaction().add(R.id.view_subreddit_submissions_container, fragment).commit();
        }
    }

    @Override
    public void onSubmissionSelected(SubredditSubmission subredditSubmission) {
        Log.d(LOG_TAG, "user has selected subreddit submission: " + subredditSubmission.getTitle());

        Intent viewSubmissionIntent = new Intent(this, ViewSubmissionActivity.class);
        viewSubmissionIntent.putExtra("selectedSubmission", subredditSubmission);
        startActivity(viewSubmissionIntent);
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
