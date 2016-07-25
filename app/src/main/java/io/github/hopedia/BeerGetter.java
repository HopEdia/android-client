package io.github.hopedia;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import java.util.HashMap;

import io.github.hopedia.Schemas.Barcode;
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
public class BeerGetter {
	private static final long ONE_DAY = 1000*60*60*24;
	private BeerListener<Beer[]> registeredListener=null;
	private DbHelper dbHelper;
	private Context ctx;
	public AsyncTask.Status status;

	public BeerGetter(Context ctx) {
		init(ctx);
	}

	private void init(Context ctx) {
		this.ctx = ctx;
		this.dbHelper = new DbHelper(ctx);
	}

	public BeerGetter(Context ctx, BeerListener<Beer[]> beerListener) {
		init(ctx);
		registeredListener = beerListener;
	}

	public void cancelAsyncTasks() {
		registeredListener=new BeerListener<Beer[]>() {
			@Override
			public void onPostAction(Beer[] beer) {
			}
		};
	}

	public interface BeerListener<T> {
		void onPostAction(T beer);
	}

	public void queryById(String id, final BeerListener listener) {
		queryById(id, null, listener);
	}
	public void queryById(final String id, String[] columns, final BeerListener<Beer> listener) {
		if (is_connected()) {
			onlineSearchId(id, columns, listener, true);
		} else {
			offlineSearchId(id, columns, listener, false);
		}
	}

	public void offlineSearchId(final String id, final String[] columns, final BeerListener<Beer> listener, final boolean noReCall) {
		dbHelper.queryBeers(id, columns, new DbHelper.DbListener<Beer[]>() {
			@Override
			public void onPostAction(Beer[] beer) {
				if (beer.length != 0)
					listener.onPostAction(beer[0]);
				else {
					if(!noReCall)
						onlineSearchId(id, columns, listener, true);
				}

			}
		});
	}

	private void onlineSearchId(final String id, final String[] columns, final BeerListener<Beer> listener, final boolean noReCall) {
		SearchOnServer.SearchCriteria criteria = new SearchOnServer.SearchCriteria();
		criteria.id = id;
		new SearchOnServer<Beer[]>(ctx, "beer", criteria, new SearchOnServer.onItem<Beer[]>() {
			@Override
			public void callback(Beer[] items) {
				if (items==null) {
					//net error->offline
					if(!noReCall)
						offlineSearchId(id, columns, listener, true);
				}
				else if (items.length == 0)
					listener.onPostAction(null);
				else
					listener.onPostAction(items[0]);
			}
		});
	}

	public void queryByBarcode(Barcode barcode, final BeerListener listener) {
		queryByBarcode(barcode, null, listener);
	}
	public void queryByBarcode(final Barcode barcode, String[] columns, final BeerListener<Beer> listener) {
		//TODO multiple id one barcode ?
		if(is_connected()) {
			onlineSearchBarcode(barcode, columns, listener, false);
		}
		else {
			offlineSearchBarcode(barcode, columns, listener, false);
		}
	}
	public void offlineSearchBarcode(final Barcode barcode, final String[] columns, final BeerListener<Beer> listener, final boolean noReCall) {
		dbHelper.queryBarcode(barcode, columns, new DbHelper.DbListener<HashMap<Barcode, String>>() {
			@Override
			public void onPostAction(HashMap<Barcode, String> map) {
				if (map.values().size() != 0) {
					queryById(map.values().iterator().next(), listener);
				} else {
					if(!noReCall)
						onlineSearchBarcode(barcode, columns, listener, true);
				}

			}
		});
	}

	private void onlineSearchBarcode(final Barcode barcode, final String[] columns, final BeerListener<Beer> listener, final boolean noReCall) {
		SearchOnServer.SearchCriteria criteria = new SearchOnServer.SearchCriteria();
		criteria.barcode = barcode.get_value();
		criteria.barcode_format = barcode.getFormat();
		new SearchOnServer<Beer[]>(ctx, "beer", criteria, new SearchOnServer.onItem<Beer[]>() {
			@Override
			public void callback(Beer[] items) {
				if (items==null) {
					//net error->offline
					if(!noReCall)
						offlineSearchBarcode(barcode, columns, listener, true);
				}
				else if (items.length == 0)
					listener.onPostAction(null);
				else
					listener.onPostAction(items[0]);
			}
		});
	}

