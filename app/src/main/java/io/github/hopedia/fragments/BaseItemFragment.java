package io.github.hopedia.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.mikepenz.iconics.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import io.github.hopedia.ApplicationHopedia;
import io.github.hopedia.BaseActivity;
import io.github.hopedia.BeerGetter;
import io.github.hopedia.DbHelper;
import io.github.hopedia.HopEdiaUtils;
import io.github.hopedia.NetRequest;
import io.github.hopedia.NetRequest.Args;
import io.github.hopedia.NetRequest.Result;
import io.github.hopedia.Post;
import io.github.hopedia.Put;
import io.github.hopedia.R;
import io.github.hopedia.ReviewsAdapter;
import io.github.hopedia.ScentOrTasteView;
import io.github.hopedia.Schemas.Abv;
import io.github.hopedia.Schemas.Barcode;
import io.github.hopedia.Schemas.BaseItem;
import io.github.hopedia.Schemas.Beer;
import io.github.hopedia.Schemas.BeerEdit;
import io.github.hopedia.Schemas.Ibu;
import io.github.hopedia.Schemas.Image;
import io.github.hopedia.Schemas.Name;
import io.github.hopedia.Schemas.Remark;
import io.github.hopedia.Schemas.Review;
import io.github.hopedia.Schemas.NewReview;
import io.github.hopedia.SearchOnServer;
import io.github.hopedia.SearchOnServer.SearchCriteria;
import io.github.hopedia.SearchOnServer.onItem;
import io.github.hopedia.SuggestionsProvider;
import io.github.hopedia.SuggestionsProvider.QueryFinished;

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
/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BaseItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BaseItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//TODO <> -> BaseItemFragment
public class BaseItemFragment extends Fragment implements onItem<BaseItem[]>, BeerGetter.BeerListener<Beer> {

	// TODO: Rename and change types of parameters
	private BaseItem item;
	private View view;
	private BaseActivity activity;
	private boolean editOn = false;
	private boolean newItem = false;

	private FloatingActionButton fabShare;
	private CollapsingToolbarLayout collapsingToolbarLayout;
	private ImageButton saveButton;
	private ImageButton drinkButton;
	private TextView drinkButtonText;
	private boolean drinkButtonState = true;
	private ImageButton sendReviewButton;
	private RatingBar ratingBar;
	private EditText remark;
	private ScentOrTasteView taste;
	private ScentOrTasteView scent;
	private RelativeLayout reviewBeer;
	private RelativeLayout drinkButtonContainer;
	private ViewSwitcher vs;
	private EditText beernameEdit;
	private ImageView beerImage;

	private HashMap<String, EditText> propertiesEdit = new HashMap<>();
	private String type;
	private Toolbar toolbar;
	private AppBarLayout app_bar;
	private CoordinatorLayout rootLayout;
	private MenuItem editSaveMenu;
	private int reviewBeerState;
	private ViewSwitcher beernameEditSwitch;
	private ImageButton cancelButton;
	private boolean barcode=false;
	private SearchView searchBar;
	private SimpleCursorAdapter adapter;
	private Barcode[] barcodeObj;
	private DbHelper dbHelper;
	private SwipeRefreshLayout swipeContainer;
	private boolean refresh;
	private boolean initialized=false;
	private HashMap<String, TextView> properties  = new HashMap<>();
	private Image insertImage;
	private RecyclerView reviews;
	private ReviewsAdapter reviewsAdapter;
	private Long recentBeerId;
	//private OnFragmentInteractionListener mListener;

	public BaseItemFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param a Item to be diplayed
	 * @return A new instance of fragment BaseItemFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static BaseItemFragment newInstance(BaseItem a) {
		return init(a, false, false);
	}

	public static BaseItemFragment newInstance(BaseItem a, boolean edit) {
		return init(a, edit, false);
	}

	public  static BaseItemFragment newInstance(BaseItem a, boolean edit, boolean newItem) {
		return init(a, edit, newItem);
	}

