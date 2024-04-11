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

package esa.s1pdgs.cpoc.ipf.execution.worker.job.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;

import org.junit.After;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.execution.worker.test.SystemUtils;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class PoolProcessorTest {
    private final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("TestProcessing");
	  
    private File testDir = new File("./3");

    @After
    public void clean() {
        if (testDir.exists()) {
            testDir.delete();
        }
    }

    @Test
    public void testExecutionOk() throws AbstractCodedException {
        final LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdMkdir()));
        final PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60, false, Collections.emptyList());
        assertFalse(testDir.exists());
        processor.process(reporting);
        assertTrue(testDir.exists() && testDir.isDirectory());
    }

    @Test
    public void testExecutionOkSeveralTasks() throws AbstractCodedException {
        final LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdMkdir()));
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdLs()));
        final PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60, false, Collections.emptyList());
        assertFalse(testDir.exists());
        processor.process(reporting);
        assertTrue(testDir.exists() && testDir.isDirectory());
    }

    /**
     * Test when task fails with exit code between 1 and 127
     * @throws AbstractCodedException
     */
    @Test
    public void testExecutionK0SeveralTasks() throws AbstractCodedException {
        final LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdFalse()));
        final PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60, false, Collections.emptyList());
        processor.process(reporting);
    }
}
