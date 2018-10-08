package pitman.co.za.readerforreddit.domainObjects;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(tableName = "submission_comment",
        primaryKeys = {"redditId", "commentId"})
public class SubmissionComment implements Parcelable {

    /* Todo: read through https://jitpack.io/com/github/mattbdean/JRAW/v1.1.0/javadoc/net/dean/jraw/models/PublicContribution.html
     * to understand comment trees, and how best to store each as a discrete database element
     */

    @NonNull
    private String redditId;
    @NonNull
    private String commentId;

    @NonNull
    public String getRedditId() {
        return redditId;
    }

    @NonNull
    public String getCommentId() {
        return commentId;
    }

    @Ignore
    public SubmissionComment() {
    }

    public SubmissionComment(String redditId,
                             String commentId) {
        this.redditId = redditId;
        this.commentId = commentId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public SubmissionComment(Parcel in) {
    }

    public static final Parcelable.Creator<SubmissionComment> CREATOR = new Parcelable.Creator<SubmissionComment>() {
        public SubmissionComment createFromParcel(Parcel source) {
            return new SubmissionComment(source);
        }

        public SubmissionComment[] newArray(int size) {
            return new SubmissionComment[size];
        }
    };
}
