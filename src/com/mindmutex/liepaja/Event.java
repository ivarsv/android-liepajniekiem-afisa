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

/**
 * POJO to store information about the event gathered from
 * liepajniekiem.lv web site.
 * <p>
 * Attempt to increase performance by not specifying getters and setters.
 */
public class Event {
	/**
	 * Name of the event such as name of the movie, name of
	 * the exhibition or similar.
	 */
	public String location;

	/**
	 * Additional description that is provided with the {@link #location}.
	 */
	public String description;

	/**
	 * Time stamp as is on web site. Current format is time
	 * short 24hr format.
	 */
	public String timestamp;

	public Event(String name, String description, String timestamp) {
		this.location = name;
		this.description = description;
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return String.format("%s %s @ %s", location, description, timestamp);
	}
}