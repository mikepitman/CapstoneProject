package za.co.pitman.readerforreddit.domainObjects;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


@Entity(tableName = "submission_comment",
        primaryKeys = {"submissionId", "commentNumber"})
public class SubmissionComment implements Parcelable {

    @NonNull
    private String submissionId;
    @NonNull
    private int commentNumber;
    private int commentDepth;
    private String commentAuthor;
    private String comment;
    private int commentScore;
    private String whenLogged;

    @NonNull
    public String getSubmissionId() {
        return submissionId;
    }

    public int getCommentNumber() {
        return commentNumber;
    }

    public int getCommentDepth() {
        return commentDepth;
    }

    public String getCommentAuthor() {
        return commentAuthor;
    }

    public String getComment() {
        return comment;
    }

    public int getCommentScore() {
        return commentScore;
    }

    public String getWhenLogged() {
        return whenLogged;
    }

    private String getString(int id) {
        return Resources.getSystem().getString(id);
    }

    @Ignore
    public SubmissionComment() {
    }

    public SubmissionComment(@NonNull String submissionId,
                             int commentNumber,
                             int commentDepth,
                             String commentAuthor,
                             String comment,
                             int commentScore,
                             String whenLogged) {
        this.submissionId = submissionId;
        this.commentNumber = commentNumber;
        this.commentDepth = commentDepth;
        this.commentAuthor = commentAuthor;
        this.comment = comment;
        this.commentScore = commentScore;
        this.whenLogged = whenLogged;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.submissionId);
        parcel.writeInt(this.commentNumber);
        parcel.writeInt(this.commentDepth);
        parcel.writeString(this.commentAuthor);
        parcel.writeString(this.comment);
        parcel.writeInt(this.commentScore);
        parcel.writeString(this.whenLogged);
    }

    public SubmissionComment(Parcel in) {
        this.submissionId = in.readString();
        this.commentNumber = in.readInt();
        this.commentDepth = in.readInt();
        this.commentAuthor = in.readString();
        this.comment = in.readString();
        this.commentScore = in.readInt();
        this.whenLogged = in.readString();
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
