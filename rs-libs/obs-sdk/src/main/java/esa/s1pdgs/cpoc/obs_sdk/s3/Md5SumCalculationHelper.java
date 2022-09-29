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
