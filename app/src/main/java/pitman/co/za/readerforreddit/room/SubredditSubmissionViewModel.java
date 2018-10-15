package pitman.co.za.readerforreddit.room;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

public class SubredditSubmissionViewModel extends AndroidViewModel {

    private SubredditSubmissionRepository mRepository;
    private LiveData<List<SubredditSubmission>> mAllSubredditSubmissions;
    private SubredditSubmission selectedSubredditSubmission;


    // Constructor never called in examples I could find, hence second setting of the mAllRecipes attribute in getAllRecipes() method
    public SubredditSubmissionViewModel(Application application) {
        super(application);
        mRepository = new SubredditSubmissionRepository(application);
        mAllSubredditSubmissions = mRepository.getAllSubredditSubmissions();
    }

    public LiveData<List<SubredditSubmission>> getAllSubredditSubmissions() {
        mAllSubredditSubmissions = mRepository.getAllSubredditSubmissions();
        return mAllSubredditSubmissions;
    }

    public LiveData<List<SubredditSubmission>> getAllSubmissionsForSubreddit(String subreddit) {
        return mRepository.getSubmissionsForSubreddit(subreddit);
    }

    // todo: this should maybe get its own ViewModel
    public LiveData<List<SubmissionComment>> getSubmissionComments(SubredditSubmission subredditSubmission) {
        return mRepository.getSubmissionComments(subredditSubmission);
    }

    public void insert(ArrayList<SubredditSubmission> subredditSubmissions) {
        mRepository.insert(subredditSubmissions);
    }

    public void setSelectedSubredditSubmission(SubredditSubmission subredditSubmission) {
        this.selectedSubredditSubmission = subredditSubmission;
    }

    public SubredditSubmission retrieveSelectedSubmission() {
        return selectedSubredditSubmission;
    }
}
