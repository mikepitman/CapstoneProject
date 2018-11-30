package za.co.pitman.readerforreddit.reddit;

import android.os.AsyncTask;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Subreddit;

import za.co.pitman.readerforreddit.SelectSubredditsActivityFragment;

/* AsyncTask to poll Reddit for subreddit using a user-supplied subreddit name
 * AsyncTask to be called from Activity from where user is able to select their subreddits to track
 * */

public class QuerySubredditExistenceAsyncTask extends AsyncTask<String, Void, String> {

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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (!isCancelled()) {
            mSelectSubredditsActivityFragment.subredditVerified(result);
        }
        mSelectSubredditsActivityFragment.dismissProgressBar();
    }

    protected void onProgressUpdate(Integer... progress) {
        // Update the progress bar on dialog
        if (!isCancelled()) {
            mSelectSubredditsActivityFragment.updateProgressBar(progress[0]);
        } else {
            mSelectSubredditsActivityFragment.dismissProgressBar();
        }
    }

    @Override
    protected String doInBackground(String... strings) {

        RedditClient redditClient = new RedditClientCreator().getRedditClient();

        String reddit = strings[0];

        if (!isCancelled()) {
            try {
                Subreddit subredditDetails = redditClient.subreddit(reddit).about();
                // https://github.com/mattbdean/JRAW/issues/243
                // for some reason, NPE is occuring before ApiException can be thrown, for non-existent subreddits
                if (subredditDetails.isNsfw()) {
                    return "NSFW";
                }
            } catch (ApiException | NullPointerException | NetworkException e) {
                return "NONEXISTENT";
            }
        } else {
            mSelectSubredditsActivityFragment.dismissProgressBar();
            return null;
        }

        return "EXISTS";
    }
}
