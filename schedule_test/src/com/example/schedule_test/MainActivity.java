package com.example.schedule_test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private ArrayList<String> mTimeArray = new ArrayList<String>();
	//private ArrayList<String> mScheduleDayGridArray = new ArrayList<String>();
	private int GRID_HEIGHT = 60; 
	private ScrollView scrollView;
	private int WEEKDAY_GRID_COUNT = 48;
	private int INDICATOR_PADDING = 8;
	SimpleDateFormat df = new SimpleDateFormat("HH:mm");
	View viewInteraction;
	View selectedGridView;

	private TextView mStartIndicator;
	private TextView mEndIndicator;
	private ImageView mUpIndicator;
	private ImageView mDownIndicator;
	/**
	 * parameters for interaction events
	 */
	private String mSelectActivedDay = "";	
	private int mSelectStartPosition = -1;
	private int mSelectEndPosition = -1;
	private enum TouchState{
		TOUCH_STATE_CLICK,
		TOUCH_STATE_MOVE,
		TOUCH_STATE_RESTING
	}
	private TouchState mGridInteractionTouchState = TouchState.TOUCH_STATE_RESTING;
	private TouchState mWeekdayTouchState = TouchState.TOUCH_STATE_RESTING;
	private ArrayList<View> mWeekdayGridViews = new ArrayList<View>();
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
		for(int i=0; i < WEEKDAY_GRID_COUNT; i++){
			mTimeArray.add(df.format(cal.getTime()));
			cal.add(Calendar.MINUTE, 30);
		}
		//set mScheduleGridArray
		//for(int i=0; i < mScheduleGridCount; i++){
		//	mScheduleGridArray.add(Integer.toString(i));
		//}
		
		scrollView = (ScrollView) findViewById(R.id.scroll_view);
		
		ExpandableHeightGridView gridView = (ExpandableHeightGridView) findViewById(R.id.gridview_time);
		gridView.setEnabled(false);
		gridView.setAdapter(new TimeAdapter(this, mTimeArray));
		gridView.setExpanded(true);
		
		ScheduleTimeTouchEvent scheduleTimeTouchEvent = new ScheduleTimeTouchEvent();
		WeekDayGridTouchEvent weekdayGridClickEvent = new WeekDayGridTouchEvent();
		// Sun to Sat grid views
		final ExpandableHeightGridView gridViewSun = (ExpandableHeightGridView) findViewById(R.id.gridview_sun);
		gridViewSun.setAdapter(new TimeAdapter(this, mTimeArray));
		gridViewSun.setExpanded(true);
		gridViewSun.setOnTouchListener(weekdayGridClickEvent);
		gridViewSun.setTag("gridview_sun");
		mWeekdayGridViews.add(gridViewSun);

		final ExpandableHeightGridView gridViewMon = (ExpandableHeightGridView) findViewById(R.id.gridview_mon);
		gridViewMon.setAdapter(new TimeAdapter(this, mTimeArray));
		gridViewMon.setExpanded(true);
		gridViewMon.setOnTouchListener(weekdayGridClickEvent);
		gridViewMon.setTag("gridview_mon");
		mWeekdayGridViews.add(gridViewMon);

		final ExpandableHeightGridView gridViewTue = (ExpandableHeightGridView) findViewById(R.id.gridview_tue);
		gridViewTue.setAdapter(new TimeAdapter(this, mTimeArray));
		gridViewTue.setExpanded(true);
		gridViewTue.setOnTouchListener(weekdayGridClickEvent);
		gridViewTue.setTag("gridview_tue");
		mWeekdayGridViews.add(gridViewTue);

		final ExpandableHeightGridView gridViewWed = (ExpandableHeightGridView) findViewById(R.id.gridview_wed);
		gridViewWed.setAdapter(new TimeAdapter(this, mTimeArray));
		gridViewWed.setExpanded(true);
		gridViewWed.setOnTouchListener(weekdayGridClickEvent);
		gridViewWed.setTag("gridview_wed");
		mWeekdayGridViews.add(gridViewWed);

		final ExpandableHeightGridView gridViewThr = (ExpandableHeightGridView) findViewById(R.id.gridview_thr);
		gridViewThr.setAdapter(new TimeAdapter(this, mTimeArray));
		gridViewThr.setExpanded(true);
		gridViewThr.setOnTouchListener(weekdayGridClickEvent);
		gridViewThr.setTag("gridview_thr");
		mWeekdayGridViews.add(gridViewThr);

		final ExpandableHeightGridView gridViewFri = (ExpandableHeightGridView) findViewById(R.id.gridview_fri);
		gridViewFri.setAdapter(new TimeAdapter(this, mTimeArray));
		gridViewFri.setExpanded(true);
		gridViewFri.setOnTouchListener(weekdayGridClickEvent);
		gridViewFri.setTag("gridview_fri");
		mWeekdayGridViews.add(gridViewFri);

		final ExpandableHeightGridView gridViewSat = (ExpandableHeightGridView) findViewById(R.id.gridview_sat);
		gridViewSat.setAdapter(new TimeAdapter(this, mTimeArray));
		gridViewSat.setExpanded(true);
		gridViewSat.setOnTouchListener(weekdayGridClickEvent);
		gridViewSat.setTag("gridview_sat");
		mWeekdayGridViews.add(gridViewSat);
		
		viewInteraction = (View) findViewById(R.id.view_interaction);
		//viewInteraction.setAdapter(new TimeAdapter(this, mTimeArray));
		//viewInteraction.setExpanded(true);
		viewInteraction.setOnTouchListener(scheduleTimeTouchEvent);
		viewInteraction.setTag("view_interaction");
		
		/*
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
		);*/
		
		mStartIndicator = (TextView) findViewById(R.id.indicator_start);
		mEndIndicator = (TextView) findViewById(R.id.indicator_end);
		mUpIndicator = (ImageView) findViewById(R.id.indicator_up);
		mDownIndicator = (ImageView) findViewById(R.id.indicator_down);
	}
	public void initialMovement(View touchedView){
		lastTouchedView = touchedView;
		int thisPosition = (int) touchedView.getTag();
		if(thisPosition == mSelectStartPosition || thisPosition == mSelectEndPosition ){
			selectionStart = true;
		}else{
			selectionStart = false;
		}
	}
	
	public void activeSelection(View touchedView, ExpandableHeightGridView gridView){
		mSelectActivedDay = (String) gridView.getTag();
		if(touchedView.getTag() != null){
			touchedView.setBackgroundColor(Color.GRAY);
			int selectionStartPosition = (int)touchedView.getTag();
	    	System.out.println(selectionStartPosition + " activeSelection");
	    	disableScroll(scrollView);	    
	    	
	    	int selectionEndPosition = selectionStartPosition;
	    	
	    	// keep track of the selection of this time
	    	/*selectedArray.clear();
	    	for(int i=0; i < scheduleData.length(); i++){
	    		try {
	    			ArrayList<Object> gridPositions = timeToGridPositions(scheduleData.getJSONObject(i));
	    			for(int m=0; m < gridPositions.size(); m++){
	    				if((int)touchedView.getTag() == (int)gridPositions.get(m)){
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
	    	}	    	*/
	    	selectionStart = false;
	    	mSelectStartPosition = selectionStartPosition;
	    	mSelectEndPosition = selectionEndPosition;
	    	showIndicators(gridView);
		}
	}
	
	public void moveSelection(View touchedView, ExpandableHeightGridView gridView){
		scrollView.requestDisallowInterceptTouchEvent(true);
		if(selectionStart && touchedView.getTag() != null){
			int currentPosition = (int) touchedView.getTag();
			int lastPosition = (int) lastTouchedView.getTag();
			if(lastTouchedView != touchedView){				
				if((lastPosition >= mSelectStartPosition && lastPosition <= mSelectEndPosition) 
						&& !(currentPosition >= mSelectStartPosition && currentPosition <= mSelectEndPosition)){
					addSelectTime(currentPosition);
					touchedView.setBackgroundColor(Color.GRAY);
					showIndicators(gridView);
				}else if((lastPosition >= mSelectStartPosition && lastPosition <= mSelectEndPosition) 
						&& (currentPosition >= mSelectStartPosition && currentPosition <= mSelectEndPosition)){
					removeSelectTime(lastPosition);
					lastTouchedView.setBackgroundColor(Color.WHITE);
					showIndicators(gridView);
				}				
				lastTouchedView = touchedView;
			}
		}
	}
	public void inactiveSelection(){
		System.out.println("inactiveSelection");
		mSelectActivedDay = "";
		/*if(editingScheduleIndex >= 0){
			HashMap<String, Integer> maxAndMinPosition = getMaxAndMinPosition();
			try {
				JSONObject startTimeObject = gridPositionToTime(maxAndMinPosition.get("min"));
				JSONObject endTimeObject = gridPositionToTime(maxAndMinPosition.get("max"));
				JSONObject thisSchedule = scheduleData.getJSONObject(editingScheduleIndex);
				thisSchedule.put("weekday", startTimeObject.getInt("weekday"));
				thisSchedule.put("start", startTimeObject.getString("start"));
				thisSchedule.put("end", endTimeObject.getString("end"));
			} catch (Exception e) {
				
			}			
		}else{
			HashMap<String, Integer> maxAndMinPosition = getMaxAndMinPosition();
			try {
				JSONObject startTimeObject = gridPositionToTime(maxAndMinPosition.get("min"));
				JSONObject endTimeObject = gridPositionToTime(maxAndMinPosition.get("max"));
				JSONObject thisSchedule = new JSONObject();
				thisSchedule.put("weekday", startTimeObject.getInt("weekday"));
				thisSchedule.put("start", startTimeObject.getString("start"));
				thisSchedule.put("end", endTimeObject.getString("end"));
				scheduleData.put(thisSchedule);
			} catch (Exception e) {
				
			}	
		}
		editingScheduleIndex = -1;*/
		selectionStart = false;
		hideIndicators();
		enableScroll(scrollView);
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
	}/*
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
	}*/
	public int timeToGridPosition(String thisTime){
		int gridPosition = 0;		
		try {
			Calendar thisCal = Calendar.getInstance();
			thisCal.setTime(df.parse(thisTime));
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse("00:00"));
			gridPosition = (int) (thisCal.getTimeInMillis() - cal.getTimeInMillis()) / 1800000;	// divided by 30 minutes
		} catch (Exception e) {
			
		}
		return gridPosition;
	}
	public String gridPositionToTime(int position){
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse("00:00"));
		} catch (ParseException e) {
			
		}	
		cal.add(Calendar.MINUTE, 30 * position);
		String thisTime = df.format(cal.getTime());
		return thisTime;
	}
	public void addSelectTime(int thisPosition){
		if(thisPosition < mSelectStartPosition){
			mSelectStartPosition = thisPosition;
		}else if(thisPosition > mSelectEndPosition){
			mSelectEndPosition = thisPosition;
		}
	}
	public void removeSelectTime(int thisPosition){
		if(thisPosition == mSelectStartPosition){
			mSelectStartPosition = thisPosition + 1;
		}else if(thisPosition == mSelectEndPosition){
			mSelectEndPosition = thisPosition - 1;
		}
	}
	
	/*
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
	}*/	
	public void checkIfPressOnArros(){
		
	}
	public void drawSelectedSchedule(ExpandableHeightGridView gridView){		
		int minPosition = mSelectStartPosition;
		int maxPosition = mSelectEndPosition;
		View startView = gridView.getChildAt(minPosition);
		View endView = gridView.getChildAt(maxPosition);
		startView.setBackgroundColor(getResources().getColor(R.color.schedule_grey));
		endView.setBackgroundColor(getResources().getColor(R.color.schedule_grey));
	}
	public void showIndicators(ExpandableHeightGridView gridView){	
		final ExpandableHeightGridView thisGridView = gridView;
		int minPosition = mSelectStartPosition;
		int maxPosition = mSelectEndPosition;
		final View startView = gridView.getChildAt(minPosition);
		final View endView = gridView.getChildAt(maxPosition);
		mStartIndicator.setText(gridPositionToTime(minPosition));
		mEndIndicator.setText(gridPositionToTime(maxPosition));
		
		mStartIndicator.setVisibility(View.VISIBLE);
		mEndIndicator.setVisibility(View.VISIBLE);
		
		mStartIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
					int marginTop = calendarLayout.getTop();
					int indicatorX = (int)thisGridView.getX() - mStartIndicator.getWidth() - INDICATOR_PADDING;
					int indicatorY = (int)startView.getY() - mStartIndicator.getHeight()/2 + marginTop;
					mStartIndicator.setX(indicatorX);
					mStartIndicator.setY(indicatorY);
					mStartIndicator.setZ(2);
				}
			}
		);
		mEndIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
					int marginTop = calendarLayout.getTop();
					int indicatorX = (int)thisGridView.getX() - mStartIndicator.getWidth() - INDICATOR_PADDING;
					int indicatorY = (int)endView.getY() + endView.getHeight() - mEndIndicator.getHeight()/2 + marginTop;
					mEndIndicator.setX(indicatorX);
					mEndIndicator.setY(indicatorY);
				}
			}
		);

		mUpIndicator.setVisibility(View.VISIBLE);
		mDownIndicator.setVisibility(View.VISIBLE);
		
		mUpIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
					int marginTop = calendarLayout.getTop();
					int indicatorX = (int)thisGridView.getX() + thisGridView.getWidth()/2 - mUpIndicator.getWidth()/2;
					int indicatorY = (int)startView.getY() - mUpIndicator.getHeight() - INDICATOR_PADDING + marginTop;
					mUpIndicator.setX(indicatorX);
					mUpIndicator.setY(indicatorY);
				}
			}
		);
		mDownIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
					int marginTop = calendarLayout.getTop();
					int indicatorX = (int)thisGridView.getX() + thisGridView.getWidth()/2 - mDownIndicator.getWidth()/2;
					int indicatorY = (int)endView.getY() + endView.getHeight() + INDICATOR_PADDING + marginTop;
					mDownIndicator.setX(indicatorX);
					mDownIndicator.setY(indicatorY);
				}
			}
		);
		
		drawSelectedSchedule(gridView);
	}
	public void hideIndicators(){
		mStartIndicator.setVisibility(View.INVISIBLE);
		mEndIndicator.setVisibility(View.INVISIBLE);
		mUpIndicator.setVisibility(View.INVISIBLE);
		mDownIndicator.setVisibility(View.INVISIBLE);
	}
	public void enableInteractGrid(View touchedWeekdayGridView, int touchedPosition){
		mSelectActivedDay = (String) touchedWeekdayGridView.getTag();
		selectedGridView = touchedWeekdayGridView;
		float thisViewX = touchedWeekdayGridView.getX();
		float thisViewY = touchedWeekdayGridView.getY();
		int thisWidth = touchedWeekdayGridView.getWidth();
		int thisHeight = touchedWeekdayGridView.getHeight();
		viewInteraction.setX(thisViewX);
		viewInteraction.setY(thisViewY);
		viewInteraction.setLayoutParams(new RelativeLayout.LayoutParams(thisWidth, thisHeight));
		System.out.println("gridViewInteraction y: " + thisViewY);
		viewInteraction.setVisibility(View.VISIBLE);
		viewInteraction.bringToFront();
		
		mSelectStartPosition = touchedPosition;
		mSelectEndPosition = touchedPosition;
		showIndicators((ExpandableHeightGridView)touchedWeekdayGridView);
		//disableScroll(scrollView);
	}
	public void disableInteractGrid(){
		WeekDayGridTouchEvent weekdayGridClickEvent = new WeekDayGridTouchEvent();
		// disable interaction grid view
		viewInteraction.setVisibility(View.GONE);
		hideIndicators();
		//enableScroll(scrollView);
		mSelectActivedDay = "";
	}
	public class WeekDayGridTouchEvent implements View.OnTouchListener{
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			float currentXPosition = event.getX();
            float currentYPosition = event.getY();
            ExpandableHeightGridView thisView = (ExpandableHeightGridView) v;
            int position = thisView.pointToPosition((int) currentXPosition, (int) currentYPosition);
            
			switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	mWeekdayTouchState = TouchState.TOUCH_STATE_CLICK;	
            	// we don't know if it's a click or a scroll yet, but until we know
                // assume it's a click	 
            	        	
                break;

            case MotionEvent.ACTION_MOVE:
            	mWeekdayTouchState = TouchState.TOUCH_STATE_MOVE;
            	
                break;

            case MotionEvent.ACTION_UP:
            	if(mWeekdayTouchState == TouchState.TOUCH_STATE_CLICK){
            		System.out.println("touchedWeekdayGridView:" + thisView.getTag());
            		System.out.println("position:" + position);
            		if(mSelectActivedDay.equals("")){
            			enableInteractGrid(thisView, position);
            		}else if(!mSelectActivedDay.equals((String) thisView.getTag())){
            			disableInteractGrid();
            		}
            		mWeekdayTouchState = TouchState.TOUCH_STATE_RESTING;
            	}else{
            		mWeekdayTouchState = TouchState.TOUCH_STATE_RESTING;
            	}
                break;

            default:
                
                break;
        }
        return true;
		}		
	}
	public class ScheduleTimeTouchEvent implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
        	float currentXPosition = event.getX();
            float currentYPosition = event.getY();
         
        	switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	mGridInteractionTouchState = TouchState.TOUCH_STATE_CLICK;

	                int position = ((ExpandableHeightGridView)selectedGridView).pointToPosition((int) currentXPosition, (int) currentYPosition);
	            	// we don't know if it's a click or a scroll yet, but until we know
	                // assume it's a click	 
	            	/*if(!mSelectActivedDay.equals("")){
            			initialMovement(touchedView);
            		} */    
	            	//System.out.println("interaction gird: " + thisView.getTag());
	            	System.out.println("interaction gird position: " + position);
	                break;

	            case MotionEvent.ACTION_MOVE:
	            	/*if(!mSelectActivedDay.equals("")){
	            		mGridInteractionTouchState = TouchState.TOUCH_STATE_MOVE;
		            	if(touchedView != null){
		            		moveSelection(touchedView, thisView);
		            	}
	            	}*/
	                break;

	            case MotionEvent.ACTION_UP:
	            	/*if(mGridInteractionTouchState == TouchState.TOUCH_STATE_CLICK){
	            		if(mSelectActivedDay.equals("")){
	            			activeSelection(touchedView, thisView);
	            		}else if(!mSelectActivedDay.equals((String) thisView.getTag())){
	            			mGridInteractionTouchState = TouchState.TOUCH_STATE_RESTING;
	            			inactiveSelection();
	            		}
	            	}*/
	                break;

	            default:
	                //inactiveSelection();
	                break;
	        }
            return true;
        }
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
		        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, GRID_HEIGHT);
		        gridView.setLayoutParams(params);
	        } else {
	            gridView = (View) convertView;
	        }
	        TextView textView = (TextView) gridView.findViewById(R.id.grid_item_label);
	        textView.setText(mGridArray.get(position));	  
	        gridView.setTag(position);
	        
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
