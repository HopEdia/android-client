package io.github.hopedia;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.github.hopedia.fragments.BarcodeFragment;
import io.github.hopedia.fragments.BeerListFragment;
/** Hop Edia android client
 Copyright (C) 2016  koko-ng <koko.fr.mu@gmail.com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class MainScreenPagerAdapter extends FragmentPagerAdapter {
	private static final int PAGES = 2;
	private BeerListFragment beerListFragment = BeerListFragment.newInstance(true);

	public MainScreenPagerAdapter(final FragmentManager fm) {
		super(fm);
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		Fragment fragment;
		switch (i) {
			case 0:
				fragment = BarcodeFragment.newInstance();
				break;
			case 1:
				fragment = beerListFragment;
				break;
			default:
				fragment = BarcodeFragment.newInstance();
				break;
		}
		// Our object is just an integer :-P
		return fragment;
	}
	//TODO
	@Override
	public int getCount() {
		return PAGES;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return "OBJECT " + (position + 1);
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}


	public String getListFragment(int viewPagerId) {
		return ("android:switcher:" + viewPagerId + ":" + 1);
	}

}
