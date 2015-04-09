package com.example.schedule_test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private ArrayList<String> mTimeArray = new ArrayList<String>();
	private ArrayList<String> mScheduleGridArray = new ArrayList<String>();
	private int mGridHigh = 60; 
	private ScrollView scrollView;
	private int mScheduleGridCount = 48*7;
	SimpleDateFormat df = new SimpleDateFormat("HH:mm");
	
	/**
	 * parameters for interaction events
	 */
	private int mActivedColumn = -1;	// -1 mean not in active mode
	private TextView mStartIndicator;
	private TextView mEndIndicator;
	private enum TouchState{
		TOUCH_STATE_CLICK,
		TOUCH_STATE_MOVE,
		TOUCH_STATE_RESTING
	}
	private TouchState mTouchState = TouchState.TOUCH_STATE_RESTING;
	private RelativeLayout calendarLayout; 
	private ArrayList<Object> selectedArray = new ArrayList<Object>();
	private View lastTouchedView;
	private boolean selectionStart = false;
	private int editingScheduleIndex = -1;
	
	/**
	 * hardcore test data
	 */
	JSONArray scheduleData = new JSONArray();
	JSONObject temp = new JSONObject();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/**
		 * hardcore test data
		 */
		try {
			temp = new JSONObject();
			temp.put("weekday", "1");
			temp.put("start", "03:30");
			temp.put("end", "06:00");
			scheduleData.put(temp);
			temp = new JSONObject();
			temp.put("weekday", "2");
			temp.put("start", "03:00");
			temp.put("end", "06:30");
			scheduleData.put(temp);
			temp = new JSONObject();
			temp.put("weekday", "3");
			temp.put("start", "00:30");
			temp.put("end", "06:30");
			scheduleData.put(temp);
			temp = new JSONObject();
			temp.put("weekday", "4");
			temp.put("start", "03:00");
			temp.put("end", "06:30");
			scheduleData.put(temp);
			temp = new JSONObject();
			temp.put("weekday", "5");
			temp.put("start", "03:30");
			temp.put("end", "06:00");
			scheduleData.put(temp);
			timeToGridPositions(scheduleData.getJSONObject(0));
		} catch (JSONException e1) {
			
		}
		//set mScheduleGrid
		Date d1;
		Calendar cal = Calendar.getInstance();
		try {
			d1 = df.parse("00:00");
			cal.setTime(d1);
		} catch (ParseException e) {
			
		}		
		for(int i=0; i < 48; i++){
			mTimeArray.add(df.format(cal.getTime()));
			cal.add(Calendar.MINUTE, 30);
		}
		//set mScheduleGridArray
		for(int i=0; i < mScheduleGridCount; i++){
			mScheduleGridArray.add(Integer.toString(i));
		}
		
		calendarLayout = (RelativeLayout) findViewById(R.id.calendar_layout);
		scrollView = (ScrollView) findViewById(R.id.scroll_view);
		
		ExpandableHeightGridView gridView = (ExpandableHeightGridView) findViewById(R.id.gridview_time);
		gridView.setEnabled(false);
		gridView.setAdapter(new TimeAdapter(this, mTimeArray));
		gridView.setExpanded(true);
		
		final ExpandableHeightGridView gridViewDays = (ExpandableHeightGridView) findViewById(R.id.gridview_days);
		gridViewDays.setAdapter(new TimeAdapter(this, mScheduleGridArray));
		gridViewDays.setExpanded(true);
		
		gridViewDays.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
			    @Override
			    public void onGlobalLayout() {
			    	// draw existed schedule on UI
			    	for(int i=0; i < scheduleData.length(); i++){
			    		try {
			    			ArrayList<Object> gridPositions = timeToGridPositions(scheduleData.getJSONObject(i));
			    			for(int m=0; m < gridPositions.size(); m++){
			    				View grid = gridViewDays.getChildAt((int) gridPositions.get(m));
			    				grid.setBackgroundColor(Color.GRAY);
			    			}
			    		} catch (Exception e) {
			    				
			    		}	
			    	}
			        // unregister listener (this is important)
			    	gridViewDays.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			    }
			}
		);
		
		mStartIndicator = (TextView) findViewById(R.id.indicator_start);
		mStartIndicator.setBackgroundColor(Color.YELLOW);
		mEndIndicator = (TextView) findViewById(R.id.indicator_end);
		mEndIndicator.setBackgroundColor(Color.YELLOW);

		gridViewDays.setOnTouchListener(new View.OnTouchListener(){
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
            	float currentXPosition = event.getX();
                float currentYPosition = event.getY();
                int position = gridViewDays.pointToPosition((int) currentXPosition, (int) currentYPosition);
                View touchedView = gridViewDays.getChildAt(position);
             
	        	switch (event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	mTouchState = TouchState.TOUCH_STATE_CLICK;	
		            	// we don't know if it's a click or a scroll yet, but until we know
		                // assume it's a click	 
		            	if(mActivedColumn != -1){
	            			initialMovement(touchedView);
	            		}           	
		                break;
	
		            case MotionEvent.ACTION_MOVE:
		            	//System.out.println(mActivedColumn + " mActivedColumn");
		            	if(mActivedColumn != -1){
			            	mTouchState = TouchState.TOUCH_STATE_MOVE;	
			            	moveSelection(touchedView, gridViewDays);
		            	}
		                break;
	
		            case MotionEvent.ACTION_UP:
		            	if(mTouchState == TouchState.TOUCH_STATE_CLICK){
		            		if(mActivedColumn == -1){
		            			activeSelection(touchedView, gridViewDays);
		            		}else if(!checkIfSameColumn((int)touchedView.getTag())){
		            			mTouchState = TouchState.TOUCH_STATE_RESTING;
		            			inactiveSelection();
		            		}
		            	}
		                break;
	
		            default:
		                //inactiveSelection();
		                break;
		        }
	            return true;
	        }
	    });
	}
	public void initialMovement(View touchedView){
		lastTouchedView = touchedView;
		int thisTag = (int) touchedView.getTag();
		HashMap<String, Integer> maxAndMinPosition = getMaxAndMinPosition();
		if(thisTag == maxAndMinPosition.get("min") || thisTag == maxAndMinPosition.get("max")){
			selectionStart = true;
		}else{
			selectionStart = false;
		}
	}
	public void activeSelection(View touchedView, ExpandableHeightGridView gridViewDays){
		if(touchedView.getTag() != null){
			touchedView.setBackgroundColor(Color.GRAY);
	    	mActivedColumn = ((int) touchedView.getTag()%7);
	    	System.out.println((int) touchedView.getTag() + " activeSelection");
	    	disableScroll(scrollView);
	    	// keep track of the selection of this time
	    	selectedArray.clear();
	    	for(int i=0; i < scheduleData.length(); i++){
	    		try {
	    			ArrayList<Object> gridPositions = timeToGridPositions(scheduleData.getJSONObject(i));
	    			for(int m=0; m < gridPositions.size(); m++){
	    				if(touchedView.getTag() == gridPositions.get(m)){
	    					selectedArray = gridPositions;
	    					editingScheduleIndex = i; 
	    					break;
	    				}
	    			}
	    			if(editingScheduleIndex >= 0){
	    				break;
	    			}
	    		} catch (Exception e) {
	    				
	    		}	
	    	}
	    	if(editingScheduleIndex < 0){
	    		selectedArray.add(touchedView.getTag());
	    	}	    	
	    	selectionStart = false;
	    	showIndicators(gridViewDays);
		}
	}
	public void moveSelection(View touchedView, ExpandableHeightGridView gridViewDays){
		scrollView.requestDisallowInterceptTouchEvent(true);
		if(selectionStart){
			Object currentTag = touchedView.getTag();
			Object lastTag = lastTouchedView.getTag();
			if(lastTouchedView != touchedView){
				if(selectedArray.contains(lastTag) && !selectedArray.contains(currentTag)){
					selectedArray.add(touchedView.getTag());
					touchedView.setBackgroundColor(Color.GRAY);
					showIndicators(gridViewDays);
				}else if(selectedArray.contains(lastTag) && selectedArray.contains(currentTag)){
					selectedArray.remove(lastTag);
					lastTouchedView.setBackgroundColor(Color.WHITE);
					showIndicators(gridViewDays);
				}				
				lastTouchedView = touchedView;
			}
			//adjust indicators position
		}
	}
	public void inactiveSelection(){
		System.out.println("inactiveSelection");
		mActivedColumn = -1;
		if(editingScheduleIndex >= 0){
			HashMap<String, Integer> maxAndMinPosition = getMaxAndMinPosition();
			try {
				JSONObject startTimeObject = gridPositionToTime(maxAndMinPosition.get("min"));
				JSONObject endTimeObject = gridPositionToTime(maxAndMinPosition.get("max"));
				JSONObject thisSchedule = scheduleData.getJSONObject(editingScheduleIndex);
				thisSchedule.put("weekday", startTimeObject.getInt("startTimeObject"));
				thisSchedule.put("start", startTimeObject.getString("start"));
				thisSchedule.put("end", endTimeObject.getString("start"));
			} catch (Exception e) {
				
			}			
		}
		editingScheduleIndex = -1;
		selectionStart = false;
		hideIndicators();
		enableScroll(scrollView);
	}	
	public boolean checkIfSameColumn(int viewTag){
		int thisColumn = viewTag % 7;
		if(mActivedColumn == thisColumn){
			return true;
		}else{
			return false;
		}
	}
	public void showIndicators(ExpandableHeightGridView gridViewDays){
		mStartIndicator.setText("start");
		mStartIndicator.setVisibility(View.VISIBLE);
		mEndIndicator.setText("end");
		mEndIndicator.setVisibility(View.VISIBLE);
		
		HashMap<String, Integer> maxAndMinPosition = getMaxAndMinPosition();
		int minPosition = maxAndMinPosition.get("min");
		int maxPosition = maxAndMinPosition.get("max");
		View startView = gridViewDays.getChildAt(minPosition);
		View endView = gridViewDays.getChildAt(maxPosition);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int)startView.getX();
		params.topMargin = (int)startView.getY() - mGridHigh/3 ;
		((ViewGroup)mStartIndicator.getParent()).removeView(mStartIndicator);
		calendarLayout.addView(mStartIndicator, params);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int)endView.getX();
		params.topMargin = (int)endView.getY() + mGridHigh/3*2 ;
		((ViewGroup)mEndIndicator.getParent()).removeView(mEndIndicator);
		calendarLayout.addView(mEndIndicator, params);
	}
	public void hideIndicators(){
		mStartIndicator.setVisibility(View.INVISIBLE);
		mEndIndicator.setVisibility(View.INVISIBLE);
	}
	public void disableScroll (ScrollView scrollView){
		scrollView.requestDisallowInterceptTouchEvent(true);
		/*scrollView.setOnTouchListener( new OnTouchListener(){ 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});*/
	}
	public void enableScroll (ScrollView scrollView){
		scrollView.requestDisallowInterceptTouchEvent(false);
		//scrollView.setOnTouchListener(null);    
	}
	public HashMap<String, Integer> getMaxAndMinPosition(){
		int maxPosition = (int)selectedArray.get(0);
		int minPosition = (int)selectedArray.get(0);
		for(int i=0; i < selectedArray.size(); i++){
			int tempTag = (int) selectedArray.get(i);
			if(tempTag < minPosition){
				minPosition = tempTag;
			}
			if(tempTag > maxPosition){
				maxPosition = tempTag;
			}
		}
		HashMap<String, Integer> maxAndMinPosition = new HashMap<String, Integer>();
		maxAndMinPosition.put("max", maxPosition);
		maxAndMinPosition.put("min", minPosition);
		return maxAndMinPosition;
	}
	public ArrayList<Object> timeToGridPositions(JSONObject thisTime){
		ArrayList<Object> gridPositions = new ArrayList<Object>();		
		try {
			int thisWeekday = thisTime.getInt("weekday");
			Date thisStart = df.parse(thisTime.getString("start"));
			Date thisEnd = df.parse(thisTime.getString("end"));
			Calendar thisStartCal = Calendar.getInstance();
			Calendar thisEndCal = Calendar.getInstance();
			thisStartCal.setTime(thisStart);
			thisEndCal.setTime(thisEnd);
			Calendar tempCal = thisStartCal;
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse("00:00"));
			while(tempCal.compareTo(thisEndCal) < 0){
				int rowNumber = (int) (tempCal.getTimeInMillis() - cal.getTimeInMillis()) / 1800000;	// divided by 30 minutes
				int position = 7 * rowNumber + thisWeekday;
				gridPositions.add(position);
				tempCal.add(Calendar.MINUTE, 30);
			}
		} catch (Exception e) {
			
		}
		return gridPositions;
	}
	
	public JSONObject gridPositionToTime(int position){
		int thisColumn = position%7;
	    int thisRow = (int) Math.floor(position / 7);
		temp = new JSONObject();
		try {
		    Date d_this = df.parse(mTimeArray.get(thisRow));
			temp.put("weekday", thisColumn);
			temp.put("start", df.format(d_this.getTime()));
			Date d1;
			Calendar cal = Calendar.getInstance();
			d1 = df.parse(df.format(d_this.getTime()));
			cal.setTime(d1);
			cal.add(Calendar.MINUTE, 30);
			temp.put("end", df.format(cal.getTime()));
		} catch (Exception e) {
			
		}		
		return temp;
	}
	
	public JSONObject checkIfInExistedSchedule(JSONObject thisTimeObject){
		try {
			for(int i=0; i < scheduleData.length(); i++){
				JSONObject schedule_temp = new JSONObject();
				schedule_temp = scheduleData.getJSONObject(i);
				int temp_weekday = schedule_temp.getInt("weekday");
				String temp_start = schedule_temp.getString("start");
				String temp_end = schedule_temp.getString("end");
					
				int thisWeekday = thisTimeObject.getInt("weekday");
				if(thisWeekday == temp_weekday){
					Date d_start = df.parse(temp_start);
					Date d_end = df.parse(temp_end);
					Date d_this = df.parse(thisTimeObject.getString("start"));
					if(d_this.compareTo(d_start) >= 0
							&& d_this.compareTo(d_end) < 0){
						return schedule_temp;
					}
				}
		    }
	    } catch (Exception e) {
				
		} 
		return new JSONObject();
	}
	public class TimeAdapter extends BaseAdapter {
	    private Context mContext;
	    private ArrayList<String> mGridArray;
	    
	    public TimeAdapter(Context c, ArrayList<String> itemArray) {
	        mContext = c;
	        mGridArray = itemArray;
	    }
	    @Override
	    public int getCount() {
	        return mGridArray.size();
	    }
	    @Override
	    public Object getItem(int position) {
	        return null;
	    }
	    @Override
	    public long getItemId(int position) {
	        return 0;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        View gridView;
	        if (convertView == null) {
	        	gridView = new View(mContext);
	            gridView = inflater.inflate(R.layout.schedule_gridview_item, null);
	            //gridView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, mGridHigh));
		        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mGridHigh);
		        gridView.setLayoutParams(params);
	        } else {
	            gridView = (View) convertView;
	        }
	        TextView textView = (TextView) gridView.findViewById(R.id.grid_item_label);
	        textView.setText(mGridArray.get(position));	  
	        gridView.setTag(position);
	        
	        /*
	        if(getCount() > 50){	// if true, it's schedule grid
	        	temp = new JSONObject();
	        	temp = gridPositionToTime(position);
	        	JSONObject schedule_temp = new JSONObject();
	        	schedule_temp = checkIfInExistedSchedule(temp);
	        	String schedule_weekday;
				try {
					schedule_weekday = schedule_temp.getString("weekday");
		        	if(schedule_weekday != null){
		        		gridView.setBackgroundColor(Color.GRAY);
		        	}
				} catch (JSONException e) {
					
				}
	        }*/
	        return gridView;
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
