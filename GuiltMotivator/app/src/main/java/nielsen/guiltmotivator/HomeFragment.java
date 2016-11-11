package nielsen.guiltmotivator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The main fragment containing the list of tasks and a button to add tasks. Sends data 2 SQL 4 safekeeping.
 */
public class HomeFragment extends Fragment {
    //preparing to butter...
    @BindView(R.id.tasklist) ListView listView;
    @BindView(R.id.buttonbutton) Button buttonbutton;


    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getting the view
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        //spreading the butter
        ButterKnife.bind(this, view);

        //get helper and get db in write mode
        DictionaryOpenHelper mDbHelper = new DictionaryOpenHelper(getContext());
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //grab arraylist of tasks from the database
        ArrayList<Task> list = mDbHelper.getAllTasks();
        final TasksAdapter tasksAdapter = new TasksAdapter(getActivity(), list);
        listView.setAdapter(tasksAdapter);

        String sql = "SELECT " + DictionaryOpenContract.FeedEntry.COLUMN_NAME_TASK + "FROM " + DictionaryOpenContract.FeedEntry.TABLE_NAME;

        //setting an onclick for the button that adds items.
        buttonbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //building the alertdialog, which pulls up an edittext and sets the value in the ArrayList.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Enter an task to do");
                final EditText edittext = new EditText(getActivity());
                alertDialogBuilder.setView(edittext);

                //positive button- enter to change the things.
                alertDialogBuilder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get the eddittext text input and put it in the textview
                        String textInput = edittext.getText().toString();
                        Task taskInput = new Task(textInput);
                        tasksAdapter.add(taskInput);

                        //put that shit into SQL
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(DictionaryOpenContract.FeedEntry.COLUMN_NAME_TASK, textInput);

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId = db.insert(DictionaryOpenContract.FeedEntry.TABLE_NAME, null, values);
                        taskInput.setId(newRowId);
                    }
                });

                //calling the alert dialog.
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });

//        long timeDiff = getTimeDiff("2016-11-10 18:34:00");

        scheduleNotification(getNotification("Scheduled Notification"), 10000);

        return view;
    }

    public void onCreate() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        int defaultValue = getResources().getColor(R.color.white);
        int background = sharedPref.getInt(MainActivity.SAVED_COLOR, defaultValue);


        getView().setBackgroundColor(background);
    }

    public interface OnFragmentInteractionListener {

        public void onMainFragmentInteraction(Uri uri);
    }

    //Helper functions for notifications:
    //https://gist.github.com/BrandonSmith/6679223

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(getActivity(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(getContext());
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        return builder.build();
    }

    public static long getTimeDiff(String inputTime) {
        // http://stackoverflow.com/questions/1459656/how-to-get-the-current-time-in-yyyy-mm-dd-hhmisec-millisecond-format-in-java
        // the input date must be in yyyy-MM-dd HH:mm:ss format. Then we subtract and put that into scheduleNotification.

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("US"));
        Date now = new Date();
        String strDate = sdfDate.format(now);

        Date dueDate = new SimpleDateFormat("yyyy-M-dd HH:mm:ss", new Locale("US")).parse((inputTime));

        long diff = Math.abs(now.getTime() - dueDate.getTime());
        return diff;
    }
}