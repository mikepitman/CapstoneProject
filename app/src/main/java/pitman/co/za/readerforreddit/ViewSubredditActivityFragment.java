package pitman.co.za.readerforreddit;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;
import pitman.co.za.readerforreddit.room.SubredditSubmissionViewModel;

public class ViewSubredditActivityFragment extends Fragment {

    private static String LOG_TAG = ViewSubredditActivityFragment.class.getSimpleName();
    private static UtilityCode sUtilityCode;
    private Callbacks mCallbacks;
    private View rootView;
    private String mSelectedSubreddit;
    private RecyclerView mSubredditSubmissionRecyclerView;
    private SubredditSubmissionViewModel mSubredditsViewModel;
    private static SubredditSubmissionCardAdapter mAdapter;


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
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_create));
        sUtilityCode = new UtilityCode();

        if (savedInstanceState != null) {
            mSelectedSubreddit = savedInstanceState.getString(getString(R.string.save_instance_state_selected_subreddit));

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
        mSubredditSubmissionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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

            this.subredditPostScoreTextView.setText(mSubredditSubmission.getFormattedSubmissionScore());
            this.subredditPostTitleTextView.setText(mSubredditSubmission.getTitle());
            this.subredditPostSubredditTextView.setText(mSubredditSubmission.getFormattedSubreddit());

            this.subredditPostAuthorTextView.setText(mSubredditSubmission.getFormattedAuthor());

//            if (mSubredditSubmission.isHasThumbnail()) {
                Uri imageUri = Uri.parse(mSubredditSubmission.getThumbnail());
                Picasso.get().load(imageUri).into(this.subredditPostThumbnailImageView);
//            } else {
//                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        0f);
//                subredditThumbnailLayout.setLayoutParams(param);
//                this.subredditPostThumbnailImageView.setVisibility(View.GONE);
//            }
        }
    }
}