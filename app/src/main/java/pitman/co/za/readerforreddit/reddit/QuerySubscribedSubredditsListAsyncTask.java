package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.EmbeddedMedia;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubmissionPreview;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;
import java.util.List;

import pitman.co.za.readerforreddit.MainActivityFragment;
import pitman.co.za.readerforreddit.R;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

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

    protected void onProgressUpdate(Integer... progress) {
        // Update the progress bar on dialog
        if (!isCancelled()) {
            mMainActivityFragment.updateProgressBar(progress[0]);
        } else {
            mMainActivityFragment.dismissProgressBar();
        }
    }

    @Override
    protected void onPostExecute(List<Listing<Submission>> result) {
        super.onPostExecute(result);

        if (!isCancelled()) {
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

                    if (mMainActivityFragment.getString(R.string.jraw_link).equals(submission.getPostHint())) {
                        subredditSubmission.setLinkUrl(submission.getUrl());
                    }

                    subredditSubmissions.add(subredditSubmission);
                }
            }

            mMainActivityFragment.generateSubredditSubmissionsAdapterWithData(subredditSubmissions);
        }
        mMainActivityFragment.dismissProgressBar();
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

        RedditClient redditClient = new RedditClientCreator().getRedditClient();

        ArrayList<String> subreddits = strings[0];
        List<Listing<Submission>> polledSubredditData = new ArrayList<>();

        for (String subreddit : subreddits) {
            if (!isCancelled()) {
                polledSubredditData.add(pollSubreddit(redditClient, subreddit, 15));
            } else {
                mMainActivityFragment.dismissProgressBar();
                return null;
            }
        }

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
