package com.newsweather;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	
	private final static String TAG = "SampleWidget";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Intent intent = new Intent(context, UpdateService.class);
	    context.startService(intent);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	public static class UpdateService extends Service
	  {

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public void onStart(Intent intent, int startId) {
			super.onStart(intent, startId);
			RemoteViews updateViews = new RemoteViews(this.getPackageName(),
			          R.layout.widget);
			updateViews.setTextViewText(R.id.widgetContent, "這是內容");
			updateViews.setTextViewText(R.id.widgetSource, "這是來源");
			ComponentName thisWidget = new ComponentName(this, MyWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
		    manager.updateAppWidget(thisWidget, updateViews);
		}
		
	  }
	
	
	
	
}
