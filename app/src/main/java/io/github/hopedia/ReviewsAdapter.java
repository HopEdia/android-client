package io.github.hopedia;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import io.github.hopedia.Schemas.Review;
import io.github.hopedia.fragments.BaseItemFragment;

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
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

	private final Context ctx;
	private ArrayList<Review> reviews;
	private final BaseItemFragment.OnListFragmentInteractionListener listener;


	public ReviewsAdapter(Context ctx,  BaseItemFragment.OnListFragmentInteractionListener listener) {
		this.ctx = ctx;
		this.listener = listener;
	}

	@Override
	public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.
				from(parent.getContext()).
				inflate(R.layout.item_review, parent, false);
		ReviewViewHolder holder = new ReviewViewHolder(itemView);
		return holder;
	}

	@Override
	public void onBindViewHolder(ReviewViewHolder holder, int position) {
		holder.mReview=reviews.get(position);
		// Check if an existing view is being reused, otherwise inflate the view

		//TextView username = (TextView) holder.mView.findViewById(R.id.review_username);
		//username.setText(holder.mReview.getUser());

		TextView date = (TextView) holder.mView.findViewById(R.id.review_date);
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(ctx);
		date.setText(dateFormat.format(holder.mReview.getDate()));
		if(holder.mReview.getRemark()!=null) {
			TextView remark = (TextView) holder.mView.findViewById(R.id.review_remark);
			//TODO same language ?
			remark.setText(holder.mReview.getRemark().get_value());
			remark.setVisibility(View.VISIBLE);
		}
		if(holder.mReview.getTaste()!=null) {
			ScentOrTasteView taste = (ScentOrTasteView) holder.mView.findViewById(R.id.review_taste);
			taste.setValues(holder.mReview.getTaste());
			taste.setName(ctx.getString(R.string.taste));
			taste.setVisibility(View.VISIBLE);
			taste.setEnabled(false);
		}
		if(holder.mReview.getScent()!=null) {
			ScentOrTasteView scent = (ScentOrTasteView) holder.mView.findViewById(R.id.review_scent);
			scent.setValues(holder.mReview.getScent());
			scent.setName(ctx.getString(R.string.scent));
			scent.setVisibility(View.VISIBLE);
			scent.setEnabled(false);
		}
		if(holder.mReview.getRate()!=null){
			RatingBar ratingbar = (RatingBar) holder.mView.findViewById(R.id.review_rating);
			ratingbar.setRating(holder.mReview.getRate()*2);
			ratingbar.setVisibility(View.VISIBLE);
			LayerDrawable stars = (LayerDrawable) ratingbar.getProgressDrawable();
			stars.getDrawable(2).setColorFilter(ctx.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
			stars.getDrawable(1).setColorFilter(ctx.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
			stars.getDrawable(0).setColorFilter(ctx.getResources().getColor(R.color.colorPrimaryTransparency), PorterDuff.Mode.SRC_ATOP);
		}
	}

	@Override
	public int getItemCount() {
		if(reviews!=null)
			return reviews.size();
		else
			return 0;
	}

	public static class ReviewViewHolder extends RecyclerView.ViewHolder {
		protected TextView vName;
		public Review mReview;
		public final View mView;

		public ReviewViewHolder(View v) {
			super(v);
			mView = v;
			vName =  (TextView) v.findViewById(R.id.beer_name);
		}
	}
	public void setItems(Review[] reviews) {
		this.reviews = new ArrayList<Review>(Arrays.asList(reviews));
		this.notifyDataSetChanged();
	}
}
