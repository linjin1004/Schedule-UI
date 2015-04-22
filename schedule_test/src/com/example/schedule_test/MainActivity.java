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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
	private String THIS_LOG_NAME = "Advanced Schedule Calendar Page.";
	private ArrayList<String> mTimeArray = new ArrayList<String>();
	private int GRID_HEIGHT; 
	private int GRID_MARGIN_LEFT; 
	private int GRID_MARGIN_TOP;
	private ScrollView scrollView;
	private int WEEKDAY_GRID_COUNT = 48;
	private int INDICATOR_PADDING;
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
	private int mSelectActivedDay = -1;	
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
	private HashMap<String, Integer> mEditingScheduleIndex = new HashMap<String, Integer>();
	/**
	 * For schedule data
	 */	
	private AdvancedScheduleHelper mAdvancedScheduleHelper = new AdvancedScheduleHelper();
	JSONArray scheduleData = new JSONArray();
	private String SCHEDULE_START_POSITION = mAdvancedScheduleHelper.SCHEDULE_START_POSITION;
	private String SCHEDULE_END_POSITION = mAdvancedScheduleHelper.SCHEDULE_END_POSITION;
	private String SCHEDULE_INDEX = mAdvancedScheduleHelper.SCHEDULE_INDEX;
	
	private String DAY = mAdvancedScheduleHelper.DAY;
	private String SCHEDULE_ARRAY = mAdvancedScheduleHelper.SCHEDULE_ARRAY;
	private String START_TIME = mAdvancedScheduleHelper.START_TIME;
	private String END_TIME = mAdvancedScheduleHelper.END_TIME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/**
		 * hardcore test data
		 */
		scheduleData = mAdvancedScheduleHelper.createTestSchedule();
		
		GRID_HEIGHT = (int) getResources().getDimension(R.dimen.advanced_schedule_grid_height);
		GRID_MARGIN_LEFT = (int) getResources().getDimension(R.dimen.advanced_schedule_time_title_total_width);
		GRID_MARGIN_TOP = (int) getResources().getDimension(R.dimen.advanced_schedule_weekday_margin_top);
		INDICATOR_PADDING = (int) getResources().getDimension(R.dimen.advanced_schedule_indicator_padding);
		//set mScheduleGrid
		Date d1;
		Calendar cal = Calendar.getInstance();
		try {
			d1 = df.parse("00:00");
			cal.setTime(d1);
		} catch (ParseException e) {
			Log.e(THIS_LOG_NAME + " Function: onCreate mScheduleGrid", e.toString());	
		}		
		for(int i=0; i < WEEKDAY_GRID_COUNT; i++){
			mTimeArray.add(df.format(cal.getTime()));
			cal.add(Calendar.MINUTE, 30);
		}
		
		scrollView = (ScrollView) findViewById(R.id.scroll_view);
				
		final ExpandableHeightGridView gridViewTime = (ExpandableHeightGridView) findViewById(R.id.gridview_time);
		gridViewTime.setEnabled(false);
		gridViewTime.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, false));
		gridViewTime.setExpanded(true);
		
		ScheduleTimeTouchEvent scheduleTimeTouchEvent = new ScheduleTimeTouchEvent();
		WeekDayGridTouchEvent weekdayGridClickEvent = new WeekDayGridTouchEvent();
		// Sun to Sat grid views
		final ExpandableHeightGridView gridViewSun = (ExpandableHeightGridView) findViewById(R.id.gridview_sun);
		gridViewSun.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, true));
		gridViewSun.setExpanded(true);
		gridViewSun.setOnTouchListener(weekdayGridClickEvent);
		gridViewSun.setTag("0");
		mWeekdayGridViews.add(gridViewSun);
		
		final ExpandableHeightGridView gridViewMon = (ExpandableHeightGridView) findViewById(R.id.gridview_mon);
		gridViewMon.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, true));
		gridViewMon.setExpanded(true);
		gridViewMon.setOnTouchListener(weekdayGridClickEvent);
		gridViewMon.setTag("1");
		mWeekdayGridViews.add(gridViewMon);
		
		final ExpandableHeightGridView gridViewTue = (ExpandableHeightGridView) findViewById(R.id.gridview_tue);
		gridViewTue.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, true));
		gridViewTue.setExpanded(true);
		gridViewTue.setOnTouchListener(weekdayGridClickEvent);
		gridViewTue.setTag("2");
		mWeekdayGridViews.add(gridViewTue);
		
		final ExpandableHeightGridView gridViewWed = (ExpandableHeightGridView) findViewById(R.id.gridview_wed);
		gridViewWed.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, true));
		gridViewWed.setExpanded(true);
		gridViewWed.setOnTouchListener(weekdayGridClickEvent);
		gridViewWed.setTag("3");
		mWeekdayGridViews.add(gridViewWed);
		
		final ExpandableHeightGridView gridViewThr = (ExpandableHeightGridView) findViewById(R.id.gridview_thr);
		gridViewThr.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, true));
		gridViewThr.setExpanded(true);
		gridViewThr.setOnTouchListener(weekdayGridClickEvent);
		gridViewThr.setTag("4");
		mWeekdayGridViews.add(gridViewThr);
		
		final ExpandableHeightGridView gridViewFri = (ExpandableHeightGridView) findViewById(R.id.gridview_fri);
		gridViewFri.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, true));
		gridViewFri.setExpanded(true);
		gridViewFri.setOnTouchListener(weekdayGridClickEvent);
		gridViewFri.setTag("5");
		mWeekdayGridViews.add(gridViewFri);
		
		final ExpandableHeightGridView gridViewSat = (ExpandableHeightGridView) findViewById(R.id.gridview_sat);
		gridViewSat.setAdapter(new TimeAdapter(MainActivity.this, mTimeArray, GRID_HEIGHT, true));
		gridViewSat.setExpanded(true);
		gridViewSat.setOnTouchListener(weekdayGridClickEvent);
		gridViewSat.setTag("6");
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

		LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
		calendarLayout.post(new Runnable() {
            @Override
            public void run() {
            	drawCurrentSchedule();
            }
        });
	}
	public void drawCurrentSchedule(){
		runOnUiThread(new Runnable() {
		    public void run() {
				for(int i=0; i < mWeekdayGridViews.size(); i++){
					ExpandableHeightGridView thisGridView = (ExpandableHeightGridView) mWeekdayGridViews.get(i);
					int weekDay = Integer.valueOf(thisGridView.getTag().toString());
					for(int position = 0; position < WEEKDAY_GRID_COUNT; position++ ){
						boolean foundSchedule = false;
						HashMap<String, Integer> thisSchedule = new HashMap<String, Integer>();		
						thisSchedule = mAdvancedScheduleHelper.getSelectedSchedule(scheduleData, weekDay, position);
						if(thisSchedule.containsKey(SCHEDULE_INDEX)){
							foundSchedule = true;
						}
						if(foundSchedule){
							thisGridView.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_blue));
						}else{
							thisGridView.getChildAt(position).setBackgroundColor(Color.WHITE);
						}
					}
				}
				System.out.println(scheduleData);
			}
		});
	}
	
	public void addGridToSelection(int position){
		if(position >= 0){
			if(position < mSelectStartPosition){
				mSelectStartPosition = position;
			}else if(position > mSelectEndPosition){
				mSelectEndPosition = position;
			}
			showIndicators((ExpandableHeightGridView)mSelectedGridView);
		}
	}
	public void removeGridFromSelection(int position){
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
		if(mEditingScheduleIndex.containsKey(SCHEDULE_INDEX) && mEditingScheduleIndex.containsKey(DAY)){
			int weekday_index = mEditingScheduleIndex.get(DAY);
			int schedule_index = mEditingScheduleIndex.get(SCHEDULE_INDEX);
			scheduleData = mAdvancedScheduleHelper.deleteSchedule(scheduleData, weekday_index, schedule_index);
			
			drawCurrentSchedule();
		}
		
		disableInteractGrid();
	}
	public void addSelection(int weekday, int minPosition, int maxPosition){
		if(mEditingScheduleIndex.containsKey(SCHEDULE_INDEX) && mEditingScheduleIndex.containsKey(DAY)){		
			int weekday_index = mEditingScheduleIndex.get(DAY);
			int schedule_index = mEditingScheduleIndex.get(SCHEDULE_INDEX);
			scheduleData = mAdvancedScheduleHelper.editSchedule(scheduleData, weekday_index, schedule_index, minPosition, maxPosition);
		}else{
			scheduleData = mAdvancedScheduleHelper.addSchedule(scheduleData, weekday, minPosition, maxPosition);
		}
		
		drawCurrentSchedule();
	}
	public void drawSelectedSchedule(ExpandableHeightGridView gridView, View startView, View endView){
		LinearLayout calendarLayout = (LinearLayout) findViewById(R.id.calendar_layout);
		int marginTop = calendarLayout.getTop();
		int selectionX = GRID_MARGIN_LEFT + (int)gridView.getX();
		int selectionY = (int)startView.getTop() + marginTop;
		int selectionBottomY = (int)endView.getTop() + endView.getHeight() + marginTop;
		int selectionWidth = gridView.getWidth();
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
			
		mStartIndicator.setText(mAdvancedScheduleHelper.gridPositionToTime(minPosition));
		String endTime = mAdvancedScheduleHelper.gridPositionToTime(maxPosition);
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(endTime));
			cal.add(Calendar.MINUTE, 30);
		} catch (ParseException e) {
			Log.e(THIS_LOG_NAME + " Function: showIndicators", e.toString());
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
					int indicatorX = GRID_MARGIN_LEFT + (int)thisGridView.getX() - mStartIndicator.getWidth() - INDICATOR_PADDING;
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
					int indicatorX = GRID_MARGIN_LEFT + (int)thisGridView.getX() - mStartIndicator.getWidth() - INDICATOR_PADDING;
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
					int indicatorX = GRID_MARGIN_LEFT + (int)thisGridView.getX() + thisGridView.getWidth()/2 - mUpIndicator.getWidth()/2;
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
					int indicatorX = GRID_MARGIN_LEFT + (int)thisGridView.getX() + thisGridView.getWidth()/2 - mDownIndicator.getWidth()/2;
					int indicatorY = (int)endView.getY() + endView.getHeight() + INDICATOR_PADDING + marginTop;
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
					int buttonX = GRID_MARGIN_LEFT + (int)thisGridView.getX() + thisGridView.getWidth()/2 - mDeleteButton.getWidth()/2;
					int endViewBottomY = (int)endView.getY() + endView.getHeight() + marginTop;
					int startViewTopY = (int)startView.getY() + marginTop;
					int selectionHeight = endViewBottomY - startViewTopY;
					int buttonY = endViewBottomY - selectionHeight/2 - mDeleteButton.getHeight()/2 ;
					mDeleteButton.setX(buttonX);
					mDeleteButton.setY(buttonY);
					mDeleteButton.setLayoutParams(new RelativeLayout.LayoutParams(endView.getWidth(), mDeleteButton.getHeight()));
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
		mSelectActivedDay = Integer.parseInt(touchedWeekdayGridView.getTag().toString());
		mSelectedGridView = touchedWeekdayGridView;
		float thisViewX = touchedWeekdayGridView.getX();
		int thisWidth = touchedWeekdayGridView.getWidth();
		int thisHeight = touchedWeekdayGridView.getHeight();
		viewInteraction.setX(GRID_MARGIN_LEFT + thisViewX);
		viewInteraction.setY(0);
		viewInteraction.setLayoutParams(new RelativeLayout.LayoutParams(thisWidth, thisHeight + GRID_MARGIN_TOP*2));
		viewInteraction.setVisibility(View.VISIBLE);
		
		// check if click on existed schedule
		boolean foundSchedule = false;
		HashMap<String, Integer> thisSchedule = new HashMap<String, Integer>();		
		thisSchedule = mAdvancedScheduleHelper.getSelectedSchedule(scheduleData, mSelectActivedDay, touchedPosition);
		if(thisSchedule.containsKey(SCHEDULE_INDEX)){
			foundSchedule = true;
			mEditingScheduleIndex.put(DAY, thisSchedule.get(DAY));
			mEditingScheduleIndex.put(SCHEDULE_START_POSITION, thisSchedule.get(SCHEDULE_START_POSITION));
			mEditingScheduleIndex.put(SCHEDULE_END_POSITION, thisSchedule.get(SCHEDULE_END_POSITION));
			mEditingScheduleIndex.put(SCHEDULE_INDEX, thisSchedule.get(SCHEDULE_INDEX));
		}
		if(foundSchedule){
			mSelectStartPosition = mEditingScheduleIndex.get(SCHEDULE_START_POSITION);
			mSelectEndPosition = mEditingScheduleIndex.get(SCHEDULE_END_POSITION);
		}else{
			mEditingScheduleIndex = new HashMap<String, Integer>();
			mSelectStartPosition = touchedPosition;
			mSelectEndPosition = touchedPosition;			
		}
		
		showIndicators((ExpandableHeightGridView)touchedWeekdayGridView);
	}
	public void disableInteractGrid(){
		viewInteraction.setVisibility(View.GONE);
		hideIndicators();
		mSelectActivedDay = -1;
		mSelectStartPosition = -1;
		mSelectEndPosition = -1;
		mSelectedGridView = null;
		mEditingScheduleIndex = new HashMap<String, Integer>();
	}
	public void disableScroll (){
		scrollView.requestDisallowInterceptTouchEvent(true);
	}
	public void enableScroll (){
		scrollView.requestDisallowInterceptTouchEvent(false);
	}
	public class ScheduleTimeTouchEvent implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
        	float currentXPosition = event.getX();
            float currentYPosition = event.getY();
            currentYPosition = currentYPosition - GRID_MARGIN_TOP; 
            int position = ((ExpandableHeightGridView)mSelectedGridView).pointToPosition((int) currentXPosition, (int) currentYPosition);
        	
        	switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	mGridInteractionTouchState = TouchState.TOUCH_STATE_CLICK;
	                // we don't know if it's a click or a scroll yet, but until we know
	                // assume it's a click	 
	                disableScroll();
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
		            				addGridToSelection(lastTouchedPosition);	    				
		            			}
		            		}else{
		            			if(position == mSelectStartPosition){
		            				removeGridFromSelection(lastTouchedPosition);
		            			}         
	            			}     
	            		}
	        			lastTouchedPosition = position;	   
	            	}else if(mSelectionStartFrom == SelectionStartPoint.DOWN_ARROW){
            			if(position > mSelectStartPosition){
		            		if(currentYPosition > lastTouchedY){	// move down
		            			if(position > mSelectEndPosition){
		            				if(position != lastTouchedPosition){
		            					addGridToSelection(lastTouchedPosition);
			                			lastTouchedPosition = position;	   
				            		}
		            			}
		            		}else{
			            		if(position == mSelectEndPosition){
			            			removeGridFromSelection(position);
			                		lastTouchedPosition = position;	   
			            		}  
		            		}
		            	}else if(position == -1 && lastTouchedPosition == WEEKDAY_GRID_COUNT -1){
		            		addGridToSelection(lastTouchedPosition);
            			}            			
	            	}
            		lastTouchedY = currentYPosition;
            		
	                break;

	            case MotionEvent.ACTION_UP:
	            	mGridInteractionTouchState = TouchState.TOUCH_STATE_RESTING;
	            	mSelectionStartFrom = null;
	            	enableScroll();
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
            		if(mSelectActivedDay == -1){
            			enableInteractGrid(thisView, position);
            		}else if(mSelectActivedDay != Integer.parseInt(thisView.getTag().toString())){
            			if(mSelectStartPosition != -1 && mSelectEndPosition != -1){
            				addSelection(mSelectActivedDay, mSelectStartPosition, mSelectEndPosition);
                			disableInteractGrid();
            			}
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
