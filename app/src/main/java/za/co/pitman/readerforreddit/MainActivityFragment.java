package za.co.pitman.readerforreddit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;
import za.co.pitman.readerforreddit.reddit.QuerySubscribedSubredditsListAsyncTask;
import za.co.pitman.readerforreddit.room.SubredditSubmissionViewModel;
import za.co.pitman.readerforreddit.widget.ReaderForRedditWidgetProvider;

public class MainActivityFragment extends Fragment {

    private static String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static UtilityCode sUtilityCode;
    private Callbacks mCallbacks;
    private RecyclerView mSubredditSubmissionRecyclerView;
    private SubredditSubmissionViewModel mSubredditsViewModel;
    private View rootView;
    private static SubredditSubmissionCardAdapter mAdapter;
    private ArrayList<String> selectedSubreddits;
    private CoordinatorLayout mCoordinatorLayout;
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private boolean queryRedditApi = false;
    private Parcelable state;
    private LinearLayoutManager mLinearLayoutManager;
    private AsyncTask querySubredditsAsyncTask;

    //// Callbacks-related code //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public interface Callbacks {
        void onSubredditSelected(SubredditSubmission subredditSubmission);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

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
        outState.putStringArrayList(getString(R.string.save_instance_state_subreddits_arraylist), selectedSubreddits);
        outState.putParcelable(getString(R.string.save_instance_state_recyclerview_state), state);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sUtilityCode = new UtilityCode();
        mContext = this.getContext();
        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        if (savedInstanceState != null) {
            selectedSubreddits = savedInstanceState.getStringArrayList(getString(R.string.save_instance_state_subreddits_arraylist));
            state = savedInstanceState.getParcelable(getString(R.string.save_instance_state_recyclerview_state));

        } else if (this.getArguments() != null) {
            selectedSubreddits = this.getArguments().getStringArrayList(getString(R.string.bundle_key_selected_subreddits_list));
            queryRedditApi = true;
        }

