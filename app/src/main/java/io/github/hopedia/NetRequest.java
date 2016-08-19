package io.github.hopedia;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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
public abstract class NetRequest extends AsyncTask<NetRequest.Args, Void, NetRequest.Result> {

	public static class Args {
		public String url;
		public Object content;
		public Class returnClass;
		public Integer timeout;
		public boolean cookies;
		public byte[] postData;

		/**
		 *
		 * @param url URL to POST
		 * @param content Content of the post request
		 * @param returnClass Class that will be returned by gson
		 * @param timeout timeout of the request
		 */
		public Args(String url, Object content, Class returnClass, Integer timeout) {
			init(url, content, returnClass, timeout, false);
		}
		public Args(String url, Object content, Class returnClass, Integer timeout, boolean cookies) {
			init(url, content, returnClass, timeout, cookies);
		}
		private void init(String url, Object criteria, Class returnClass, Integer timeout, boolean cookies) {
			this.url = url;
			this.content = criteria;
			this.returnClass = returnClass;
			this.timeout = timeout;
			this.cookies=cookies;
		}

	}
	public static class Result<T> {
		public T content;
		public boolean status;
		public List<String> cookies;

		public Result(T content, boolean status){
			this.content = content;
			this.status = status;
		}
	}

	public final static Object NETWORK_ERROR=0;
	public final static Object NETWORK_TIMEOUT=1;
	private Context ctx;
	static final String COOKIES_HEADER = "Set-Cookie";

	public interface TaskListener<T> {
		void onFinished(Result<T> result);
	}

	private final TaskListener taskListener;

	public NetRequest(Context a, TaskListener cb) {
		this.ctx = a;
		this.taskListener = cb;

	}

	@Override
	protected Result doInBackground(Args... params) {
		ConnectivityManager connMgr = (ConnectivityManager)
				ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {


			HttpURLConnection conn;
			try {
				URL u = new URL(params[0].url);
				conn = (HttpURLConnection) u.openConnection();
				AccountManager accountManager = new AccountManager(ctx);
				if(accountManager.getSessionCookie() != null) {
					conn.setRequestProperty("Cookie", accountManager.getSessionCookie());
				}
				conn.setConnectTimeout(params[0].timeout);

				conn=getConn(conn, params);
				if(conn.getDoOutput()) {
					OutputStream os = conn.getOutputStream();
					os.write(params[0].postData);
					os.close();
				}
				conn.connect();
				int status = conn.getResponseCode();


				switch (status) {
					case 200:
					case 201:
					case 304:

						Result result = new Result(null, true);

						if(params[0].cookies) {
							Map<String, List<String>> headerFields = conn.getHeaderFields();
							result.cookies= headerFields.get(COOKIES_HEADER);
						}
						if(params[0].returnClass == null) {}
						else if(params[0].returnClass.getName() == String.class.getName()) {
							result.content=generateString(conn.getInputStream());
						}
						else if(params[0].returnClass.getName() == Drawable.class.getName()) {
							result.content=Drawable.createFromStream(conn.getInputStream(), null);
						}
						else {
							Gson gson = new Gson();
							result.content=gson.fromJson(generateString(conn.getInputStream()), params[0].returnClass);
						}
						conn.disconnect();
						return result;
					default:
						conn.disconnect();
						//between 400 and 599 (4xx and 5xx error codes)
						if(status-400 < 200)
							return new Result(generateString(conn.getErrorStream()), false);
						else
							return new Result(generateString(conn.getInputStream()), false);
				}

			} catch (MalformedURLException e) {
				return networkError(e);
			} catch (ProtocolException e) {
				return networkError(e);
			} catch (IOException e) {
				//server can't be reach
				return networkError(e);
			}

		}
		else {
			// display error, network not connected
			return new Result(NETWORK_ERROR, false);
		}
	}

	protected abstract HttpURLConnection getConn(HttpURLConnection conn, Args[] params) throws ProtocolException;

	@Override
	protected void onPostExecute(Result result){
		super.onPostExecute(result);
		if(taskListener != null){
			this.taskListener.onFinished(result);
		}
	}

	private Result networkError(IOException e) {
		e.printStackTrace();
		return new Result(NETWORK_TIMEOUT, false);
	}

	private String generateString(InputStream inputStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		String aux;

		while ((aux = br.readLine()) != null) {
			builder.append(aux);
		}
		String sb = builder.toString();

		br.close();
		return sb;
	}
}
