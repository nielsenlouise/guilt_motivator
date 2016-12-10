package nielsen.guiltmotivator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by DHZ_Bill on 11/29/16.
 */
public class NotificationEventReceiver extends WakefulBroadcastReceiver{

    private static final String ACTION_START_NOTIFICATION_SERVICE = "ACTION_START_NOTIFICATION_SERVICE";
    private static final String ACTION_DELETE_NOTIFICATION = "ACTION_DELETE_NOTIFICATION";
    private static WeakReference<Activity> mActivityRef;
    //private static final int NOTIFICATIONS_INTERVAL_IN_HOURS = 60;
    @SuppressLint("NewApi")
    public static void setupAlarm(Context context) {
        //get helper and get db in write mode
        DatabaseHelper mDbHelper = new DatabaseHelper(context);

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //grab arraylist of tasks from the database
        ArrayList<Task> tasks = mDbHelper.getAllTasks();


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ArrayList<PendingIntent> intentArray = new ArrayList<PendingIntent>();
        Log.d("Number of tasks:", "" + tasks.size());
        for(int i = 0; i < tasks.size(); i++) {
            PendingIntent alarmIntent = getStartPendingIntent(context,i);
            Log.d("current task id: ", ""+i);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    getTriggerAt(tasks.get(i).getDueDate().getTime()),
                    // NOTIFICATIONS_INTERVAL_IN_HOURS,
                    alarmIntent);
            intentArray.add(alarmIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = null;
        checkAllTasks(context);
        if (ACTION_START_NOTIFICATION_SERVICE.equals(action)) {
            Log.i(getClass().getSimpleName(), "onReceive from alarm, starting notification service");
            serviceIntent = NotificationIntentService.createIntentStartNotificationService(context);
        } else if (ACTION_DELETE_NOTIFICATION.equals(action)) {
            Log.i(getClass().getSimpleName(), "onReceive delete notification action, starting notification service to handle delete");
            serviceIntent = NotificationIntentService.createIntentDeleteNotification(context);
        }

        if (serviceIntent != null) {
            startWakefulService(context, serviceIntent);
        }
    }

    public static void updateActivity(Activity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    public void checkAllTasks(Context context){
        DatabaseHelper mDbHelper = new DatabaseHelper(context);
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //grab arraylist of tasks from the database
        ArrayList<Task> tasks = mDbHelper.getAllTasks();
        Calendar cur = Calendar.getInstance();
        for (int i = 0; i < tasks.size(); i++){
            if (tasks.get(i).getDueDate().compareTo(cur) >= 0 && !tasks.get(i).isChecked()){
                sendEmail(context,tasks.get(i));
            }
        }
    }

    private void sendEmail(Context context, Task task){
//        Activity activity = (Activity) context;
        //get helper and get db in write mode
        DatabaseHelper mDbHelper = new DatabaseHelper(context);
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ArrayList<Contact> contacts = mDbHelper.getContacts(task);
        Activity activity = mActivityRef.get();
        final SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        final String tone = sharedPref.getString(MainActivity.SAVED_TONE, "polite");
        final String name = sharedPref.getString(MainActivity.SAVED_NAME, "none");
        final String politeMsg = "polite";
        final String profaneMsg = "profane";
//        String msg = tone == "polite"? politeMsg : profaneMsg;
        String msg = name + tone;
        for (int i = 0; i < contacts.size();i++){
            //Creating SendMail object
            SendMail sm = new SendMail(context, contacts.get(i).getAddress(), "From Guilt Motivator", msg);
            //Executing sendmail to send email
            sm.execute();
        }
    }

    private static long getTriggerAt(Date dueDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dueDate);
        //calendar.add(Calendar.MINUTE, NOTIFICATIONS_INTERVAL_IN_HOURS);
        return calendar.getTimeInMillis();
    }

    private static PendingIntent getStartPendingIntent(Context context, int i) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_START_NOTIFICATION_SERVICE);
        return PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_DELETE_NOTIFICATION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
