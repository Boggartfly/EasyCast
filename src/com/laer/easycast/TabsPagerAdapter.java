/*
 * Copyright 2014 Parth Sane
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
   limitations under the License.
 */
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
