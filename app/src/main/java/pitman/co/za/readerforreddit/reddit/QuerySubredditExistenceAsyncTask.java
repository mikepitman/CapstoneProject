package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;
import android.util.Log;

/* AsyncTask to poll Reddit for subreddit using a user-supplied subreddit name
* Reject if the subreddit classified as NSFW
* AsyncTask to be called from Activity from where user is able to select their subreddits to track
* */

public class QuerySubredditExistenceAsyncTask extends AsyncTask<String, Void, String> {

    private static String LOG_TAG = QuerySubredditExistenceAsyncTask.class.getCanonicalName();

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Log.d(LOG_TAG, "message");
    }

    @Override
    protected String doInBackground(String... strings) {
        return null;
    }
}
