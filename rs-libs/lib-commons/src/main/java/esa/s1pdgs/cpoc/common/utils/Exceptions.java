/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

public class Exceptions {	
	public static Throwable unwrap(final Exception e) {
		Throwable res = e;
		
		while (res instanceof ExecutionException) {
			res = res.getCause();
		}
		return res;
	}
	
    public static final String toString(final Throwable throwable) {
        final StringWriter writer     = new StringWriter();        
        try (final PrintWriter printWriter = new PrintWriter(writer)){
        	throwable.printStackTrace(printWriter);
        }
        return writer.toString();
    }
    
	public static final String messageOf(final Throwable e) {
		if (e instanceof AbstractCodedException) {
			return ((AbstractCodedException) e).getLogMessage();
		}
		if (e.getMessage() == null) {
			return "(no errormessage provided)";
		}
		return e.getMessage();
	}
}
