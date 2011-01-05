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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;

import com.mindmutex.liepaja.ContentLoader.ContentHandlerStatus;

/** 
 * Activity that provides event list from liepajniekiem.lv. 
 * <p>
 * Implements expandable list view to view events per 
 * category including buttons to refresh and switch between dates.
 * 
 * TODO: implement cache.
 */
public class LiepajasAfisa extends ExpandableListActivity implements OnDateSetListener {
	
	/**
	 * {@link Handler} used to send UI requests from non UI threads.
	 * See documentation for more details on subject.
	 */
	private Handler handler = new Handler();

	/**
	 * {@link ExpandableListView} control.
	 */
	private ExpandableListView eventList;

	/**
	 * Date selected to view. Defaults to todays date.
	 */
	private Calendar calendar = Calendar.getInstance();

	/**
	 * {@link LiepajasAfisaListAdapter} adapter implementation to provide
	 * list groups and events
	 */
	private LiepajasAfisaListAdapter adapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = getLayoutInflater();

		eventList = getExpandableListView();
		eventList.addHeaderView(
				inflater.inflate(R.layout.main_header, null), null, false);

		// enable click event on date button (in main_header)
		handleDateButtonClick();

		adapter = new LiepajasAfisaListAdapter(this);
		setListAdapter(adapter);

		handleContentRefresh();
	}

	/**
	 * See <i>res/menu/main.xml</i> for more details what buttons are created.
	 * 
	 * @param menu menu provided by reference where the new buttons are registered.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	/**
	 * Handle button actions created in {@link #onCreateOptionsMenu(Menu)} method.
	 * 
	 * @param item item selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			handleContentRefresh();
			return true;
		case R.id.settings:
			startActivity(new Intent(LiepajasAfisa.this, LiepajasAfisaPreferences.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Refresh current event list by making a connection to web site
	 * and extracting relevant information.
	 * <p>
	 * {@link #eventList} is automatically updated once finished loading.
	 */
	private void handleContentRefresh() {
		final ProgressDialog progressDialog = new ProgressDialog(this);

		Button button = (Button) findViewById(R.id.date);

		DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
		button.setText(format.format(calendar.getTime()));

		progressDialog.setCancelable(false);
		progressDialog.setMessage(
				getString(R.string.refresh_loading));
		progressDialog.show();

		// retrieve URL used to make the connection
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String site = preferences.getString("site_url",
		"http://www.liepajniekiem.lv/lat/afisas-kalendars/");

		ContentLoader runnable = new ContentLoader(site, new ContentHandlerStatus() {
			@Override
			public void onComplete(Map<String, List<Event>> events) {
				progressDialog.dismiss();
				adapter.setEvents(events);
				handler.post(new Runnable() {
					@Override
					public void run() {
						eventList.invalidate();
						adapter.notifyDataSetChanged();
					}
				});
			}
			@Override
			public void onError(final int errorCode, final String message) {
				progressDialog.dismiss();
				handler.post(new Runnable() {
					@Override
					public void run() {
						String errorMessage = getString(errorCode);
						if (message != null) {
							errorMessage += ":" + message;
						}
						createSimpleAlertBox(errorMessage);
					}
				});
			}
		});
		runnable.setCalendar(calendar);

		Thread thread = new Thread(runnable);
		thread.start();
	}

	/**
	 * Creates a simple alert box that contains the provided message and
	 * simple button to dismiss the dialog.
	 * 
	 * @param message message
	 */
	private void createSimpleAlertBox(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * Date button i.e. the header that allows to switch between dates. Shows date picker
	 * dialog that allows date change.
	 */
	private void handleDateButtonClick() {
		Button button = (Button) findViewById(R.id.date);
		button.setClickable(true);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DatePickerDialog datePicker = new DatePickerDialog(
						LiepajasAfisa.this, LiepajasAfisa.this,
						calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH),
						calendar.get(Calendar.DATE));

				datePicker.setCancelable(true);
				datePicker.show();
			}
		});
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		calendar.set(year, monthOfYear, dayOfMonth);
		handleContentRefresh();
	}
}
