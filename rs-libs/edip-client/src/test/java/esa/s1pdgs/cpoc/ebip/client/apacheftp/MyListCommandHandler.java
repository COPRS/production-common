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

package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.fake.command.ListCommandHandler;

public class MyListCommandHandler extends ListCommandHandler {
	
	private final int sleepSec;
	
	public MyListCommandHandler(final int sleepSec) {
		super();
		this.sleepSec = sleepSec;
		
	}

	@Override
	protected void handle(Command command, Session session) {
		try {
			Thread.sleep(sleepSec * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		super.handle(command, session);
	}

}