	private static BaseItemFragment init(BaseItem a, boolean edit, boolean b) {
		BaseItemFragment fragment = new BaseItemFragment();
		fragment.item = a;
		fragment.editOn = edit;
		fragment.newItem = b;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		//am = new AccountManager(getActivity());
		//getFragmentManager().getBackStackEntryAt()


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_base_item, container, false);

	}

	@Override
	public void onViewCreated(View a, Bundle savedInstanceState) {
		view = a;
		activity = (BaseActivity) getActivity();
		super.onViewCreated(view, savedInstanceState);

		toolbar = (android.support.v7.widget.Toolbar) view.findViewById(R.id.toolbar);
		activity.setSupportActionBar(toolbar);

		//set class member
		app_bar = (AppBarLayout) view.findViewById(R.id.app_bar);

		beernameEdit = (EditText) view.findViewById(R.id.beer_name_edit);

		beernameEditSwitch = (ViewSwitcher) view.findViewById(R.id.beer_name_edit_switch);

		vs = (ViewSwitcher) view.findViewById(R.id.beer_desc_switch);

		collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);

		saveButton = (ImageButton) view.findViewById(R.id.save_button);

		cancelButton = (ImageButton) view.findViewById(R.id.cancel_button);

		drinkButtonText = (TextView) view.findViewById(R.id.drink_button_text);
		//Scent & taste
		taste = (ScentOrTasteView) view.findViewById(R.id.taste);
		scent = (ScentOrTasteView) view.findViewById(R.id.scent);

		ratingBar = (RatingBar) view.findViewById(R.id.beer_rating);

		remark = (EditText) view.findViewById(R.id.remark);

		reviewBeer = (RelativeLayout) view.findViewById(R.id.review_beer);

		fabShare = (FloatingActionButton) view.findViewById(R.id.fabShareBeer);

		sendReviewButton = (ImageButton) view.findViewById(R.id.send_review);

		drinkButton = (ImageButton) view.findViewById(R.id.drink_button);

		drinkButtonContainer = (RelativeLayout) view.findViewById(R.id.beer_button_container);

		rootLayout = (CoordinatorLayout) view.findViewById(R.id.root_layout);

		beernameEdit.bringToFront();

		swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

		beerImage = (ImageView) view.findViewById(R.id.beer_image);

		reviewsAdapter = new ReviewsAdapter(getContext() , new OnListFragmentInteractionListener() {

		});
		reviews = (RecyclerView) view.findViewById(R.id.reviews);
		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		reviews.setLayoutManager(llm);
		reviews.setAdapter(reviewsAdapter);

		LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
		stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
		stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
		stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.colorPrimaryTransparency), PorterDuff.Mode.SRC_ATOP);

		//reviews = (RelativeLayout) view.findViewById(R.id.reviews);
		collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.textColorPrimaryInverse));
		activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
		//listeners
		final View.OnClickListener drinkButtonListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//change state & change image
				drinkButtonState = !drinkButtonState;
				setDrinkButtonState(drinkButton, drinkButtonState);
			}
		};

		drinkButtonContainer.setOnClickListener(drinkButtonListener);
		drinkButton.setOnClickListener(drinkButtonListener);

		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
				editMode(!editOn);
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						getContext());

				// set title
				alertDialogBuilder.setTitle(getString(R.string.save_beer));

				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, close
								// current activity
								editMode(false);
								save();
								dialog.cancel();
							}
						})
						.setNegativeButton("No",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								editMode(false);
								dialog.cancel();
							}
						});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
		});

		fabShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				share();
			}
		});

		sendReviewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendReview();
			}
		});

		// Setup refresh listener which triggers new data loading

		swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Your code to refresh the list here.
				// Make sure you call swipeContainer.setRefreshing(false)
				// once the network request has completed successfully.
				if(newItem)
					swipeContainer.setRefreshing(false);
				else {
					SearchCriteria criteria = new SearchCriteria();
					criteria.id = item.get_id();
					newItem = false;
					refresh = true;
					new SearchOnServer(getContext(), type, criteria, BaseItemFragment.this);
				}
			}

		});

		if(savedInstanceState != null)
			new BeerGetter(getContext()).queryById(savedInstanceState.getString("id"), this);
		else
			initValues();



	}

	private void initValues(BaseItem item) {
		this.item = item;
		initValues();
	}
	private void initValues(BaseItem item, boolean init) {
		this.item = item;
		initValues(init);
	}
	public void initValues(){initValues(true);}

	private void initValues(boolean init) {
		//content

		if(newItem && item == null)
			item=new Beer();

		collapsingToolbarLayout.setTitle(item.getName());

		if (item instanceof Beer) {
			type = "beer";
			final Beer beer = (Beer) item;
			//store beer for offline use
			if(!newItem)
				new BeerGetter(getContext()).insert(beer);

			if(beer.getImage()!=null) {
				beer.getImage().getImage(getContext(), new Image.ImageReady() {

					@Override
					public void onLoaded(Drawable image) {
						if (image != null) {
							beerImage.setImageDrawable(image);
							beerImage.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
							beerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
						}
						else {
							beerImage.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									//intent
									dispatchTakePictureIntent();

								}
							});
						}
					}
				});
			}
			else {
				beerImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//intent
						dispatchTakePictureIntent();

					}
				});
			}
			TableLayout tl_edit = (TableLayout) view.findViewById(R.id.beer_short_desc_edit);
			TableLayout tl = (TableLayout) view.findViewById(R.id.beer_short_desc);

			if(beer.getBarcode() != null && newItem)
				barcode=true;
			if(newItem) {
				beernameEditSwitch.setDisplayedChild(beernameEditSwitch.indexOfChild(view.findViewById(R.id.beer_name_edit_auto)));

				AutoCompleteTextView beernameEditAuto = (AutoCompleteTextView) view.findViewById(R.id.beer_name_edit_auto);
				propertiesEdit.put("name", beernameEditAuto);
				adapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_1, null,
						new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
						new int[] {android.R.id.text1},
						0);
				beernameEditAuto.setAdapter(adapter);
				adapter.setFilterQueryProvider(new FilterQueryProvider() {
					public Cursor runQuery(CharSequence str) {
						String[] query = new String[]{str.toString()};
						SuggestionsProvider sp =  new SuggestionsProvider(getContext());

						sp.setQueryListener(new QueryFinished() {
							@Override
							public void onFinished(Cursor c) {
								adapter.changeCursor(c);
							}
						});
						Cursor result = sp.query(null, null, null, query, null);
						return result;
					} });

				adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
					public CharSequence convertToString(Cursor cur) {
						int index = cur.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
						return cur.getString(index);
					}});
				beernameEditAuto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						SearchCriteria criteria = new SearchCriteria();
						criteria.id = adapter.getCursor().getString(2);
						barcodeObj = ((Beer) item).barcode;
						newItem =false;
						new SearchOnServer(getContext(), type, criteria, BaseItemFragment.this);
					}

				});

			}
			else if (barcode && barcodeObj!=null)
				((Beer) item).barcode = barcodeObj;

			if(!newItem)
				propertiesEdit.put("name", beernameEdit);

			propertiesEdit.get("name").setText(item.getName());
			if(init) {
				if (beer.getAbv() != null) {
					addPropRow(tl, "abv", beer.getAbv(), false);

					addPropRow(tl_edit, "abv", beer.getAbv(), true);

				} else {
					addPropRow(tl_edit, "abv", "", true);
				}

				if (beer.getIbu() != null) {
					addPropRow(tl, "ibu", beer.getIbu(), false);

					addPropRow(tl_edit, "ibu", beer.getIbu(), true);

				} else {
					addPropRow(tl_edit, "ibu", "", true);
				}
			}
			else {
				if (beer.getAbv() != null) {
					propertiesEdit.get("abv").setText(beer.getAbv().toString());
					if(properties.get("abv") !=null)
						properties.get("abv").setText(String.format(getString(getResources().getIdentifier("abv", "string", getActivity().getPackageName())), beer.getAbv().toString()));
					else
						addPropRow(tl, "abv", beer.getAbv(), false);
				}
				if (beer.getIbu() != null) {
					propertiesEdit.get("ibu").setText(beer.getIbu().toString());
					if(properties.get("ibu")!= null)
						properties.get("ibu").setText(String.format(getString(getResources().getIdentifier("ibu", "string", getActivity().getPackageName())), beer.getIbu().toString()));
					else
						addPropRow(tl, "ibu", beer.getIbu(), false);
				}
			}
			if(beer.getReviews() != null) {
				reviewsAdapter.setItems(beer.getReviews());
				//setListViewHeightBasedOnChildren(listViewCompat);
				//((RelativeLayout) view.findViewById(R.id.content)).addView(listViewCompat);
				/*for (Review review : beer.reviews) {
					//View v = pushReview(review);
					reviews.addView(v);
				}*/
			}
		}

		editMode(editOn);
		initialized=true;

	}


	private void save() {
		Log.e("LOG", Boolean.toString(newItem));
		final BaseItem editedProperties = getEditedProperties();
		abstract class Callback implements NetRequest.TaskListener<String> {
			@Override
			public void onFinished(Result<String> result) {
				if (result.status) {
					newItem = false;
					Toast toast = Toast.makeText(getContext(), getEditSuccess(), Toast.LENGTH_LONG);
					toast.show();
					final SearchCriteria criteria = getCriteria();
					if(((BeerEdit) editedProperties).image) {
						Args args = new Args(result.content, insertImage.image, String.class, getResources().getInteger(R.integer.timeout), true);
						new Put(getContext(), new NetRequest.TaskListener() {
							@Override
							public void onFinished(Result result) {
								new SearchOnServer(getContext(), type, criteria, BaseItemFragment.this);
							}
						}).execute(args);
					}
					else {
						new SearchOnServer(getContext(), type, criteria, BaseItemFragment.this);
					}
				} else {
					if(result.content==NetRequest.NETWORK_ERROR) {
						Toast toast = Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG);
						toast.show();
					}
					Toast toast = Toast.makeText(getContext(), getEditFailed(), Toast.LENGTH_LONG);
					toast.show();
				}
			}
			public abstract SearchCriteria getCriteria();
			public abstract int getEditSuccess();
			public abstract int getEditFailed();

		}
		if (newItem) {
			//new item -> /insert

			((BaseActivity) getActivity()).am.login(new Post.TaskListener() {
				@Override
				public void onFinished(Result result) {
					//url, content, class, timeout
					Args args = new Args(getString(R.string.server_url) + "/insertBeer", editedProperties, String.class, getResources().getInteger(R.integer.timeout));
					Post post = new Post(getContext(), new Callback() {
						@Override
						public SearchCriteria getCriteria() {
							SearchCriteria criteria = new SearchCriteria();
							criteria.name = editedProperties.getName();
							return criteria;
						}

						@Override
						public int getEditSuccess() {
							return R.string.beer_added;
						}

						@Override
						public int getEditFailed() {
							return R.string.beer_add_failed;
						}
					});
					post.execute(args);
				}
			});
		} else {
			//only edit -> /edit
			((BaseActivity) getActivity()).am.login(new Post.TaskListener() {
				@Override
				public void onFinished(Result result) {
					//url, content, class, timeout
					Args args = new Args(getString(R.string.server_url) + "/editBeer", editedProperties, String.class, getResources().getInteger(R.integer.timeout));
					Post post = new Post(getContext(), new Callback() {
						@Override
						public SearchCriteria getCriteria() {
							SearchCriteria criteria = new SearchCriteria();
							criteria.id = item._id;
							return criteria;
						}
						@Override
						public int getEditSuccess() {
							return R.string.beer_edited;
						}

						@Override
						public int getEditFailed() {
							return R.string.beer_edit_failed;
						}
					});
					post.execute(args);
				}
			});
		}

	}



	//instance of beer, brewery, etc. TODO
	private void sendReview() {
		final NewReview review = new NewReview();
		review.beerId = item._id;
		review.review = new Review();
		review.review.scent = scent.getValues();
		review.review.taste = taste.getValues();
		//TODO scent or taste values
		//Log.e("LOG", taste.getValues().sweet.toString());
		review.review.rate = ratingBar.getRating() * 2;
		review.review.remark = new Remark();
		review.review.remark._value = remark.getText().toString();
		review.review.remark.lang = Locale.getDefault().getLanguage();
		//TODO
		((BaseActivity) getActivity()).am.login(new Post.TaskListener() {
			@Override
			public void onFinished(Result result) {
				//url, content, class, timeout
				Args args = new Args(getString(R.string.server_url) + "/reviewBeer", review, String.class, getResources().getInteger(R.integer.timeout));
				Post post = new Post(getContext(), new Post.TaskListener() {
					@Override
					public void onFinished(Result result) {
						if (result.status) {
							Toast toast = Toast.makeText(getContext(), R.string.review_added, Toast.LENGTH_LONG);
							toast.show();
						} else {
							Toast toast = Toast.makeText(getContext(), R.string.review_failed, Toast.LENGTH_LONG);
							toast.show();
						}
					}
				});
				post.execute(args);
			}
		});
	}

	private void addPropRow(TableLayout tl, String prop, Number val, boolean edit) {
		addPropRow(tl, prop, val.toString(), edit);
	}

	private void addPropRow(TableLayout tl, final String prop, String val, boolean edit) {
		Integer propRessource;
			/* Find Tablelayout defined in main.xml */
			/* Create a new row to be added. */

		propRessource = getResources().getIdentifier(prop, "string", getActivity().getPackageName());

		Integer LayoutVal;
		if (edit) {
			LayoutVal = R.layout.beer_desc_value_edit;
		} else {
			LayoutVal = R.layout.beer_desc_value;
		}
		TableRow tr = new TableRow(getContext());
		tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT));

		if(edit) {
			TextView property = (TextView) getLayoutInflater(null).inflate(R.layout.beer_desc_property, null);
			property.setText(getString(propRessource).split("%1\\$s")[0]);
			tr.addView(property);
		}

		TextView value = (TextView) getLayoutInflater(null).inflate(LayoutVal, null);

		if (!edit) {
			properties.put(prop, value);
			value.setText(String.format(getString(propRessource), val));
		} else {
			propertiesEdit.put(prop, (EditText) value);
			value.setText(val);
		}
		tr.addView(value);

		//Add row to TableLayout.
		tl.addView(tr);
	}

	private BaseItem getEditedProperties() {
		BaseItem result = new BaseItem();
		result._id = item._id;

		if(!HopEdiaUtils.objectsEquals(propertiesEdit.get("name").getText().toString(), item.getName()) || newItem)
			result.name= new Name[]{new Name(propertiesEdit.get("name").getText().toString())};

		if (item instanceof Beer) {
			BeerEdit beerResult = new BeerEdit(result);
			try {
				if (propertiesEdit.get("abv").getText().toString().length() != 0) {
					beerResult.abv = new Abv[]{new Abv(Float.parseFloat(propertiesEdit.get("abv").getText().toString()))};

				}
				if (propertiesEdit.get("ibu").getText().toString().length() != 0) {
					beerResult.ibu = new Ibu[]{new Ibu(Float.parseFloat(propertiesEdit.get("ibu").getText().toString()))};
				}
				if (barcode && ((Beer) item).barcode != null) {
					beerResult.barcode = ((Beer) item).barcode;

				}
				if (insertImage != null) {
					//insertImage.image.toString();
					beerResult.image = true;
				}
			}
			catch (java.lang.NumberFormatException e) {
				Toast.makeText(getContext(), R.string.invalidValue, Toast.LENGTH_SHORT).show();
				editMode(true);
			}
			return beerResult;
		} else {
			throw new Error("no item provided");
		}
	}

	public void editMode(boolean a) {
		editOn = a;
		//ImageButton editSaveButton = (ImageButton) view.findViewById(R.id.editItem);
		//ImageButton cancelButton = (ImageButton) view.findViewById(R.id.cancel_button);
		if (editOn) {
			activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			if(editSaveMenu != null) {
				editSaveMenu.setTitle(R.string.save);
			}
			// editSaveButton.setImageResource(android.R.drawable.ic_menu_save);
			//cancelButton.setVisibility(View.VISIBLE);
			setDrinkButtonState(drinkButton, true);
			fabShare.setVisibility(View.GONE);
			collapsingToolbarLayout.setTitle(" ");
			saveButton.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
			//view.findViewById(R.id.rowItemName).bringToFront();
			beernameEditSwitch.setVisibility(View.VISIBLE);

			vs.setDisplayedChild(1);
		} else {
			activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			if(editSaveMenu != null) {
				editSaveMenu.setTitle(R.string.edit);
			}
			//editSaveButton.setImageResource(android.R.drawable.ic_menu_edit);
			//cancelButton.setVisibility(View.GONE);
			setDrinkButtonState(drinkButton, drinkButtonState);
			fabShare.setVisibility(View.VISIBLE);
			if(item.name != null)
				collapsingToolbarLayout.setTitle(item.name[0]._value);
			saveButton.setVisibility(View.GONE);
			cancelButton.setVisibility(View.GONE);

			beernameEditSwitch.setVisibility(View.GONE);

			vs.setDisplayedChild(0);
		}
	}

	// TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
