package za.co.pitman.readerforreddit;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import za.co.pitman.readerforreddit.domainObjects.SubredditSubmission;
import za.co.pitman.readerforreddit.room.SubredditSubmissionViewModel;

public class ViewSubredditActivityFragment extends Fragment {

    private static String LOG_TAG = ViewSubredditActivityFragment.class.getSimpleName();
    private static UtilityCode sUtilityCode;
    private Callbacks mCallbacks;
    private View rootView;
    private String mSelectedSubreddit;
    private RecyclerView mSubredditSubmissionRecyclerView;
    private SubredditSubmissionViewModel mSubredditsViewModel;
    private static SubredditSubmissionCardAdapter mAdapter;
    private Parcelable state;
    private LinearLayoutManager mLinearLayoutManager;

    //// Callbacks-related code //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public interface Callbacks {
        void onSubmissionSelected(SubredditSubmission subredditSubmission);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
//// Callbacks-related code //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.save_instance_state_selected_subreddit), mSelectedSubreddit);
        outState.putParcelable(getString(R.string.save_instance_state_recyclerview_state), state);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_create));
        sUtilityCode = new UtilityCode();
        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        if (savedInstanceState != null) {
            mSelectedSubreddit = savedInstanceState.getString(getString(R.string.save_instance_state_selected_subreddit));
            state = savedInstanceState.getParcelable(getString(R.string.save_instance_state_recyclerview_state));
        } else {
            if (this.getArguments() != null) {
                Bundle bundle = this.getArguments();
                mSelectedSubreddit = bundle.getString(getString(R.string.bundle_key_selected_subreddit));
            }
        }

        // https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#13
        mSubredditsViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);
        mSubredditsViewModel.getAllSubmissionsForSubreddit(mSelectedSubreddit).observe(this, new Observer<List<SubredditSubmission>>() {
            @Override
            public void onChanged(@Nullable final List<SubredditSubmission> subreddits) {
                mAdapter.swapData(subreddits);
                mLinearLayoutManager.onRestoreInstanceState(state);
            }
        });
        mAdapter = new SubredditSubmissionCardAdapter(mSubredditsViewModel.getAllSubmissionsForSubreddit(mSelectedSubreddit).getValue());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_create_view));

        rootView = inflater.inflate(R.layout.fragment_view_subreddit, container, false);
        mSubredditSubmissionRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_view_subreddit_card_recyclerview);
        mSubredditSubmissionRecyclerView.setHasFixedSize(true);
        mSubredditSubmissionRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSubredditSubmissionRecyclerView.setAdapter(mAdapter);

        return rootView;
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
            mCallbacks.onSubmissionSelected(mSubredditSubmission);
        }

        public void bindSubredditSubmission(SubredditSubmission subredditSubmission) {
            this.mSubredditSubmission = subredditSubmission;
            String formattedSubreddit = getString(R.string.subreddit_prefix) + mSubredditSubmission.getSubreddit();
            String formattedAuthor = getString(R.string.user_prefix) + mSubredditSubmission.getAuthor();

            this.subredditPostScoreTextView.setText(sUtilityCode.formatSubredditSubmissionsScore(mSubredditSubmission.getSubmissionScore()));
            this.subredditPostTitleTextView.setText(mSubredditSubmission.getTitle());
            this.subredditPostSubredditTextView.setText(formattedSubreddit);
            this.subredditPostAuthorTextView.setText(formattedAuthor);

            Uri imageUri = Uri.parse(mSubredditSubmission.getThumbnail());
            Picasso.get().load(imageUri).into(this.subredditPostThumbnailImageView);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_pause));
        // https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state
        state = mLinearLayoutManager.onSaveInstanceState();
    }
}