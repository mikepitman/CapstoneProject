package pitman.co.za.readerforreddit.domainObjects;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/* The members of this class comprise only those attributes shown to users in this app from a Submission object returned by Reddit API
 * Check implementation of domainObject classes in BakingApp for implementation guidance
 * */

@Entity(tableName = "subreddit_submission",
        primaryKeys = {"redditId", "subreddit"})
public class SubredditSubmission implements Parcelable {

    @NonNull
    private String redditId;
    @NonNull
    private String subreddit;
    private String author;
    private String title;
    private Integer submissionScore;
    private Integer commentCount;
    private String postHint;

    // Text-only posts indicated with flag below, content for post in String below
    private boolean isSelfPost;
    private String selfPost;

    private boolean hasThumbnail;
    private String thumbnail;

    @Ignore
    public SubredditSubmission() {
    }

    public SubredditSubmission(@NonNull String redditId,
                               @NonNull String subreddit,
                               String author,
                               String title,
                               Integer submissionScore,
                               Integer commentCount,
                               String postHint,

                               boolean isSelfPost,
                               String selfPost,

                               boolean hasThumbnail,
                               String thumbnail) {
        this.redditId = redditId;
        this.subreddit = subreddit;
        this.author = author;
        this.title = title;
        this.submissionScore = submissionScore;
        this.commentCount = commentCount;
        this.postHint = postHint;

        this.isSelfPost = isSelfPost;
        this.selfPost = selfPost;

        this.hasThumbnail = hasThumbnail;
        this.thumbnail = thumbnail;
    }

    @NonNull
    public String getRedditId() {
        return redditId;
    }

    @NonNull
    public String getSubreddit() {
        return subreddit;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public Integer getSubmissionScore() {
        return submissionScore;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public String getPostHint() {
        return postHint;
    }

    public boolean isSelfPost() {
        return isSelfPost;
    }

    public String getSelfPost() {
        return selfPost;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.redditId);
        parcel.writeString(this.subreddit);
        parcel.writeString(this.author);
        parcel.writeString(this.title);
        parcel.writeInt(this.submissionScore);
        parcel.writeInt(this.commentCount);
        parcel.writeString(this.postHint);
        parcel.writeInt(this.isSelfPost ? 1 : 0);
        parcel.writeString(this.selfPost);
        parcel.writeInt(this.hasThumbnail ? 1 : 0);
        parcel.writeString(this.thumbnail);
    }

    public SubredditSubmission(Parcel in) {
        this.redditId = in.readString();
        this.subreddit = in.readString();
        this.author = in.readString();
        this.title = in.readString();
        this.submissionScore = in.readInt();
        this.commentCount = in.readInt();
        this.postHint = in.readString();
        this.isSelfPost = (in.readInt() == 1);
        this.selfPost = in.readString();
        this.hasThumbnail = (in.readInt() == 1);
        this.thumbnail = in.readString();
    }

    public static final Parcelable.Creator<SubredditSubmission> CREATOR = new Parcelable.Creator<SubredditSubmission>() {
        public SubredditSubmission createFromParcel(Parcel source) {
            return new SubredditSubmission(source);
        }

        public SubredditSubmission[] newArray(int size) {
            return new SubredditSubmission[size];
        }
    };
}