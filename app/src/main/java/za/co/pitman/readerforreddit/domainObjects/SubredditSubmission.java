package za.co.pitman.readerforreddit.domainObjects;

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

    private String previewUrl;
    private int previewHeight;
    private int previewWidth;

    private String videoUrl;
    private int videoHeight;
    private int videoWidth;

    private String linkUrl;

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

    public String getPreviewUrl() {
        return previewUrl;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    // these setters shouldn't be used, but rather the methods for setting all three values together
    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public void addPreview(String url, int height, int width) {
        this.previewUrl = url;
        this.previewHeight = height;
        this.previewWidth = width;
    }

    public void addRedditVideo(String url, int height, int width) {
        this.videoUrl = url;
        this.videoHeight = height;
        this.videoWidth = width;
    }

    public void setLinkUrl(String url) {
        this.linkUrl = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

//    public String getFormattedSubmissionScore() {
//        String formattedSubmissionScore = submissionScore.toString();
//        if (submissionScore > 999 && submissionScore < 10000) {
//             https://stackoverflow.com/questions/5195837/format-float-to-n-decimal-places
//            formattedSubmissionScore = String.format(java.util.Locale.US, "%.1f", (float) submissionScore / 1000);
//            formattedSubmissionScore += "k";
//        } else if (submissionScore > 9999) {
//            formattedSubmissionScore = String.valueOf(submissionScore / 1000);
//            formattedSubmissionScore += "k";
//        }
//        return formattedSubmissionScore;
//    }

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

        parcel.writeString(this.previewUrl);
        parcel.writeInt(this.previewHeight);
        parcel.writeInt(this.previewWidth);
        parcel.writeString(this.videoUrl);
        parcel.writeInt(this.videoHeight);
        parcel.writeInt(this.videoWidth);
        parcel.writeString(this.linkUrl);
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

        this.previewUrl = in.readString();
        this.previewHeight = in.readInt();
        this.previewWidth = in.readInt();
        this.videoUrl = in.readString();
        this.videoHeight = in.readInt();
        this.videoWidth = in.readInt();
        this.linkUrl = in.readString();
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