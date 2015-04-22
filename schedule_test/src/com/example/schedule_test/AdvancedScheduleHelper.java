package com.example.schedule_test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class AdvancedScheduleHelper {
	SimpleDateFormat df = new SimpleDateFormat("HH:mm");
	private String THIS_LOG_NAME = "AdvancedScheduleHelper. ";
	public String SCHEDULE_ARRAY = "schedule_array";
	public String START_TIME = "start";
	public String END_TIME = "end";
	public String DAY = "day";
	public String SCHEDULE_START_POSITION = "schedule_start_position";
	public String SCHEDULE_END_POSITION = "schedule_end_position";
	public String SCHEDULE_INDEX = "schedule_index";

	public int timeToGridPosition(String thisTime){
		int gridPosition = -1;		
		try {
			Calendar thisCal = Calendar.getInstance();
			thisCal.setTime(df.parse(thisTime));
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse("00:00"));
			gridPosition = (int) (thisCal.getTimeInMillis() - cal.getTimeInMillis()) / 1800000;	// divided by 30 minutes
		} catch (Exception e) {
			Log.e(THIS_LOG_NAME + " Function: timeToGridPosition", e.toString());	
		}
		return gridPosition;
	}
	public String gridPositionToTime(int position){
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse("00:00"));
		} catch (ParseException e) {
			Log.e(THIS_LOG_NAME + " Function: gridPositionToTime", e.toString());	
		}	
		cal.add(Calendar.MINUTE, 30 * position);
		String thisTime = df.format(cal.getTime());
		return thisTime;
	}
	public JSONArray deleteSchedule(JSONArray scheduleData, int weekday_index, int schedule_index){
		try {
			for(int i=0; i < scheduleData.length(); i++){
				JSONObject tempDayObject = new JSONObject();
				tempDayObject = scheduleData.getJSONObject(i);
				int thisDay = tempDayObject.getInt(DAY);
				if(thisDay == weekday_index){
					JSONArray tempArray = new JSONArray();
					if(tempDayObject.has(SCHEDULE_ARRAY)){
						tempArray = tempDayObject.getJSONArray(SCHEDULE_ARRAY);
						JSONArray newArray = new JSONArray();
						for(int m=0; m < tempArray.length(); m++){
							if(m != schedule_index){
								newArray.put(tempArray.get(m));
							}
						}	
						tempDayObject.put(SCHEDULE_ARRAY, newArray);
					}
				}		
			}
		} catch (Exception e) {
			Log.e(THIS_LOG_NAME + " Function: deleteSchedule", e.toString());	
		}
		return scheduleData;
	}
	public JSONArray addSchedule(JSONArray scheduleData, int weekday, int minPosition, int maxPosition){
		try {
			for(int i=0; i < scheduleData.length(); i++){
				JSONObject tempDayObject = new JSONObject();
				tempDayObject = scheduleData.getJSONObject(i);
				int thisDay = tempDayObject.getInt(DAY);
				if(thisDay == weekday){
					JSONArray tempArray = new JSONArray();
					JSONArray newArray = new JSONArray();
					if(tempDayObject.has(SCHEDULE_ARRAY)){
						tempArray = tempDayObject.getJSONArray(SCHEDULE_ARRAY);
						for(int m=0; m < tempArray.length(); m++){
							boolean abandonThis = false;
							JSONObject temp = new JSONObject();
							temp = (tempArray.getJSONObject(m));
							if(temp.has(START_TIME) && temp.has(END_TIME)){
								String startTime = temp.getString(START_TIME);
								String endTime = temp.getString(END_TIME);
								Calendar thisCal = Calendar.getInstance();
								thisCal.setTime(df.parse(endTime));
								thisCal.add(Calendar.MINUTE, -30);
								endTime = df.format(thisCal.getTime());
								int startPosition = timeToGridPosition(startTime);
								int endPosition = timeToGridPosition(endTime);
								if(minPosition >= startPosition && minPosition <= endPosition){
									minPosition = startPosition;
									abandonThis = true;
								}
								if(maxPosition >= startPosition && maxPosition <= endPosition){
									maxPosition = endPosition;
									abandonThis = true;
								}
								if(minPosition <= startPosition && maxPosition >= endPosition){
									abandonThis = true;
								}
								if(!abandonThis){
									newArray.put(tempArray.get(m));
								}
							}else{
								newArray.put(tempArray.get(m));
							}
						}
					}
					String startTime = gridPositionToTime(minPosition);
					String endTime = gridPositionToTime(maxPosition);
					Calendar cal = Calendar.getInstance();
					cal.setTime(df.parse(endTime));
					cal.add(Calendar.MINUTE, 30);	
					endTime = df.format(cal.getTime());
					JSONObject temp = new JSONObject();
					temp.put(START_TIME, startTime);
					temp.put(END_TIME, endTime);
					newArray.put(temp);	
					tempDayObject.put(SCHEDULE_ARRAY, newArray);
				}		
			}	
		} catch (Exception e) {
			Log.e(THIS_LOG_NAME + " Function: addSchedule", e.toString());
		}
		return scheduleData;
	}
	public JSONArray editSchedule(JSONArray scheduleData, int weekday_index, int schedule_index, int minPosition, int maxPosition){
		try {
			for(int i=0; i < scheduleData.length(); i++){
				JSONObject tempDayObject = new JSONObject();
				tempDayObject = scheduleData.getJSONObject(i);
				int thisDay = tempDayObject.getInt(DAY);
				if(thisDay == weekday_index){
					JSONArray tempArray = new JSONArray();
					JSONArray newArray = new JSONArray();
					if(tempDayObject.has(SCHEDULE_ARRAY)){
						tempArray = tempDayObject.getJSONArray(SCHEDULE_ARRAY);
						for(int m=0; m < tempArray.length(); m++){
							boolean abandonThis = false;
							JSONObject temp = new JSONObject();
							temp = (tempArray.getJSONObject(m));
							if(temp.has(START_TIME) && temp.has(END_TIME)){
								if(m == schedule_index){
									abandonThis = true;
								}else{
									String startTime = temp.getString(START_TIME);
									String endTime = temp.getString(END_TIME);
									Calendar thisCal = Calendar.getInstance();
									thisCal.setTime(df.parse(endTime));
									thisCal.add(Calendar.MINUTE, -30);
									endTime = df.format(thisCal.getTime());
									int startPosition = timeToGridPosition(startTime);
									int endPosition = timeToGridPosition(endTime);
									if(minPosition >= startPosition && minPosition <= endPosition){
										minPosition = startPosition;
										abandonThis = true;
									}
									if(maxPosition >= startPosition && maxPosition <= endPosition){
										maxPosition = endPosition;
										abandonThis = true;
									}
									if(minPosition <= startPosition && maxPosition >= endPosition){
										abandonThis = true;
									}
								}
								if(!abandonThis){
									newArray.put(tempArray.get(m));
								}
							}else{
								newArray.put(tempArray.get(m));
							}
							
						}	
						tempDayObject.put(SCHEDULE_ARRAY, newArray);
					}
					String startTime = gridPositionToTime(minPosition);
					String endTime = gridPositionToTime(maxPosition);
					Calendar cal = Calendar.getInstance();
					cal.setTime(df.parse(endTime));
					cal.add(Calendar.MINUTE, 30);	
					endTime = df.format(cal.getTime());
					JSONObject temp = new JSONObject();
					temp.put(START_TIME, startTime);
					temp.put(END_TIME, endTime);
					newArray.put(temp);	
					tempDayObject.put(SCHEDULE_ARRAY, newArray);
				}		
			}	
		} catch (Exception e) {
			Log.e(THIS_LOG_NAME + " Function: editSchedule", e.toString());
		}
		return scheduleData;
	}
	public HashMap<String, Integer> getSelectedSchedule(JSONArray scheduleData, int weekday_index, int touchedPosition){
		HashMap<String, Integer> thisSchedule = new HashMap<String, Integer>();
		try {
			for(int i=0; i < scheduleData.length(); i++){
				JSONObject tempDayObject = new JSONObject();
				tempDayObject = scheduleData.getJSONObject(i);
				int thisDay = tempDayObject.getInt(DAY);
				if(thisDay == weekday_index){
					JSONArray tempArray = new JSONArray();
					if(tempDayObject.has(SCHEDULE_ARRAY)){
						tempArray = tempDayObject.getJSONArray(SCHEDULE_ARRAY);
						for(int m=0; m < tempArray.length(); m++){
							JSONObject temp = new JSONObject();
							temp = (tempArray.getJSONObject(m));
							if(temp.has(START_TIME) && temp.has(END_TIME)){
								String startTime = temp.getString(START_TIME);
								String endTime = temp.getString(END_TIME);
								Calendar thisCal = Calendar.getInstance();
								thisCal.setTime(df.parse(endTime));
								thisCal.add(Calendar.MINUTE, -30);
								endTime = df.format(thisCal.getTime());
								int startPosition = timeToGridPosition(startTime);
								int endPosition = timeToGridPosition(endTime);
								if(startPosition != -1 && endPosition != -1){
									if(touchedPosition >= startPosition && touchedPosition <= endPosition){
										thisSchedule.put(DAY, thisDay);
										thisSchedule.put(SCHEDULE_START_POSITION, startPosition);
										thisSchedule.put(SCHEDULE_END_POSITION, endPosition);
										thisSchedule.put(SCHEDULE_INDEX, m);
										break;
									}	
								}
							}
						}	
					}
				}		
			}	
		} catch (Exception e) {
			Log.e(THIS_LOG_NAME + " Function: getSelectedSchedule", e.toString());
		}
		return thisSchedule;
	}
	public JSONArray createTestSchedule(){
		JSONArray scheduleData = new JSONArray();
		/**
		 * hardcore test data
		 */
		JSONObject temp = new JSONObject();
		JSONArray tempArray = new JSONArray();
		JSONObject tempDayObject = new JSONObject();
		try {
			temp = new JSONObject();
			tempArray = new JSONArray();
			tempDayObject = new JSONObject();
			tempArray.put(temp);
			tempDayObject.put(DAY, "0");
			tempDayObject.put(SCHEDULE_ARRAY, tempArray);
			scheduleData.put(tempDayObject);
			temp = new JSONObject();
			tempArray = new JSONArray();
			tempDayObject = new JSONObject();
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
			temp = new JSONObject();
			temp.put("start", "09:00");
			temp.put("end", "12:00");
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
			temp = new JSONObject();
			tempArray = new JSONArray();
			tempDayObject = new JSONObject();
			tempArray.put(temp);
			tempDayObject.put(DAY, "6");
			tempDayObject.put(SCHEDULE_ARRAY, tempArray);
			scheduleData.put(tempDayObject);
		} catch (JSONException e) {
			Log.e(THIS_LOG_NAME + " Function: createTestSchedule", e.toString());	
		}
		return scheduleData;
	}
}
