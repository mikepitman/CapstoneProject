package pitman.co.za.readerforreddit.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

@Dao
public interface SubredditSubmissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSubmission(SubredditSubmission subredditSubmission);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveComment(List<SubmissionComment> comments);

    @Transaction
    @Query("select * from subreddit_submission order by subreddit, commentCount")
    LiveData<List<SubredditSubmission>> getSubredditSubmissions();

    @Query("select * from subreddit_submission where redditId = :redditId")
    SubredditSubmission getSubredditSubmission(String redditId);

    // todo: complete this as required
//    @Query("select * from SubmissionComment where SubredditSubmission = :parentSubmission")
//    List<SubmissionComment> getSubmissionComments(String subredditSubmission);

//    @Query("select * from recipe_step where parentRecipe = :parentRecipe order by id")
//    List<RecipeStep> getRecipeSteps(String parentRecipe);
}