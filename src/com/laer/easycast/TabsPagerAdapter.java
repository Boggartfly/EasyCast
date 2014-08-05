package com.laer.easycast;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.laer.easycast.ImagePane;
import com.laer.easycast.VideoPane;
import com.laer.easycast.MusicPane;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		// TODO Auto-generated constructor stub
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {
		// TODO Auto-generated method stub
		switch (index) {
		case 0:
			// Top Rated fragment activity
			return new ImagePane();
		case 1:
			// Games fragment activity
			return new VideoPane();
		case 2:
			// Movies fragment activity
			return new MusicPane();
		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

}
