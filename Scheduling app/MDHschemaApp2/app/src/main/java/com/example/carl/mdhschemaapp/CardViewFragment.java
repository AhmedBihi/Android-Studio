/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.carl.mdhschemaapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CardViewFragment extends Fragment {
    //private static final String TAG = CardViewFragment.class.getSimpleName();

    /** The CardView widget. */
    private ContactAdapter ac;
    private RecyclerView recList;
    private int weeks = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static CardViewFragment newInstance(int w) {
        CardViewFragment fragment = new CardViewFragment(w);
        fragment.setRetainInstance(true);
        return fragment;
    }

    private CardViewFragment(int w) {
        weeks = w;
    }

    public CardViewFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Required empty public constructor
        StoreData db = new StoreData(getActivity().getBaseContext());

        recList = (RecyclerView) getActivity().findViewById(R.id.cardList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getBaseContext(), LinearLayoutManager.VERTICAL, false);
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);

        ac = new ContactAdapter(db.convertToCardInfo(weeks), recClick);
        recList.setAdapter(ac);
        db.close();
    }

    private View.OnClickListener recClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int itemPosition = recList.getChildPosition(v);
            CardInfo item = ac.get(itemPosition);

            Intent intent = new Intent(getActivity().getBaseContext(), MapActivity.class);
            Bundle b = new Bundle();
            b.putString("room", item.Place);
            intent.putExtras(b);
            startActivity(intent);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_card_view, container, false);
    }
}

