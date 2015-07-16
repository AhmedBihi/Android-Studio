package com.example.carl.mdhschemaapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

class CustomAdapter extends ArrayAdapter<String> {
    private final int tableSize;
    public CustomAdapter(Context context, String Times[]) {
        super(context, 0, Times);
        tableSize = Times.length - 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        String currentTime = sdf.format(cal.getTime());
        int result, resultPast, resultLast2, resultLast1, resultLast;
        // Get the data item for this position
        String time = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_cell, parent, false);
        }
        TextView tvTime = (TextView) convertView.findViewById(R.id.tv);
        tvTime.setText(time);
        
        if (dayOfWeek > 1 && dayOfWeek < 7) //Only works mon-fri
        {
            String TimeLast2 = getItem(tableSize-2);
            String TimeLast1 = getItem(tableSize-1);
            String TimeLast = getItem(tableSize);
            String pastTime;
            if(position == 0 || position == 1 || position == 2)
            {
                pastTime = "0";
            }
            else {
                pastTime = getItem(position - 3);
            }

            result = time.compareTo(currentTime);
            resultPast = pastTime.compareTo(currentTime);
            resultLast2 = currentTime.compareTo(TimeLast2);
            resultLast1 = currentTime.compareTo(TimeLast1);
            resultLast = currentTime.compareTo(TimeLast);
            //If it's the first times of the day and the time of day is late
            if(resultLast2 >= 0 && position == 0){
                tvTime.setTextColor(Color.GREEN);
                return convertView;
            }
            else if (resultLast1 >= 0  && position == 1)
            {
                tvTime.setTextColor(Color.GREEN);
                return convertView;
            }
            else if (resultLast >= 0 && position == 2)
            {
                tvTime.setTextColor(Color.GREEN);
                return convertView;
            }
            //If the the result is that the time is greater than the current time and the last time is earlier than
            else if(result > 0 && resultPast < 0)
            {
                tvTime.setTextColor(Color.GREEN);
                return convertView;
            }
        }
        return convertView;
    }

}
