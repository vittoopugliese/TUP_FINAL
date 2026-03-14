package com.example.tup_final.ui.inspection;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class InspectionDetailPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 2;

    public InspectionDetailPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new DevicesFragment();
        }
        return new GeneralInfoFragment();
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
