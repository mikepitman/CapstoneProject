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
//        mAllSubredditSubmissions = mRepository.getAllSubredditSubmissions();
    }

    public LiveData<List<SubredditSubmission>> getAllSubredditSubmissions(ArrayList<String> subreddits) {
        mAllSubredditSubmissions = mRepository.getAllSubredditSubmissions(subreddits);
        return mAllSubredditSubmissions;
    }

    public LiveData<List<SubredditSubmission>> getAllSubmissionsForSubreddit(String subreddit) {
        return mRepository.getSubmissionsForSubreddit(subreddit);
    }

    public LiveData<List<SubmissionComment>> getSubmissionComments(SubredditSubmission subredditSubmission) {
        return mRepository.getSubmissionComments(subredditSubmission);
    }

    public void insert(ArrayList<SubredditSubmission> subredditSubmissions) {
        mRepository.insert(subredditSubmissions);
    }

    public void insertComments(ArrayList<SubmissionComment> submissionComments) {
        mRepository.insertComments(submissionComments);
    }

    public void deleteSubreddit(String subreddit) {
        mRepository.deleteSubreddit(subreddit);
    }

    public void setSelectedSubredditSubmission(SubredditSubmission subredditSubmission) {
        this.selectedSubredditSubmission = subredditSubmission;
    }

    public SubredditSubmission retrieveSelectedSubmission() {
        return selectedSubredditSubmission;
    }
}
