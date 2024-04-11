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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Reporting extends ReportingFactory {	
	public interface Builder extends ReportingFactory {		
		Builder predecessor(UUID predecessor);
		Builder root(UUID root);
		Builder parent(UUID parent);		
		default Builder addTag(final String tag) {
			return addTags(Collections.singleton(tag));
		}
		Builder addTags(Collection<String> tags);
		Builder rsChainName(String rsChainName);
		Builder rsChainVersion(String rsChainVersion);
	}
	
	enum Event {
		BEGIN,
		END
	}
	
	enum Status {
		OK(0),
		NOK(1);
		
		private final int errCode;

		private Status(final int errCode) {
			this.errCode = errCode;
		}
		
		public final int errCode() {
			return errCode;
		}
	}
	
	public static final Logger REPORT_LOG = LogManager.getLogger(Reporting.class);
	
	public static final Reporting NULL = new Reporting() {	
		@Override
		public UUID getUid() {
			return null;
		}
		@Override
		public void begin(final ReportingInput input, final ReportingMessage reportingMessage) {}

		@Override
		public void end(final ReportingOutput output, final ReportingMessage reportingMessage) {}
		
		@Override
		public void end(ReportingOutput output, ReportingMessage reportingMessage, Map<String, String> quality) {}

		@Override
		public void end(final ReportingOutput output, final ReportingMessage reportingMessage, final List<MissingOutput> missingOutputs) {}
		
		@Override
		public void warning(final ReportingOutput output, final ReportingMessage reportingMessage) {}

		@Override
		public void error(final ReportingMessage reportingMessage) {}
		
		@Override
		public void error(final ReportingMessage reportingMessage, final List<MissingOutput> missingOutputs) {}

		@Override
		public Reporting newReporting(final String taskName) {
			return NULL;
		}

	};
	
	UUID getUid();
	
	default void begin(final ReportingMessage reportingMessage) {
		begin(ReportingInput.NULL, reportingMessage);
	}
	
	default void end(final ReportingMessage reportingMessage) {
		end(ReportingOutput.NULL, reportingMessage);
	}	
	
	void begin(ReportingInput input, ReportingMessage reportingMessage);
	void end(ReportingOutput output, ReportingMessage reportingMessage);
	void end(ReportingOutput output, ReportingMessage reportingMessage, Map<String, String> quality);
	void end(ReportingOutput output, ReportingMessage reportingMessage, List<MissingOutput> missingOutputs);
	void warning(ReportingOutput output, ReportingMessage reportingMessage);
	void error(ReportingMessage reportingMessage);
	void error(ReportingMessage reportingMessage, List<MissingOutput> missingOutputs);
}