package io.github.hopedia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
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
public class BaseActivity extends AppCompatActivity {

	private View inflated;
	private ViewStub stub;
	public AccountManager am;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		am = new AccountManager(this);
		setContentView(R.layout.activity_base);
		stub = (ViewStub) findViewById(R.id.base_content);
	}
	public void setLayout(int layout) {
		if(inflated != null) {
			setContentView(R.layout.activity_base);
			stub = (ViewStub) findViewById(R.id.base_content);
		}
		stub.setLayoutResource(layout);
		inflated = stub.inflate();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode,Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		Log.e("LOG", "HZGH");
		if(requestCode == Application.LOGIN_INTENT) {
			// code 1 = take picture
			if (resultCode == RESULT_OK) {
				am.tl.onFinished(new Post.Result(null, true));
			} else {
				am.tl.onFinished(new Post.Result(null, false));
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	public void enableToolbar(int layout) {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setVisibility(View.VISIBLE);
		ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(inflated.getLayoutParams());
		marginParams.setMargins(0, R.attr.actionBarSize, 0, 0);
		CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(marginParams);
		inflated.setLayoutParams(layoutParams);
	}

	public View getRootView() {
		if(inflated !=null)
			return inflated;
		else
			return stub;
	}
}
