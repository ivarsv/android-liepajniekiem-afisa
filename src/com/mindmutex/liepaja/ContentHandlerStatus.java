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

import java.util.List;
import java.util.Map;

/**
 * Handler definition used by client to get notifications when
 * request successfully completes or fails.
 */
public interface ContentHandlerStatus {
    
    /**
     * Method called when the request is successful.
     *
     * @param events list of events extracted from HTML where the key is category
     *               and value a list of events attached to the category.
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