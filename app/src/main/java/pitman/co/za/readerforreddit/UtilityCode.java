package pitman.co.za.readerforreddit;

public class UtilityCode {

    public String formatSubredditSubmissionsScore(Integer submissionScore) {
        String formattedSubmissionScore = submissionScore.toString();
        if (submissionScore > 999 && submissionScore < 10000) {
            // https://stackoverflow.com/questions/5195837/format-float-to-n-decimal-places
            formattedSubmissionScore = String.format(java.util.Locale.US,"%.1f", (float) submissionScore/1000);
            formattedSubmissionScore += 'k';
        } else if (submissionScore > 9999) {
            formattedSubmissionScore = String.valueOf(submissionScore/1000);
            formattedSubmissionScore += 'k';
        }
        return formattedSubmissionScore;
    }
}