package com.example.carl.mdhschemaapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BusTimeTableFragment extends Fragment{
    //private static final String BUSS = BusTimeTableFragment.class.getSimpleName();

    private static final String[] busTimesV2E = new String[] {
            "06:00", "06:10", "06:50",
            "07:00", "07:10", "07:50",
            "08:15", "08:25", "09:05",
            "09:15", "09:25", "10:05",
            "10:15", "10:25", "11:05",
            "11:15", "11:25", "12:05",
            "12:15", "12:25", "13:05",
            "13:15", "13:25", "14:05",
            "14:15", "14:25", "15:05",
            "15:15", "15:25", "16:05",
            "16:15", "16:25", "17:05",
            "17:15", "17:25", "18:05",
            "18:15", "18:25", "19:05"
    };

    private static final String[] busTimesE2V = new String[] {
            "07:00",	"07:45",	"07:50",
            "08:15",	"09:00",	"09:05",
            "09:15",	"10:00",	"10:05",
            "11:15",	"12:00",	"12:05",
            "12:15",	"13:00",	"13:05",
            "13:15",	"14:00",	"14:05",
            "15:15",	"16:00",	"16:05",
            "16:15",	"17:00",	"17:05",
            "17:15",	"18:00",	"18:05",
            "18:15",	"19:00",	"19:05"

    };

    public static BusTimeTableFragment newInstance() {
        BusTimeTableFragment frag = new BusTimeTableFragment();
        frag.setRetainInstance(true);
        return frag;
    }
    public BusTimeTableFragment () {
        // Empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buss_time_table, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ExpandableHeightGridView gridViewA2E = (ExpandableHeightGridView) view.findViewById(R.id.myId);
        ExpandableHeightGridView gridViewE2A = (ExpandableHeightGridView) view.findViewById(R.id.gridViewE2V);

        CustomAdapter adapterV2E = new CustomAdapter(this.getActivity(), busTimesV2E);
        CustomAdapter adapterE2V = new CustomAdapter(this.getActivity(), busTimesE2V);

        gridViewA2E.setAdapter(adapterV2E);
        gridViewA2E.setExpanded(true);

        gridViewE2A.setAdapter(adapterE2V);
        gridViewE2A.setExpanded(true);
    }

}
