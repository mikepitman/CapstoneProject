package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pitman.co.za.readerforreddit.MainActivity;

// reddit client ID: CGG1OAPhpEmzgw
public class QuerySubredditsAsyncTask extends AsyncTask<String, Void, String> {

    private MainActivity mMainActivity;
    private static String LOG_TAG = QuerySubredditsAsyncTask.class.getCanonicalName();

    // Set mMainActivity for callback
    public QuerySubredditsAsyncTask(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Log.d(LOG_TAG, "message");
    }

    /* Seems it's not possible to have a single query for multiple subreddits, where n posts are retrieved for each subreddit in accordance with
    * criteria, from the API, and using the JRAW wrapper
    * eg if given 3 subreddits and limit of 5 posts sorted by 'top', it'll be the top 5 posts out of all 3 subreddits, rather than 5 from each
    * eg if given 3 subreddits and limit of 5 posts sorted by 'new', it'll be the 5 newest posts from all 3 subreddits, rather than 5 from each
    * Top-rated posts seem most pertinent for the app and avoiding bias towards high-traffic subreddits is desirable.
    * In absence of option for single query returning n sorted posts for each of x subreddits, x queries will be sent for n sorted posts
    * */

    @Override
    protected String doInBackground(String... strings) {

        // https://mattbdean.gitbooks.io/jraw/quickstart.html
        UserAgent userAgent = new UserAgent("android", "za.co.pitman.readerForReddit", "v0.1", "narfice");
        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        Credentials credentials = Credentials.userlessApp("CGG1OAPhpEmzgw", UUID.randomUUID());
        RedditClient redditClient = OAuthHelper.automatic(adapter, credentials);

        ArrayList<String> subreddits = new ArrayList<>();
        subreddits.add("Nokia7Plus");
        subreddits.add("gifs");
        subreddits.add("Android");
        subreddits.add("science");

        List<Listing<Submission>> polledSubredditData = new ArrayList<>();
        for (String subreddit : subreddits) {
            polledSubredditData.add(pollSubreddit(redditClient, subreddit, 5));
        }

        Log.d(LOG_TAG, "Number of entries in polledSubredditData: " + polledSubredditData.size());
        for (Listing<Submission> listing : polledSubredditData) {
            Log.d(LOG_TAG, "size: " + listing.size());
            Log.d(LOG_TAG, "Subreddit: " + listing.get(0).getSubreddit());
        }

        return null;
    }

    private Listing<Submission> pollSubreddit(RedditClient redditClient, String subredditName, int limit) {
        DefaultPaginator<Submission> paginator = redditClient
                .subreddit(subredditName)
                .posts()
                .sorting(SubredditSort.TOP)
                .timePeriod(TimePeriod.WEEK)
                .limit(limit)
                .build();
        return paginator.next();
    }
}
