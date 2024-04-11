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

package esa.s1pdgs.cpoc.xml.model.tasktable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableCfgFile;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class TaskTableCfgFileTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableCfgFile() {
        EqualsVerifier.forClass(TaskTableCfgFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
    
    /**
     * Check constructors and setters
     */
    @Test
    public void testBasic() {
        TaskTableCfgFile obj = new TaskTableCfgFile("file", "v1");
        assertEquals("file", obj.getFileName());
        assertEquals("v1", obj.getVersion());
        
        obj = new TaskTableCfgFile();
        obj.setFileName("file2");
        obj.setVersion("v2");
        assertEquals("file2", obj.getFileName());
        assertEquals("v2", obj.getVersion());
    }

}
