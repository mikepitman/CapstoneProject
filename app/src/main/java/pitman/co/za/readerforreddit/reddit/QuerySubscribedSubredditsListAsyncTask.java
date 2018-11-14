package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.EmbeddedMedia;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubmissionPreview;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pitman.co.za.readerforreddit.MainActivityFragment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

// reddit client ID: CGG1OAPhpEmzgw
public class QuerySubscribedSubredditsListAsyncTask extends AsyncTask<ArrayList<String>, Void, List<Listing<Submission>>> {

    private MainActivityFragment mMainActivityFragment;
    private static String LOG_TAG = QuerySubscribedSubredditsListAsyncTask.class.getCanonicalName();

    // Set mMainActivity for callback
    public QuerySubscribedSubredditsListAsyncTask(MainActivityFragment mainActivityFragment) {
        this.mMainActivityFragment = mainActivityFragment;
    }

    @Override
    protected void onPreExecute() {
        mMainActivityFragment.showProgressBar();
    }

    protected void onProgressUpdate(Integer... progress){
        // Update the progress bar on dialog
        mMainActivityFragment.updateProgressBar(progress[0]);
    }

    @Override
    protected void onPostExecute(List<Listing<Submission>> result) {
        super.onPostExecute(result);

        ArrayList<SubredditSubmission> subredditSubmissions = new ArrayList<>();
        for (Listing<Submission> submissionListing : result) {
            for (Submission submission : submissionListing) {
                SubredditSubmission subredditSubmission = new SubredditSubmission(
                        submission.getId(),
                        submission.getSubreddit(),
                        submission.getAuthor(),
                        submission.getTitle(),
                        submission.getScore(),
                        submission.getCommentCount(),
                        submission.getPostHint(),
                        submission.isSelfPost(),
                        submission.getSelfText(),
                        submission.hasThumbnail(),
                        submission.getThumbnail());

                SubmissionPreview preview = submission.getPreview();
                if (preview != null && preview.getImages().size() > 0) {
                    SubmissionPreview.Variation variation = preview.getImages().get(0).getSource();
                    subredditSubmission.addPreview(
                            variation.getUrl(),
                            variation.getHeight(),
                            variation.getWidth());
                }

                EmbeddedMedia media = submission.getEmbeddedMedia();
                if (media != null && media.getRedditVideo() != null) {
                    subredditSubmission.addRedditVideo(
                            media.getRedditVideo().getFallbackUrl(),
                            media.getRedditVideo().getHeight(),
                            media.getRedditVideo().getWidth());
                }

                if ("link".equals(submission.getPostHint())) {
                    subredditSubmission.setLinkUrl(submission.getUrl());
                }

                subredditSubmissions.add(subredditSubmission);
                Log.d(LOG_TAG, "title: " + submission.getTitle() + "  posthint: " + submission.getPostHint());
            }
        }

        mMainActivityFragment.dismissProgressBar();
        mMainActivityFragment.generateSubredditSubmissionsAdapterWithData(subredditSubmissions);
    }

    /* Seems it's not possible to have a single query for multiple subreddits, where n posts are retrieved for each subreddit in accordance with
     * criteria, from the API, and using the JRAW wrapper
     * eg if given 3 subreddits and limit of 5 posts sorted by 'top', it'll be the top 5 posts out of all 3 subreddits, rather than 5 from each
     * eg if given 3 subreddits and limit of 5 posts sorted by 'new', it'll be the 5 newest posts from all 3 subreddits, rather than 5 from each
     * Top-rated posts seem most pertinent for the app and avoiding bias towards high-traffic subreddits is desirable.
     * In absence of option for single query returning n sorted posts for each of x subreddits, x queries will be sent for n sorted posts
     * */
    @Override
    protected List<Listing<Submission>> doInBackground(ArrayList<String>... strings) {

        // https://mattbdean.gitbooks.io/jraw/quickstart.html
        UserAgent userAgent = new UserAgent("android", "za.co.pitman.readerForReddit", "v0.1", "narfice");
        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        Credentials credentials = Credentials.userlessApp("CGG1OAPhpEmzgw", UUID.randomUUID());
        RedditClient redditClient = OAuthHelper.automatic(adapter, credentials);

        ArrayList<String> subreddits = strings[0];

        List<Listing<Submission>> polledSubredditData = new ArrayList<>();
        for (String subreddit : subreddits) {
            polledSubredditData.add(pollSubreddit(redditClient, subreddit, 5));
        }

//        Log.d(LOG_TAG, "Number of entries in polledSubredditData: " + polledSubredditData.size());
//        for (Listing<Submission> listing : polledSubredditData) {
//            Log.d(LOG_TAG, "size: " + listing.size() + ";  Subreddit: " + listing.get(0).getSubreddit());
//        }

        return polledSubredditData;
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
