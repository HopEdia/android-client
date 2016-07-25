package io.github.hopedia;

import android.content.Context;

import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.Charset;

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
public class Post extends NetRequest {

	public Post(Context a, TaskListener cb) {
		super(a, cb);
	}

	@Override
	public HttpURLConnection getConn(HttpURLConnection conn, Args[] params) throws ProtocolException {
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("charset", "utf-8");
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		Gson gson = new Gson();
		String criteria_str = gson.toJson(params[0].content);
		params[0].postData = criteria_str.getBytes(Charset.forName("UTF-8"));
		int postDataLength = params[0].postData.length;
		conn.setFixedLengthStreamingMode(postDataLength);
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		return conn;
	}
}
