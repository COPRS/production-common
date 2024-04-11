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

package esa.s1pdgs.cpoc.common.errors.os;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.os.OsEntityException;
import esa.s1pdgs.cpoc.common.errors.os.OsEntityInternaloErrorException;
import esa.s1pdgs.cpoc.common.errors.os.OsFloatingIpNotActiveException;
import esa.s1pdgs.cpoc.common.errors.os.OsServerNotActiveException;
import esa.s1pdgs.cpoc.common.errors.os.OsServerNotDeletedException;
import esa.s1pdgs.cpoc.common.errors.os.OsVolumeNotAvailableException;

/**
 * @author Viveris Technologies
 */
public class OsEntityExceptionTest {

    /**
     * Test the exception OsEntityException
     */
    @Test
    public void testConstructor() {
        OsEntityException exc = new OsEntityException("type", "id",
                ErrorCode.INTERNAL_ERROR, "error message");
        assertEquals("type", exc.getType());
        assertEquals("id", exc.getIdentifier());
        assertEquals("error message", exc.getMessage());
        assertEquals(ErrorCode.INTERNAL_ERROR, exc.getCode());
        assertNull(exc.getCause());

        String str = exc.getLogMessage();
        assertTrue(str.contains("[type id]"));
        assertTrue(str.contains("[msg error message]"));

        OsEntityException exc2 = new OsEntityException("type2", "id2",
                ErrorCode.INVALID_PRODUCT_FORMAT, "error message",
                new IllegalArgumentException("cause message"));
        assertEquals("type2", exc2.getType());
        assertEquals("id2", exc2.getIdentifier());
        assertEquals("error message", exc2.getMessage());
        assertEquals(ErrorCode.INVALID_PRODUCT_FORMAT, exc2.getCode());
        assertNotNull(exc2.getCause());

        String str2 = exc2.getLogMessage();
        assertTrue(str2.contains("[type2 id2]"));
        assertTrue(str2.contains("[msg error message]"));
    }

    /**
     * Test the exception OsEntityInternaloErrorException
     */
    @Test
    public void testOsEntityInternaloErrorException() {

        OsEntityInternaloErrorException exc2 =
                new OsEntityInternaloErrorException("type2", "id2",
                        "error message",
                        new IllegalArgumentException("cause message"));
        assertEquals("type2", exc2.getType());
        assertEquals("id2", exc2.getIdentifier());
        assertEquals("error message", exc2.getMessage());
        assertEquals(ErrorCode.INTERNAL_ERROR, exc2.getCode());
        assertNotNull(exc2.getCause());

    }

    /**
     * Test the exception OsFloatingIpNotActiveException
     */
    @Test
    public void testOsFloatingIpNotActiveException() {
        OsFloatingIpNotActiveException exc =
                new OsFloatingIpNotActiveException("id", "error message");
        assertEquals("serverId", exc.getType());
        assertEquals("id", exc.getIdentifier());
        assertEquals("error message", exc.getMessage());
        assertEquals(ErrorCode.OS_FLOATING_IP_NOT_ACTIVE, exc.getCode());
        assertNull(exc.getCause());
    }

    /**
     * Test the exception OsVolumeNotAvailableException
     */
    @Test
    public void testOsVolumeNotAvailableException() {
        OsVolumeNotAvailableException exc =
                new OsVolumeNotAvailableException("id", "error message");
        assertEquals("volumeName", exc.getType());
        assertEquals("id", exc.getIdentifier());
        assertEquals("error message", exc.getMessage());
        assertEquals(ErrorCode.OS_VOLUME_NOT_CREATED, exc.getCode());
        assertNull(exc.getCause());
    }

    /**
     * Test the exception OsServerNotDeletedException
     */
    @Test
    public void testOsServerNotDeletedException() {
        OsServerNotDeletedException exc =
                new OsServerNotDeletedException("id", "error message");
        assertEquals("serverId", exc.getType());
        assertEquals("id", exc.getIdentifier());
        assertEquals("error message", exc.getMessage());
        assertEquals(ErrorCode.OS_SERVER_NOT_DELETED, exc.getCode());
        assertNull(exc.getCause());
    }

    /**
     * Test the exception OsServerNotActiveException
     */
    @Test
    public void testOsServerNotActiveException() {
        OsServerNotActiveException exc =
                new OsServerNotActiveException("id", "error message");
        assertEquals("serverId", exc.getType());
        assertEquals("id", exc.getIdentifier());
        assertEquals("error message", exc.getMessage());
        assertEquals(ErrorCode.OS_SERVER_NOT_ACTIVE, exc.getCode());
        assertNull(exc.getCause());
    }
}
