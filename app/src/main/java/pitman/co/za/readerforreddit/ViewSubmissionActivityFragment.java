package pitman.co.za.readerforreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;
import pitman.co.za.readerforreddit.reddit.QuerySubredditSubmissionCommentsAsyncTask;
import pitman.co.za.readerforreddit.room.SubredditSubmissionViewModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class ViewSubmissionActivityFragment extends Fragment {

    private static String LOG_TAG = ViewSubmissionActivityFragment.class.getSimpleName();
    private SubredditSubmission mSelectedSubmission;
    private SubredditSubmissionViewModel mSubmissionViewModel;
    private ArrayList<SubmissionComment> mSubmissionComments;
    private RecyclerView mSubmissionCommentsRecyclerView;
    private View rootView;
    private SubmissionCommentsAdapter mAdapter;

    public ViewSubmissionActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("selectedSubmission", mSelectedSubmission);
        outState.putParcelableArrayList("submissionComments", mSubmissionComments);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.mSelectedSubmission = savedInstanceState.getParcelable("selectedSubmission");
            this.mSubmissionComments = savedInstanceState.getParcelableArrayList("submissionComments");
        } else {
            if (this.getArguments() != null) {
                Bundle bundle = this.getArguments();
                mSelectedSubmission = bundle.getParcelable("selectedSubmission");
                Log.d(LOG_TAG, "selected submission " + mSelectedSubmission.getTitle());
            }
        }

        // Launch asyncTask to retrieve comments for selected submission
        new QuerySubredditSubmissionCommentsAsyncTask(this).execute(mSelectedSubmission.getRedditId());

        // https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#13
        mSubmissionViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);
        mSubmissionViewModel.getSubmissionComments(mSelectedSubmission).observe(this, new Observer<List<SubmissionComment>>() {
            @Override
            public void onChanged(@Nullable final List<SubmissionComment> comments) {
                mAdapter.swapData(comments);
            }
        });
        mAdapter = new SubmissionCommentsAdapter(mSubmissionViewModel.getSubmissionComments(mSelectedSubmission).getValue());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_view_submission, container, false);

        mSubmissionCommentsRecyclerView = (RecyclerView) rootView.findViewById(R.id.submission_comment_recyclerview);
        mSubmissionCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubmissionCommentsRecyclerView.setAdapter(mAdapter);

        bindSubmissionViews();

        return rootView;
    }

    private void bindSubmissionViews() {
        if (rootView == null) {
            return;
        }

        TextView authorTextView = (TextView) rootView.findViewById(R.id.submission_author);
        TextView titleTextView = (TextView) rootView.findViewById(R.id.submission_title);
        TextView selfPostTextView = (TextView) rootView.findViewById(R.id.submission_selfPostText);
        ImageView submissionImageView = (ImageView) rootView.findViewById(R.id.submission_image);

        String formattedAuthor = "u/" + mSelectedSubmission.getAuthor();
        authorTextView.setText(formattedAuthor);
        titleTextView.setText(mSelectedSubmission.getTitle());


        // Simplest type of submission - text only, no media - this special case is complete
        if (mSelectedSubmission.isSelfPost()) {
            selfPostTextView.setText(mSelectedSubmission.getSelfPost());
        } else {
            selfPostTextView.setVisibility(View.INVISIBLE);
        }

        // Picasso doesn't handle animated gifs at all :-)
        String postHint = mSelectedSubmission.getPostHint();
        // hosted:video --> video from reddit, url is in mSelectedSubmission.getVideoUrl
        // rich:video --> video hosted elsewhere(?)
        // link --> post basically contains just a link to share



        // Video stream hosted by reddit
        if (postHint.contains("video") && postHint.contains("hosted")) {


            Log.d(LOG_TAG, "video url loaded: " + mSelectedSubmission.getVideoUrl());
        }

        if (!mSelectedSubmission.getPreviewUrl().isEmpty()) {
            Uri imageUri = Uri.parse(mSelectedSubmission.getPreviewUrl());
//            Picasso.get().load(imageUri).fit().centerInside().into(submissionImageView);
            Picasso.get().load(imageUri).resize(mSelectedSubmission.getPreviewWidth(), mSelectedSubmission.getPreviewHeight()).into(submissionImageView);
//            Picasso.get().load(imageUri).into(submissionImageView);
            submissionImageView.setContentDescription(mSelectedSubmission.getTitle());
        } else {
            submissionImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void populateSubmissionCommentsAdapterWithData(ArrayList<SubmissionComment> retrievedSubmissionComments) {
        if (retrievedSubmissionComments != null) {
            mSubmissionViewModel.insertComments(retrievedSubmissionComments);
        }
    }

    /// todo: refactor the following for subreddit comments:
    private class SubmissionCommentsAdapter extends RecyclerView.Adapter<SubmissionCommentViewHolder> {

        private String LOG_TAG = SubmissionCommentViewHolder.class.getSimpleName();
        private List<SubmissionComment> mSubmissionComments;

        private SubmissionCommentsAdapter(List<SubmissionComment> submissionComments) {
            mSubmissionComments = submissionComments;
        }

        @Override
        public int getItemCount() {
            if (mSubmissionComments == null) {
                Log.d(LOG_TAG, "number of items is 0, this should not happen!");
                return 0;
            }
            return mSubmissionComments.size();
        }

        @Override
        public SubmissionCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View commentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.submission_comment_view, parent, false);
            return new SubmissionCommentViewHolder(commentView);
        }

        @Override
        public void onBindViewHolder(SubmissionCommentViewHolder holder, int position) {
            holder.bindSubmissionComment(mSubmissionComments.get(position));
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public void swapData(List<SubmissionComment> submissionComments) {
            int numberOfOldEntries = mSubmissionComments == null ? 0 : mSubmissionComments.size();
            if (mSubmissionComments != null) {
                mSubmissionComments.clear();
            } else {
                mSubmissionComments = new ArrayList<SubmissionComment>();
            }
            mSubmissionComments.addAll(submissionComments);
            notifyDataSetChanged();
        }
    }

    private class SubmissionCommentViewHolder extends RecyclerView.ViewHolder {

        private SubmissionComment mSubmissionComment;
        //        private TextView commentSpacer;
        private TextView commentAuthor;
        private TextView commentPoints;
        private TextView commentAge;
        private TextView submissionComment;

        SubmissionCommentViewHolder(View itemView) {
            super(itemView);
//            commentSpacer = (TextView) itemView.findViewById(R.id.comment_spacer);
            commentAuthor = (TextView) itemView.findViewById(R.id.comment_author);
            commentPoints = (TextView) itemView.findViewById(R.id.comment_points);
            commentAge = (TextView) itemView.findViewById(R.id.comment_age);
            submissionComment = (TextView) itemView.findViewById(R.id.submission_comment);
        }

        public void bindSubmissionComment(SubmissionComment submissionComment) {
            this.mSubmissionComment = submissionComment;

//            this.commentSpacer.setText("");
            String formattedCommentPoints = String.format(
                    String.valueOf(mSubmissionComment.getCommentScore()) + " point%s",
                    mSubmissionComment.getCommentScore() == 1 ? "" : "s");

            this.commentAuthor.setText(mSubmissionComment.getCommentAuthor());

            this.commentPoints.setText(formattedCommentPoints);
            this.commentAge.setText(mSubmissionComment.getCommentAge());
            this.submissionComment.setText(mSubmissionComment.getComment());
        }
    }
}