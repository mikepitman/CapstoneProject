package za.co.pitman.readerforreddit.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ReaderForRedditWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ReaderForRedditWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
