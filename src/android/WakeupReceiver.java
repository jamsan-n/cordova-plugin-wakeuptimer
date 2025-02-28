package org.jk.cordova.wakeupplugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

public class WakeupReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = "WakeupReceiver";

	public boolean isRunning(Context ctx) {
		ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

		for (ActivityManager.RunningTaskInfo task : tasks) {
			if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
				if (task.numRunning > 0) {
					return true;
				}
			}
		}

		return false;
	}

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	public void onReceive(Context context, Intent intent) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Log.d(LOG_TAG, "wakeuptimer expired at " + sdf.format(new Date().getTime()));

		try {
			Bundle extrasBundle = intent.getExtras();

			if (extrasBundle != null && extrasBundle.get("skipOnAwake") != null) {
				if (extrasBundle.get("skipOnAwake").equals(true)) {
					PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
					boolean isScreenAwake = (Build.VERSION.SDK_INT < 20 ? powerManager.isScreenOn() : powerManager.isInteractive());

					if (isScreenAwake) {
						Log.d(LOG_TAG, "screen is awake. Postponing launch.");
						return;
					}
				}
			}

			if (extrasBundle != null && extrasBundle.get("skipOnRunning") != null) {
				if (extrasBundle.get("skipOnRunning").equals(true)) {
					if (isRunning(context)) {
						Log.d(LOG_TAG, "app is already running. No need to launch");
						return;
					}
				}
			}

			String packageName = context.getPackageName();
			Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
			launchIntent.putExtra("cdvStartInBackground", true);

			String className = launchIntent.getComponent().getClassName();
			Log.d(LOG_TAG, "launching activity for class " + className);

			@SuppressWarnings("rawtypes")
			Class c = Class.forName(className);

			Intent i = new Intent(context, c);
			i.putExtra("wakeup", true);

			if (extrasBundle != null && extrasBundle.get("startInBackground") != null) {
				if (extrasBundle.get("startInBackground").equals(true)) {
					Log.d(LOG_TAG, "starting app in background");
					i.putExtra("cdvStartInBackground", true);
				}
			}


			JSONObject alarm = WakeupPlugin.lastAlarm;
			String extras=null;
			if (extrasBundle!=null && extrasBundle.get("extra")!=null) {
				extras = extrasBundle.get("extra").toString();
			}

			if (extras!=null) {
				i.putExtra("extra", extras);
			}
			if (alarm.has("extra")){
				JSONObject extra = alarm.getJSONObject("extra");
				if (extra.has("moveForeground") && extra.getBoolean("moveForeground")){
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);
				}
			}

			if (extrasBundle.get("type").equals("repeatingSeconds")){
			    // 再次设置alarm
				Calendar alarmDate= WakeupPlugin.getTimeFromNow(WakeupPlugin.lastRepeatingSeconds);
				Intent nintent = new Intent(context, WakeupReceiver.class);
				if(alarm.has("extra")){
					nintent.putExtra("extra", alarm.getJSONObject("extra").toString());
					nintent.putExtra("type", extrasBundle.getString("type"));
					nintent.putExtra("skipOnAwake", alarm.getBoolean("skipOnAwake"));
					nintent.putExtra("skipOnRunning", alarm.getBoolean("skipOnRunning"));
					nintent.putExtra("startInBackground", alarm.getBoolean("startInBackground"));
				}
				WakeupPlugin.setNotification(context, extrasBundle.getString("type"), alarmDate, nintent, WakeupPlugin.ID_REPEATSECOND_OFFSET);
			}

			if(WakeupPlugin.connectionCallbackContext!=null) {
				JSONObject o=new JSONObject();
				o.put("type", "wakeup");
				if (extras!=null) {
					o.put("extra", extras);
				}
				o.put("cdvStartInBackground", true);
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, o);
				pluginResult.setKeepCallback(true);
				WakeupPlugin.connectionCallbackContext.sendPluginResult(pluginResult);
			}

			if (extrasBundle!=null && extrasBundle.getString("type")!=null && extrasBundle.getString("type").equals("daylist")) {
				// repeat in one week
				Date next = new Date(new Date().getTime() + (7 * 24 * 60 * 60 * 1000));
				Log.d(LOG_TAG,"resetting alarm at " + sdf.format(next));

				Intent reschedule = new Intent(context, WakeupReceiver.class);
				if (extras!=null) {
					reschedule.putExtra("extra", intent.getExtras().get("extra").toString());
				}
				reschedule.putExtra("day", WakeupPlugin.daysOfWeek.get(intent.getExtras().get("day")));
				reschedule.putExtra("cdvStartInBackground", true);

				PendingIntent sender = PendingIntent.getBroadcast(context, 19999 + WakeupPlugin.daysOfWeek.get(intent.getExtras().get("day")), intent, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				if (Build.VERSION.SDK_INT>=23) {
					alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTime(), sender);
				} else if (Build.VERSION.SDK_INT>=19) {
					alarmManager.setExact(AlarmManager.RTC_WAKEUP, next.getTime(), sender);
				} else {
					alarmManager.set(AlarmManager.RTC_WAKEUP, next.getTime(), sender);
				}
			}

		} catch (JSONException e){
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
