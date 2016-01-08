package com.locution.hereyak;

import java.util.ArrayList;
import java.util.Collections;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.content.Context;
import android.text.Editable;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class YakAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private ArrayList<YakMessage> data;
    private DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeNoMillis();
    private String username = "";
    private SparseIntArray queueIndexes; 
    private int lastItemAddedIndex;

    public YakAdapter(Context context, ArrayList<YakMessage> data, String username){
	    // Caches the LayoutInflater for quicker use
	    this.inflater = LayoutInflater.from(context);
	    // Sets the events data
	    Collections.sort(data);
	    this.data = data;
	    this.username=username;
	    this.queueIndexes = new SparseIntArray();
	    lastItemAddedIndex = getCount()-1;
    }

    public int getCount() {
        return this.data.size();
    }
    
    public ArrayList<YakMessage> getData() {
    	return this.data;
    }

    public YakMessage getItem(int position) throws IndexOutOfBoundsException{
        return this.data.get(position);
    }

    public long getItemId(int position) throws IndexOutOfBoundsException{
        if(position < getCount() && position >= 0 ){
            return position;
        }
        return 0;
    }

    public int getViewTypeCount(){
        return 1;
    }
    
    public int getLastItemAddedIndex(){
    	return lastItemAddedIndex;
    }
    
    public int addQueueItem(YakMessage newData) {
    	data.add(newData);
    	notifyDataSetChanged();
    	queueIndexes.append(getCount()-1, 1);
    	return getCount()-1;
    }
    
    public void setQueueItemToFailed(int index) {
    	queueIndexes.put(index, 0);
    }
    
    public void setQueueItemToSent(int index) {
    	queueIndexes.put(index, 1);
    }
    
    public void removeSentQueueItems() {
    	int key = 0;
    	for(int i = 0; i < queueIndexes.size(); i++) {
    	   key = queueIndexes.keyAt(i);
    	   if (queueIndexes.get(key) == 1) {
    		   data.remove(key);
    	   }
    	}
    }
    
   
    public void add(ArrayList<YakMessage> newData) {
    	Collections.sort(newData);
    	data.addAll(newData);
    	notifyDataSetChanged();
    	lastItemAddedIndex = getCount()-1;
    }
    
    public void set(ArrayList<YakMessage> newData) {
    	Collections.sort(newData);
    	data = newData;
    	notifyDataSetChanged();
    	lastItemAddedIndex = getCount()-1;
    }

    public View getView(int position, View convertView, ViewGroup parent){
    	YakMessage myData = getItem(position);           

        if(convertView == null){ // If the View is not cached
            // Inflates the Common View from XML file
            convertView = this.inflater.inflate(R.layout.listview_row, null);
        }
        
        
        //DateUtils.getRelativeDateTimeString(context,parser2.parseMillis(myData.getPostDate()),DateUtils.MINUTE_IN_MILLIS,DateUtils.WEEK_IN_MILLIS,0)
        
        Time now = new Time();
        now.setToNow();
        
        TextView usernameView = (TextView) convertView.findViewById(R.id.username);
        
        if (myData.getUsername().equals(username)) {
        	//LinearLayout ll =  (LinearLayout) convertView.findViewById(R.id.messagecontent_box);
        	//ll.setBackgroundResource(R.drawable.messagebox);
        	usernameView.setText("me <"+myData.getUsername()+">");
        } else {
        	usernameView.setText(myData.getUsername());
        }
        
        usernameView.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View view) {
				EditText messageBox = (EditText) ((View) view.getRootView()).findViewById(R.id.message_box);
				Editable editableText = messageBox.getText();
				editableText.append("@"+((TextView)view.findViewById(R.id.username)).getText()+" ");
				
			}
        });
        
        
        
        ((TextView)convertView.findViewById(R.id.time)).setText(DateUtils.getRelativeTimeSpanString(parser2.parseMillis(myData.getPostDate()),now.toMillis(true), DateUtils.MINUTE_IN_MILLIS));
        ((TextView)convertView.findViewById(R.id.likes)).setText("");
        
        ((TextView)convertView.findViewById(R.id.message_content)).setText(myData.getContent());
 
        return convertView;
    }


}