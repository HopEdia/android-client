package io.github.hopedia;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class Put extends NetRequest {
	public Put(Context a, TaskListener cb) {
		super(a, cb);
	}

	@Override
	public HttpURLConnection getConn(HttpURLConnection conn, Args[] params) throws ProtocolException {
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("PUT");
		conn.setRequestProperty("charset", "utf-8");
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		if(params[0].content.getClass().getName() == File.class.getName()) {
			//assume image
			conn.setRequestProperty("Content-Type", "image/jpeg");
			File file = (File) params[0].content;
			try {
				BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
				params[0].postData=new byte[(int) file.length()];
				buf.read(params[0].postData, 0, params[0].postData.length);
				buf.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int postDataLength = params[0].postData.length;
		conn.setFixedLengthStreamingMode(postDataLength);
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		return conn;
	}
}
