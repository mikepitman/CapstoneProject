package za.co.pitman.readerforreddit.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import za.co.pitman.readerforreddit.domainObjects.SubmissionComment;
import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;

@Dao
public interface SubredditSubmissionDao {

    @Query("delete from subreddit_submission where subreddit = :subreddit")
    void deleteSubreddit(String subreddit);

    @Query("delete from submission_comment")
    void deleteComments();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSubmissions(SubredditSubmission[] subredditSubmission);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveComments(SubmissionComment[] comments);

    @Transaction
    @Query("select * from subreddit_submission where subreddit in (:subreddits) order by subreddit, commentCount")
    LiveData<List<SubredditSubmission>> getSubredditSubmissions(String[] subreddits);

    @Query("select * from subreddit_submission where subreddit in (:subreddits) order by subreddit, commentCount")
    List<SubredditSubmission> getStaticSubredditSubmissions(String[] subreddits);

    @Transaction
    @Query("select * from subreddit_submission where subreddit = :subreddit")
    LiveData<List<SubredditSubmission>> getSubmissionsForSubreddit(String subreddit);

    @Query("select * from subreddit_submission where redditId = :redditId")
    SubredditSubmission getSubredditSubmission(String redditId);

    @Query("select * from submission_comment where submissionId = :submissionId")
    LiveData<List<SubmissionComment>> getCommentsForSubredditSubmission(String submissionId);

    @Query("select * from submission_comment where submissionId = :submissionId order by submissionId desc limit 1")
    SubmissionComment getFirstComment(String submissionId);
}