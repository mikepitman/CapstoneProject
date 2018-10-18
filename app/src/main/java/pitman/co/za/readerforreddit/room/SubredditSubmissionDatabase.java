package pitman.co.za.readerforreddit.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import pitman.co.za.readerforreddit.domainObjects.SubmissionComment;
import pitman.co.za.readerforreddit.domainObjects.SubredditSubmission;

/*
 * Copied from codelabs - link below
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#6
 * */
@Database(entities = {SubredditSubmission.class, SubmissionComment.class}, version = 3)
public abstract class SubredditSubmissionDatabase extends RoomDatabase {

    public abstract SubredditSubmissionDao mSubredditSubmissionDao();

    private static SubredditSubmissionDatabase INSTANCE;

    public static SubredditSubmissionDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SubredditSubmissionDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SubredditSubmissionDatabase.class, "subreddit_submission_database")
                            .fallbackToDestructiveMigration()
//                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Procedure to change versions provided below
    // https://medium.com/google-developers/understanding-migrations-with-room-f01e04b07929
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("appropriate sql to migrate database");
        }
    };
}

