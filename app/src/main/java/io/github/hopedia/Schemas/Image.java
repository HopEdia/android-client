package io.github.hopedia.Schemas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.hopedia.Get;
import io.github.hopedia.NetRequest;
import io.github.hopedia.R;

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
public class Image extends Modifications<String> {
	private transient Drawable drawable;
	public Integer maxSize;
	public transient File image;
	private final static int[] sizes=new int[]{144, 360, 480, 720, 1080};

	public Image() {
		super();
	}

	public Image(String val) {
		set_value(val);
	}


	public interface ImageReady {
		void onLoaded(Drawable image);
	}

	public void getImage(final Context ctx, final ImageReady cb) {
		if(drawable != null)
			cb.onLoaded(drawable);
		else if(_value != null){
			File file;
			try {
				file = createFile(ctx, get_value());
				if(file.exists()) {
					drawable = Drawable.createFromStream(new FileInputStream(file), file.getName());
					cb.onLoaded(null);
					cb.onLoaded(drawable);
				}
				else {
					getFileOnline(ctx, cb);
				}
			} catch (IOException e) {
				getFileOnline(ctx, cb);
			}

		}
		else
			cb.onLoaded(null);
	}

	private void getFileOnline(final Context ctx, final ImageReady cb) {
		Get get = new Get(ctx, new NetRequest.TaskListener<Drawable>() {
			@Override
			public void onFinished(NetRequest.Result<Drawable> result) {
				drawable = result.content;
				cb.onLoaded(drawable);
				//cache
				Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
				File file = null;
				try {
					file = createFile(ctx, get_value());

					if(!file.createNewFile()) {
						file.delete();
						file.createNewFile();
					}
					FileOutputStream fo = new FileOutputStream(file);
					fo.write(bytes.toByteArray());
					fo.close();
				} catch (IOException e) {
					cb.onLoaded(null);
				}
			}
		});
		int i = 0;
		int size = Image.sizes[i];

		DisplayMetrics displaymetrics = new DisplayMetrics();
		((WindowManager) ctx.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displaymetrics);;
		Integer maxDisplaySize = displaymetrics.widthPixels/2;
		while (size < maxSize && size < maxDisplaySize) {
			i += 1;
			size = sizes[i];
		}

		NetRequest.Args args = new NetRequest.Args(ctx.getString(R.string.server_url) + "/uploads/" + size + "/" + _value + ".jpg", null, Drawable.class, ctx.getResources().getInteger(R.integer.timeout));
		get.execute(args);
	}

	public File createFile(Context ctx) throws IOException {
		return createFile(ctx, null);
	}
	public File createFile(Context ctx, String uid) throws IOException {
		// Create an image file name
		String imageFileName;
		if(uid == null) {
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			imageFileName = "JPEG_" + timeStamp + "_";
		}
		else {
			imageFileName=uid;
		}
		image = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + imageFileName);
		// Save a file: path for use with ACTION_VIEW intents
		return image;
	}
}
