package io.github.hopedia.Schemas;
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
public class Beer extends BaseItem {
	public BreweryId[] brewery_id;
	public Abv[] abv;
	public Ibu[] ibu;
	public Barcode[] barcode;
	public WikidataId[] wikidata_id;
	public CatId[] categories;
	public Review[] reviews;
	public Image[] images;

	public transient String dbInsertTimestamp;

	public Beer() {this.type = "beer";}
	public Beer(BaseItem p) {
		super(p);
	}
	public Beer(String _id, String type, String name, String brewery_id, Float abv, Float ibu, String wikidata_id, String categories, String barcodeValue, String barcodeFormat, String barcodeType) {
		super(_id, type, name);
		this.brewery_id = new BreweryId[]{new BreweryId(brewery_id)};
		this.abv = new Abv[]{new Abv(abv)};
		this.ibu = new Ibu[]{new Ibu(ibu)};
		this.barcode = new Barcode[]{new Barcode(barcodeValue, barcodeFormat, barcodeType)};
		this.wikidata_id = new WikidataId[]{new WikidataId(wikidata_id)};
		this.categories = new CatId[]{new CatId(categories)};
	}

	public String getBrewery_id() {
		try {
			return brewery_id[0].get_value();
		} catch (Exception e) {
			return null;
		}
	}

	public Float getAbv() {
		try {
			return abv[0].get_value();
		} catch (Exception e) {
			return null;
		}
	}

	public Float getIbu() {
		try {
			return ibu[0].get_value();
		} catch (Exception e) {
			return null;
		}
	}

	public String getBarcode() {
		try {
			return barcode[0].get_value();
		} catch (Exception e) {
			return null;
		}
	}

	public String getWikidata_id() {
		try {
			return wikidata_id[0].get_value();
		} catch (Exception e) {
			return null;
		}
	}

	public String getCategories() {
		try {
			return categories[0].get_value();
		} catch (Exception e) {
			return null;
		}
	}
	public Image getImage() {
		if(images != null && images.length>0) {
			if(images[0]!= null)
				return images[0];
			else
				return null;
		}
		else
			return null;
	}

	/*public Review[] getReviews() {
		return reviews;
	}*/

	public void setBrewery_id(String  brewery_id) {
		this.brewery_id = new BreweryId[]{new BreweryId(brewery_id)};
	}

	public void setAbv(Float abv) {
		this.abv = new Abv[]{new Abv(abv)};
	}

	public void setIbu(Float ibu) {
		this.ibu = new Ibu[]{new Ibu(ibu)};
	}

	public void setBarcode(String  barcode, String format, String type) {
		this.barcode = new Barcode[]{new Barcode(barcode, format, type)};
	}

	public void setWikidata_id(String wikidata_id) {
		this.wikidata_id = new WikidataId[]{new WikidataId(wikidata_id)};
	}

	public void setCategories(String categories) {
		this.categories = new CatId[]{new CatId(categories)};
	}
	public void newImage() {

	}


	public Review[] getReviews() {
		return reviews;
	}

	public void setImage(String image) {
		if(getImage()==null)
			this.images = new Image[]{new Image()};
		getImage().set_value(image);
	}

	public String getImageId() {
		if(getImage()==null)
			return null;
		else
			return getImage().get_value();
	}
}
