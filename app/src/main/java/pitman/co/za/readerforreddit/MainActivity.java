package pitman.co.za.readerforreddit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import pitman.co.za.readerforreddit.reddit.QuerySubscribedSubredditsListAsyncTask;

public class MainActivity extends AppCompatActivity {

private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_main);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, "Nokia7plus");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);

        new QuerySubscribedSubredditsListAsyncTask(this).execute();
    }
}
