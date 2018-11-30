package za.co.pitman.readerforreddit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import za.co.pitman.readerforreddit.MainActivity;
import za.co.pitman.readerforreddit.R;

public class ReaderForRedditWidgetProvider extends AppWidgetProvider {

    // https://developer.android.com/guide/topics/appwidgets/
    private static final String LOG_TAG = ReaderForRedditWidgetProvider.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final ComponentName thisAppWidget = new ComponentName(context.getPackageName(), ReaderForRedditWidgetProvider.class.getName());
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_provider);

            Intent intent = new Intent(context, ReaderForRedditWidgetRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));  // ? what's this for?

            views.setRemoteAdapter(R.id.subredditWidgetListView, intent);
            views.setEmptyView(R.layout.widget_provider, R.id.empty_view);

            // Open the app when user clicks on the widget
            Intent widgetClickedIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, widgetClickedIntent, 0);
            views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

            // https://stackoverflow.com/questions/42129390/onupdate-not-calling-the-widget-service
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.subredditWidgetListView);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
