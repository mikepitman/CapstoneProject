package pitman.co.za.readerforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

public class ViewSubmissionActivity extends AppCompatActivity {

    private static String LOG_TAG = ViewSubmissionActivity.class.getSimpleName();
    private SubredditSubmission mSelectedSubmission;
    private ArrayList<SubmissionComment> mSubmissionComments;

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_view_submission;      // returns de-referenced value, for different screen sizes - not strictly necessary here
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("submission", mSelectedSubmission);
        outState.putParcelableArrayList("submissionComments", mSubmissionComments);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mSelectedSubmission = savedInstanceState.getParcelable("submission");
            this.mSubmissionComments = savedInstanceState.getParcelableArrayList("submissionComments");
        } else {
            Intent intent = getIntent();
            mSelectedSubmission = intent.getParcelableExtra("selectedSubmission");
            Log.d(LOG_TAG, "selected submission " + mSelectedSubmission.getTitle());
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
            fragment = new ViewSubmissionActivityFragment();

            Bundle fragmentBundle = new Bundle();
            fragmentBundle.putParcelable("selectedSubmission", mSelectedSubmission);
            fragment.setArguments(fragmentBundle);

            fm.beginTransaction().add(R.id.view_subreddit_submissions_container, fragment).commit();
        }
    }

}