*/
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		//     if (context instanceof OnFragmentInteractionListener) {
		//           mListener = (OnFragmentInteractionListener) context;
		//     } else {
		//         throw new RuntimeException(context.toString()
		//                + " must implement OnFragmentInteractionListener");
		//    }
	}

	@Override
	public void onDetach() {
		super.onDetach();
		//     mListener = null;
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
   /* public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("id", item.get_id());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.menu_base_item, menu);
		editSaveMenu = menu.getItem(1);
		if(editOn)
			editSaveMenu.setTitle(R.string.save);
		else
			editSaveMenu.setTitle(R.string.edit);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.edit:
				editOn = !editOn;
				if (!editOn)
					save();
				editMode(editOn);
				return true;
			case R.id.menuShareBeer:
				share();
				return true;
			case android.R.id.home:
				activity.onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void share() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		String share = getText(R.string.share_beer) + new String(Character.toChars(0x1f37b)) + " \n" + getString(R.string.server_url) + "/beer?id=" + item._id;
		sendIntent.putExtra(Intent.EXTRA_TEXT, share);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_beer_menu)));
	}

	private void setDrinkButtonState(ImageButton v, boolean dbs) {

		if (dbs) {
			drinkButtonText.setText(R.string.drink_button_text_full);
			v.setImageResource(R.drawable.ic_beer_full);
			reviewBeer.setVisibility(View.GONE);
			sendReviewButton.setVisibility(View.GONE);
			if(recentBeerId!=null) {
				new DbHelper(getContext()).removeRecentBeer(recentBeerId, new DbHelper.DbListener<Integer>() {
					@Override
					public void onPostAction(Integer integer) {

					}
				});
			}
		} else {
			drinkButtonText.setText(R.string.drink_button_text_empty);
			v.setImageResource(R.drawable.ic_beer_empty);
			reviewBeer.setVisibility(View.VISIBLE);
			sendReviewButton.setVisibility(View.VISIBLE);

			new DbHelper(getContext()).addRecentBeers(item._id, new DbHelper.DbListener<Long>() {
				@Override
				public void onPostAction(Long id) {
					recentBeerId=id;
				}
			});
		}

	}

	@Override
	public void callback(BaseItem[] items) {
		if(refresh) {
			refresh=false;
			swipeContainer.setRefreshing(false);
		}
		if(items != null)
			initValues(items[0], false);
		else
			Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onPostAction(Beer beer) {
		if(refresh) {
			refresh=false;
			swipeContainer.setRefreshing(false);
		}
		initValues(beer, !initialized);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ApplicationHopedia.CAMERA_INTENT && resultCode == Activity.RESULT_OK) {
			if(insertImage != null && insertImage.image.exists()) {
				if(!newItem)
					save();
				else {
					beerImage.setImageDrawable(Drawable.createFromPath(insertImage.image.getAbsolutePath()));
					beerImage.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
					beerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
				}
			}
		}

	}
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
			// Create the File where the photo should go
			insertImage = new Image();
			File photoFile = null;
			try {
				photoFile = insertImage.createFile(getContext());
			} catch (IOException ex) {
				// Error occurred while creating the File
				ex.printStackTrace();
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				Uri photoURI = FileProvider.getUriForFile(getContext(),
						"io.github.hopedia.imageprovider",
						photoFile);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, ApplicationHopedia.CAMERA_INTENT);
			}
		}
	}
	/*private LinearLayout pushReview(Review review) {
		LinearLayout relativeLayout = new LinearLayout(getContext());
		relativeLayout.setOrientation(LinearLayout.VERTICAL);
		//same language ? TODO
		TextView username = new TextView(getContext());
		username.setText(review.getUser());
		relativeLayout.addView(username);
		if(review.getRate()!=null) {
			RatingBar ratingBar = new RatingBar(getContext());
			ratingBar.setRating(review.getRate()/2);
			relativeLayout.addView(ratingBar);
		}
		if(review.getRemark()!=null) {
			if (review.getRemark().get_value() != null) {
				TextView textView = new TextView(getContext());
				textView.setText(review.getRemark().get_value());
				relativeLayout.addView(textView);
			}
		}
		if(review.getScent()!= null)
			relativeLayout.addView(new ScentOrTasteView(getContext(), getString(R.string.scent), review.getScent()));
		if (review.getTaste()!= null)
			relativeLayout.addView(new ScentOrTasteView(getContext(), getString(R.string.taste), review.getTaste()));
		return relativeLayout;
	}*/
	private static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
			return;

		int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
		int totalHeight = 0;
		View view = null;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			view = listAdapter.getView(i, view, listView);
			if (i == 0)
				view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

			view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
			totalHeight += view.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	public class OnListFragmentInteractionListener {
	}
}
