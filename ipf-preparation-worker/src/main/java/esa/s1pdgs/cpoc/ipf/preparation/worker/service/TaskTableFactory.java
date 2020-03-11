package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerBuildTaskTableException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;

@Component
public class TaskTableFactory {	
	private final XmlConverter xmlConverter;
	
	@Autowired
	public TaskTableFactory(final XmlConverter xmlConverter) {
		this.xmlConverter = xmlConverter;
	}

	public final TaskTable buildTaskTable(final File xmlFile, final ApplicationLevel level) throws IpfPrepWorkerBuildTaskTableException {
		// Retrieve task table
		try {
			final TaskTable taskTable = (TaskTable) xmlConverter.convertFromXMLToObject(xmlFile.getAbsolutePath());
			taskTable.setLevel(level);
			return taskTable;
			
		} catch (IOException | JAXBException e) {
			throw new IpfPrepWorkerBuildTaskTableException(xmlFile.getName(), e.getMessage(), e);
		}
	}
}
