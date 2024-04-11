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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TaskTableInputAlternativeTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableInputAlternative() {
        EqualsVerifier.forClass(TaskTableInputAlternative.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    
    @Test
    public final void testComparator() {
    	final List<TaskTableInputAlternative> elements = new ArrayList<>();
    	elements.add(new TaskTableInputAlternative(1004));
    	elements.add(new TaskTableInputAlternative(4));
    	elements.add(new TaskTableInputAlternative(1));
    	elements.add(new TaskTableInputAlternative(4));
    	elements.add(new TaskTableInputAlternative(42));
    	Collections.sort(elements, TaskTableInputAlternative.ORDER);
    	assertEquals(1, elements.get(0).getOrder());
    	assertEquals(4, elements.get(1).getOrder());
    	assertEquals(4, elements.get(2).getOrder());
    	assertEquals(42, elements.get(3).getOrder());
    	assertEquals(1004, elements.get(4).getOrder());
    }
}
