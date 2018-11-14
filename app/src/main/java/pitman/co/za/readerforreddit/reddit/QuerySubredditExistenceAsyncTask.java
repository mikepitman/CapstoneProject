package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;

import java.util.UUID;

import pitman.co.za.readerforreddit.SelectSubredditsActivityFragment;

/* AsyncTask to poll Reddit for subreddit using a user-supplied subreddit name
 * AsyncTask to be called from Activity from where user is able to select their subreddits to track
 * */

public class QuerySubredditExistenceAsyncTask extends AsyncTask<String, Void, SubredditExistenceQueryResult> {

    private static String LOG_TAG = QuerySubredditExistenceAsyncTask.class.getCanonicalName();

    private SelectSubredditsActivityFragment mSelectSubredditsActivityFragment;

    public QuerySubredditExistenceAsyncTask(SelectSubredditsActivityFragment selectSubredditsActivityFragment) {
        this.mSelectSubredditsActivityFragment = selectSubredditsActivityFragment;
    }

    @Override
    protected void onPreExecute() {
        mSelectSubredditsActivityFragment.showProgressBar();
    }

    @Override
    protected void onPostExecute(SubredditExistenceQueryResult result) {
        super.onPostExecute(result);

        mSelectSubredditsActivityFragment.dismissProgressBar();
        mSelectSubredditsActivityFragment.subredditVerified(result);
    }

    protected void onProgressUpdate(Integer... progress){
        // Update the progress bar on dialog
        mSelectSubredditsActivityFragment.updateProgressBar(progress[0]);
    }

    @Override
    protected SubredditExistenceQueryResult doInBackground(String... strings) {

        // https://mattbdean.gitbooks.io/jraw/quickstart.html
        UserAgent userAgent = new UserAgent("android", "za.co.pitman.readerForReddit", "v0.1", "narfice");
        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        Credentials credentials = Credentials.userlessApp("CGG1OAPhpEmzgw", UUID.randomUUID());
        RedditClient redditClient = OAuthHelper.automatic(adapter, credentials);

        String reddit = strings[0];
        Log.d(LOG_TAG, "reddit queried: " + reddit);

        try {
            Subreddit subredditDetails = redditClient.subreddit(reddit).about();
            // https://github.com/mattbdean/JRAW/issues/243
            // for some reason, NPE is occuring before ApiException can be thrown, for non-existent subreddits
            Log.d(LOG_TAG, "comment");
            if (subredditDetails.isNsfw()) {
                return SubredditExistenceQueryResult.NSFW;
            }
        } catch (ApiException | NullPointerException | NetworkException e) {
            return SubredditExistenceQueryResult.NONEXISTENT;
        }

        return SubredditExistenceQueryResult.EXISTS;
    }
}
