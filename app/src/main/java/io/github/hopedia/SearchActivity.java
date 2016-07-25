package io.github.hopedia;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.hopedia.Schemas.Barcode;
import io.github.hopedia.Schemas.BaseItem;
import io.github.hopedia.Schemas.Beer;
import io.github.hopedia.Schemas.Name;
import io.github.hopedia.fragments.BaseItemFragment;
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
public class SearchActivity extends BaseActivity implements BeerListFragment.OnListFragmentInteractionListener, BeerListFragment.OnFragmentInteractionListener, BeerGetter.BeerListener<Beer[]>{

	private ViewStub baseContent;
	private boolean editOn = false;
	private String barcode;
	private String barcodeFormat;
	private String name;
	private Intent intent = getIntent();
	private SearchOnServer.SearchCriteria criteria = new SearchOnServer.SearchCriteria();
	private boolean fragmentComitted=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setLayout(R.layout.loading);

		// Get the intent, verify the action and get the query
		intent = getIntent();

		baseContent = (ViewStub) findViewById(R.id.base_content);

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			name = intent.getStringExtra(SearchManager.QUERY);
			if(name != null) {
				new BeerGetter(this).queryByName(name, false, this);
			}
		}
		else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String beerId = intent.getData().toString();
			new BeerGetter(this).queryById(beerId, new BeerGetter.BeerListener<Beer>() {
				@Override
				public void onPostAction(Beer beer) {
					SearchActivity.this.onPostAction(new Beer[]{beer});
				}
			});
		}
		else if("io.github.hopedia.BARCODE".equals(intent.getAction())) {
			barcode = intent.getStringExtra("barcode");
			barcodeFormat = intent.getStringExtra("barcodeFormat");

			//SearchOnServer.SearchCriteria criteria = new SearchOnServer.SearchCriteria();
			if(barcode != null) {
				new BeerGetter(this).queryByBarcode(new Barcode(barcode, barcodeFormat, null), new BeerGetter.BeerListener<Beer>() {
					@Override
					public void onPostAction(Beer beer) {
						SearchActivity.this.onPostAction(new Beer[]{beer});
					}
				});
			}
		}
		else {
			finish();
		}
	}

	public void onListFragmentInteraction(BaseItem item) {
		if(item == null) {
			Beer beer = new Beer();
			beer.setName(name);
			setupItemDisplay(beer, true);
		}
		else
			setupItemDisplay(item);
	}
	public void setupItemDisplay(final BaseItem item) {
		setupItemDisplay(item, false);
	}
	private void setupItemDisplay(final BaseItem item, final boolean editOn) {
		boolean newItem = false;
		if(editOn) {
			newItem = true;
		}
		final boolean newItemb = newItem;
		//super.setLayout(R.layout.fragment_layout);
		Fragment frag =BaseItemFragment.newInstance(item, editOn, newItemb);
		if(!fragmentComitted) {
			super.setLayout(R.layout.fragment_layout);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, frag);
			transaction.commit();
		}
		else {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, frag);
			transaction.commit();
		}

	}


	private void initListResult(BaseItem[] result) {
		super.setLayout(R.layout.fragment_layout);
		BeerListFragment listFragment = BeerListFragment.newInstance(false);
		listFragment.setDefaultItems(new ArrayList<>(Arrays.asList((Beer[]) result)));
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, listFragment);
		transaction.commitAllowingStateLoss();
		fragmentComitted=true;
	}
	private void noBeerFound(){
		//add button
		Snackbar snackbar = Snackbar
				.make(super.getRootView(), getString(R.string.beer_not_found), Snackbar.LENGTH_LONG)
				.setAction(getString(R.string.add), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Beer item = new Beer();
						if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
							name = intent.getStringExtra(SearchManager.QUERY);

							if(name != null) {
								item.name = new Name[]{new Name(name)};
							}
						}
						else if("io.github.hopedia.BARCODE".equals(intent.getAction())) {
							barcode = intent.getStringExtra("barcode");
							barcodeFormat = intent.getStringExtra("barcodeFormat");

							if(barcode != null) {
								item.barcode = new Barcode[]{new Barcode(barcode, barcodeFormat, null)};
							}
						}

						setupItemDisplay(item, true);
					}
				});
		snackbar.setActionTextColor(getResources().getColor(R.color.error));
		View snackbarView = snackbar.getView();
		snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
		textView.setTextColor(getResources().getColor(R.color.textColorPrimary));
		snackbar.show();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

	@Override
	public void onPostAction(Beer[] result) {
		if(result != null) {
			if(result.length == 1 && result[0] != null) {
				if(Intent.ACTION_SEARCH.equals(intent.getAction()))
					if(result[0].getName().equalsIgnoreCase(criteria.name))
						setupItemDisplay(result[0]);
					else
						initListResult(result);
				else
					setupItemDisplay(result[0]);
			}
			else if(result.length==0 || result[0]==null) {
				noBeerFound();
			}
			else{
				initListResult(result);
			}
		}
		else {
			noBeerFound();
		}
	}



}


