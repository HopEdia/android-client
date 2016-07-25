package io.github.hopedia;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import io.github.hopedia.Schemas.BaseItem;
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
public class MainActivity extends BaseActivity implements BarcodeFragment.OnFragmentInteractionListener, BeerListFragment.OnListFragmentInteractionListener, BeerListFragment.OnFragmentInteractionListener  {
	// When requested, this adapter returns a DemoObjectFragment,
	// representing an object in the collection.
	MainScreenPagerAdapter mMainScreenPagerAdapter;
	ViewPager mViewPager;
	private boolean fragmentListCommitted =false;
	private Fragment fragment;
	private FragmentTransaction ft;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	public void init() {
		super.setLayout(R.layout.activity_main);

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mMainScreenPagerAdapter =
				new MainScreenPagerAdapter(
						getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mMainScreenPagerAdapter);
	}
	public void onResume() {
		super.onResume();
	}
	@Override
	public void onBackPressed() {
		if (mViewPager.getCurrentItem() == 1 && fragmentListCommitted) {
			///BeerListFragment
			super.onBackPressed();
			((BeerListFragment) getSupportFragmentManager().findFragmentByTag(mMainScreenPagerAdapter.getListFragment(R.id.pager))).showList();
			getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
			fragmentListCommitted=false;
		}
		else if (mViewPager.getCurrentItem() == 0) {
			// If the user is currently looking at the first step, allow the system to handle the
			// Back button. This calls finish() on this activity and pops the back stack.
			super.onBackPressed();
		} else{
			// Otherwise, select the previous step.
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
		}

	}
	public void setPagerItem(int item) {
		if(mViewPager == null)
			mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setCurrentItem(item);
	}
	@Override
	public void onFragmentInteraction(Uri uri) {

	}
	public void onListFragmentInteraction(BaseItem item) {

		//item=TODO get beer TODO TODO
		((BeerListFragment) getSupportFragmentManager().findFragmentByTag(mMainScreenPagerAdapter.getListFragment(R.id.pager))).showDetails(item);
		fragmentListCommitted =true;
	}
}

