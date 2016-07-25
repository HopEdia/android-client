/*
 * Hop Edia android client
 *  Copyright (C) 2016  koko-ng <koko.fr.mu@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * * Hop Edia android client
 *  Copyright (C) 2016  koko-ng <koko.fr.mu@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * * Hop Edia android client
 *  Copyright (C) 2016  koko-ng <koko.fr.mu@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.github.hopedia;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

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
 * TODO: document your custom view class.
 */
public class SeekBarScentOrTasteView extends TableRow {
	private String mProperty;
	private String mPropertyString;
	private Integer mValue;

	private SeekBar seekBar;
	private TextView textView;

	private TypedArray a;
	private boolean readOnly;

	public SeekBarScentOrTasteView(Context context) {
		super(context);
	}

	public SeekBarScentOrTasteView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		View v = LayoutInflater.from(ctx).inflate(R.layout.sample_seek_bar_scent_or_taste_view,
				this, true);

		seekBar = (SeekBar) v.findViewById(R.id.seekBar);
		
		textView = (TextView) v.findViewById(R.id.property);
		// Load attributes
		a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarScentOrTasteView);

		mProperty = a.getString(R.styleable.SeekBarScentOrTasteView_property);
		try {
			mPropertyString = a.getResources().getString(a.getResources().getIdentifier(mProperty, "string", ctx.getPackageName()));
		}
		catch (NullPointerException e) {
			mPropertyString = mProperty;
		}
		mValue = a.getInteger(R.styleable.SeekBarScentOrTasteView_value, 0);
		if(a.getBoolean(R.styleable.SeekBarScentOrTasteView_ro, false)) {
			setReadOnly(true);
		}

		textView.setText(mPropertyString);
		seekBar.setProgress(mValue);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);




	}
	public Integer getValue() {
		return seekBar.getProgress();
	}

	public void setValue(Integer value) {
		seekBar.setProgress(value);
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		if(readOnly) {
			seekBar.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					return true;
				}
			});
		}
	}
}
