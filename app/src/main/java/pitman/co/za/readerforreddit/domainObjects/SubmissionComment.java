package pitman.co.za.readerforreddit.domainObjects;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import pitman.co.za.readerforreddit.R;


@Entity(tableName = "submission_comment",
        primaryKeys = {"submissionId", "commentNumber"})
public class SubmissionComment implements Parcelable {

    /* The combination of the comment order (commentNumber, assigned consecutively when iterating through commentTree)
     * and commentDepth should be sufficient to order comments with spacing.
     * API docs aren't super enlightening re structure of RootCommentNode, debugger much more helpful in determining attributes worth saving
     */

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

    @Ignore
    public String getCommentAge() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(getString(R.string.submission_comment_date_format));    // reddit timestamp format is painful!
        LocalDateTime timestamp = formatter.parseLocalDateTime(whenLogged);
        Period commentAge = Period.fieldDifference(timestamp, new LocalDateTime());

        if (commentAge.getMonths() > 0) {
            return String.format(commentAge.getMonths() + getString(R.string.submission_comment_months), 
                    commentAge.getMonths() > 1 ? getString(R.string.submission_comment_plural) : getString(R.string.submission_comment_blank));

        } else if (commentAge.getWeeks() > 0) {
            return String.format(commentAge.getWeeks() + getString(R.string.submission_comment_weeks), 
                    commentAge.getWeeks() > 1 ? getString(R.string.submission_comment_plural) : getString(R.string.submission_comment_blank));

        } else if (commentAge.getDays() > 0) {
            return String.format(commentAge.getDays() + getString(R.string.submission_comment_days), 
                    commentAge.getDays() > 1 ? getString(R.string.submission_comment_plural) : getString(R.string.submission_comment_blank));

        } else if (commentAge.getHours() > 0) {
            return String.format(commentAge.getHours() + getString(R.string.submission_comment_hours), 
                    commentAge.getHours() > 1 ? getString(R.string.submission_comment_plural) : getString(R.string.submission_comment_blank));

        } else if (commentAge.getMinutes() > 0) {
            return String.format(commentAge.getMinutes() + getString(R.string.submission_comment_minutes), 
                    commentAge.getMinutes() > 1 ? getString(R.string.submission_comment_plural) : getString(R.string.submission_comment_blank));
        }

        return getString(R.string.submission_comment_indeterminate_period);
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
