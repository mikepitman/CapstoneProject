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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Set;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callbacks, ViewSubredditActivityFragment.Callbacks {

    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private static int RC_SIGN_IN = 9999;
    private static UtilityCode sUtilityCode;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GoogleSignInClient mGoogleSignInClient;
    private Set<String> mSelectedSubreddits;
    private boolean mIsTablet;
//    private DriveClient mDriveClient;
//    private DriveResourceClient mDriveResourceClient;
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

        // https://developers.google.com/identity/sign-in/android/sign-in?authuser=1
        // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN.
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        // Build a GoogleSignInClient with the options specified by gso.
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        mIsTablet = getResources().getBoolean(R.bool.is_tablet);

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

                    fm.beginTransaction().replace(R.id.selected_subreddits_frame, subredditFragment, "SUBREDDIT_LISTING_TAG").commit();
                }
            }
        }

        Toolbar fragmentToolbar = (Toolbar) findViewById(R.id.toolbar_mainActivity);
        setSupportActionBar(fragmentToolbar);
        ActionBar newBar = getSupportActionBar();
        newBar.setTitle(R.string.app_name);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if (account != null) {
//            Log.d(LOG_TAG, "User has logegd in before!");

            // todo: get stored information from the account, and use it instead of stored preferences.
            // and persist any changes to sotred preferences to this account
//            account.get
//        }
//    }

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
                firebaseBundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, "SELECT_SUBREDDITS");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, firebaseBundle);
                return true;

//            case R.id.google_sign_in:
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Google Sign-in ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Log.d(LOG_TAG, "logged in successfully");

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(LOG_TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Callbacks ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override   // callback from MainActivityFragment
    public void onSubredditSelected(SubredditSubmission subredditSubmission) {
        Log.d(LOG_TAG, "subreddit was selected: " + subredditSubmission.getTitle());

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
        Log.d(LOG_TAG, "user has selected subreddit submission, in mainActivity (tablet version): " + subredditSubmission.getTitle());

        /* Display the list of subreddit submissions on left pane, and the selected submission on the right pane
         * Hence, create an intent to ViewSubredditActivity, with the mIsTablet parameter passed in as true
         * */
        Intent viewSubredditIntent = new Intent(this, ViewSubredditActivity.class);
        viewSubredditIntent.putExtra(getString(R.string.intent_extra_key_selected_submission), subredditSubmission);
        viewSubredditIntent.putExtra(getString(R.string.intent_extra_key_is_tablet), mIsTablet);
        startActivity(viewSubredditIntent);
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
