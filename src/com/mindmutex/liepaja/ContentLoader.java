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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

/** 
 * Opens a connection to site provided in constructor and attempts to extract event information 
 * from HTML provided in connection response. 
 */
public class ContentLoader implements Runnable {
	
	/** 
	 * Handler definition used by client to get notifications when 
	 * request successfully completes or fails. 
	 */
	public interface ContentHandlerStatus {
		/** 
		 * Method called when the request is successful. 
		 * 
		 * @param events list of events extracted from HTML where the key is category 
		 * 				 and value a list of events attached to the category.  
		 */
		void onComplete(Map<String, List<Event>> events);
		
		/** 
		 * Method called when fails to extract events from HTML provided or other. 
		 * 
		 * @param errorCode provides resource code ({@link R}) explaining the error 
		 * @param message optional exception message
		 */
		void onError(int errorCode, String message);
	}

	/**
	 * {@link ContentHandlerStatus} used to notify client.
	 */
	private ContentHandlerStatus handler = null;

	/**
	 * {@link HtmlCleaner} used to filter out events from other data using XPATH.
	 */
	private HtmlCleaner htmlCleaner = null;

	/**
	 * Events are retrieved per date. This variable defines which date to be retrieved. 
	 */
	private Calendar calendar = Calendar.getInstance();

	/**
	 * URL used to make connection. 
	 * Note URL can be modified in application preferences. 
	 */
	private String site;

	
	public ContentLoader(String site, ContentHandlerStatus handler) {
		this.handler = handler;
		
		// simple sanity check to make sure tiny problem does not cause the 
		// application to crash or return invalid results.
		if (!site.endsWith("/")) {
			site += "/";
		}
		this.site = site;
	}

	/**
	 * Keep a single instance of {@link #htmlCleaner} per instance.
	 * @return {@link #htmlCleaner}
	 */
	private HtmlCleaner getHtmlCleaner() {
		if (htmlCleaner == null) {
			htmlCleaner = new HtmlCleaner();
		}
		return htmlCleaner;
	}

	/**
	 * Sets the calendar and applies time zone that works. Web site 
	 * seems to use UTC time zone and day starts 2 hours before midnight.     
	 * 
	 * @param calendar calendar
	 */
	public void setCalendar(Calendar calendar) {
		this.calendar.set(
			calendar.get(Calendar.YEAR),
			calendar.get(Calendar.MONTH),
			calendar.get(Calendar.DAY_OF_MONTH), -2, 0, 0);

		this.calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void run() {
		try {
			Map<String, List<Event>> events = internalExecute();
			handler.onComplete(events);
		} catch (IOException ex) {
			handler.onError(R.string.error_nodes_general, ex.getMessage());
		} catch (XPatherException ex) {
			handler.onError(
				R.string.error_nodes_general, ex.getMessage());
			
		} catch (IllegalStateException ex) {
			handler.onError(R.string.error_nodes_empty, null);
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, List<Event>> internalExecute()
			throws IOException, XPatherException {
		
		Map<String, List<Event>> events = new LinkedHashMap<String, List<Event>>();
		
		URL url = new URL(site + "?" + (calendar.getTimeInMillis() / 1000));
		URLConnection connection = url.openConnection();

		TagNode root = getHtmlCleaner().clean(
				new InputStreamReader(connection.getInputStream()));
		Object[] nodes = root.evaluateXPath("//div[@id='events']//tr");
		if (nodes == null) {
			throw new IllegalStateException();
		}

		String category = null;
		for (Object object : nodes) {
			List<Object> children = ((TagNode) object).getChildren();
			if (children.size() != 4 || (children.size() > 0
					&& children.get(0).toString().equals("th"))) {
				continue;
			}
			String tempCategory = extractNodeTextIfPossible(children.get(0));
			if (!tempCategory.equals("&nbsp;")) {
				category = tempCategory;
				if (!events.containsKey(category)) {
					events.put(category, new ArrayList<Event>());
				}
			}
			events.get(category).add(new Event(
				extractNodeTextIfPossible(children.get(1)),
				extractNodeTextIfPossible(children.get(3)),
				extractNodeTextIfPossible(children.get(2))));
		}
		return events;
	}

	/**
	 * Given the HTML node filtered by XPath attempt to extract
	 * the node text if possible.
	 * 
	 * @param node HTML node returned by filtering using XPath
	 * @return node text otherwise {@link IllegalStateException} is thrown
	 */
	protected String extractNodeTextIfPossible(Object node) {
		if (node instanceof TagNode) {
			return ((TagNode) node).getText().toString();
		}
		if (node instanceof ContentNode) {
			return ((ContentNode) node).getContent().toString();
		} else {
			throw new IllegalStateException("Unexpected node type");
		}
	}
}
