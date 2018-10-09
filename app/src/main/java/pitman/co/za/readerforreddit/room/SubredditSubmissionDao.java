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
    void saveComment(SubmissionComment comment);

    @Transaction
    @Query("select * from subreddit_submission order by subreddit, commentCount")
    LiveData<List<SubredditSubmission>> getSubredditSubmissions();

    @Query("select * from subreddit_submission where redditId = :redditId")
    SubredditSubmission getSubredditSubmission(String redditId);

    @Query("select * from submission_comment where submissionId = :submissionId")
    LiveData<List<SubmissionComment>> getCommentsForSubredditSubmission(String submissionId);

    @Query("select * from submission_comment where submissionId = :submissionId order by submissionId desc limit 1")
    SubmissionComment getFirstComment(String submissionId);

    // todo: delete all subreddit_submission objects not in set of newly retrieved subredditSubmissions
    // todo: delete all submission_comment objects not linked to subreddit submissions (based on ID) in set or newly retrieved subredditSubmissions
    // todo: alternatively, drop all objects and insert new ones every time there's new data?
}