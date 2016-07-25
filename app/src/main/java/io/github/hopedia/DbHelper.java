package io.github.hopedia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
public class DbHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 4;
	public static final String DATABASE_NAME = "db.db";

	public static final String ASCENDING_ORDER = " ASC";
	public static final String DESCENDING_ORDER = " DESC";
	protected static final String TEXT_TYPE = " TEXT";
	protected static final String INTEGER_TYPE = " INTEGER";
	protected static final String TIMESTAMP_TYPE = " TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
	protected static final String COMMA_SEP = ",";
	private final Context ctx;
	private SQLiteDatabase db;



	public static abstract class DbListener<Result> {
		public abstract void onPostAction(Result result);
	}


	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.ctx=context;

	}
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Beers_SQL_CREATE_ENTRIES);
		db.execSQL(RecentBeers_SQL_CREATE_ENTRIES);
		db.execSQL(Barcode_SQL_CREATE_ENTRIES);
		db.execSQL("CREATE TRIGGER updateLastModifiedDate " +
				"AFTER UPDATE ON "+ BeerTableModel.BeerColumns.TABLE_NAME +" FOR EACH ROW BEGIN " +
				"UPDATE "+ BeerTableModel.BeerColumns.TABLE_NAME +" SET " + BeerTableModel.BeerColumns.INSERTED_DATE +" = date('now') " +
				"WHERE _id = NEW._id; " +
				"END;");
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over TODO
		db.execSQL(Beers_SQL_DELETE_ENTRIES);
		db.execSQL(RecentBeers_SQL_DELETE_ENTRIES);
		db.execSQL(Barcode_SQL_DELETE_ENTRIES);
		onCreate(db);
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//TODO
		onUpgrade(db, oldVersion, newVersion);
	}
	public SQLiteDatabase getDb() {
		return ((Application) ctx.getApplicationContext()).db;
	}
	private abstract class AsyncOps<Params, Result> extends AsyncTask<Params, Void, Result> {
		private DbListener listener;

		public AsyncOps(DbListener listener) {
			this.listener = listener;
		}
		@Override
		protected void onPostExecute(Result o) {
			if(listener != null)
				listener.onPostAction(o);
		}
	}
	private class QueryArgs {
		public boolean distinct;
		public String table;
		public String[] columns;
		public String selection;
		public String[] selectionArgs;
		public String groupBy;
		public String having;
		public String orderBy;
		public String limit;

		public QueryArgs() {}
		public QueryArgs(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
			this.distinct = distinct;
			this.table = table;
			this.columns = columns;
			this.selection = selection;
			this.selectionArgs = selectionArgs;
			this.groupBy = groupBy;
			this.having = having;
			this.orderBy = orderBy;
			this.limit = limit;
		}
	}
	private class AsyncQuery extends AsyncOps<QueryArgs, Cursor> {

		public AsyncQuery(DbListener listener) {
			super(listener);
		}

		@Override
		protected Cursor doInBackground(QueryArgs... params) {
			return getDb().query(
					params[0].distinct,
					params[0].table,            // The table to query
					params[0].columns,          // The columns to return
					params[0].selection,	    // The columns for the WHERE clause
					params[0].selectionArgs,    // The values for the WHERE clause
					params[0].groupBy,	        // don't group the rows
					params[0].having,	        // don't filter by row groups
					params[0].orderBy,	        // The sort order
					params[0].limit
			);
		}

	}

	private class InsertArgs {
		public String table;
		public String nullColumnHack;
		public ContentValues values;

		public InsertArgs(String table, String nullColumnHack, ContentValues values) {
			this.table = table;
			this.nullColumnHack = nullColumnHack;
			this.values = values;
		}
	}
	private class AsyncInsert extends AsyncOps<InsertArgs, Long> {

		public AsyncInsert(DbListener listener) {
			super(listener);
		}

		@Override
		protected Long doInBackground(InsertArgs... params) {
			return getDb().insert(
					params[0].table,
					params[0].nullColumnHack,
					params[0].values
			);
		}

	}

	private class UpdateArgs {
		public String table;
		public ContentValues values;
		public String whereClause;
		public String[] whereArgs;

		public UpdateArgs(String table, ContentValues values, String whereClause, String[] whereArgs) {
			this.table = table;
			this.values = values;
			this.whereClause = whereClause;
			this.whereArgs = whereArgs;
		}

	}
	private class AsyncUpdate extends AsyncOps<UpdateArgs, Integer> {

		public AsyncUpdate(DbListener listener) {
			super(listener);
		}

		@Override
		protected Integer doInBackground(UpdateArgs... params) {
			return getDb().update(
					params[0].table,
					params[0].values,
					params[0].whereClause,
					params[0].whereArgs
			);
		}
	}

	private class DeleteArgs {
		public String table;
		public String whereClause;
		public String[] whereArgs;

		public DeleteArgs(String table, String whereClause, String[] whereArgs) {
			this.table = table;
			this.whereClause = whereClause;
			this.whereArgs = whereArgs;
		}

	}
	private class AsyncDelete extends AsyncOps<DeleteArgs, Integer> {

		public AsyncDelete(DbListener listener) {
			super(listener);
		}

		@Override
		protected Integer doInBackground(DeleteArgs... params) {
			return getDb().delete(
					params[0].table,
					params[0].whereClause,
					params[0].whereArgs
			);
		}
	}
	public long count(String table) {
		return DatabaseUtils.queryNumEntries(getDb(), table);
	}

	public void query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String sort, String sortOrder, String count, String offset, DbListener<Cursor> listener) {
		QueryArgs args = new QueryArgs(false, table, columns, selection, selectionArgs, groupBy, null, null, null);
		if(sort != null & sortOrder != null)
			args.orderBy=sort+sortOrder;
		if (offset != null & count != null)
			args.limit=offset+COMMA_SEP+count;

		new AsyncQuery(listener).execute(args);
	}
	public void query(String table, String[] columns, String sort, String sortOrder, String count, String offset, DbListener<Cursor> listener) {
		query(table, columns, null, null, null, sort, sortOrder, count, offset, listener);
	}
	public void query(String table, String[] columns, String selection, String[] selectionArgs, DbListener<Cursor> listener) {
		query(table, columns, selection, selectionArgs, null, null, null, null, null, listener);
	}

	public void insert(String table, String nullColumnHack, ContentValues values, DbListener<Long> listener) {
		InsertArgs args = new InsertArgs(table, nullColumnHack, values);

		new AsyncInsert(listener).execute(args);
	}

	public void update(String table, ContentValues values, String whereClause, String[] whereArgs, DbListener<Integer> listener) {
		UpdateArgs args = new UpdateArgs(table, values, whereClause, whereArgs);
		new AsyncUpdate(listener).execute(args);
	}
	public void delete(String table, String whereClause, String[] whereArgs, DbListener<Integer> listener) {
		DeleteArgs args = new DeleteArgs(table, whereClause, whereArgs);
		new AsyncDelete(listener).execute(args);
	}

	/*Recent beers*/
	private static final String RecentBeers_SQL_CREATE_ENTRIES =
			"CREATE TABLE " + RecentBeersModel.RecentBeerColumns.TABLE_NAME + " (" +
					BaseColumns._ID + " INTEGER PRIMARY KEY," +
					RecentBeersModel.RecentBeerColumns.COLUMN_NAME_BEER_ID + TEXT_TYPE + COMMA_SEP +
					RecentBeersModel.RecentBeerColumns.COLUMN_DATE_ADDED + TIMESTAMP_TYPE +
					" )";

	private static final String RecentBeers_SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + RecentBeersModel.RecentBeerColumns.TABLE_NAME;


	public long countRecentBeers() {
		return DbHelper.this.count(RecentBeersModel.RecentBeerColumns.TABLE_NAME);
	}
	public void queryRecentBeers(String count, String offset, DbListener<Cursor> listener) {
		//String[] columns = new String[]{RecentBeersModel.RecentBeerColumns.COLUMN_NAME_BEER_ID};
		DbHelper.this.query(RecentBeersModel.RecentBeerColumns.TABLE_NAME, null, RecentBeersModel.RecentBeerColumns.COLUMN_DATE_ADDED, DESCENDING_ORDER, count, offset, listener);
	}
	public void addRecentBeers(String id, DbListener<Long> listener) {
		ContentValues values = new ContentValues();
		values.put(RecentBeersModel.RecentBeerColumns.COLUMN_NAME_BEER_ID, id);

		DbHelper.this.insert(RecentBeersModel.RecentBeerColumns.TABLE_NAME, null, values, listener);
	}

	public void removeRecentBeer(Long id, DbListener<Integer> listener) {
		ContentValues values = new ContentValues();
		values.put(RecentBeersModel.RecentBeerColumns.COLUMN_NAME_BEER_ID, id);

		DbHelper.this.delete(RecentBeersModel.RecentBeerColumns.TABLE_NAME, BaseColumns._ID+"=?", new String[]{id.toString()}, listener);
	}
	/**
	 * Created by koko-ng on 08.06.16.
	 */
	public final class RecentBeersModel {

		// To prevent someone from accidentally instantiating the contract class,
		// give it an empty constructor.
		public RecentBeersModel() {}

		/* Inner class that defines the table contents */
		public abstract class RecentBeerColumns implements BaseColumns {
			public static final String TABLE_NAME = "recent_beers";
			public static final String COLUMN_NAME_BEER_ID = "beer_id";
			public static final String COLUMN_DATE_ADDED = "date";
		}
	}


	//Beers
	public final class BeerTableModel {

		// To prevent someone from accidentally instantiating the contract class,
		// give it an empty constructor.
		public BeerTableModel() {}

		/* Inner class that defines the table contents */
		public abstract class BeerColumns implements BaseColumns {
			public static final String TABLE_NAME = "beers";
			public static final String TABLE_BARCODE_NAME = "beers_barcode";
			public static final String COLUMN_BEER_ID = "beer_id";
			public static final String COLUMN_NAME = "name";
			public static final String COLUMN_IBU = "ibu";
			public static final String COLUMN_ABV = "abv";
			public static final String COLUMN_BREWERY_ID = "brewery_id";
			public static final String COLUMN_WIKIDATA_ID = "wikidata_id";
			public static final String COLUMN_CAT_ID = "categories";
			public static final String COLUMN_IMAGE_ID = "image";
			public static final String INSERTED_DATE = "inserted_date";
			public static final String COLUMN_BARCODE_FORMAT = "barcode_format";
			public static final String COLUMN_BARCODE_VALUE = "barcode_value";
			public static final String COLUMN_BARCODE_TYPE = "barcode_type";
		}
	}

	private static final String Beers_SQL_CREATE_ENTRIES =
			"CREATE TABLE " + BeerTableModel.BeerColumns.TABLE_NAME + " (" +
					BaseColumns._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_BEER_ID + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_IBU + INTEGER_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_ABV + INTEGER_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_BREWERY_ID + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_WIKIDATA_ID + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_CAT_ID + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_IMAGE_ID + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.INSERTED_DATE + TIMESTAMP_TYPE +
					//" PRIMARY KEY ( " + BaseColumns._ID + COMMA_SEP + BeerTableModel.BeerColumns.COLUMN_BEER_ID + " )" +
					" ); ";
	private static final String Barcode_SQL_CREATE_ENTRIES = "CREATE TABLE " + BeerTableModel.BeerColumns.TABLE_BARCODE_NAME + " (" +
					BaseColumns._ID + " INTEGER PRIMARY KEY," +
					BeerTableModel.BeerColumns.COLUMN_BARCODE_FORMAT + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_BARCODE_VALUE + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_BARCODE_TYPE + TEXT_TYPE + COMMA_SEP +
					BeerTableModel.BeerColumns.COLUMN_BEER_ID + TEXT_TYPE +
					")"
			;

	private static final String Beers_SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + BeerTableModel.BeerColumns.TABLE_NAME;
	private static final String Barcode_SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + BeerTableModel.BeerColumns.TABLE_BARCODE_NAME;



	public void queryBeers(String selection, String[] selectionArgs, final String[] columns, final DbListener<Beer[]> listener) {
		DbHelper.this.query(BeerTableModel.BeerColumns.TABLE_NAME,
				columns,
				selection,
				selectionArgs,
				new DbListener<Cursor>(){

					@Override
					public void onPostAction(Cursor cursor) {
						ArrayList<Beer> beers = new ArrayList<>();
						//TODO if barcode, get barcode from db
						try {
							while (cursor.moveToNext()) {
								try {
									Beer beer = new Beer();
									List<String> l = Arrays.asList(cursor.getColumnNames());
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_BEER_ID))
										beer.set_id(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_BEER_ID)));
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_NAME))
										beer.setName(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_NAME)));
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_BREWERY_ID))
										beer.setBrewery_id(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_BREWERY_ID)));
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_ABV)) {
										if(cursor.isNull(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_ABV)))
											beer.setAbv(null);
										else
											beer.setAbv(cursor.getFloat(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_ABV)));
									}
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_IBU)) {
										if(cursor.isNull(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_IBU)))
											beer.setIbu(null);
										else
											beer.setIbu(cursor.getFloat(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_IBU)));
									}
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_WIKIDATA_ID))
										beer.setWikidata_id(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_WIKIDATA_ID)));
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_CAT_ID))
										beer.setCategories(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_CAT_ID)));
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_IMAGE_ID))
										beer.setImage(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_IMAGE_ID)));
									if(l.contains(BeerTableModel.BeerColumns.INSERTED_DATE))
										beer.dbInsertTimestamp = cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.INSERTED_DATE));
									beers.add(beer);
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						} finally {
							listener.onPostAction(beers.toArray(new Beer[beers.size()]));
						}


					}
				});
	}

	public void queryBeers(String id, String[] columns, final DbListener<Beer[]> listener) {
		queryBeers(BeerTableModel.BeerColumns.COLUMN_BEER_ID+"=?", new String[]{id}, columns, listener);
	}
	public void insertBeers(Beer beer, final DbListener<Long> listener) {
		int arrayLength=0;
		if(beer.barcode != null)
			arrayLength=beer.barcode.length;

		DbListener<Long> finalListener = new DbListener<Long>() {
			@Override
			public void onPostAction(Long aLong) {
			}
		};
		DbHelper.this.insert(BeerTableModel.BeerColumns.TABLE_NAME, null, beer2cv(beer), new DbListener<Long>() {
			@Override
			public void onPostAction(Long aLong) {
				listener.onPostAction(aLong);
			}
		});


		int i = 0;
		while (i<arrayLength) {
			ContentValues values = new ContentValues();
			values.put(BeerTableModel.BeerColumns.COLUMN_BARCODE_VALUE, beer.barcode[i]._value);
			values.put(BeerTableModel.BeerColumns.COLUMN_BARCODE_TYPE, beer.barcode[i].type);
			values.put(BeerTableModel.BeerColumns.COLUMN_BARCODE_FORMAT, beer.barcode[i].format);
			values.put(BeerTableModel.BeerColumns.COLUMN_BEER_ID, beer.get_id());
			DbHelper.this.insert(BeerTableModel.BeerColumns.TABLE_BARCODE_NAME, null, values, finalListener );
			i++;
		}

	}
	public void updateBeer(String id, Beer beer, DbListener<Integer> listener) {
		update(BeerTableModel.BeerColumns.TABLE_NAME, beer2cv(beer), BeerTableModel.BeerColumns.COLUMN_BEER_ID + "=?", new String[]{id}, listener);
	}

	public void queryBarcode(Barcode barcode, final String[] columns, final DbListener<HashMap<Barcode, String>> listener) {
		String selection=BeerTableModel.BeerColumns.COLUMN_BARCODE_FORMAT+"=? AND "+ BeerTableModel.BeerColumns.COLUMN_BARCODE_VALUE+"=?";
		String[] selectionArgs = new String[]{barcode.getFormat(), barcode.get_value()};
		DbHelper.this.query(BeerTableModel.BeerColumns.TABLE_BARCODE_NAME,
				columns,
				selection,
				selectionArgs,
				new DbListener<Cursor>(){

					@Override
					public void onPostAction(Cursor cursor) {
						HashMap<Barcode, String > barcodes = new HashMap<Barcode, String>();
						//TODO if barcode, get barcode from db
						try {
							while (cursor.moveToNext()) {
								try {
									List<String> l = Arrays.asList(cursor.getColumnNames());
									if(l.contains(BeerTableModel.BeerColumns.COLUMN_BEER_ID)) {
										Barcode barcode = new Barcode();
										if (l.contains(BeerTableModel.BeerColumns.COLUMN_BARCODE_VALUE))
											barcode.set_value(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_BARCODE_VALUE)));
										if (l.contains(BeerTableModel.BeerColumns.COLUMN_BARCODE_TYPE))
											barcode.setType(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_BARCODE_TYPE)));
										if (l.contains(BeerTableModel.BeerColumns.COLUMN_BARCODE_FORMAT))
											barcode.setFormat(cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_BARCODE_FORMAT)));
										barcodes.put(barcode, cursor.getString(cursor.getColumnIndexOrThrow(BeerTableModel.BeerColumns.COLUMN_BEER_ID)));
									}
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						} finally {
							listener.onPostAction(barcodes);
						}


					}
				});
	}

	private static ContentValues beer2cv(Beer beer) {
		ContentValues values = new ContentValues();
		values.put(BeerTableModel.BeerColumns.COLUMN_BEER_ID, beer.get_id());
		values.put(BeerTableModel.BeerColumns.COLUMN_NAME, beer.getName());
		values.put(BeerTableModel.BeerColumns.COLUMN_BREWERY_ID, beer.getBrewery_id());
		values.put(BeerTableModel.BeerColumns.COLUMN_ABV, beer.getAbv());
		values.put(BeerTableModel.BeerColumns.COLUMN_IBU, beer.getIbu());
		values.put(BeerTableModel.BeerColumns.COLUMN_WIKIDATA_ID, beer.getWikidata_id());
		values.put(BeerTableModel.BeerColumns.COLUMN_CAT_ID, beer.getCategories());
		values.put(BeerTableModel.BeerColumns.COLUMN_IMAGE_ID, beer.getImageId());
		return values;
	}

}

