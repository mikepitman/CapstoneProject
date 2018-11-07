package pitman.co.za.readerforreddit;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pitman.co.za.readerforreddit.reddit.QuerySubredditExistenceAsyncTask;
import pitman.co.za.readerforreddit.reddit.SubredditExistenceQueryResult;
import pitman.co.za.readerforreddit.room.SubredditSubmissionViewModel;

public class SelectSubredditsActivityFragment extends Fragment {

    private static String LOG_TAG = SelectSubredditsActivityFragment.class.getSimpleName();
    private static SelectedSubredditCardAdapter mAdapter;
    private RecyclerView mSelectedSubredditsRecyclerView;
    private SubredditSubmissionViewModel mSubredditsViewModel;
    private CoordinatorLayout mCoordinatorLayout;
    private View rootView;
    private EditText mEditTextView;
    private Set<String> selectedSubreddits;
    private String submittedSubreddit;

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        outState.putString("selectedSubreddit", mSelectedSubreddits);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "2. onCreate()");

//        if (savedInstanceState != null) {
//            mSelectedSubreddits = savedInstanceState.getString("selectedSubreddit");
//            Log.d(LOG_TAG, "selectedSubreddit retrieved from savedInstanceState");

//        } else {
//            if (this.getArguments() != null) {
//                Bundle bundle = this.getArguments();
//                mSelectedSubreddits = bundle.getString("selectedSubreddit");
//                Log.d(LOG_TAG, "In ViewSubredditActivityFragment, selectedSubreddit is: " + mSelectedSubreddits);
//            }
//        }

        SharedPreferences preferences = getActivity().getSharedPreferences("selectedSubreddits", Context.MODE_PRIVATE);
        if (preferences != null) {
            selectedSubreddits = preferences.getStringSet("subreddits", null);
            Log.d(LOG_TAG, "preferences retrieved");
            if (selectedSubreddits == null) {
                selectedSubreddits = new HashSet<>();
                Log.d(LOG_TAG, "generating list of preferences");
                selectedSubreddits.add("Android");
                showSnackbar(R.string.supply_default_subreddit);
            }
        }

        mAdapter = new SelectedSubredditCardAdapter(new ArrayList<>(selectedSubreddits));

        mSubredditsViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "3. onCreateView()");

        rootView = inflater.inflate(R.layout.fragment_select_subreddits, container, false);

        mSelectedSubredditsRecyclerView = (RecyclerView) rootView.findViewById(R.id.selected_subreddits_recycler_view);
        mSelectedSubredditsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSelectedSubredditsRecyclerView.setAdapter(mAdapter);

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.fragment_select_subreddits_coordinatorLayout);

        mEditTextView = (EditText) rootView.findViewById(R.id.add_subreddit_text_input);
        Button submitSubredditButton = (Button) rootView.findViewById(R.id.add_subreddit_submit_button);
        submitSubredditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySubredditAddition(v);
            }
        });

        return rootView;
    }

    public void verifySubredditAddition(View view) {
        submittedSubreddit = mEditTextView.getText().toString();
        Log.d(LOG_TAG, "button to add subreddit clicked: " + submittedSubreddit);

        if (submittedSubreddit.length() > 1 && submittedSubreddit.substring(0,1).equals("r/")) {
            submittedSubreddit = submittedSubreddit.substring(2);
            showSnackbar(R.string.subreddit_includes_prefix);
        }

        // length must be longer than 3 (excl '/r') and less than 20
        if (submittedSubreddit.length() < 3 || submittedSubreddit.length() > 20) {
            showSnackbar(R.string.subreddit_incorrect_length);

        } else if (!selectedSubreddits.contains(submittedSubreddit)) {
            new QuerySubredditExistenceAsyncTask(this).execute(submittedSubreddit);
            hideKeyboard();
        } else {
            showSnackbar(R.string.subreddit_already_in_list);
        }
    }

    public void subredditVerified(SubredditExistenceQueryResult queryResult) {
        if (SubredditExistenceQueryResult.NSFW.equals(queryResult)) {
            Log.d(LOG_TAG, "subreddit NSFW: " + submittedSubreddit);
            showSnackbar(R.string.subreddit_nsfw);
            clearUserInputView();
        } else if (SubredditExistenceQueryResult.NONEXISTENT.equals(queryResult)) {
            Log.d(LOG_TAG, "subreddit nonexistent: " + submittedSubreddit);
            showSnackbar(R.string.subreddit_nonexistent);
            clearUserInputView();
        } else {
            selectedSubreddits.add(submittedSubreddit);
            updateSharedPrefs();
        }
    }

    private void showSnackbar(int message) {
        https://materialdoc.com/components/snackbars-and-toasts/#with-code
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void clearUserInputView() {
        mEditTextView.setText("");
        submittedSubreddit = "";
    }

    private void hideKeyboard() {
        // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    public void removeSubredditFromList(String subredditName) {
        Log.d(LOG_TAG, "Button clicked to remove subreddit " + subredditName);
        mSubredditsViewModel.deleteSubreddit(subredditName);
        selectedSubreddits.remove(subredditName);
        updateSharedPrefs();
    }

    private void updateSharedPrefs() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("selectedSubreddits", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("subreddits", selectedSubreddits);
//        editor.commit();
        editor.apply();

        mAdapter.swapData(new ArrayList<>(selectedSubreddits));
        clearUserInputView();
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
            View subredditNameView = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_subreddit_cardview, parent, false);
            return new SelectedSubredditCardViewHolder(subredditNameView);
        }

        @Override
        public void onBindViewHolder(SelectedSubredditCardViewHolder holder, int position) {
            holder.bindSubredditName(mSelectedSubreddits.get(position));
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

    private class SelectedSubredditCardViewHolder extends RecyclerView.ViewHolder {

        private String subredditName;
        private TextView subredditNameTextView;
        private ImageButton removeSubredditButton;

        SelectedSubredditCardViewHolder(View itemView) {
            super(itemView);
            subredditNameTextView = (TextView) itemView.findViewById(R.id.selected_subreddit_name);
            removeSubredditButton = (ImageButton) itemView.findViewById(R.id.remove_selected_subreddit_button);

            removeSubredditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubredditFromList(subredditName);
                }
            });
        }

        public void bindSubredditName(String subredditName) {
            String displayName = "r/" + subredditName;
            this.subredditName = subredditName;
            this.subredditNameTextView.setText(displayName);
        }
    }
}
