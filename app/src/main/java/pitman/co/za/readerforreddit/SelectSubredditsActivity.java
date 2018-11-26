package pitman.co.za.readerforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectSubredditsActivity extends AppCompatActivity {

    private static String LOG_TAG = SelectSubredditsActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private ArrayList<String> curatedSubreddits = new ArrayList<>();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_select_subreddits;      // returns de-referenced value, for different screen sizes - not strictly necessary here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        firebaseAnonymousSignIn();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.activity_select_subreddits_container);

        if (fragment == null) {
            fragment = new SelectSubredditsActivityFragment();

            Bundle fragmentBundle = new Bundle();
            fragment.setArguments(fragmentBundle);

            fm.beginTransaction().add(R.id.activity_select_subreddits_container, fragment).commit();
        }

        Toolbar fragmentToolbar = (Toolbar) findViewById(R.id.toolbar_selectSubreddits);
        setSupportActionBar(fragmentToolbar);
        ActionBar newBar = getSupportActionBar();
        newBar.setTitle(R.string.toolbar_select_subreddits);
        newBar.setDisplayShowHomeEnabled(true);
        newBar.setDisplayHomeAsUpEnabled(true);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// Activity lifecycle methods for debugging/understanding/etc //////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void firebaseAnonymousSignIn() {
        // https://firebase.google.com/docs/auth/android/anonymous-auth#authenticate-with-firebase-anonymously
        mAuth = FirebaseAuth.getInstance();
//        mAuth.getCurrentUser();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in successful
                            // Get the text file of curated subreddits
                            Log.d(LOG_TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            getCuratedSubredditsFromFirebase();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "signInAnonymously:failure", task.getException());
                            updateFragmentWithCuratedReddits(false);
                        }
                    }
                });
    }

    private void getCuratedSubredditsFromFirebase() {
        // Retrieve file with top selected subreddits from firebase, allowing list to change over time and be curated
        // https://firebase.google.com/docs/storage/android/start?authuser=2
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();       // Points to the root reference
        String fileName = "CuratedSubreddits.txt";
        final StorageReference curatedSubredditsFile = storageRef.child(fileName);

        final long ONE_KILOBYTE = 1024;
        curatedSubredditsFile.getBytes(ONE_KILOBYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    String fileContent = new String(bytes, "UTF-8");
                    Log.d(LOG_TAG, "converted string: " + fileContent);

                    String[] subredditsString = fileContent.split("%");
                    List<String> subredditsList = Arrays.asList(subredditsString);
                    if (subredditsList.size() != 0) {
                        curatedSubreddits.addAll(subredditsList);

                        // https://stackoverflow.com/questions/3585053/is-it-possible-to-check-if-a-string-only-contains-ascii
                        ArrayList<String> asciiCuratedSubreddits = new ArrayList<>();
                        for (String element : curatedSubreddits) {
                            if (Charset.forName("US-ASCII").newEncoder().canEncode(element)) {
                                asciiCuratedSubreddits.add(element);
                            }
                        }
                        curatedSubreddits = asciiCuratedSubreddits;
                        updateFragmentWithCuratedReddits((curatedSubreddits.size() > 0));
                    } else {
                        updateFragmentWithCuratedReddits(false);
                    }

                } catch (UnsupportedEncodingException e) {
                    Log.d(LOG_TAG, "Firebase-hosted file conversion failed - UnsupportedEncodingException");
                    updateFragmentWithCuratedReddits(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(LOG_TAG, exception.getLocalizedMessage());
                updateFragmentWithCuratedReddits(false);
            }
        });
    }

    private void updateFragmentWithCuratedReddits(boolean redditsRetrieved) {
        FragmentManager fm = getSupportFragmentManager();
        SelectSubredditsActivityFragment fragment = (SelectSubredditsActivityFragment) fm.findFragmentById(R.id.activity_select_subreddits_container);

        if (redditsRetrieved) {
            fragment.updateCuratedSubreddits(curatedSubreddits);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent()");
    }

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