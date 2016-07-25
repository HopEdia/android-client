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
public class BaseItem {
	public String _id;
	public String type;
	public Name[] name;

	public BaseItem() {}
	public BaseItem(BaseItem item) {
		this._id = item._id;
		this.type = item.type;
		this.name = item.name;
	}
	public BaseItem(String _id, String type, String name) {
		this._id = _id;
		this.type = type;
		this.name = new Name[]{new Name(name)};
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public void setName(String name) {
		this.name = new Name[]{new Name(name)};
	}

	public String get_id() {
		return _id;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		try {
			return name[0].get_value();
		} catch (Exception e) {
			return null;
		}
	}
}