        // https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#13
        mSubredditsViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);
        mSubredditsViewModel.getAllSubredditSubmissions(selectedSubreddits).observe(this, new Observer<List<SubredditSubmission>>() {
            @Override
            public void onChanged(@Nullable final List<SubredditSubmission> subreddits) {
                mAdapter.swapData(sUtilityCode.parseTopSubredditSubmissions(subreddits));
                mLinearLayoutManager.onRestoreInstanceState(state);
            }
        });
        List<SubredditSubmission> topSubredditSubmissions = sUtilityCode.parseTopSubredditSubmissions(mSubredditsViewModel.getAllSubredditSubmissions(selectedSubreddits).getValue());
        mAdapter = new SubredditSubmissionCardAdapter(topSubredditSubmissions);

        // https://android--examples.blogspot.com/2017/02/android-asynctask-with-progress-dialog.html
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(this.getActivity());
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // Progress dialog horizontal style
        mProgressDialog.setTitle(getString(R.string.progress_dialog_mainFragment_title));  // Progress dialog title
        mProgressDialog.setMessage(getString(R.string.progress_dialog_mainFragment_message));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.main_fragment_container);

        mSubredditSubmissionRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_main_subreddit_card_recyclerview);
        mSubredditSubmissionRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSubredditSubmissionRecyclerView.setAdapter(mAdapter);

        // Launch asyncTask to retrieve top submissions from selected subreddits - only if required
        if (queryRedditApi) {
            if (sUtilityCode.isNetworkAvailable(getActivity())) {
                querySubredditsAsyncTask = new QuerySubscribedSubredditsListAsyncTask(this).execute(selectedSubreddits);
                queryRedditApi = false;
            } else {
                Log.e(LOG_TAG, getString(R.string.error_network_connectivity));
                sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.no_network_connection, mContext);
            }
        }
        return rootView;
    }

    public void generateSubredditSubmissionsAdapterWithData(ArrayList<SubredditSubmission> retrievedSubredditSubmissions) {
        if (retrievedSubredditSubmissions != null) {
            mSubredditsViewModel.insert(retrievedSubredditSubmissions);

            // Update the widget with retrieved data
            // https://stackoverflow.com/questions/3455123/programmatically-update-widget-from-activity-service-receiver
            Intent updateWidgetIntent = new Intent(this.mContext, ReaderForRedditWidgetProvider.class);
            updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            this.mContext.sendBroadcast(updateWidgetIntent);
        }
    }

    private class SubredditSubmissionCardAdapter extends RecyclerView.Adapter<SubmissionCardViewHolder> {

        private String LOG_TAG = SubredditSubmissionCardAdapter.class.getSimpleName();
        private List<SubredditSubmission> mSubredditSubmissions;

        private SubredditSubmissionCardAdapter(List<SubredditSubmission> subredditSubmissions) {
            mSubredditSubmissions = subredditSubmissions;
        }

        @Override
        public int getItemCount() {
            if (mSubredditSubmissions == null) {
                return 0;
            }
            return mSubredditSubmissions.size();
        }

        @Override
        public SubmissionCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View recipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.subreddit_post_view, parent, false);
            return new SubmissionCardViewHolder(recipeView);
        }

        @Override
        public void onBindViewHolder(SubmissionCardViewHolder holder, int position) {
            holder.bindSubredditSubmission(mSubredditSubmissions.get(position));
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public void swapData(List<SubredditSubmission> subredditSubmissions) {
            int numberOfOldEntries = mSubredditSubmissions == null ? 0 : mSubredditSubmissions.size();
            if (mSubredditSubmissions != null) {
                mSubredditSubmissions.clear();
            } else {
                mSubredditSubmissions = new ArrayList<SubredditSubmission>();
            }
            mSubredditSubmissions.addAll(subredditSubmissions);
            notifyDataSetChanged();
        }
    }

    private class SubmissionCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private SubredditSubmission mSubredditSubmission;
        private TextView subredditPostScoreTextView;
        private TextView subredditPostTitleTextView;
        private TextView subredditPostSubredditTextView;
        private TextView subredditPostAuthorTextView;
        private ImageView subredditPostThumbnailImageView;
        private LinearLayout subredditThumbnailLayout;

        SubmissionCardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            subredditPostScoreTextView = (TextView) itemView.findViewById(R.id.subreddit_post_score);
            subredditPostTitleTextView = (TextView) itemView.findViewById(R.id.subreddit_post_title);
            subredditPostSubredditTextView = (TextView) itemView.findViewById(R.id.subreddit_post_subreddit);
            subredditPostAuthorTextView = (TextView) itemView.findViewById(R.id.subreddit_post_author);
            subredditPostThumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            subredditThumbnailLayout = (LinearLayout) itemView.findViewById(R.id.subreddit_thumbnail_vert_layout);
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onSubredditSelected(mSubredditSubmission);
        }

        public void bindSubredditSubmission(SubredditSubmission subredditSubmission) {
            this.mSubredditSubmission = subredditSubmission;

            if (mSubredditSubmission.getSubmissionScore() != null) {
                this.subredditPostScoreTextView.setText(sUtilityCode.formatSubredditSubmissionsScore(mSubredditSubmission.getSubmissionScore()));
            }

            this.subredditPostTitleTextView.setText(mSubredditSubmission.getTitle());
            this.subredditPostTitleTextView.setMaxLines(3);
            this.subredditPostTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
            String formattedSubreddit = getString(R.string.subreddit_prefix) + mSubredditSubmission.getSubreddit();
            this.subredditPostSubredditTextView.setText(formattedSubreddit);
            String formattedAuthor = getString(R.string.user_prefix) + mSubredditSubmission.getAuthor();
            this.subredditPostAuthorTextView.setText(formattedAuthor);

            if (mSubredditSubmission.isHasThumbnail()) {
                Uri imageUri = Uri.parse(mSubredditSubmission.getThumbnail());
                Picasso.get().load(imageUri).into(this.subredditPostThumbnailImageView);
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Activity lifecycle methods for debugging/understanding/etc //////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_pause));
        // https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state
        state = mLinearLayoutManager.onSaveInstanceState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_destroy));

        if (querySubredditsAsyncTask != null && querySubredditsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            querySubredditsAsyncTask.cancel(true);
        }

        // Prevent window leaks from Dialog remaining open after fragment is removed
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
