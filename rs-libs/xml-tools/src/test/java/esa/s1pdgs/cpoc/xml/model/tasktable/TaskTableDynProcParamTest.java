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

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableDynProcParam;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class TaskTableDynProcParamTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableDynProcParam() {
        EqualsVerifier.forClass(TaskTableDynProcParam.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
    
    /**
     * Check constructors and setters
     */
    @Test
    public void testBasic() {
        TaskTableDynProcParam obj = new TaskTableDynProcParam("file", "type", "dft");
        assertEquals("file", obj.getName());
        assertEquals("type", obj.getType());
        assertEquals("dft", obj.getDefaultValue());
        
        obj = new TaskTableDynProcParam();
        obj.setName("file2");
        obj.setType("v2");
        obj.setDefaultValue("tutu");
        assertEquals("file2", obj.getName());
        assertEquals("v2", obj.getType());
        assertEquals("tutu", obj.getDefaultValue());
    }

}
