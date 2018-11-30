package za.co.pitman.readerforreddit.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import za.co.pitman.readerforreddit.domainObjects.SubmissionComment;
import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#8
 * */

public class SubredditSubmissionRepository {

    private static String LOG_TAG = SubredditSubmissionRepository.class.getSimpleName();
    private SubredditSubmissionDao mSubredditSubmissionDao;
    private LiveData<List<SubredditSubmission>> mAllSubredditSubmissions;
    private LiveData<List<SubredditSubmission>> mSubmissionsForSubreddit;
    private LiveData<List<SubmissionComment>> mSubmissionComments;

    SubredditSubmissionRepository(Application application) {
        SubredditSubmissionDatabase db = SubredditSubmissionDatabase.getDatabase(application);
        mSubredditSubmissionDao = db.mSubredditSubmissionDao();
//        mAllSubredditSubmissions = mSubredditSubmissionDao.getSubredditSubmissions();
    }

    // Stored recipes are static, but list may have recipes added as new recipes added to json listing
    LiveData<List<SubredditSubmission>> getAllSubredditSubmissions(ArrayList<String> subreddits) {
//        return mAllSubredditSubmissions;
        return mSubredditSubmissionDao.getSubredditSubmissions(subreddits.toArray(new String[subreddits.size()]));
    }

    LiveData<List<SubredditSubmission>> getSubmissionsForSubreddit(String subreddit) {
        mSubmissionsForSubreddit = mSubredditSubmissionDao.getSubmissionsForSubreddit(subreddit);
        return mSubmissionsForSubreddit;
    }

    public LiveData<List<SubmissionComment>> getSubmissionComments(final SubredditSubmission subredditSubmission) {
        /* https://medium.freecodecamp.org/room-sqlite-beginner-tutorial-2e725e47bfab */
        mSubmissionComments = mSubredditSubmissionDao.getCommentsForSubredditSubmission(subredditSubmission.getRedditId());
        return mSubmissionComments;
    }

    public void insert(List<SubredditSubmission> subredditSubmissions) {
        // delete subreddit submissions, to avoid stale subreddits hanging around

        new insertSubredditSubmissionsAsyncTask(mSubredditSubmissionDao).
                execute(subredditSubmissions.toArray(new SubredditSubmission[subredditSubmissions.size()]));
    }

    public void insertComments(List<SubmissionComment> submissionComments) {
        // delete subreddit submission comments, to avoid comment from stale subreddits hanging around
        new insertSubmissionCommentAsyncTask(mSubredditSubmissionDao).
                execute(submissionComments.toArray(new SubmissionComment[submissionComments.size()]));
    }

    public void deleteSubreddit(String subreddit) {
        new deleteSubredditAsyncTask(mSubredditSubmissionDao).execute(subreddit);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static class insertSubredditSubmissionsAsyncTask extends AsyncTask<SubredditSubmission[], Void, Void> {
        private SubredditSubmissionDao mAsyncTaskDao;

        insertSubredditSubmissionsAsyncTask(SubredditSubmissionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubredditSubmission[]... params) throws SQLiteConstraintException {
            mAsyncTaskDao.saveSubmissions(params[0]);
            return null;
        }
    }

    private static class insertSubmissionCommentAsyncTask extends AsyncTask<SubmissionComment[], Void, Void> {
        private SubredditSubmissionDao mAsyncTaskDao;

        insertSubmissionCommentAsyncTask(SubredditSubmissionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubmissionComment[]... params) throws SQLiteConstraintException {
            mAsyncTaskDao.deleteComments();
            mAsyncTaskDao.saveComments(params[0]);
            return null;
        }
    }

    private static class deleteSubredditAsyncTask extends AsyncTask<String, Void, Void> {
        private SubredditSubmissionDao mAsyncTaskDao;

        deleteSubredditAsyncTask(SubredditSubmissionDao dao) { mAsyncTaskDao = dao; }

        @Override
        protected Void doInBackground(final String... params) throws SQLiteConstraintException {
            mAsyncTaskDao.deleteSubreddit(params[0]);
            return null;
        }
    }
}

