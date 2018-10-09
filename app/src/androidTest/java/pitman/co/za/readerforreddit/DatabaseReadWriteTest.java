package pitman.co.za.readerforreddit;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;
import pitman.co.za.readerforreddit.room.SubredditSubmissionDao;
import pitman.co.za.readerforreddit.room.SubredditSubmissionDatabase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/*
 * Source example for this unit test:
 * https://developer.android.com/training/data-storage/room/testing-db
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseReadWriteTest {

    private SubredditSubmissionDao mSubmissionDao;
    private SubredditSubmissionDatabase mDb;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, SubredditSubmissionDatabase.class).build();
        mSubmissionDao = mDb.mSubredditSubmissionDao();
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    @Test
    public void saveRecipeAndRetrieve() throws Exception {
        SubredditSubmission submission = generateSubmission();
        mSubmissionDao.saveSubmission(submission);

        SubredditSubmission retrievedSubmission = mSubmissionDao.getSubredditSubmission("redditId");
        assertThat(retrievedSubmission.getRedditId(), equalTo(submission.getRedditId()));
        assertThat(retrievedSubmission.getAuthor(), equalTo(submission.getAuthor()));
    }

    @Test
    public void saveCommentAndRetrieve() throws Exception {
        SubmissionComment comment = generateComment();
        mSubmissionDao.saveComment(comment);

//        SubmissionComment retrievedComment = mSubmissionDao.getCommentsForSubredditSubmission("submissionId");
        SubmissionComment retrievedComment = mSubmissionDao.getFirstComment("submissionId");
        assertThat(retrievedComment.getCommentAuthor(), equalTo(comment.getCommentAuthor()));
        assertThat(retrievedComment.getComment(), equalTo(comment.getComment()));
    }

    private SubredditSubmission generateSubmission() {
        return new SubredditSubmission(
                "redditId",
                "subreddit",
                "Charles Dickens",
                "Nicholas Nickleby",
                100,
                true,
                "thumbnail");
    }

    private SubmissionComment generateComment() {
        return new SubmissionComment(
                "submissionId",
                "1",
                0,
                "Neil Armstrong",
                "One small step for a man",
                10,
                "2018-01-01");
    }
}

