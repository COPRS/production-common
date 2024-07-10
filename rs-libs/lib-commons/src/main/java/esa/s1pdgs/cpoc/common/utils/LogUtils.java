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

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtils {
	// S1PRO-1561: this is handle to a Logger instance configured in s1pro-configuration to log without any formatting to allow passing
	// through messages that are already formatted (in e.g. JSON)
	public static final Logger PLAINTEXT = LogManager.getLogger(LogUtils.class);
	

    public static void traceLog(final Log logger, final String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);
        }
    }
    
    public static final String toString(final Throwable throwable) {
    	return Exceptions.toString(throwable);
    }
}
