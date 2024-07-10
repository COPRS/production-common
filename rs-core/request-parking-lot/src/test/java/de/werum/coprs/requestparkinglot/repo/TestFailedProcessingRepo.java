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

package de.werum.coprs.requestparkinglot.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import de.werum.coprs.requestparkinglot.config.TestConfig;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import(TestConfig.class)
@TestPropertySource(locations="classpath:default-mongodb-port.properties")
public class TestFailedProcessingRepo {	
    @Autowired
    private MongoOperations ops;

    @Autowired
    private FailedProcessingRepo uut;
    
    @Test
    public final void testFindById_OnExistingId_ShallReturnObject()
    {
    	ops.insert(newFailedProcessing("1"));    	
    	ops.insert(newFailedProcessing("2"));   
    	
    	final Optional<FailedProcessing> actual = uut.findById("1");
    	assertEquals("1", actual.get().getId());
    }
    
    @Test
    public final void testDeleteById_OnExistingId_ShallDeleteObject()
    {
    	ops.insert(newFailedProcessing("3"));    	
    	ops.insert(newFailedProcessing("4"));    
    	uut.deleteById("4");
    	final Optional<FailedProcessing> actual = uut.findById("4");
    	assertTrue(actual.isEmpty());
    }
        
    
    private final FailedProcessing newFailedProcessing(final String id)
    {
    	final FailedProcessing proc = new FailedProcessing();
    	proc.setId(id);
    	return proc;
    }    
}
