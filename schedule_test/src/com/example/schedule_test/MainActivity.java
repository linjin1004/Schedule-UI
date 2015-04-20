package com.example.schedule_test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private ArrayList<String> mTimeArray = new ArrayList<String>();
	//private ArrayList<String> mScheduleDayGridArray = new ArrayList<String>();
	private int GRID_HEIGHT = 30; 
	private int GRID_LEFT = 59; 
	private int GRID_TOP = 24;
	private ScrollView scrollView;
	private int WEEKDAY_GRID_COUNT = 48;
	private int INDICATOR_PADDING = 8;
	int indicatorPadding;
	int scheduleLeftMargin;
	int scheduleTopMargin;
	SimpleDateFormat df = new SimpleDateFormat("HH:mm");
	View viewInteraction;
	View mSelectedGridView;
	RelativeLayout mDrawSelectionLayout;

	private TextView mStartIndicator;
	private TextView mEndIndicator;
	private ImageView mUpIndicator;
	private ImageView mDownIndicator;
	private ImageView mDeleteButton;
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
	private float lastTouchedY;
	private int lastTouchedPosition;
	private enum SelectionStartPoint{
		UP_ARROW,
		DOWN_ARROW
	}
	private SelectionStartPoint mSelectionStartFrom = null;
	/**
	 * For schedule data
	 */	
	JSONArray scheduleData = new JSONArray();
	private String START_TIME = "start";
	private String END_TIME = "end";
	private String DAY = "day";
	private String SCHEDULE_ARRAY = "schedule_array";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/**
		 * hardcore test data
		 */
		JSONObject temp = new JSONObject();
		JSONArray tempArray = new JSONArray();
		JSONObject tempDayObject = new JSONObject();
		try {
			temp = new JSONObject();
			temp.put(START_TIME, "03:30");
			temp.put(END_TIME, "06:00");
			tempArray.put(temp);
			tempDayObject.put(DAY, "1");
			tempDayObject.put(SCHEDULE_ARRAY, tempArray);
			scheduleData.put(tempDayObject);
			temp = new JSONObject();
			tempArray = new JSONArray();
			tempDayObject = new JSONObject();
			temp.put(START_TIME, "03:00");
			temp.put(END_TIME, "06:30");
			tempArray.put(temp);
			tempDayObject.put(DAY, "2");
			tempDayObject.put(SCHEDULE_ARRAY, tempArray);
			scheduleData.put(tempDayObject);
			temp = new JSONObject();
			tempArray = new JSONArray();
			tempDayObject = new JSONObject();
			temp.put("start", "00:30");
			temp.put("end", "06:30");
			tempArray.put(temp);
			tempDayObject.put(DAY, "3");
			tempDayObject.put(SCHEDULE_ARRAY, tempArray);
			scheduleData.put(tempDayObject);
			temp = new JSONObject();
			tempArray = new JSONArray();
			tempDayObject = new JSONObject();
			temp.put("start", "03:00");
			temp.put("end", "06:30");
			tempArray.put(temp);
			tempDayObject.put(DAY, "4");
			tempDayObject.put(SCHEDULE_ARRAY, tempArray);
			scheduleData.put(tempDayObject);
			temp = new JSONObject();
			tempArray = new JSONArray();
			tempDayObject = new JSONObject();;
			temp.put("start", "03:30");
			temp.put("end", "06:00");
			tempArray.put(temp);
			tempDayObject.put(DAY, "5");
			tempDayObject.put(SCHEDULE_ARRAY, tempArray);
			scheduleData.put(tempDayObject);
		} catch (JSONException e1) {
			
		}
		//System.out.println(scheduleData);
		
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
		
		scrollView = (ScrollView) findViewById(R.id.scroll_view);
		final int scheduleGridHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, GRID_HEIGHT, getResources().getDisplayMetrics());
		scheduleLeftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, GRID_LEFT, getResources().getDisplayMetrics());
		indicatorPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_PADDING, getResources().getDisplayMetrics());
		scheduleTopMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, GRID_TOP, getResources().getDisplayMetrics());
		
		final ExpandableHeightGridView gridViewTime = (ExpandableHeightGridView) findViewById(R.id.gridview_time);
		gridViewTime.setEnabled(false);
		gridViewTime.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, false));
		gridViewTime.setExpanded(true);

		ScheduleTimeTouchEvent scheduleTimeTouchEvent = new ScheduleTimeTouchEvent();
		WeekDayGridTouchEvent weekdayGridClickEvent = new WeekDayGridTouchEvent();
		// Sun to Sat grid views
		final ExpandableHeightGridView gridViewSun = (ExpandableHeightGridView) findViewById(R.id.gridview_sun);
		gridViewSun.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, true));
		gridViewSun.setExpanded(true);
		gridViewSun.setOnTouchListener(weekdayGridClickEvent);
		gridViewSun.setTag("gridview_sun");
		mWeekdayGridViews.add(gridViewSun);

		final ExpandableHeightGridView gridViewMon = (ExpandableHeightGridView) findViewById(R.id.gridview_mon);
		gridViewMon.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, true));
		gridViewMon.setExpanded(true);
		gridViewMon.setOnTouchListener(weekdayGridClickEvent);
		gridViewMon.setTag("gridview_mon");
		mWeekdayGridViews.add(gridViewMon);

		final ExpandableHeightGridView gridViewTue = (ExpandableHeightGridView) findViewById(R.id.gridview_tue);
		gridViewTue.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, true));
		gridViewTue.setExpanded(true);
		gridViewTue.setOnTouchListener(weekdayGridClickEvent);
		gridViewTue.setTag("gridview_tue");
		mWeekdayGridViews.add(gridViewTue);

		final ExpandableHeightGridView gridViewWed = (ExpandableHeightGridView) findViewById(R.id.gridview_wed);
		gridViewWed.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, true));
		gridViewWed.setExpanded(true);
		gridViewWed.setOnTouchListener(weekdayGridClickEvent);
		gridViewWed.setTag("gridview_wed");
		mWeekdayGridViews.add(gridViewWed);

		final ExpandableHeightGridView gridViewThr = (ExpandableHeightGridView) findViewById(R.id.gridview_thr);
		gridViewThr.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, true));
		gridViewThr.setExpanded(true);
		gridViewThr.setOnTouchListener(weekdayGridClickEvent);
		gridViewThr.setTag("gridview_thr");
		mWeekdayGridViews.add(gridViewThr);

		final ExpandableHeightGridView gridViewFri = (ExpandableHeightGridView) findViewById(R.id.gridview_fri);
		gridViewFri.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, true));
		gridViewFri.setExpanded(true);
		gridViewFri.setOnTouchListener(weekdayGridClickEvent);
		gridViewFri.setTag("gridview_fri");
		mWeekdayGridViews.add(gridViewFri);

		final ExpandableHeightGridView gridViewSat = (ExpandableHeightGridView) findViewById(R.id.gridview_sat);
		gridViewSat.setAdapter(new TimeAdapter(this, mTimeArray, scheduleGridHeight, true));
		gridViewSat.setExpanded(true);
		gridViewSat.setOnTouchListener(weekdayGridClickEvent);
		gridViewSat.setTag("gridview_sat");
		mWeekdayGridViews.add(gridViewSat);
		
		mDrawSelectionLayout = (RelativeLayout) findViewById(R.id.layout_draw_selection);
		mDrawSelectionLayout.setTag("draw_selection_view");
		viewInteraction = (View) findViewById(R.id.view_interaction);
		viewInteraction.setOnTouchListener(scheduleTimeTouchEvent);
		viewInteraction.setTag("view_interaction");
		
		mStartIndicator = (TextView) findViewById(R.id.indicator_start);
		mEndIndicator = (TextView) findViewById(R.id.indicator_end);
		mUpIndicator = (ImageView) findViewById(R.id.indicator_up);
		mDownIndicator = (ImageView) findViewById(R.id.indicator_down);
		mDeleteButton = (ImageView) findViewById(R.id.indicator_delete);
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteSelection();
			}
		});
	}
	public void drawCurrentSchedule(){
		try {
			for(int i=0; i < scheduleData.length(); i++){
				JSONObject tempDayObject = new JSONObject();
				tempDayObject = scheduleData.getJSONObject(i);
				int thisDay = tempDayObject.getInt(DAY);
				ExpandableHeightGridView thisGridView = (ExpandableHeightGridView) mWeekdayGridViews.get(thisDay-1);
				JSONArray tempArray = new JSONArray();
				tempArray = tempDayObject.getJSONArray(SCHEDULE_ARRAY);
				for(int m=0; m < tempArray.length(); m++){
					JSONObject temp = new JSONObject();
					temp = (tempArray.getJSONObject(m));
					String startTime = temp.getString(START_TIME);
					String endTime = temp.getString(END_TIME);
					Calendar thisCal = Calendar.getInstance();
					thisCal.setTime(df.parse(endTime));
					thisCal.add(Calendar.MINUTE, -30);
					endTime = df.format(thisCal.getTime());
					int startPosition = timeToGridPosition(startTime);
					int endPosition = timeToGridPosition(endTime);
					for(int position = startPosition; position <= endPosition; position++ ){
						thisGridView.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_blue));
					}
				}				
			}	
		} catch (Exception e) {
			
		}
	}
	
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
	public void addGridToSchedule(int position){
		if(position >= 0){
			if(position < mSelectStartPosition){
				mSelectStartPosition = position;
			}else if(position > mSelectEndPosition){
				mSelectEndPosition = position;
			}
			showIndicators((ExpandableHeightGridView)mSelectedGridView);
		}
	}
	public void removeGridFromSchedule(int position){
		if(position >= 0){
			if(position == mSelectStartPosition){
				mSelectStartPosition = position + 1;
			}else if(position == mSelectEndPosition){
				mSelectEndPosition = position - 1;
			}
			showIndicators((ExpandableHeightGridView)mSelectedGridView);
		}
	}
	public void deleteSelection(){
		disableInteractGrid();
	}
	public void drawSelectedSchedule(ExpandableHeightGridView gridView, View startView, View endView){
		LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
		int marginTop = calendarLayout.getTop();
		int selectionX = scheduleLeftMargin + (int)gridView.getX();
		System.out.println("selectionX:" + selectionX);
		int selectionY = (int)startView.getTop() + marginTop;
		System.out.println("selectionY:"+ selectionY);
		int selectionBottomY = (int)endView.getTop() + endView.getHeight() + marginTop;
		int selectionWidth = startView.getWidth();
		int selectionHeight = selectionBottomY - selectionY;
		final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(selectionWidth, selectionHeight);
		lp.setMargins((int)selectionX, (int)selectionY, 0, 0);
		mDrawSelectionLayout.setLayoutParams(lp);
		mDrawSelectionLayout.setVisibility(View.VISIBLE);
	}
	public void showIndicators(ExpandableHeightGridView gridView){	
		final ExpandableHeightGridView thisGridView = gridView;
		int minPosition = mSelectStartPosition;
		int maxPosition = mSelectEndPosition;
		final View startView = gridView.getChildAt(minPosition);
		final View endView = gridView.getChildAt(maxPosition);
			
		mStartIndicator.setText(gridPositionToTime(minPosition));
		String endTime = gridPositionToTime(maxPosition);
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(endTime));
			cal.add(Calendar.MINUTE, 30);
		} catch (ParseException e) {
			
		}		
		endTime = df.format(cal.getTime());
		mEndIndicator.setText(endTime);
		
		mStartIndicator.setVisibility(View.VISIBLE);
		mEndIndicator.setVisibility(View.VISIBLE);
		
		mStartIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
					int marginTop = calendarLayout.getTop();
					int indicatorX = scheduleLeftMargin + (int)thisGridView.getX() - mStartIndicator.getWidth() - indicatorPadding;
					int indicatorY = (int)startView.getY() - mStartIndicator.getHeight()/2 + marginTop;
					mStartIndicator.setX(indicatorX);
					mStartIndicator.setY(indicatorY);
				}
			}
		);
		mEndIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
					int marginTop = calendarLayout.getTop();
					int indicatorX = scheduleLeftMargin + (int)thisGridView.getX() - mStartIndicator.getWidth() - indicatorPadding;
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
					int indicatorX = scheduleLeftMargin + (int)thisGridView.getX() + thisGridView.getWidth()/2 - mUpIndicator.getWidth()/2;
					int indicatorY = (int)startView.getY() - mUpIndicator.getHeight() - indicatorPadding + marginTop;
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
					int indicatorX = scheduleLeftMargin + (int)thisGridView.getX() + thisGridView.getWidth()/2 - mDownIndicator.getWidth()/2;
					int indicatorY = (int)endView.getY() + endView.getHeight() + indicatorPadding + marginTop;
					mDownIndicator.setX(indicatorX);
					mDownIndicator.setY(indicatorY);
				}
			}
		);
		
		mDeleteButton.setVisibility(View.VISIBLE);	
		//mDeleteButton.setLayoutParams(new RelativeLayout.LayoutParams(startView.getWidth(), RelativeLayout.LayoutParams.WRAP_CONTENT));
		mDeleteButton.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
					int marginTop = calendarLayout.getTop();
					int buttonX = scheduleLeftMargin + (int)thisGridView.getX() + thisGridView.getWidth()/2 - mDeleteButton.getWidth()/2;
					int endViewBottomY = (int)endView.getY() + endView.getHeight() + marginTop;
					int startViewTopY = (int)startView.getY() + marginTop;
					int selectionHeight = endViewBottomY - startViewTopY;
					int buttonY = endViewBottomY - selectionHeight/2 - mDeleteButton.getHeight()/2 ;
					mDeleteButton.setX(buttonX);
					mDeleteButton.setY(buttonY);
				}
			}
		);
		
		drawSelectedSchedule(thisGridView, startView, endView);
	}
	public void hideIndicators(){
		mStartIndicator.setVisibility(View.INVISIBLE);
		mEndIndicator.setVisibility(View.INVISIBLE);
		mUpIndicator.setVisibility(View.INVISIBLE);
		mDownIndicator.setVisibility(View.INVISIBLE);
		mDeleteButton.setVisibility(View.INVISIBLE);
		mDrawSelectionLayout.setVisibility(View.GONE);
	}
	public void enableInteractGrid(View touchedWeekdayGridView, int touchedPosition){
		mSelectActivedDay = (String) touchedWeekdayGridView.getTag();
		mSelectedGridView = touchedWeekdayGridView;
		float thisViewX = touchedWeekdayGridView.getX();
		int thisWidth = touchedWeekdayGridView.getWidth();
		int thisHeight = touchedWeekdayGridView.getHeight();
		viewInteraction.setX(scheduleLeftMargin + thisViewX);
		viewInteraction.setY(0);
		viewInteraction.setLayoutParams(new RelativeLayout.LayoutParams(thisWidth, thisHeight + scheduleTopMargin*2));
		viewInteraction.setVisibility(View.VISIBLE);
		
		mSelectStartPosition = touchedPosition;
		mSelectEndPosition = touchedPosition;
		showIndicators((ExpandableHeightGridView)touchedWeekdayGridView);
	}
	public void disableInteractGrid(){
		viewInteraction.setVisibility(View.GONE);
		hideIndicators();
		mSelectActivedDay = "";
	}
	public void disableScroll (ScrollView scrollView){
		scrollView.requestDisallowInterceptTouchEvent(true);
	}
	public void enableScroll (ScrollView scrollView){
		scrollView.requestDisallowInterceptTouchEvent(false);
	}
	public class ScheduleTimeTouchEvent implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
        	float currentXPosition = event.getX();
            float currentYPosition = event.getY();
            currentYPosition = currentYPosition - scheduleTopMargin; 
            int position = ((ExpandableHeightGridView)mSelectedGridView).pointToPosition((int) currentXPosition, (int) currentYPosition);
        	
        	switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	mGridInteractionTouchState = TouchState.TOUCH_STATE_CLICK;
	                // we don't know if it's a click or a scroll yet, but until we know
	                // assume it's a click	 
	                disableScroll(scrollView);
	            	if(event.getY() < mSelectedGridView.getHeight() && position == mSelectStartPosition - 1){
	            		mSelectionStartFrom = SelectionStartPoint.UP_ARROW;
	            		lastTouchedY = currentYPosition;
	            		lastTouchedPosition = position;
	            	}else if(position == mSelectEndPosition + 1){
	            		mSelectionStartFrom = SelectionStartPoint.DOWN_ARROW;
	            		lastTouchedY = currentYPosition;
	            		lastTouchedPosition = position;
	            	}else if(event.getY() > mSelectedGridView.getHeight()  && lastTouchedPosition == WEEKDAY_GRID_COUNT -1){            	
		            	// if down arrow on bottom, out of the index of weekday grid
	            		mSelectionStartFrom = SelectionStartPoint.DOWN_ARROW;
	            		lastTouchedY = currentYPosition;
	            	}else{
	            		mSelectionStartFrom = null;
	            		lastTouchedY = -500;
	            	}

	                break;

	            case MotionEvent.ACTION_MOVE:
            		mGridInteractionTouchState = TouchState.TOUCH_STATE_MOVE;
	            	if(mSelectionStartFrom == SelectionStartPoint.UP_ARROW){
	            		if(position < mSelectEndPosition){
		            		if(currentYPosition < lastTouchedY){	// move up
		            			if(lastTouchedPosition != position){
		            				addGridToSchedule(lastTouchedPosition);	    				
		            			}
		            		}else{
		            			if(position == mSelectStartPosition){
		            				System.out.println("removeGridFromSchedule:" + lastTouchedPosition);
		            				removeGridFromSchedule(lastTouchedPosition);
		            			}         
	            			}     
	            		}
	        			lastTouchedPosition = position;	   
	            	}else if(mSelectionStartFrom == SelectionStartPoint.DOWN_ARROW){
            			if(position > mSelectStartPosition){
		            		if(currentYPosition > lastTouchedY){	// move down
		            			if(position > mSelectEndPosition){
		            				if(position != lastTouchedPosition){
			            				addGridToSchedule(lastTouchedPosition);
			                			lastTouchedPosition = position;	   
				            		}
		            			}
		            		}else{
			            		if(position == mSelectEndPosition){
			            			removeGridFromSchedule(position);
			                		lastTouchedPosition = position;	   
			            		}  
		            		}
		            	}else if(position == -1 && lastTouchedPosition == WEEKDAY_GRID_COUNT -1){
            				addGridToSchedule(lastTouchedPosition);
            			}            			
	            	}
            		lastTouchedY = currentYPosition;
            		
	                break;

	            case MotionEvent.ACTION_UP:
	            	mGridInteractionTouchState = TouchState.TOUCH_STATE_RESTING;
	            	mSelectionStartFrom = null;
	            	enableScroll(scrollView);
	                break;

	            default:
	                //inactiveSelection();
	                break;
	        }
            return true;
        }
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
