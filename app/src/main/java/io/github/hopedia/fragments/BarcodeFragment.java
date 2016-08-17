package io.github.hopedia.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import io.github.hopedia.BaseActivity;
import io.github.hopedia.MainActivity;
import io.github.hopedia.R;
import io.github.hopedia.SearchActivity;
import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BarcodeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BarcodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeFragment extends Fragment implements ZXingScannerView.ResultHandler {

	private OnFragmentInteractionListener mListener;
	private View view;
	private BaseActivity activity;
	private Intent intent;


	public BarcodeFragment() {
		// Required empty public constructor
	}
	public static BarcodeFragment newInstance() {
		return new BarcodeFragment();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_barcode, container, false);
	}
	
	@Override
	public void onViewCreated(View a, Bundle savedInstanceState) {
		view = a;
		activity = (BaseActivity) getActivity();

		super.onViewCreated(view, savedInstanceState);
		intent = new Intent(view.getContext(), SearchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		//TODO disable searchview on back

		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			setupUI(activity.findViewById(R.id.base));

		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			searchBar = (SearchView) view.findViewById(R.id.search_bar);
			searchBar.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
			setupUI(searchBar);
			searchBar.setOnClickListener(new View.OnClickListener() {
				@TargetApi(Build.VERSION_CODES.HONEYCOMB)
				@Override
				public void onClick(View v) {
					searchBar.setIconified(false);
					searchBar.requestFocus();
				}
			});
			searchBar.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
		}
		else {
			final EditText searchOld = (EditText) view.findViewById(R.id.search_bar_old);
			searchOld.setOnKeyListener(new TextView.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
							keyCode == EditorInfo.IME_ACTION_DONE ||
							event.getAction() == KeyEvent.ACTION_DOWN &&
									event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
						intent.setAction(Intent.ACTION_SEARCH);
						intent.putExtra(SearchManager.QUERY, searchOld.getText().toString());
						startActivity(intent);
						return true;
					}
					else {
						return false;
					}
				}
			});
		}

		contentFrame = (ViewGroup) view.findViewById(R.id.barcode_scanner);
		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
			//don't have permission, so ask and remember we asked
			//TODO tinydb ?
			if(!permissionAsked) {
				permissionAsked = true;
				requestCameraPermission();
			}
			else {
				add_request_camera_button(contentFrame);
			}
		}
		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
			//no else if because we need to check if has just been granted
			add_barcode();
		}

		ImageButton recentBeers = (ImageButton) view.findViewById(R.id.beer_history);
		recentBeers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) activity).setPagerItem(1);
			}
		});
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
		if(mScannerView != null) {
			mScannerView.stopCamera();
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int ZXING_CAMERA_PERMISSION = 1;
	private ZXingScannerView mScannerView;
	private boolean permissionAsked = false;

	private SearchView searchBar;

	static final String BARCODE = "io.github.hopedia.BARCODE";
	private ViewGroup contentFrame;

	
	public void setupUI(final View view) {
		//Set up touch listener for non-text box views to hide keyboard.
		if(!(view instanceof SearchView)) {
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					//searchBar.setIconified(true);
					view.requestFocus();
					hideSoftKeyboard(activity);
					return false;
				}
			});
		}
		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupUI(innerView);
			}
		}
	}

	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		try {
			inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private View.OnClickListener allowCameraButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			requestCameraPermission();
		}
	};


	private void add_request_camera_button(ViewGroup contentFrame) {
		View grant_camera = activity.getLayoutInflater().inflate(R.layout.grant_camera, null);
		contentFrame.addView(grant_camera);
		view.findViewById(R.id.grantCamera).setOnClickListener(allowCameraButtonListener);
	}


	private void add_barcode() {
		mScannerView = new ZXingScannerView(activity) {
			@Override
			protected IViewFinder createViewFinderView(Context context) {
				return new CustomViewFinderView(context);
			}
		};
		mScannerView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					mScannerView.toggleFlash();
				}
				catch (java.lang.RuntimeException e) {
					//doesn't have flash, ignore
				}
				return false;
			}
		});
		contentFrame.addView(mScannerView);

	}
	private void requestCameraPermission() {
		requestPermissions(new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
	}

	@Override
	public void onResume() {
		super.onResume();

		if(mScannerView != null) {
			mScannerView.setResultHandler(this);
			mScannerView.startCamera();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(mScannerView != null) {
			mScannerView.stopCamera();
		}
	}

	@Override
	public void handleResult(com.google.zxing.Result rawResult) {
		//for barcode
		intent.setAction(BARCODE);
		intent.putExtra("barcode", rawResult.getText());
		intent.putExtra("barcodeFormat",rawResult.getBarcodeFormat().toString());
		startActivity(intent);

		// Note:
		// * Wait 2 seconds to resume the preview.
		// * On older devices continuously stopping and resuming camera preview can result in freezing the app.
		// * I don't know why this is the case but I don't have the time to figure out.
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mScannerView.resumeCameraPreview(BarcodeFragment.this);
			}
		}, 2000);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
	                                       int[] grantResults) {

		if (requestCode == ZXING_CAMERA_PERMISSION) {
			// BEGIN_INCLUDE(permission_result)
			// Received permission result for camera permission.

			// Check if the only required permission has been granted
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Camera permission has been granted, preview can be displayed
				add_barcode();
			} else {
				//not granted
				add_request_camera_button(contentFrame);

			}
			// END_INCLUDE(permission_result)

		}

		else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private static class CustomViewFinderView extends ViewFinderView {
		public CustomViewFinderView(Context context) {
			super(context);
		}

		public CustomViewFinderView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
		}

	}




}
