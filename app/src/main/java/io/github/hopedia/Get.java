package io.github.hopedia;

import android.content.Context;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

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
public class Get extends NetRequest {

	public Get(Context a, TaskListener cb) {
		super(a, cb);
	}

	@Override
	public HttpURLConnection getConn(HttpURLConnection conn, Args[] params) throws ProtocolException {
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("charset", "utf-8");
		conn.setUseCaches(true);
		conn.setDoInput(true);
		conn.setDoOutput(false);
		return conn;
	}
}
