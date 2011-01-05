/**
 * Copyright (C) 2011 by mindmutex.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mindmutex.liepaja;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

/**
 * Adapter that would accept the propriety format of categories and events and transform
 * in valid data source fed in {@link ExpandableListView}. Also includes custom UI.
 */
public class LiepajasAfisaListAdapter extends BaseExpandableListAdapter {
	/**
	 * Available categories that are set when new data set of events
	 * is provided. Separate because requires index not existing value in map (the string).
	 */
	private List<String> categories = new ArrayList<String>();

	/**
	 * Categories and events provided to adapter.
	 */
	private Map<String, List<Event>> events = new LinkedHashMap<String, List<Event>>();

	/**
	 * Context required to obtain instance of {@link LayoutInflater}. In most cases context
	 * is {@link LiepajasAfisa}.
	 */
	private Context context;

	/**
	 * {@link LayoutInflater} to inflate XML layouts instead of building manually.
	 */
	private LayoutInflater inflater;

	public LiepajasAfisaListAdapter(Context context) {
		this.inflater = (LayoutInflater)
		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

	public void setEvents(Map<String, List<Event>> events) {
		this.categories =
			new ArrayList<String>(events.keySet());
		this.events = events;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return events.get(categories.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition,
			int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		View entry = inflater.inflate(R.layout.main, null);

		Event event = (Event) getChild(groupPosition, childPosition);
		String nameAndTime = event.location;
		if (event.timestamp != null && event.timestamp.length() > 0) {
			nameAndTime += String.format(" @ %s", event.timestamp);
		}

		((TextView) entry.findViewById(R.id.description)).setText(Html.fromHtml(event.description));
		((TextView) entry.findViewById(R.id.name)).setText(Html.fromHtml(nameAndTime));

		return entry;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return events.get(categories.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return categories.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return categories.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		TextView textView = getGenericView();
		textView.setText(getGroup(groupPosition).toString());
		return textView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private TextView getGenericView() {
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);

		TextView textView = new TextView(context);
		textView.setLayoutParams(params);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		textView.setPadding(36, 0, 0, 0);

		return textView;
	}
}
