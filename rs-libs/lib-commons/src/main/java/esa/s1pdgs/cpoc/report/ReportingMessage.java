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

package esa.s1pdgs.cpoc.report;

public class ReportingMessage {
	private final String message;
	private final Object[] args;
	private final long transferAmount;
	
	public ReportingMessage(final long transferAmount, final String message, final Object... args) {
		this.message = message;
		this.args = args;
		this.transferAmount = transferAmount;
	}
	
	public ReportingMessage(final String message, final Object... args) {
		this(0L,message,args);
	}

	public String getMessage() {
		return message;
	}

	public Object[] getArgs() {
		return args;
	}

	public long getTransferAmount() {
		return transferAmount;
	}
}
