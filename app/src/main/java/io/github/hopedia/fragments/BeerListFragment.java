package io.github.hopedia.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.hopedia.BaseActivity;
import io.github.hopedia.BeerCardAdapter;
import io.github.hopedia.BeerGetter;
import io.github.hopedia.DbHelper;
import io.github.hopedia.DbHelper.DbListener;
import io.github.hopedia.R;
import io.github.hopedia.Schemas.BaseItem;
import io.github.hopedia.Schemas.Beer;

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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BeerListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BeerListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeerListFragment extends Fragment {

	private int viewId = 42;
	private BeerListFragment.OnListFragmentInteractionListener mListener;
	private long itemCount = 0;
	private DbHelper dbHelper;
	private View view;
	private FrameLayout container;
	private boolean recentBeers = false;
	private ArrayList<Beer> defaultItems;
	private BeerCardAdapter adapter;
	private Toolbar toolbar;
	private Menu menu;
	private boolean detailsShown=false;
	private ItemTouchHelper mIth;
	//private SwipeRefreshLayout listRoot;

	public static BeerListFragment newInstance(boolean recentBeers){
		BeerListFragment fragment = new BeerListFragment();
		fragment.recentBeers=recentBeers;
		return fragment;
	}

	@Override
	public void onResume() {
		super.onResume();
		//container.setVisibility(View.VISIBLE);
		if(recentBeers && new DbHelper(getContext()).countRecentBeers() != itemCount){
			getItems(0);
		}
		//if(detailsShown)
		//container.findViewById(R.id.list_toolbar).setVisibility(View.GONE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(recentBeers)
			setHasOptionsMenu(true);
	}
	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState != null)
			recentBeers = savedInstanceState.getBoolean("recentBeers");
		this.view = view;
		viewId=generateViewId();
		view.setId(viewId);
		container = (FrameLayout) view.findViewById(R.id.recent_drink_cardview_container);
		//listRoot = (SwipeRefreshLayout) view.findViewById(R.id.list_root) ;
		container.findViewById(R.id.nothing_drinked).setVisibility(View.GONE);
		container.findViewById(R.id.cardList).setVisibility(View.VISIBLE);
		final RecyclerView recList = (RecyclerView) container.findViewById(R.id.cardList);
		recList.setHasFixedSize(true);
		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		recList.setLayoutManager(llm);

		if(defaultItems != null) {
			adapter= new BeerCardAdapter(getContext(), defaultItems, mListener);
		}
		else {
			adapter = new BeerCardAdapter(getContext(), mListener);
		}
		recList.setAdapter(adapter);
		if(recentBeers) {
			toolbar = (Toolbar) view.findViewById(R.id.list_toolbar);
			toolbar.setVisibility(View.VISIBLE);
			((BaseActivity)getActivity()).setSupportActionBar(toolbar);
			getItems(0);
			class SnackThread implements Runnable{
				ArrayList<Runnable> runnables = new ArrayList<>();
				@Override
				public void run() {
					runnables.get(0).run();
				}
				public void addRunnable(final Runnable runnable) {
					Runnable run = new Runnable() {
						private int index;
						@Override
						public void run() {
							runnable.run();
							index=runnables.size();
							next(index);;
						}
					};
					runnables.add(run);
					Log.e("LOG", String.valueOf(runnables.size()));
					if(runnables.size()==1) {
						run();
					}
				}
				public void next(int i) {
					runnables.remove(i-1);
					if(runnables.size()>0)
						run();
				}
			}
			final SnackThread snackThread = new SnackThread();
			mIth = new ItemTouchHelper(
					new ItemTouchHelper.SimpleCallback(0,
							ItemTouchHelper.LEFT) {
						DbHelper dbHelper=new DbHelper(getContext());
						public boolean onMove(RecyclerView recyclerView,
						                      RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
							return false;// true if moved, false otherwise
						}
						public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
							// remove from adapter
							//final Beer item = adapter.getItem(((BeerCardAdapter.BeerCardViewHolder) viewHolder).index);
							((BeerCardAdapter.BeerCardViewHolder) viewHolder).mView.setVisibility(View.GONE);
							//adapter.bindViewHolder((BeerCardAdapter.BeerCardViewHolder)viewHolder, ((BeerCardAdapter.BeerCardViewHolder) viewHolder).index);
							snackThread.addRunnable(new Runnable() {
								@Override
								public void run() {
									snack(viewHolder);

								}
							});
							snackThread.addRunnable(new Runnable() {
								@Override
								public void run() {
									Log.e("LOG", "I am useless");
								}
							});
						}
					});

			mIth.attachToRecyclerView(recList);
			/*listRoot.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					// Your code to refresh the list here.
					// Make sure you call swipeContainer.setRefreshing(false)
					// once the network request has completed successfully.
					if(new DbHelper(getContext()).countRecentBeers() != itemCount){
						getItems(0);
					}
					else
						listRoot.setRefreshing(false);
				}

			});*/
			//listRoot.

		}
		else {
			adapter.setAddButton(true);
		}

	}
	public void setDefaultItems(ArrayList<Beer> a) {
		this.defaultItems = a;
		//return this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_beers_list, container, false);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;

		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}



	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	private void getItems(Integer offset) {
		dbHelper = new DbHelper(getContext());
		itemCount = dbHelper.countRecentBeers();
		if(itemCount != 0) {
			adapter.clearBeerList();
			dbHelper.queryRecentBeers("10", offset.toString(), new DbListener<Cursor>() {
				@Override
				public void onPostAction(Cursor cursor) {
					//cursor, by last beer added
					try {
						while (cursor.moveToNext()) {
							final Long entryId = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
							final Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.RecentBeersModel.RecentBeerColumns.COLUMN_DATE_ADDED)));
							String id = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.RecentBeersModel.RecentBeerColumns.COLUMN_NAME_BEER_ID));
							new BeerGetter(getContext()).offlineSearchId(id, null, new BeerGetter.BeerListener<Beer>() {
								@Override
								public void onPostAction(Beer beer) {
									adapter.addItem(beer, date, entryId);
									adapter.notifyDataSetChanged();
//									listRoot.setRefreshing(false);
								}
							}, false);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} finally {
						cursor.close();
					}
				}
			});
		}
	}
	public void showDetails(BaseItem item) {
		container.setVisibility(View.GONE);
		view.findViewById(R.id.list_toolbar).setVisibility(View.GONE);
		setHasOptionsMenu(false);
		if(menu != null)
			((MenuBuilder) menu).clearAll();
		detailsShown=true;
		FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
		//noinspection ResourceType
		trans.replace(viewId, BaseItemFragment.newInstance(item));
		trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		trans.addToBackStack(null);
		trans.commit();
	}
	public void showList() {
		container.setVisibility(View.VISIBLE);
		view.findViewById(R.id.list_toolbar).setVisibility(View.VISIBLE);
		detailsShown=false;
		if(recentBeers) {
			onCreateOptionsMenu(menu, getActivity().getMenuInflater());
		}
	}
	@Override
	public void onSaveInstanceState(Bundle x) {
		super.onSaveInstanceState(x);
		x.putSerializable("recentBeers", recentBeers);
	}

	public interface OnListFragmentInteractionListener {
		void onListFragmentInteraction(BaseItem item);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(recentBeers)
			menuInflater.inflate(R.menu.recent_beers_menu, menu);
		this.menu = menu;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		for (int i = 0; i < menu.size(); i++)
			menu.getItem(i).setVisible(false);
		switch (item.getItemId()) {
			case R.id.menuAbout:
				new LibsBuilder()
						.withFields(R.string.class.getFields())
						.withLicenseDialog(true)
						.withLicenseShown(true)
						//provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
						.withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
						//start the activity
						.start(getContext());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private static int generateViewId() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
			for (;;) {
				final int result = sNextGeneratedId.get();
				// aapt-generated IDs have the high byte nonzero; clamp to the range under that.
				int newValue = result + 1;
				if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
				if (sNextGeneratedId.compareAndSet(result, newValue)) {
					return result;
				}
			}
		} else {
			return View.generateViewId();
		}

	}
	private void snack(final RecyclerView.ViewHolder viewHolder)  {
		final boolean[] canceled = {false};
		Log.e("LOG", "snack");
		Snackbar.make(view, R.string.recent_beer_deleted, Snackbar.LENGTH_SHORT).
				setAction(R.string.cancel, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//adapter.addItem(item);
						//adapter.notifyDataSetChanged();
						((BeerCardAdapter.BeerCardViewHolder) viewHolder).mView.setVisibility(View.VISIBLE);
						canceled[0] = true;
						Log.e("LOG", String.valueOf(((BeerCardAdapter.BeerCardViewHolder) viewHolder).dbId)+" canceled");
					}
				}).
				setCallback(new Snackbar.Callback() {
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						Log.e("LOG", "dismiss");
						if(!canceled[0]) {
							adapter.removeItem(((BeerCardAdapter.BeerCardViewHolder) viewHolder).dbId);
							dbHelper.removeRecentBeer(((BeerCardAdapter.BeerCardViewHolder) viewHolder).dbId, null);
						}
					}

					@Override
					public void onShown(Snackbar snackbar) {
						super.onShown(snackbar);
					}
				}).show();
	}
}
