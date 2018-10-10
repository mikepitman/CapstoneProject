package pitman.co.za.readerforreddit;

import android.app.Activity;
import android.support.v4.app.Fragment;

import pitman.co.za.readerforreddit.room.SubredditSubmissionViewModel;

public class MainActivityFragment extends Fragment {

    private static String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private Callbacks mCallbacks;
    private SubredditSubmissionViewModel mRecipeViewModel;

//// Callbacks-related code //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public interface Callbacks {
        void onSubredditSelected();
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
}
