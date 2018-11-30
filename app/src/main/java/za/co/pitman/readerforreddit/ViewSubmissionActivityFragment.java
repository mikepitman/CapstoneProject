package za.co.pitman.readerforreddit;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import za.co.pitman.readerforreddit.domainObjects.SubmissionComment;
import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;
import za.co.pitman.readerforreddit.reddit.QuerySubredditSubmissionCommentsAsyncTask;
import za.co.pitman.readerforreddit.room.SubredditSubmissionViewModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class ViewSubmissionActivityFragment extends Fragment implements View.OnClickListener {

    private static String LOG_TAG = ViewSubmissionActivityFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;
    private SubredditSubmission mSelectedSubmission;
    private static UtilityCode sUtilityCode;
    private SubredditSubmissionViewModel mSubmissionViewModel;
    private ArrayList<SubmissionComment> mSubmissionComments;
    private View rootView;
    private CoordinatorLayout mCoordinatorLayout;
    private SubmissionCommentsAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private AsyncTask queryCommentsAsyncTask;
    private Parcelable state;
    private LinearLayoutManager mLinearLayoutManager;
    boolean queryRedditApi = false;

    //// Progress dialog code ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void updateProgressBar(Integer progress) {
        mProgressDialog.setProgress(progress);
    }

    public void dismissProgressBar() {
        mProgressDialog.dismiss();
    }

    public void showProgressBar() {
        mProgressDialog.show();
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.save_instance_state_selected_submission), mSelectedSubmission);
        outState.putParcelableArrayList(getString(R.string.save_instance_state_submission_comments), mSubmissionComments);
        outState.putParcelable(getString(R.string.save_instance_state_recyclerview_state), state);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sUtilityCode = new UtilityCode();
        mContext = this.getContext();
        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

        if (savedInstanceState != null) {
            this.mSelectedSubmission = savedInstanceState.getParcelable(getString(R.string.save_instance_state_selected_submission));
            this.mSubmissionComments = savedInstanceState.getParcelableArrayList(getString(R.string.save_instance_state_submission_comments));
            this.state = savedInstanceState.getParcelable(getString(R.string.save_instance_state_recyclerview_state));
        } else {
            if (this.getArguments() != null) {
                Bundle bundle = this.getArguments();
                mSelectedSubmission = bundle.getParcelable(getString(R.string.bundle_key_selected_submission));
                queryRedditApi = true;
            }
        }

        // https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#13
        mSubmissionViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);
        mSubmissionViewModel.getSubmissionComments(mSelectedSubmission).observe(this, new Observer<List<SubmissionComment>>() {
            @Override
            public void onChanged(@Nullable final List<SubmissionComment> comments) {
                mAdapter.swapData(comments);
                mLinearLayoutManager.onRestoreInstanceState(state);
            }
        });
        mAdapter = new SubmissionCommentsAdapter(mSubmissionViewModel.getSubmissionComments(mSelectedSubmission).getValue());

        // https://android--examples.blogspot.com/2017/02/android-asynctask-with-progress-dialog.html
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(this.getActivity());
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // Progress dialog horizontal style
        mProgressDialog.setTitle(getString(R.string.progress_dialog_submission_title));  // Progress dialog title
        mProgressDialog.setMessage(getString(R.string.progress_dialog_submission_message));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_view_submission, container, false);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.view_submission_fragment_container);

        RecyclerView mSubmissionCommentsRecyclerView = (RecyclerView) rootView.findViewById(R.id.submission_comment_recyclerview);
        mSubmissionCommentsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSubmissionCommentsRecyclerView.setAdapter(mAdapter);
        if (state != null) {
            mSubmissionCommentsRecyclerView.getLayoutManager().onRestoreInstanceState(state);
        }
        // don't want recyclerView to scroll independently of the nestedScrollView
        ViewCompat.setNestedScrollingEnabled(mSubmissionCommentsRecyclerView, false);

        bindSubmissionViews();

        if (queryRedditApi) {
            if (sUtilityCode.isNetworkAvailable(getActivity())) {
                queryCommentsAsyncTask =
                        new QuerySubredditSubmissionCommentsAsyncTask(this)
                                .execute(mSelectedSubmission.getRedditId());
                queryRedditApi = false;
            } else {
                Log.e(LOG_TAG, getString(R.string.error_network_connectivity));
                sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.no_network_connection_comment_query, mContext);
            }
        }

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
        TextView linkTextView = (TextView) rootView.findViewById(R.id.link_text);

        String formattedAuthor = getString(R.string.user_prefix) + mSelectedSubmission.getAuthor();
        authorTextView.setText(formattedAuthor);
        titleTextView.setText(mSelectedSubmission.getTitle());

        String postHint = mSelectedSubmission.getPostHint();
        if (mSelectedSubmission.isSelfPost()) {     // Simplest type of submission - text only, no media - this special case is complete
            selfPostTextView.setText(mSelectedSubmission.getSelfPost());
            submissionImageView.setVisibility(View.GONE);
            // postHint and preview will/(should) be null

        } else if (postHint != null) {  // Multimedia post - either just a link, reddit-hosted video, rich:video(? video hosted elsewhere?)
            // Remove the textView for selfPost from layout
            selfPostTextView.setVisibility(View.GONE);

            // Picasso doesn't handle animated gifs at all :-(

            if (postHint.contains(getString(R.string.jraw_link)) || postHint.equals(getString(R.string.jraw_rich_video)) || postHint.equals(getString(R.string.jraw_hosted_video))) {
                linkTextView.setText(mSelectedSubmission.getLinkUrl());
                linkTextView.setOnClickListener(this);

                String displayImageUrl = mSelectedSubmission.getPreviewUrl();
                if (displayImageUrl.isEmpty() && mSelectedSubmission.isHasThumbnail()) {
                    displayImageUrl = mSelectedSubmission.getThumbnail();
                }

                if (displayImageUrl != null && !displayImageUrl.isEmpty()) {
                    Uri imageUri = Uri.parse(mSelectedSubmission.getPreviewUrl());
                    Picasso.get().load(imageUri).resize(mSelectedSubmission.getPreviewWidth(), mSelectedSubmission.getPreviewHeight()).into(submissionImageView);
                    submissionImageView.setContentDescription(mSelectedSubmission.getTitle());
                } else {
                    submissionImageView.setVisibility(View.GONE);
                }
            }
        } else {
            Log.e(LOG_TAG, getString(R.string.error_posthint_null));
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.error_notification_general, mContext);
        }
    }

    // Link textView clicked, initiate intent to open app better suited to displaying the content
    @Override
    public void onClick(View view) {

        // https://developer.android.com/guide/components/intents-filters
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse(mSelectedSubmission.getLinkUrl()));

        Bundle firebaseBundle = new Bundle();
        // Verify that the intent will resolve to an activity
        if (sendIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(sendIntent);

            firebaseBundle.putString(FirebaseAnalytics.Param.LOCATION, mSelectedSubmission.getLinkUrl());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, firebaseBundle);
        } else {
            Log.e(LOG_TAG, getString(R.string.error_content_not_handled));
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.error_notification_no_implicit_intent_app, mContext);

            firebaseBundle.putString(getString(R.string.firebase_analytics_app_unavailable), mSelectedSubmission.getLinkUrl());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, firebaseBundle);
        }
    }

    public void populateSubmissionCommentsAdapterWithData(ArrayList<SubmissionComment> retrievedSubmissionComments) {
        if (retrievedSubmissionComments != null) {
            mSubmissionViewModel.insertComments(retrievedSubmissionComments);
        }
    }

    private class SubmissionCommentsAdapter extends RecyclerView.Adapter<SubmissionCommentViewHolder> {

        private String LOG_TAG = SubmissionCommentViewHolder.class.getSimpleName();
        private List<SubmissionComment> mSubmissionComments;

        private SubmissionCommentsAdapter(List<SubmissionComment> submissionComments) {
            mSubmissionComments = submissionComments;
        }

        @Override
        public int getItemCount() {
            if (mSubmissionComments == null) {
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
            commentAuthor = (TextView) itemView.findViewById(R.id.comment_author);
            commentPoints = (TextView) itemView.findViewById(R.id.comment_points);
            commentAge = (TextView) itemView.findViewById(R.id.comment_age);
            submissionComment = (TextView) itemView.findViewById(R.id.submission_comment);
        }

        public void bindSubmissionComment(SubmissionComment submissionComment) {
            this.mSubmissionComment = submissionComment;

            String formattedCommentPoints = String.format(
                    String.valueOf(mSubmissionComment.getCommentScore()) + getString(R.string.submission_comment_score_points),
                    mSubmissionComment.getCommentScore() == 1 ? getString(R.string.submission_comment_blank) : getString(R.string.submission_comment_plural));

            this.commentAuthor.setText(mSubmissionComment.getCommentAuthor());

            this.commentPoints.setText(formattedCommentPoints);
            String commentAge = sUtilityCode.getCommentAge(mContext, mSubmissionComment.getWhenLogged());
            this.commentAge.setText(commentAge);
            this.submissionComment.setText(mSubmissionComment.getComment());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_pause));
        state = mLinearLayoutManager.onSaveInstanceState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_destroy));

        if (queryCommentsAsyncTask != null && queryCommentsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            queryCommentsAsyncTask.cancel(true);
        }

        // Prevent window leaks from Dialog remaining open after fragment is removed
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}