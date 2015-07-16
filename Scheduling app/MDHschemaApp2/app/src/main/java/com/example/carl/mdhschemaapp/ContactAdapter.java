package com.example.carl.mdhschemaapp;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView vTime;
        final TextView vCourse;
        final TextView vCourseCode;
        final TextView vDescription;
        final TextView vPlace;
        final TextView vDay;
        final View color_ll;

        public ContactViewHolder(View v, View.OnClickListener clickListener) {
            super(v);
            vTime =  (TextView) v.findViewById(R.id.Time);
            vCourse = (TextView)  v.findViewById(R.id.Course);
            vCourseCode = (TextView)  v.findViewById(R.id.CourseCode);
            vDescription = (TextView) v.findViewById(R.id.Description);
            vPlace = (TextView)  v.findViewById(R.id.Place);
            vDay = (TextView) v.findViewById(R.id.Day);
            color_ll = v.findViewById(R.id.color_code);
            v.setOnClickListener(clickListener);
        }

        @Override
        public void onClick(View v) {
            //v.callOnClick();
        }
    }

    private final List<CardInfo> contactList;

    private final View.OnClickListener clickListener;

    public ContactAdapter(List<CardInfo> contactList, View.OnClickListener clickListener) {
        this.contactList = contactList;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public CardInfo get(int pos) { return contactList.get(pos); }

    //This will fix the output for the strings in the DB
    //formating, decides how you want to format the string, and sTime is the string
    private String fixDate(String formating, String sTime)
    {
        DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // ?? Why?
        dfDay.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        Date date1 = null;
        try {
            date1 = dfDay.parse(sTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat outputFormatter1 = new SimpleDateFormat(formating, Locale.getDefault());
        outputFormatter1.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));

        return outputFormatter1.format(date1);
    }

    // Assigns a background color to a linearlayout contained in the card
    private void assign_color(CardInfo course){
        int sum = 0, r, g, b;
        for(char c : course.courseCode.substring(0, 5).toCharArray())
            sum += c;
        Random rand = new Random(sum);
        r = rand.nextInt(255);
        g = rand.nextInt(255);
        b = rand.nextInt(255);
        if(course.courseCode.substring(0, 5).equals("DVA123")){
            r = 255;
            g = 255;
            b = 255;
        }
        course.color = "#" + Integer.toHexString(Color.rgb(r, g, b));
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        CardInfo ci = contactList.get(i);

        String day = fixDate("EEEE", ci.startTime);
        // Display day only if the day has not yet been displayed
        if (i <= 0 || !day.equals(fixDate("EEEE", contactList.get(i-1).startTime))) {
            contactViewHolder.vDay.setVisibility(View.VISIBLE);
            contactViewHolder.vDay.setText(day.substring(0, 1).toUpperCase() + day.substring(1)); // Capitalize first letter
        }
        else {
            contactViewHolder.vDay.setVisibility(View.GONE);
        }
        contactViewHolder.vTime.setText(fixDate("HH:mm",ci.startTime) + "-" + fixDate("HH:mm",ci.endTime));
        contactViewHolder.vCourse.setText(ci.course);
        contactViewHolder.vCourseCode.setText(ci.courseCode);
        contactViewHolder.vDescription.setText(ci.description);
        contactViewHolder.vPlace.setText(ci.Place);
        assign_color(ci);
        contactViewHolder.color_ll.setBackgroundColor(Color.parseColor(ci.color));
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new ContactViewHolder(itemView, clickListener);
    }
}
