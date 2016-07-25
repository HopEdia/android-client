package io.github.hopedia.Schemas;

import java.util.Date;

public class Review {
	public String shop;
	public String ebc;
	public ScentOrTaste taste;
	public ScentOrTaste scent;
	public Remark remark;
	public Float rate;
	public String user;
	public Date date;

	public String getShop() {
		return shop;
	}

	public String getEbc() {
		return ebc;
	}

	public ScentOrTaste getTaste() {
		return taste;
	}

	public ScentOrTaste getScent() {
		return scent;
	}

	public Remark getRemark() {
		return remark;
	}

	public Float getRate() {
		return rate;
	}

	public String getUser() {
		return user;
	}

	public Date getDate() {
		return date;
	}


}

