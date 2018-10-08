package pitman.co.za.readerforreddit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pitman.co.za.readerforreddit.reddit.QuerySubscribedSubredditsListAsyncTask;

public class MainActivity extends AppCompatActivity {

    private static String subreddit1 = "Nokia7Plus";
    private static String subreddit2 = "techsupport";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new QuerySubscribedSubredditsListAsyncTask(this).execute();
    }
}
