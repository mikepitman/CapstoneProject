package za.co.pitman.readerforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;

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
        outState.putString(getString(R.string.save_instance_state_selected_subreddit_title), mSelectedSubreddit);
        outState.putParcelable(getString(R.string.save_instance_state_selected_submission), mSelectedSubmission);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsTablet = getResources().getBoolean(R.bool.is_tablet);

        if (savedInstanceState != null) {
            this.mSelectedSubreddit = savedInstanceState.getString(getString(R.string.save_instance_state_selected_subreddit_title));
            this.mSelectedSubmission = savedInstanceState.getParcelable(getString(R.string.save_instance_state_selected_submission));
        } else {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(getString(R.string.intent_extra_key_selected_submission))) {
                mSelectedSubmission = intent.getParcelableExtra(getString(R.string.intent_extra_key_selected_submission));
                mSelectedSubreddit = mSelectedSubmission.getSubreddit();
            }
        }

        setContentView(getLayoutResId());

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

            fm.beginTransaction().replace(R.id.selected_subreddit_submission_frame, submissionFragment, getString(R.string.subreddit_submission_tag)).commit();
        } else {
            // https://stackoverflow.com/questions/46313949/detail-fragment-re-starts-even-when-rotated-from-landscape-to-vertical-master
            Fragment submissionFragment = fm.findFragmentByTag(getString(R.string.subreddit_submission_tag));
            if (submissionFragment != null) {
                fm.beginTransaction().remove(submissionFragment).commit();
            }
        }

        Toolbar fragmentToolbar = (Toolbar) findViewById(R.id.toolbar_viewSubreddit);
        setSupportActionBar(fragmentToolbar);
        ActionBar newBar = getSupportActionBar();
        String formattedTitleString = getString(R.string.subreddit_prefix) + mSelectedSubreddit;
        newBar.setTitle(formattedTitleString);
        newBar.setDisplayShowHomeEnabled(true);
        newBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSubmissionSelected(SubredditSubmission subredditSubmission) {
        if (mIsTablet) {
            mSelectedSubmission = subredditSubmission;
            Fragment submissionFragment = new ViewSubmissionActivityFragment();

            Bundle viewSubmissionFragmentBundle = new Bundle();
            viewSubmissionFragmentBundle.putParcelable(getString(R.string.bundle_key_selected_submission), subredditSubmission);
            submissionFragment.setArguments(viewSubmissionFragmentBundle);

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.selected_subreddit_submission_frame, submissionFragment, getString(R.string.subreddit_listing_tag)).commit();
        } else {
            Intent viewSubmissionIntent = new Intent(this, ViewSubmissionActivity.class);
            viewSubmissionIntent.putExtra(getString(R.string.intent_extra_key_selected_submission), subredditSubmission);
            startActivity(viewSubmissionIntent);
        }
    }
}