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

package esa.s1pdgs.cpoc.obs_sdk;

public class ObsValidationException extends Exception {
	   private static final long serialVersionUID = 5121673912582265431L;

	   public ObsValidationException(final String message, final Throwable cause, final Object... arguments) {
	      super(format(message, arguments), cause);
	   }

	   public ObsValidationException(final String message, final Object... arguments) {
	      this(message, null, arguments);
	   }

	   @Override
	   public String getMessage() {
	      return getDeepMessage(super.getMessage(), this.getCause());
	   }

	   static String getDeepMessage(final String msg, final Throwable cause) {
	      if(null != cause) {
	         return msg + ": " + getDeepMessage(cause.getMessage(), cause.getCause());
	      } else {
	         return msg;
	      }
	   }
	   
	   private static String format(final String message, final Object... arguments) {
	      return String.format(message.replaceAll("\\{\\}", "%s"), arguments);
	   }
}
