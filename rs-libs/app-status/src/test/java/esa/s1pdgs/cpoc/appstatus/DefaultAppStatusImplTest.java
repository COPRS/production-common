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

package esa.s1pdgs.cpoc.appstatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

public class DefaultAppStatusImplTest {
    
    /**
     * Application status for test
     */
    private DefaultAppStatusImpl appStatus;
    
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
              
        appStatus = new DefaultAppStatusImpl(3,3, () -> {});
    }
    
    /**
     * Check constructor
     */
    @Test
    public void checkConstructor() {
        assertEquals(false, appStatus.isShallBeStopped());
        assertEquals(AppState.WAITING, appStatus.getStatus().getState());
        assertTrue(appStatus.getStatus().isWaiting());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        assertTrue(System.currentTimeMillis() >= appStatus.getStatus().getDateLastChangeMs());
        assertTrue(appStatus.getStatus().getDateLastChangeMs() > 0);
        
        appStatus.setShallBeStopped(true);
        assertEquals(true, appStatus.isShallBeStopped());
    }

    /**
     * Test set waiting
     */
    @Test
    public void testWaiting() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertTrue(appStatus.getStatus().isWaiting());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertTrue(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setError("NEXT_MESSAGE");
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertTrue(appStatus.getStatus().isWaiting());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.getStatus().setFatalError();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertFalse(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertFalse(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }

    /**
     * Test set stopping
     */
    @Test
    public void testStopping() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.setWaiting();
        assertFalse(appStatus.isShallBeStopped());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        
        appStatus = new DefaultAppStatusImpl(3,3, () -> {});
        appStatus.setWaiting();
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
    }

    /**
     * Test set error
     */
    @Test
    public void testError() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.getStatus().setWaiting();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("NEXT_MESSAGE");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(1, appStatus.getStatus().getErrorCounterNextMessage());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("NEXT_MESSAGE");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(2, appStatus.getStatus().getErrorCounterNextMessage());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("NEXT_MESSAGE");
        assertFalse(appStatus.getStatus().isError());
        assertTrue(appStatus.getStatus().isFatalError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(3, appStatus.getStatus().getErrorCounterNextMessage());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("NEXT_MESSAGE");
        assertFalse(appStatus.getStatus().isError());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }
}
