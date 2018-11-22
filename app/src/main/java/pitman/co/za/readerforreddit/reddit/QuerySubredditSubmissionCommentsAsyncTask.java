package pitman.co.za.readerforreddit.reddit;

import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import pitman.co.za.readerforreddit.ViewSubmissionActivityFragment;
import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;

public class QuerySubredditSubmissionCommentsAsyncTask extends AsyncTask<String, Void, ArrayList<SubmissionComment>> {

    private ViewSubmissionActivityFragment mViewSubmissionActivityFragment;
    private static String LOG_TAG = QuerySubredditSubmissionCommentsAsyncTask.class.getCanonicalName();

    // Set mMainActivity for callback
    public QuerySubredditSubmissionCommentsAsyncTask(ViewSubmissionActivityFragment viewSubmissionActivityFragment) {
        this.mViewSubmissionActivityFragment = viewSubmissionActivityFragment;
    }

    @Override
    protected void onPreExecute() {
        mViewSubmissionActivityFragment.showProgressBar();
    }

    protected void onProgressUpdate(Integer... progress){
        // Update the progress bar on dialog
        if (!isCancelled()) {
            mViewSubmissionActivityFragment.updateProgressBar(progress[0]);
        } else {
            mViewSubmissionActivityFragment.dismissProgressBar();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<SubmissionComment> result) {
        super.onPostExecute(result);

        if (!isCancelled()) {
            Log.d(LOG_TAG, "number of comments: " + result.size());
            mViewSubmissionActivityFragment.dismissProgressBar();
            mViewSubmissionActivityFragment.populateSubmissionCommentsAdapterWithData(result);
        }
    }

    @Override
    protected ArrayList<SubmissionComment> doInBackground(String... strings) {

        // https://mattbdean.gitbooks.io/jraw/quickstart.html
        UserAgent userAgent = new UserAgent("android", "za.co.pitman.readerForReddit", "v0.1", "narfice");
        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        Credentials credentials = Credentials.userlessApp("CGG1OAPhpEmzgw", UUID.randomUUID());
        RedditClient redditClient = OAuthHelper.automatic(adapter, credentials);

        String submissionId = strings[0];
        RootCommentNode rootCommentNode;

        if (!isCancelled()) {
            rootCommentNode = redditClient.submission(submissionId).comments();
        } else {
            mViewSubmissionActivityFragment.dismissProgressBar();
            return null;
        }

        return unpackCommentNode(rootCommentNode, submissionId);
    }

    private ArrayList<SubmissionComment> unpackCommentNode(RootCommentNode rootNode, String submissionId) {

        ArrayList<SubmissionComment> commentsList = new ArrayList<>();

        // https://mattbdean.gitbooks.io/jraw/cookbook.html
        Iterator<CommentNode<PublicContribution<?>>> it = rootNode.walkTree().iterator();
        int commentCounter = 0;

        while (it.hasNext()) {
            // A PublicContribution is either a Submission or a Comment.
            CommentNode<?> commentNode = it.next();
            PublicContribution<?> commentNodeSubject = commentNode.getSubject();

            // Do something with each Submission/Comment
            if (commentNodeSubject.getBody() != null) {
                SubmissionComment comment = new SubmissionComment(
                        submissionId,
                        commentCounter,
                        commentNode.getDepth(),
                        commentNodeSubject.getAuthor(),
                        commentNodeSubject.getBody(),
                        commentNodeSubject.getScore(),
                        commentNodeSubject.getCreated().toString());
                commentsList.add(comment);
                commentCounter++;
            }

        }
        return commentsList;
    }
}
