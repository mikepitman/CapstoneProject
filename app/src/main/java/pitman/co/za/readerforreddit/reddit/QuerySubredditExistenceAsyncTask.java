package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Subreddit;

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

        RedditClient redditClient = new RedditClientCreator().getRedditClient();

        String reddit = strings[0];

        try {
            Subreddit subredditDetails = redditClient.subreddit(reddit).about();
            // https://github.com/mattbdean/JRAW/issues/243
            // for some reason, NPE is occuring before ApiException can be thrown, for non-existent subreddits
            if (subredditDetails.isNsfw()) {
                return SubredditExistenceQueryResult.NSFW;
            }
        } catch (ApiException | NullPointerException | NetworkException e) {
            return SubredditExistenceQueryResult.NONEXISTENT;
        }

        return SubredditExistenceQueryResult.EXISTS;
    }
}
