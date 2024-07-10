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

package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public class Md5SumCalculationHelper {
    private final DigestInputStream digestIn;

    public static Md5SumCalculationHelper createFor(final InputStream in) {
        return new Md5SumCalculationHelper(in);
    }

    private Md5SumCalculationHelper(final InputStream in) {
        try {
            this.digestIn = new DigestInputStream(in, MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("could not create message digest for algorithm MD5");
        }
    }

    public InputStream getInputStream() {
        return digestIn;
    }

    public String getMd5Sum() {
        {
            final byte[] hash = digestIn.getMessageDigest().digest();
            return Hex.encodeHexString(hash, true);
        }
    }
}
