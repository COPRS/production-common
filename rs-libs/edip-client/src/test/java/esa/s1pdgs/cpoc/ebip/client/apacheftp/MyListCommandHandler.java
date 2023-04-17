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
