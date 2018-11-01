package pitman.co.za.readerforreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectSubredditsActivityFragment extends Fragment {

    private static String LOG_TAG = SelectSubredditsActivityFragment.class.getSimpleName();
    private static SelectedSubredditCardAdapter mAdapter;
    private RecyclerView mSelectedSubredditsRecyclerView;
    private View rootView;
    private Set<String> selectedSubreddits;

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        outState.putString("selectedSubreddit", mSelectedSubreddits);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "2. onCreate()");

        if (savedInstanceState != null) {
//            mSelectedSubreddits = savedInstanceState.getString("selectedSubreddit");
//            Log.d(LOG_TAG, "selectedSubreddit retrieved from savedInstanceState");

        } else {
            if (this.getArguments() != null) {
                Bundle bundle = this.getArguments();
//                mSelectedSubreddits = bundle.getString("selectedSubreddit");
//                Log.d(LOG_TAG, "In ViewSubredditActivityFragment, selectedSubreddit is: " + mSelectedSubreddits);
            }
        }

        // https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#13
//        mSubredditsViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);
//        mSubredditsViewModel.getAllSubmissionsForSubreddit(mSelectedSubreddits).observe(this, new Observer<List<SubredditSubmission>>() {
//            @Override
//            public void onChanged(@Nullable final List<SubredditSubmission> subreddits) {
//                mAdapter.swapData(subreddits);
//            }
//        });
//        mAdapter = new ViewSubredditActivityFragment.SelectedSubredditCardAdapter(mSubredditsViewModel.getAllSubmissionsForSubreddit(mSelectedSubreddits).getValue());
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (preferences != null) {
            selectedSubreddits = preferences.getStringSet("subreddits", null);
            Log.d(LOG_TAG, "preferences retrieved");
        } else {
            selectedSubreddits = new HashSet<>();
            Log.d(LOG_TAG, "generating list of preferences");
            selectedSubreddits.add("r/Android");
        }

        mAdapter = new SelectedSubredditCardAdapter(new ArrayList<>(selectedSubreddits));

        // todo: save set of values to sharedpreferences
//        preference = preferences.getString(context.getString(R.string.sort_param_key), context.getString(R.string.sort_param_default));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "3. onCreateView()");


        // Get bundle arguments from MainActivity
//        boolean isTablet = false;
//        Bundle arguments = this.getArguments();
//        if (arguments != null) {
//            isTablet = arguments.getBoolean("isTablet");
//        }

        rootView = inflater.inflate(R.layout.fragment_select_subreddits, container, false);
        mSelectedSubredditsRecyclerView = (RecyclerView) rootView.findViewById(R.id.selected_subreddits_recycler_view);
        mSelectedSubredditsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSelectedSubredditsRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    private class SelectedSubredditCardAdapter extends RecyclerView.Adapter<SelectedSubredditCardViewHolder> {

        private String LOG_TAG = SelectedSubredditCardAdapter.class.getSimpleName();
        private List<String> mSelectedSubreddits;

        private SelectedSubredditCardAdapter(List<String> selectedSubreddits) {
            mSelectedSubreddits = selectedSubreddits;
        }

        @Override
        public int getItemCount() {
            if (mSelectedSubreddits == null) {
                Log.d(LOG_TAG, "number of items is 0, this should not happen!");
                return 0;
            }
            return mSelectedSubreddits.size();
        }

        @Override
        public SelectedSubredditCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View subredditNameView = LayoutInflater.from(parent.getContext()).inflate(R.layout.subreddit_post_view, parent, false);
            return new SelectedSubredditCardViewHolder(subredditNameView);
        }

        @Override
        public void onBindViewHolder(SelectedSubredditCardViewHolder holder, int position) {
            holder.subredditNameTextView.setText(mSelectedSubreddits.get(position));
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public void swapData(List<String> selectedSubreddits) {
            int numberOfOldEntries = mSelectedSubreddits == null ? 0 : mSelectedSubreddits.size();
            if (mSelectedSubreddits != null) {
                mSelectedSubreddits.clear();
            } else {
                mSelectedSubreddits = new ArrayList<String>();
            }
            mSelectedSubreddits.addAll(selectedSubreddits);
            notifyDataSetChanged();
        }
    }

    private class SelectedSubredditCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView subredditNameTextView;
        // might need to have a button declared, to click and remove a selected subreddit from the list

        SelectedSubredditCardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            subredditNameTextView = (TextView) itemView.findViewById(R.id.selected_subreddit_name);
        }

        @Override
        public void onClick(View view) {
//            mCallbacks.onSubmissionSelected(mSubredditSubmission);
        }
    }
}
