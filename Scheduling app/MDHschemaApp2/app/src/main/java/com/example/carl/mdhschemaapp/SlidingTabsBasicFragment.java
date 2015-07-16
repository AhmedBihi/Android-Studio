
package com.example.carl.mdhschemaapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class SlidingTabsBasicFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    public static SlidingTabsBasicFragment newInstance() {
        SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }
    public SlidingTabsBasicFragment(){


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager, getActivity().getBaseContext());

        // rip kod
        mSlidingTabLayout.setOnPageChangeListener(OnPageChangeListener);
        StoreData db = new StoreData(getActivity().getBaseContext());
        getFragmentManager().beginTransaction().replace(R.id.containerKEK, CardViewFragment.newInstance(db.getFirstWeek())).commit();
        db.close();
        // END_INCLUDE (setup_slidingtablayout)
    }

    private ViewPager.OnPageChangeListener OnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            StoreData db = new StoreData(getActivity().getBaseContext());
            getFragmentManager().beginTransaction().replace(R.id.containerKEK, CardViewFragment.newInstance(db.getFirstWeek() + position)).commit();
            db.close();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    class SamplePagerAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position){
            // Inflate a new layout from our resources
            return getActivity().getLayoutInflater().inflate(R.layout.empty,
                    container, false);
        }

        @Override
        public int getCount()
        {
            StoreData db = new StoreData(getActivity().getBaseContext());
            int tmp = db.getAmountOfWeeks();
            db.close();
            return tmp + 1;
        }


        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return "Item " + (position + 1);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);

        }

    }
}
