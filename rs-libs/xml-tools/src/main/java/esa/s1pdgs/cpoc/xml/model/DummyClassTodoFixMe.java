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
