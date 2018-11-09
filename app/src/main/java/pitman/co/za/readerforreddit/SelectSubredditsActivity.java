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

        Toolbar fragmentToolbar = (Toolbar) findViewById(R.id.toolbar_selectSubreddits);
        setSupportActionBar(fragmentToolbar);
        ActionBar newBar = getSupportActionBar();
        newBar.setTitle(R.string.toolbar_select_subreddits);
        newBar.setDisplayShowHomeEnabled(true);
        newBar.setDisplayHomeAsUpEnabled(true);
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