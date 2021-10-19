package esa.s1pdgs.cpoc.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DummyClassTodoFixMe")
@XmlAccessorType(XmlAccessType.NONE)
public class DummyClassTodoFixMe {
	// Workaround that temporary solves the problem that the two enums
	// 1. esa.s1pdgs.cpoc.jobgenerator.model.joborder.enums.JobOrderFileNameType
	// 2. esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableFileNameType
	// have the same annotation @XmlType(name = "File_Name_Type")
}
