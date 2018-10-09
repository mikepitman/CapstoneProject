package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.tree.RootCommentNode;

import java.util.UUID;

import pitman.co.za.readerforreddit.MainActivity;

public class QuerySubredditSubmissionCommentsAsyncTask extends AsyncTask<String, Void, String> {

    private MainActivity mMainActivity;
    private static String LOG_TAG = QuerySubredditSubmissionCommentsAsyncTask.class.getCanonicalName();

    // Set mMainActivity for callback
    public QuerySubredditSubmissionCommentsAsyncTask(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Log.d(LOG_TAG, "message");
    }

    @Override
    protected String doInBackground(String... strings) {

        // https://mattbdean.gitbooks.io/jraw/quickstart.html
        UserAgent userAgent = new UserAgent("android", "za.co.pitman.readerForReddit", "v0.1", "narfice");
        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        Credentials credentials = Credentials.userlessApp("CGG1OAPhpEmzgw", UUID.randomUUID());
        RedditClient redditClient = OAuthHelper.automatic(adapter, credentials);

        RootCommentNode root = redditClient.submission(strings[0]).comments();

        root.walkTree().iterator();

        return "string";
    }
}
