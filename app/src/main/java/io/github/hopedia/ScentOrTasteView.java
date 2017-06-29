package io.github.hopedia;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.github.hopedia.Schemas.ScentOrTaste;


public class ScentOrTasteView extends TableLayout {
	private View view;
	private Context ctx;
	private TableLayout seekBarTable;
	private TextView name;
	private ImageButton dropdownButton;
	private boolean deployed = false;
	private Field[] fields;
	private Map<String, SeekBarScentOrTasteView> map = new HashMap<String, SeekBarScentOrTasteView>();
	private boolean readOnly=false;

	public ScentOrTasteView(Context context) {
		super(context);
		init(context, "");
		initValues();
	}

	public ScentOrTasteView(Context context, AttributeSet a) {
		super(context, a);
		init(context, getContext().obtainStyledAttributes(a, R.styleable.ScentOrTasteView).getString(R.styleable.SeekBarScentOrTasteView_property));
		initValues();
		if(getContext().obtainStyledAttributes(a, R.styleable.ScentOrTasteView).getBoolean(R.styleable.ScentOrTasteView_readOnly, false)) {
			setReadOnly(true);
		}
	}

	public ScentOrTasteView(Context context, String name, ScentOrTaste item) {
		super(context);
		init(context, name);
		initValues(item);

	}
	private void init(Context ctx, String a) {
		this.ctx = ctx;
		view = LayoutInflater.from(ctx).inflate(R.layout.sample_scent_or_taste_view, this, true);
		name = (TextView) view.findViewById(R.id.view_name);
		name.setText(a);
		seekBarTable = (TableLayout) view.findViewById(R.id.tableSeekBar);


		dropdownButton = (ImageButton) view.findViewById(R.id.scent_or_taste_dropdown);
		dropdownButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(deployed) {
					deployed = !deployed;
					seekBarTable.setVisibility(GONE);
					dropdownButton.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
				}
				else {
					deployed = !deployed;
					seekBarTable.setVisibility(VISIBLE);
					dropdownButton.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);

				}
			}
		});
	}
	private void initValues(ScentOrTaste item) {
		fields = io.github.hopedia.Schemas.ScentOrTaste.class.getFields();
		SeekBarScentOrTasteView linked_element;
		for (Field field : fields) {
			linked_element = (SeekBarScentOrTasteView) view.findViewById(getResources().getIdentifier(field.getName(), "id", ctx.getPackageName()));
			if(linked_element==null)
				continue;
			//map containing every seekbar by property name
			map.put(field.getName(),  linked_element);
			try {
				if(field.isSynthetic()){
					continue;
				}
				Integer value = (Integer) field.get(item);
				if(value!=null)
					setValue(field.getName(), value);
				else
					setValue(field.getName(), 0);
			}
			catch (IllegalArgumentException e) {
				setValue(field.getName(), 0);
			}
			catch (IllegalAccessException e) {
				//value is empty
				e.printStackTrace();
			}
		}
	}
	private void initValues() {
		fields = io.github.hopedia.Schemas.ScentOrTaste.class.getFields();
		for (Field field : fields) {
			//if(field.get)
			//map containing every seekbar by property name
			map.put(field.getName(), (SeekBarScentOrTasteView) view.findViewById(getResources().getIdentifier(field.getName(), "id", ctx.getPackageName())) );
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	public void setValue(String property, Integer value) {
			map.get(property).setValue(value);
	}

	/**
	 * Get the values selected by the progress bars
	 * @return ScentOrTaste object got by progress bars
	 * @throws NoSuchFieldException
	 */
	public ScentOrTaste getValues() {
		ScentOrTaste scentOrTaste = new ScentOrTaste();
		Iterator it = map.entrySet().iterator();
		//loop through map & insert values in object
		Integer nullValues=0;
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			SeekBarScentOrTasteView pairScentOrTaste = (SeekBarScentOrTasteView) pair.getValue();
			String key = (String) pair.getKey();
			try {
				if(pairScentOrTaste != null && pairScentOrTaste.getValue()!=0)
					scentOrTaste.getClass().getField(key).set(scentOrTaste, pairScentOrTaste.getValue());
				else
					nullValues+=1;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			//it.remove(); // avoids a ConcurrentModificationException
		}
		if(nullValues==map.size())
			return null;
		else
			return scentOrTaste;
	}
	public void setValues(ScentOrTaste scentOrTaste) {
		initValues(scentOrTaste);
	}
	public void setName(String name) {
		this.name.setText(name);
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		if(readOnly) {
			Iterator it = map.entrySet().iterator();
			//loop through map & insert values in object
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				//instant run bug
				if(pair.getValue()!=null)
					((SeekBarScentOrTasteView) pair.getValue()).setReadOnly(readOnly);
			}
		}
	}
}

