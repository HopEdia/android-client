package io.github.hopedia;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import io.github.hopedia.NetRequest.Args;
import io.github.hopedia.NetRequest.Result;
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

//https://developer.android.com/reference/android/os/AsyncTask.html
public class SearchOnServer<T> {
	public interface onItem<T> {
		void callback(T items);
	}
	public static class SearchCriteria{
		public String name;
		public String barcode;
		public String barcode_format;
		public String id;
		public String name_completion;
	}

	private Context ctx;
	private onItem Caller;
	private T returnResult;

	public AsyncTask.Status status;

	public SearchOnServer(Context context, String type, SearchCriteria criteria) {
		init(context, type, criteria, (onItem) context);
	}
	public SearchOnServer(Context context, String type, SearchCriteria criteria, onItem caller) {
		init(context, type, criteria, caller);
	}
	private void init(Context context, String type, SearchCriteria criteria, final onItem caller){
		this.ctx = context;
		this.Caller = caller;
		this.status= AsyncTask.Status.RUNNING;

		Class returnClass;

		if(type == "beer") {
			returnClass = Beer[].class;
		}
		else {
			throw new UnknownError("Unknown type: "+type);
		}
		String url = ctx.getResources().getString(R.string.server_url) + "/" + type + "JSON";
		Args args = new Args(url, criteria, returnClass, ctx.getResources().getInteger(R.integer.timeout));
		Post result = new Post(ctx, new Post.TaskListener() {
			@Override
			public void onFinished(Result result) {
				status = AsyncTask.Status.FINISHED;
				if(result.status) {
					returnResult = (T) result.content;
					Caller.callback(result.content);
					return;
				}
				else if(result.content == NetRequest.NETWORK_TIMEOUT || result.content == NetRequest.NETWORK_ERROR) {
					Toast.makeText(ctx, R.string.network_error, Toast.LENGTH_LONG).show();
					Caller.callback(null);
					return;
				}
				else {
					Caller.callback(null);
					return;
				}
			}
		});
		result.execute(args);
	}
	public void cancel() {
		this.Caller = new onItem<T>() {
			@Override
			public void callback(T items) {
			}
		};
	}
	public T get() {
		return returnResult;
	}

}
