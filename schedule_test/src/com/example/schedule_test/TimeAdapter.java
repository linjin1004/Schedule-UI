package com.example.schedule_test;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TimeAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mGridArray;
    private int mGridHeight; 
    private boolean mIsScheduleGrid;
    
    public TimeAdapter(Context c, ArrayList<String> itemArray, int gridHeight, boolean isScheduleGrid) {
        mContext = c;
        mGridArray = itemArray;
        mGridHeight = gridHeight;
        mIsScheduleGrid = isScheduleGrid;
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
	        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mGridHeight);
	        gridView.setLayoutParams(params);
        } else {
            gridView = (View) convertView;
        }

        TextView textView = (TextView) gridView.findViewById(R.id.grid_item_label);
        if(mIsScheduleGrid){
	        //textView.setText(mGridArray.get(position));
        }else if(position%2 == 0){
        	 textView.setText(mGridArray.get(position));
        }
        gridView.setTag(position);
        
        return gridView;
    }
}