	public void queryByName(String name, boolean completion) {
		queryByName(name, completion, registeredListener);
	}
	public void queryByName(String name, boolean completion, BeerListener<Beer[]> listener) {
		queryByName(name, null, completion, listener);
	}
	public void queryByName(final String name, String[] columns, final boolean completion, final BeerListener<Beer[]> listener) {
		if(is_connected()) {
			onlineSearchName(name, columns, completion, listener, false);
		}
		else {
			offlineSearchName(name, columns, completion, listener, false);
		}
	}
	public void offlineSearchName(final String name, final String[] columns, final boolean completion, final BeerListener<Beer[]> listener, final boolean noReCall) {
		dbHelper.queryBeers(DbHelper.BeerTableModel.BeerColumns.COLUMN_NAME + " like ?", new String[]{"%" + name + "%"}, columns, new DbHelper.DbListener<Beer[]>() {
			@Override
			public void onPostAction(Beer[] beer) {
				if (beer.length > 0)
					listener.onPostAction(beer);
				else {
					if (!noReCall)
						onlineSearchName(name, columns, completion, listener, true);
				}
			}
		});
	}
	private void onlineSearchName(final String name, final String[] columns, final boolean completion, final BeerListener<Beer[]> listener, final boolean noRecall) {
		SearchOnServer.SearchCriteria criteria = new SearchOnServer.SearchCriteria();
		if(completion)
			criteria.name_completion=name;
		else
			criteria.name = name;

		new SearchOnServer<Beer[]>(ctx, "beer", criteria, new SearchOnServer.onItem<Beer[]>() {
			@Override
			public void callback(Beer[] items) {
				if (items==null) {
					//net error->offline
					if(!noRecall)
						offlineSearchName(name, columns, completion, listener, true);
				}
				else {
					listener.onPostAction(items);
				}
			}
		});
	}
	public void query(String selection, String[] selectionArgs,  final String[] columns, final BeerListener<Beer[]> listener) {
		dbHelper.queryBeers(selection, selectionArgs, columns, new DbHelper.DbListener<Beer[]>() {
			@Override
			public void onPostAction(Beer[] beer) {
				listener.onPostAction(beer);
			}
		});
	}
	public void insert(final Beer beer) {
		dbHelper.queryBeers(beer._id, new String[]{BaseColumns._ID}, new DbHelper.DbListener<Beer[]>() {

			@Override
			public void onPostAction(Beer[] savedBeer) {
				if(savedBeer.length == 1) {
					dbHelper.updateBeer(beer._id, beer, new DbHelper.DbListener<Integer>() {
						@Override
						public void onPostAction(Integer integer) {

						}
					});
				}
				else {
					dbHelper.insertBeers(beer, new DbHelper.DbListener<Long>() {
						@Override
						public void onPostAction(Long aLong) {
						}
					});
				}
			}
		});
	}
	/*
		private void postAction(final BeerListener<Beer> listener, Beer item) {
			if(item.dbInsertTimestamp != null) {
				if (new Date().getTime() - Timestamp.valueOf(item.dbInsertTimestamp).getTime() > ONE_DAY){
					SearchOnServer.SearchCriteria criteria = new SearchOnServer.SearchCriteria();
					criteria.id=item.get_id();
					new SearchOnServer(ctx, "beer", criteria, new SearchOnServer.onItem<Beer>() {
						@Override
						public void callback(Beer items) {
							listener.onPostAction(items);
							insert(items);
						}
					});
				}
			}
			listener.onPostAction(item);
		}
		*/
	private boolean is_connected() {
		ConnectivityManager connMgr = (ConnectivityManager)
				ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return  (networkInfo != null && networkInfo.isConnected());
	}
}
