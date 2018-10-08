package pitman.co.za.readerforreddit.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#8
 * */


public class SubredditSubmissionRepository {

    private static String LOG_TAG = SubredditSubmissionRepository.class.getSimpleName();
    private SubredditSubmissionDao mSubredditSubmissionDao;
    private LiveData<List<SubredditSubmission>> mAllSubredditSubmissions;

    SubredditSubmissionRepository(Application application) {
        SubredditSubmissionDatabase db = SubredditSubmissionDatabase.getDatabase(application);
        mSubredditSubmissionDao = db.mSubredditSubmissionDao();
        mAllSubredditSubmissions = mSubredditSubmissionDao.getSubredditSubmissions();
    }

    // Stored recipes are static, but list may have recipes added as new recipes added to json listing
    LiveData<List<SubredditSubmission>> getAllSubredditSubmissions() {
        return mAllSubredditSubmissions;
    }

    public void getSubmissionComments(final SubredditSubmission subredditSubmission) {
        /* Example used for off-thread retrieval of ingredients and steps for a selected recipe
         * https://medium.freecodecamp.org/room-sqlite-beginner-tutorial-2e725e47bfab
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                // todo: complete this
//                subredditSubmission.setComments((ArrayList<SubmissionComment>) mSubredditSubmissionDao.getComments(subredditSubmission.getRedditId()));
            }
        }).start();
    }

    public void insert(ArrayList<SubredditSubmission> subredditSubmissions) {
        for (SubredditSubmission submission : subredditSubmissions) {
            new insertRecipeAsyncTask(mSubredditSubmissionDao).execute(submission);
        }
    }

    private static class insertRecipeAsyncTask extends AsyncTask<SubredditSubmission, Void, Void> {

        private SubredditSubmissionDao mAsyncTaskDao;

        insertRecipeAsyncTask(SubredditSubmissionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubredditSubmission... params) throws SQLiteConstraintException {
            SubredditSubmission submission = params[0];
            // todo: complete this section
            // Only add recipe if it isn't already in the database (recipe name used as unique primary key)
//            if (mAsyncTaskDao.getRecipe(recipe.getName()) == null) {
//                mAsyncTaskDao.saveRecipe(recipe);
//                mAsyncTaskDao.saveRecipeSteps(recipe.getSteps());
//                mAsyncTaskDao.saveIngredients(recipe.getIngredients());
//            }
            return null;
        }
    }
}

