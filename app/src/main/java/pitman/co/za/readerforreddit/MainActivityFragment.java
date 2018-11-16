package pitman.co.za.readerforreddit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;
import pitman.co.za.readerforreddit.reddit.QuerySubscribedSubredditsListAsyncTask;
import pitman.co.za.readerforreddit.room.SubredditSubmissionViewModel;

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
    public void updateProgressBar(Integer progress) { mProgressDialog.setProgress(progress); }

    public void dismissProgressBar() { mProgressDialog.dismiss(); }

    public void showProgressBar() {
        mProgressDialog.show();
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // todo: save relevant state
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "2. onCreate()");
        sUtilityCode = new UtilityCode();
        mContext = this.getContext();

        Bundle activityArguments = this.getArguments();
        if (activityArguments != null) {
            selectedSubreddits = activityArguments.getStringArrayList("selectedSubredditsBundleForFragment");
        }

        // https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#13
        mSubredditsViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);
        mSubredditsViewModel.getAllSubredditSubmissions(selectedSubreddits).observe(this, new Observer<List<SubredditSubmission>>() {
            @Override
            public void onChanged(@Nullable final List<SubredditSubmission> subreddits) {
                mAdapter.swapData(parseTopSubredditSubmissions(subreddits));
            }
        });
        List<SubredditSubmission> topSubredditSubmissions = parseTopSubredditSubmissions(mSubredditsViewModel.getAllSubredditSubmissions(selectedSubreddits).getValue());
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
        Log.d(LOG_TAG, "3. onCreateView()");

        if (savedInstanceState != null) {
//            recipesUpdated = savedInstanceState.getBoolean("recipesUpdated");
//            todo: restore state
        }

        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.main_fragment_container);

        mSubredditSubmissionRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_main_subreddit_card_recyclerview);
        mSubredditSubmissionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubredditSubmissionRecyclerView.setAdapter(mAdapter);

        // Launch asyncTask to retrieve top submissions from selected subreddits
        if (sUtilityCode.isNetworkAvailable(getActivity())) {
            new QuerySubscribedSubredditsListAsyncTask(this).execute(selectedSubreddits);
        } else {
            Log.d(LOG_TAG, "No network connectivity!");
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.no_network_connection, mContext);
        }

        return rootView;
    }

    public void generateSubredditSubmissionsAdapterWithData(ArrayList<SubredditSubmission> retrievedSubredditSubmissions) {
        if (retrievedSubredditSubmissions != null) {
            mSubredditsViewModel.insert(retrievedSubredditSubmissions);
        }
    }

    // Collect the top-scored submissions for each subreddit for display on 'home screen' from list of all returned subreddit submissions
    private List<SubredditSubmission> parseTopSubredditSubmissions(List<SubredditSubmission> subredditSubmissions) {
        List<SubredditSubmission> topSubredditSubmissions = new ArrayList<>();
        if (subredditSubmissions != null) {     // NPE results if code is executed before the asyncTask returns
            Set<String> subredditsParsed = new HashSet<>();
            SubredditSubmission topSubmissionForSubreddit = null;
            for (SubredditSubmission submission : subredditSubmissions) {
                if (!subredditsParsed.contains(submission.getSubreddit())) {
                    subredditsParsed.add(submission.getSubreddit());
                    topSubmissionForSubreddit = submission;
                    topSubredditSubmissions.add(topSubmissionForSubreddit);
                } else {
                    if (submission.getSubmissionScore() > topSubmissionForSubreddit.getSubmissionScore()) {
                        topSubmissionForSubreddit = submission;
                    }
                }
            }
        }
        return topSubredditSubmissions;
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
                Log.d(LOG_TAG, "number of items is 0, this should not happen!");
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
                this.subredditPostScoreTextView.setText(
                        sUtilityCode.formatSubredditSubmissionsScore(mSubredditSubmission.getSubmissionScore()));
            }

            this.subredditPostTitleTextView.setText(mSubredditSubmission.getTitle());
            this.subredditPostTitleTextView.setMaxLines(3);
            this.subredditPostTitleTextView.setEllipsize(TextUtils.TruncateAt.END);

            String formattedSubreddit = "r/" + mSubredditSubmission.getSubreddit();
            this.subredditPostSubredditTextView.setText(formattedSubreddit);

            String formattedAuthor = "u/" + mSubredditSubmission.getAuthor();
            this.subredditPostAuthorTextView.setText(formattedAuthor);

//            if (mSubredditSubmission.isHasThumbnail()) {
                Uri imageUri = Uri.parse(mSubredditSubmission.getThumbnail());
                Picasso.get().load(imageUri).into(this.subredditPostThumbnailImageView);
//            } else {
//                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        0f);
//                subredditThumbnailLayout.setLayoutParams(param);
//                this.subredditPostThumbnailImageView.setVisibility(View.GONE);
//            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Activity lifecycle methods for debugging/understanding/etc //////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
    }
}
