package io.github.hopedia;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;

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
public class SuggestionsProvider extends ContentProvider {
	public abstract static class QueryFinished {
		public abstract void onFinished(Cursor c);
	}

	private Context ctx;
	private QueryFinished queryListener;

	public SuggestionsProvider() {
	}
	public SuggestionsProvider(Context a) {
		this.ctx = a;
	}

	public static final String AUTHORITY = "io.github.hopedia.suggestions_provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/beer");
	private static final String[] SEARCH_SUGGEST_COLUMNS = {
			BaseColumns._ID,
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA
	};
	private MatrixCursor asyncCursor;
	private BeerGetter ongoingTask;
	// UriMatcher constant for search suggestions
	private static final int SEARCH_SUGGEST = 1;

	private static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
	}

	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case SEARCH_SUGGEST:
				return SearchManager.SUGGEST_MIME_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		asyncCursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS, 10);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
	                    String[] selectionArgs, String sortOrder) {

		String query = selectionArgs[0];
		if (query == null || query.length() == 0) {
			return null;
		}

		SearchOnServer.SearchCriteria criteria = new SearchOnServer.SearchCriteria();
		criteria.name_completion=query;

		//String query = uri.getLastPathSegment().toLowerCase();
		try {
			// Avoid the problem of HTTPRequest1 finishing after HTTPRequest2
			// and updating with "old" results
			if (ongoingTask != null && ongoingTask.status != AsyncTask.Status.FINISHED)
				ongoingTask.cancelAsyncTasks();

			ongoingTask = new BeerGetter(getCtx(), new BeerGetter.BeerListener<Beer[]>() {
				@Override
				public void onPostAction(Beer[] items) {
					try {
						// Search Content Provider does not block
						updateHTTP(items);
					}
					finally {

					}
				}
			});
			ongoingTask.queryByName(query, true);
			return asyncCursor;



		}
		finally {

		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
	                  String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	/*private Object[] createRow(Integer id, String text1, String text2,
	                           String name) {
		return new Object[] { id, // _id
				text1, // text1
				text2, // text2
				text1, "android.intent.action.SEARCH", // action
				SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
	}*/
	private void updateHTTP(BaseItem[] results) {
		// Creates a new cursor when we get a HTTP response returns
		MatrixCursor nCursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS, 10);
		//TODO id??
		//BaseItem[] results = j;
		for (int i = 0; i < results.length; i++) {
			BaseItem j = results[i];
			nCursor.addRow(new String[] {
					"0", j.name[0]._value, j._id
			});
		}
		if(queryListener != null){
			queryListener.onFinished(nCursor);
		}
		asyncCursor = nCursor;
	}

	private Context getCtx() {
		if(this.ctx == null){
			return getContext();
		}
		else return ctx;
	}

	public void setQueryListener(QueryFinished cb) {
		this.queryListener = cb;
	}


}


