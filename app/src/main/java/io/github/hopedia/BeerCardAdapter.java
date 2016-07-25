package io.github.hopedia;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.hopedia.Schemas.Beer;
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
public class BeerCardAdapter extends RecyclerView.Adapter<BeerCardAdapter.BeerCardViewHolder> {

	private final Context ctx;
	private ArrayList<Beer> beerList;
	private ArrayList<Date> dateList;
	private ArrayList<Long> dbIdsList = new ArrayList<Long>();
	private BeerListFragment.OnListFragmentInteractionListener listener;
	private boolean addButton;


	public void setAddButton(boolean addButton) {
		//add entry
		this.addButton = addButton;
		if(addButton)
			beerList.add(null);
		this.notifyDataSetChanged();
	}

	public BeerCardAdapter(Context ctx, ArrayList<Beer> beers, BeerListFragment.OnListFragmentInteractionListener listener) {
		this.ctx = ctx;
		this.beerList = beers;
		this.listener=listener;
	}

	public BeerCardAdapter(Context ctx, BeerListFragment.OnListFragmentInteractionListener listener) {
		this.ctx = ctx;
		this.beerList = new ArrayList<Beer>();
		this.dateList = new ArrayList<Date>();
		this.listener=listener;
	}

	@Override
	public int getItemCount() {
		return beerList.size();
	}

	@Override
	public void onBindViewHolder(final BeerCardViewHolder beerCardViewHolder, int i) {
		final Beer beer = beerList.get(i);

		if(i == beerList.size()-1 && addButton) {
			beerCardViewHolder.vName.setText(ctx.getResources().getString(R.string.add_beer));
		}
		else {
			beerCardViewHolder.vName.setText(beer.getName());
		}
		if(dateList!=null && dateList.get(i)!=null) {
			DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(ctx);
			beerCardViewHolder.vDate.setText(dateFormat.format(dateList.get(i)));
		}
		beerCardViewHolder.index=i;
		try {
			beerCardViewHolder.dbId=dbIdsList.get(i);
		} catch (java.lang.IndexOutOfBoundsException e) {
			//e.printStackTrace();
		}
		beerCardViewHolder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != beerCardViewHolder && listener !=null) {
					// Notify the active callbacks interface (the activity, if the
					// fragment is attached to one) that an item has been selected.
					listener.onListFragmentInteraction(beer);
				}
			}
		});
	}

	@Override
	public BeerCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View itemView = LayoutInflater.
				from(viewGroup.getContext()).
				inflate(R.layout.item_recent_beer_card, viewGroup, false);
		BeerCardViewHolder holder = new BeerCardViewHolder(itemView);

		return holder;
	}

	public static class BeerCardViewHolder extends RecyclerView.ViewHolder {
		protected TextView vName;
		protected TextView vDate;
		public final View mView;
		//public final TextView mContentView;
		public int index;
		public Long dbId;


		public BeerCardViewHolder(View v) {
			super(v);
			mView = v;
			vName =  (TextView) v.findViewById(R.id.beer_name);
			vDate = (TextView) v.findViewById(R.id.beer_date);
		}
	}

	public void addItem(Beer beer) {
		beerList.add(beer);
	}
	public void addItem(Beer[] beers) { beerList.addAll(new ArrayList<Beer>(Arrays.asList(beers))); }
	public void addItem(Beer beer, Long entryId) {
		dbIdsList.add(beerList.size(), entryId);
		beerList.add(beer);

	}
	public void addItem(Beer beer, Date date) {
		dateList.add(beerList.size(), date);
		addItem(beer);
	}
	public void addItem(Beer[] beers, Date[] date) {
		dateList.addAll(beerList.size(), new ArrayList<Date>(Arrays.asList(date)));
		addItem(beers);
	}
	public void addItem(Beer beer, Date date, Long entryId) {
		dateList.add(beerList.size(), date);
		addItem(beer, entryId);

	}
	public void removeItem(Long dbId) {
		int i=dbIdsList.indexOf(dbId);
		Log.e("LOG", String.valueOf(i)+" removed");
		if(i>=0) {
			beerList.remove(i);
			dateList.remove(i);
			dbIdsList.remove(i);
			Log.e("LOG", String.valueOf(i)+" removed");
			notifyItemRemoved(i);
		}
	}


	public void clearBeerList() {
		beerList.clear();
	}

	public Beer getItem(int i) {
		return beerList.get(i);
	}
}