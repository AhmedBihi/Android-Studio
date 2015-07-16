package com.example.carl.mdhschemaapp;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class CourseSearch extends Fragment{

    public CourseSearch() {}
    private EditText textInput;
    private ListView spinner;

    public static CourseSearch newInstance() {
        CourseSearch fragment = new CourseSearch();
        fragment.setRetainInstance(true);
        return fragment;
    }

    private View.OnClickListener btnAddClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<String> temp = new ArrayList<>();
            StoreData db = new StoreData(getActivity().getBaseContext());
            db.deleteCourses();
            SparseBooleanArray sparseBooleanArray = spinner.getCheckedItemPositions();
            for(int i = 0; i < spinner.getCount(); ++i) {
                if(sparseBooleanArray.get(i)) {
                    temp.add(spinner.getItemAtPosition(i).toString());
                    db.insertRecord((CourseInfo)spinner.getItemAtPosition(i));
                }
            }
            db.close();

            // Minimize soft keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);

            GetiCal(temp.toArray(new String[temp.size()]));
            makeToast(getString(R.string.search_toast_coursesupdated));
        }
    };

    private void makeToast(String s)
    {
        Context context = getActivity().getBaseContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, s, duration);
        toast.show();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        textInput = (EditText) getActivity().findViewById(R.id.search_text);
        textInput.addTextChangedListener(inputSearchChange);

        Button button = (Button) getActivity().findViewById(R.id.button_add);
        button.setOnClickListener(btnAddClick);

        spinner = (ListView) getActivity().findViewById(R.id.spinner);
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (spinner.isItemChecked(position))
                spinner.setItemChecked(position, true);
            else
                spinner.setItemChecked(position, false);
            }
        });


        StoreData db = new StoreData(getActivity().getBaseContext());
        List<CourseInfo> list = db.convertToCourseInfo();

        ArrayAdapter<CourseInfo> aa = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, list);
        spinner.setAdapter(aa);
        for (int i = 0; i < list.size(); ++i)
            spinner.setItemChecked(i, true);

        db.close();
    }

    private TextWatcher inputSearchChange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        private Timer timer = new Timer();

        @Override
        public void afterTextChanged(Editable s) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String input = "";
                    try {
                        input = URLEncoder.encode(textInput.getText().toString(), "UTF-8");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (input.isEmpty())
                        return;
                    Search(input);
                }
            }, 500);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    private Boolean IsConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    private String HttpGetRequest(String url)
    {
        if (!IsConnected()) {
            makeToast(getString(R.string.search_toast_notconnected));
            return "";
        }

        StringBuilder builder = new StringBuilder("");
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(in)));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }
            } else {
                makeToast(getString(R.string.http_error));
            }
        } catch (IOException e) {
            e.printStackTrace();
            makeToast("error " + e.toString());
        }
        return builder.toString();
    }

    private void Search(String name)
    {
        final String term = name;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = HttpGetRequest("http://webbschema.mdh.se/ajax/ajax_autocompleteResurser.jsp?typ=kurs&term=" + term);
                if (response.equals(""))
                    return;
                ArrayList<CourseInfo> list = new ArrayList<>();

                // Save checked items from past search
                SparseBooleanArray sparseBooleanArray = spinner.getCheckedItemPositions();
                int s = 0;
                for(int i = 0; i < spinner.getCount(); ++i) {
                    if (sparseBooleanArray.get(i)) {
                        CourseInfo c = (CourseInfo)spinner.getItemAtPosition(i);
                        list.add(new CourseInfo(c.courseCode, c.courseName));
                        ++s;
                    }
                }

                // Add already added courses
                StoreData db = new StoreData(getActivity().getBaseContext());
                List<CourseInfo> l = db.convertToCourseInfo();
                for(CourseInfo c : l) {
                    boolean added = false;
                    for(int i = 0; i < spinner.getCount(); ++i) {
                        if (sparseBooleanArray.get(i) && ((CourseInfo)spinner.getItemAtPosition(i)).courseCode.equals(c.courseCode))
                            added = true;
                    }
                    if (!added) {
                        list.add(new CourseInfo(c.courseCode, c.courseName));
                        ++s;
                    }
                }
                db.close();

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String courseCode = obj.getString("value");
                        boolean added = false;
                        for(int n = 0; n < list.size(); ++n)
                            if (list.get(n).courseCode.equals(courseCode)) {
                                added = true;
                                break;
                            }
                        if (!added) {
                            String courseName = jsonArray.getJSONObject(i).getString("label").split("6>, ")[1].split("<")[0];
                            list.add(new CourseInfo(courseCode, courseName));
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }

                final ArrayList<CourseInfo> ta = list;
                final int saved = s;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView lv = (ListView) getActivity().findViewById(R.id.spinner);
                        ArrayAdapter<CourseInfo> aa = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, ta);
                        lv.setAdapter(aa);
                        for(int i = 0; i < saved; ++i)
                            lv.setItemChecked(i, true);
                    }
                });
            }
        }).start();
    }

    private void GetiCal(String[] course)
   {
        if (course.length == 0) {
            StoreData db = new StoreData(getActivity().getBaseContext());
            db.delete();
            db.close();
            return;
        }

        // http://webbschema.mdh.se/setup/jsp/Schema.jsp?startDatum=idag&intervallTyp=m&intervallAntal=6&forklaringar=true&sokMedAND=false&sprak=SV&resurser=k.DVA222-14039V15-%2Ck.DVA222-%2C
        // http://webbschema.mdh.se/setup/jsp/SchemaICAL.ics?startDatum=idag&intervallTyp=m&intervallAntal=6&forklaringar=true&sokMedAND=false&sprak=SV&resurser=k.DVA222-14039V15-%2Ck.DVA222-%2C
        String temp = "";
        for(String str : course)
            temp += "k." + str + "%2C";

        final String term = temp;

        new Thread(new Runnable(){
            @Override
            public void run(){
                final String response = HttpGetRequest("http://webbschema.mdh.se/setup/jsp/SchemaICAL.ics?startDatum=idag&intervallTyp=m&intervallAntal=6&forklaringar=true&sokMedAND=false&sprak=SV&resurser=" + term);
                if (response.equals(""))
                    return;
                handleCalendar(response);
            }
        }).start();
    }

    private void handleCalendar(String ical) {
        StoreData db = new StoreData(getActivity().getBaseContext());
        try {
            StringReader sin = new StringReader(ical);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(sin);

            db.delete();
            for(int i = 0; i < calendar.getComponents(Component.VEVENT).size(); ++i) {
                VEvent event = (VEvent)calendar.getComponents(Component.VEVENT).get(i);
                String[] summary = event.getSummary().getValue().split("([A-Za-z\\.]*)(: )"); // Kurs.grp = 1, Sign = 2, Moment = 3, Aktivitetstyp = 4
                for(int x = 0; x < summary.length; ++x)
                    summary[x] = summary[x].trim();
                DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                dfDay.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
                DateFormat dfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                dfTime.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
                DtStart start = event.getStartDate();
                DtEnd end = event.getEndDate();

                DateFormat dfWeek = new SimpleDateFormat("w", Locale.getDefault());
                dfWeek.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));

                CardInfo ci = new CardInfo();

                List<CourseInfo> courses = db.convertToCourseInfo();

                ci.course = summary[3];
                for(CourseInfo c : courses) {
                    String[] split = summary[1].split("\\s+");
                    boolean added = false;
                    for(String s : split) {
                        if (c.courseCode.equals(s)) {
                            ci.course = c.courseName;
                            added = true;
                            break;
                        }
                    }
                    if (added)
                        break;
                }

                //Date foo = dfTime.parse(dfTime.format(start.getDate()) + "-" + dfTime.format(end.getDate()));
                ci.startTime = dfDay.format(start.getDate());
                ci.endTime = dfDay.format(end.getDate());
                ci.courseCode = summary[1];
                ci.description = summary[3];
                ci.week = Integer.parseInt(dfWeek.format(start.getDate()));
                ci.Place = event.getLocation().getValue();

                db.insertRecord(ci);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        db.close();
    }
}
