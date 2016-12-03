package nielsen.guiltmotivator;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by zlan on 11/7/16.
 *
 */
public class ContactAdapter extends ArrayAdapter<Contact> {
    public static int pos;
    private ArrayList<Contact> contacts;
    private DatabaseHelper mDbHelper = new DatabaseHelper(getContext());
    final SQLiteDatabase db = mDbHelper.getWritableDatabase();


    public ContactAdapter(Context context, ArrayList<Contact> items) {
        super(context, 0, items);
        this.contacts = items;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ContactHolder holder;
        //myDb = new DatabaseHelper(this.getContext());
        // Get the data item for this position
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView != null) {
            holder = (ContactHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
            holder = new ContactHolder();
            convertView.setTag(holder);
        }

        holder.contact = getItem(position);

        // I didn't figure out how to implement ButterKnife here.
        // Because all elements here is a property of the holder class

        holder.name = (TextView) convertView.findViewById(R.id.itemName);
        holder.method = (TextView) convertView.findViewById(R.id.itemMethod);
        holder.delete = (ImageButton) convertView.findViewById(R.id.delete);

        holder.name.setText(holder.contact.getName());
        holder.method.setText(holder.contact.getMethod());

        // create onClickListener to edit the contact info
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //later.
            }
        });
        
        // Create onClickListener for delete Button
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact deleted = holder.contact;
                remove(deleted); //remove the item from the adapter
                mDbHelper.deleteContact(deleted);
                notifyDataSetChanged();
            }
        });
        convertView.setTag(holder);
        setupItem(holder);
        // Return the completed view to render on screen
        return convertView;
    }
    // set the TextView
    private void setupItem(ContactHolder holder){
        holder.name.setText(holder.contact.getName());
        holder.method.setText(holder.contact.getMethod());
    }

    public static class ContactHolder {
        TextView name;
        TextView method;
        ImageButton delete;
        Contact contact;
    }
}
