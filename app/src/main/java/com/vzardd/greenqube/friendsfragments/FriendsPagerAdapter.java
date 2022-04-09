package com.vzardd.greenqube.friendsfragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FriendsPagerAdapter extends FragmentStateAdapter {
    public FriendsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0: return new FriendsFrag();
            case 1: return  new RequestsFrag();
        }
        return new FriendsFrag();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
