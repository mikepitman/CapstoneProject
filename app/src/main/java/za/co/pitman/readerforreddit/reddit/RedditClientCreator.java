package za.co.pitman.readerforreddit.reddit;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;

import java.util.UUID;

public class RedditClientCreator {

    // https://mattbdean.gitbooks.io/jraw/quickstart.html
    public RedditClient getRedditClient() {
        UserAgent userAgent = new UserAgent(
                "android",
                "za.co.pitman.readerForReddit",
                "v1.0",
                "narfice");
        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        Credentials credentials = Credentials.userlessApp("CGG1OAPhpEmzgw", UUID.randomUUID());
        return OAuthHelper.automatic(adapter, credentials);
    }
}
