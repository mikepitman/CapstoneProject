package pitman.co.za.readerforreddit.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import java.util.List;

import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#8
 * */

public class SubredditSubmissionRepository {

    private static String LOG_TAG = SubredditSubmissionRepository.class.getSimpleName();
    private SubredditSubmissionDao mSubredditSubmissionDao;
    private LiveData<List<SubredditSubmission>> mAllSubredditSubmissions;
    private LiveData<List<SubmissionComment>> mSubmissionComments;

    SubredditSubmissionRepository(Application application) {
        SubredditSubmissionDatabase db = SubredditSubmissionDatabase.getDatabase(application);
        mSubredditSubmissionDao = db.mSubredditSubmissionDao();
        mAllSubredditSubmissions = mSubredditSubmissionDao.getSubredditSubmissions();
    }

    // Stored recipes are static, but list may have recipes added as new recipes added to json listing
    LiveData<List<SubredditSubmission>> getAllSubredditSubmissions() {
        return mAllSubredditSubmissions;
    }

    public LiveData<List<SubmissionComment>> getSubmissionComments(final SubredditSubmission subredditSubmission) {
        /* Example used for off-thread retrieval of ingredients and steps for a selected recipe
         * https://medium.freecodecamp.org/room-sqlite-beginner-tutorial-2e725e47bfab
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSubmissionComments = mSubredditSubmissionDao.getCommentsForSubredditSubmission(subredditSubmission.getRedditId());
            }
        }).start();
        return mSubmissionComments;
    }

    // todo: would be better to pass list<> than create a new asyncTask for each element in list
    public void insert(List<SubredditSubmission> subredditSubmissions) {
        for (SubredditSubmission submission : subredditSubmissions) {
            new insertSubredditSubmissionsAsyncTask(mSubredditSubmissionDao).execute(submission);
        }
    }

    public void insertComments(List<SubmissionComment> submissionComments) {
        for (SubmissionComment comment : submissionComments) {
            new insertSubmissionCommentAsyncTask(mSubredditSubmissionDao).execute(comment);
        }
    }

    private static class insertSubredditSubmissionsAsyncTask extends AsyncTask<SubredditSubmission, Void, Void> {
        private SubredditSubmissionDao mAsyncTaskDao;

        insertSubredditSubmissionsAsyncTask(SubredditSubmissionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubredditSubmission... params) throws SQLiteConstraintException {
            mAsyncTaskDao.saveSubmission(params[0]);
            return null;
        }
    }

    private static class insertSubmissionCommentAsyncTask extends AsyncTask<SubmissionComment, Void, Void> {
        private SubredditSubmissionDao mAsyncTaskDao;

        insertSubmissionCommentAsyncTask(SubredditSubmissionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubmissionComment... params) throws SQLiteConstraintException {
            mAsyncTaskDao.saveComment(params[0]);
            return null;
        }
    }
}

