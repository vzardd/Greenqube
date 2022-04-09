package com.vzardd.greenqube;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vzardd.greenqube.homefragments.ChatFrag;
import com.vzardd.greenqube.homefragments.GroupsFrag;
import com.vzardd.greenqube.homefragments.UsersFrag;

public class PagesAdapter extends FragmentStateAdapter {
    public PagesAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0: return new ChatFrag();
            case 1: return new GroupsFrag();
            case 2: return new UsersFrag();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
