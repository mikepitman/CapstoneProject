package za.co.pitman.readerforreddit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
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

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import za.co.pitman.readerforreddit.reddit.QuerySubredditExistenceAsyncTask;
import za.co.pitman.readerforreddit.room.SubredditSubmissionViewModel;

public class SelectSubredditsActivityFragment extends Fragment {

    private static String LOG_TAG = SelectSubredditsActivityFragment.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;
    private static SelectedSubredditCardAdapter mAdapter;
    private static UtilityCode sUtilityCode;
    private RecyclerView mSelectedSubredditsRecyclerView;
    private SubredditSubmissionViewModel mSubredditsViewModel;
    private CoordinatorLayout mCoordinatorLayout;
    private View rootView;
    private EditText mEditTextView;
    private Set<String> selectedSubreddits;
    private String submittedSubreddit;
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private boolean failedToReadFirebaseFile = false;
    private boolean isSubredditListEmpty = false;
    private Parcelable state;
    private LinearLayoutManager mLinearLayoutManager;
    private AsyncTask querySubredditExistsAsyncTask;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(getString(R.string.save_instance_state_subreddits_arraylist), new ArrayList<>(selectedSubreddits));
        outState.putString(getString(R.string.save_instance_state_selected_subreddit), submittedSubreddit);
        outState.putParcelable(getString(R.string.save_instance_state_recyclerview_state), state);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_create));
        sUtilityCode = new UtilityCode();
        mContext = this.getContext();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        if (savedInstanceState != null) {
            selectedSubreddits = new HashSet<>(savedInstanceState.getStringArrayList(getString(R.string.save_instance_state_subreddits_arraylist)));
            submittedSubreddit = savedInstanceState.getString(getString(R.string.save_instance_state_selected_subreddit));
            state = savedInstanceState.getParcelable(getString(R.string.save_instance_state_recyclerview_state));

        } else if (getActivity().getSharedPreferences(getString(R.string.shared_prefs_subreddits_pref), Context.MODE_PRIVATE) != null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_prefs_subreddits_pref), Context.MODE_PRIVATE);
            selectedSubreddits = preferences.getStringSet(getString(R.string.shared_prefs_subreddits_list_key), null);
            if (selectedSubreddits == null || selectedSubreddits.isEmpty()) {
                selectedSubreddits = new HashSet<>();
                selectedSubreddits.add(getString(R.string.default_supplied_subreddit));
                isSubredditListEmpty = true;
            }
        }
        mAdapter = new SelectedSubredditCardAdapter(new ArrayList<>(selectedSubreddits));


        mSubredditsViewModel = ViewModelProviders.of(this).get(SubredditSubmissionViewModel.class);

        // https://android--examples.blogspot.com/2017/02/android-asynctask-with-progress-dialog.html
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(this.getActivity());
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // Progress dialog horizontal style
        mProgressDialog.setTitle(getString(R.string.progress_dialog_selectSubreddit_title));  // Progress dialog title
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, getString(R.string.debug_lifecycle_on_create_view));

        rootView = inflater.inflate(R.layout.fragment_select_subreddits, container, false);

        mSelectedSubredditsRecyclerView = (RecyclerView) rootView.findViewById(R.id.selected_subreddits_recycler_view);
        mSelectedSubredditsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSelectedSubredditsRecyclerView.setAdapter(mAdapter);
        // https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position-using-recyclerview-state
        if (state != null) {
            mSelectedSubredditsRecyclerView.getLayoutManager().onRestoreInstanceState(state);
        }

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.fragment_select_subreddits_coordinatorLayout);

        mEditTextView = (EditText) rootView.findViewById(R.id.add_subreddit_text_input);
        Button submitSubredditButton = (Button) rootView.findViewById(R.id.add_subreddit_submit_button);
        submitSubredditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySubredditAddition(v);
            }
        });

        if (failedToReadFirebaseFile) {
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.firebase_file_read_failure, mContext);
        }
        // snackBar needs the coordinatorLayout to be initialised, but isSubredditListEmpty is determined before that occurs.
        if (isSubredditListEmpty) {
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.supply_default_subreddit, mContext);
            updateSharedPrefs();
        }

        return rootView;
    }

    // Called from SelectSubredditsActivity with new subreddits retrieved from FireBase
    public void updateCuratedSubreddits(ArrayList<String> curatedSubreddits) {
        if (selectedSubreddits != null &&
                selectedSubreddits.size() == 1 &&
                (new ArrayList<>(selectedSubreddits).get(0).equals(getString(R.string.default_supplied_subreddit)))) {
            selectedSubreddits.clear();
            selectedSubreddits.addAll(curatedSubreddits);
            updateSharedPrefs();
            if (mCoordinatorLayout != null) {
                sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.curated_subreddits_retrieved, mContext);
            }
        }
    }

    public void verifySubredditAddition(View view) {
        submittedSubreddit = mEditTextView.getText().toString();

        if (submittedSubreddit.length() > 1 && submittedSubreddit.substring(0, 1).equals(getString(R.string.subreddit_prefix))) {
            submittedSubreddit = submittedSubreddit.substring(2);
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.subreddit_includes_prefix, mContext);
        }

        // length must be longer than 3 (excl '/r') and less than 20
        if (submittedSubreddit.length() < 3 || submittedSubreddit.length() > 20) {
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.subreddit_incorrect_length, mContext);

        } else if (!selectedSubreddits.contains(submittedSubreddit)) {
            if (sUtilityCode.isNetworkAvailable(getActivity())) {
                String dialogMessage =
                        getString(R.string.progress_dialog_selectSubreddit_if) +
                                " '" + submittedSubreddit + "' " +
                                getString(R.string.progress_dialog_selectSubreddit_has_a_subreddit);
                mProgressDialog.setMessage(dialogMessage);

                querySubredditExistsAsyncTask = new QuerySubredditExistenceAsyncTask(this).execute(submittedSubreddit);
            } else {
                sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.no_network_connection_add_subreddits, mContext);
            }
            hideKeyboard();
        } else {
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.subreddit_already_in_list, mContext);
        }
    }

    public void subredditVerified(String queryResult) {
        if ("NSFW".equals(queryResult)) {
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.subreddit_nsfw, mContext);
            clearUserInputView();
        } else if ("NONEXISTENT".equals(queryResult)) {
            sUtilityCode.showSnackbar(mCoordinatorLayout, R.string.subreddit_nonexistent, mContext);
            clearUserInputView();
        } else {
            selectedSubreddits.add(submittedSubreddit);
            updateSharedPrefs();

            Bundle firebaseBundle = new Bundle();
            firebaseBundle.putString(FirebaseAnalytics.Param.SUCCESS, submittedSubreddit);
        }
    }

    public void showProgressBar() {
        mProgressDialog.show();
    }

    public void updateProgressBar(Integer progress) {
        mProgressDialog.setProgress(progress);
    }

    public void dismissProgressBar() {
        mProgressDialog.dismiss();
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
        mSubredditsViewModel.deleteSubreddit(subredditName);
        selectedSubreddits.remove(subredditName);
        updateSharedPrefs();
    }

    private void updateSharedPrefs() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.shared_prefs_subreddits_pref), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(getString(R.string.shared_prefs_subreddits_list_key), selectedSubreddits);
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
            String displayName = getString(R.string.subreddit_prefix) + subredditName;
            this.subredditName = subredditName;
            this.subredditNameTextView.setText(displayName);
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

        if (querySubredditExistsAsyncTask != null && querySubredditExistsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            querySubredditExistsAsyncTask.cancel(true);
        }

        // https://github.com/googlesamples/google-services/issues/200
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
