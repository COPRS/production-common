package esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;

@Component
public class TaskTableFactory {
	private final XmlConverter xmlConverter;

	@Autowired
	public TaskTableFactory(final XmlConverter xmlConverter) {
		this.xmlConverter = xmlConverter;
	}

	public final TaskTable buildTaskTable(final File xmlFile, final ApplicationLevel level,
			final String pathTaskTableXslt) {
		// Retrieve task table
		try {
			final TaskTable taskTable;

			if (pathTaskTableXslt == null || pathTaskTableXslt.isEmpty()) {
				taskTable = (TaskTable) xmlConverter.convertFromXMLToObject(xmlFile.getAbsolutePath());
			} else {
				final TransformerFactory transFactory = TransformerFactory.newInstance();
				final Transformer transformer = transFactory.newTransformer(new StreamSource(pathTaskTableXslt));
				final ByteArrayOutputStream transformationStream = new ByteArrayOutputStream();

				transformer.transform(new StreamSource(xmlFile), new StreamResult(transformationStream));
				String taskTableString = transformationStream.toString(Charset.defaultCharset().name());
				InputStream taskTableStream = new ByteArrayInputStream(taskTableString.getBytes(StandardCharsets.UTF_8));
				
				taskTable = (TaskTable) xmlConverter.convertFromStreamToObject(taskTableStream);
			}
			taskTable.setLevel(level);
			return taskTable;

		} catch (IOException | JAXBException e) {
			throw new RuntimeException(
					String.format("Error reading taskTable %s: %s", xmlFile.getPath(), e.getMessage()), e);
		} catch (TransformerException e) {
			throw new RuntimeException(String.format("Error converting taskTable %s with xsltFile %s: %s",
					xmlFile.getPath(), pathTaskTableXslt, e.getMessage()), e);
		}
	}
}
