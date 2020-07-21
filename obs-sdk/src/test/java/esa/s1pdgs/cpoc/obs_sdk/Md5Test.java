package esa.s1pdgs.cpoc.obs_sdk;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

public class Md5Test {

    @Test
    public void md5KeyFor() {
        String md5file1 = Md5.md5KeyFor("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml");
        assertEquals("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml.md5sum", md5file1);

        String md5file2 = Md5.md5KeyFor("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00035.raw");
        assertEquals("L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00035.raw.md5sum", md5file2);

        String md5file3 = Md5.md5KeyFor("S1__AUX_WND_V20181002T210000_G20180929T181057.SAFE/");
        assertEquals("S1__AUX_WND_V20181002T210000_G20180929T181057.SAFE.md5sum", md5file3);

        String md5file4 = Md5.md5KeyFor("S1B_OPER_MPL_ORBPRE_20190711T200257_20190718T200257_0001.EOF");
        assertEquals("S1B_OPER_MPL_ORBPRE_20190711T200257_20190718T200257_0001.EOF.md5sum", md5file4);

        String md5file5 = Md5.md5KeyFor("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE/manifest.safe");
        assertEquals("S1__AUX_WND_V20181002T120000_G20180929T061310.SAFE.md5sum", md5file5);
    }

    @Test
    public void md5ByStream() throws NoSuchAlgorithmException, IOException {

        try (
                final DigestInputStream md5 = new DigestInputStream(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), MessageDigest.getInstance("MD5"));
                final BufferedReader reader = new BufferedReader(new InputStreamReader(md5))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            final MessageDigest messageDigest = md5.getMessageDigest();
            final byte[] hash = messageDigest.digest();

            System.out.println(new BigInteger(1, hash).toString(16));
        }

        System.out.println(DigestUtils.md5Hex("abc"));
    }

    @Test
    public void testParseNewFormat() {
        final Md5.Entry entry = Md5.parse("md5Hash s3ETag fileName45");

        assertThat(entry, is(notNullValue()));
        assertThat(entry.getMd5Hash(), is(equalTo("md5Hash")));
        assertThat(entry.getETag(), is(equalTo("s3ETag")));
        assertThat(entry.getFileName(), is(equalTo("fileName45")));
    }

    @Test
    public void testParseNewFormatWithMultipleDifferentWhiteSpaces() {
        final Md5.Entry entry = Md5.parse("md5Hash  s3ETag \t fileName45");

        assertThat(entry, is(notNullValue()));
        assertThat(entry.getMd5Hash(), is(equalTo("md5Hash")));
        assertThat(entry.getETag(), is(equalTo("s3ETag")));
        assertThat(entry.getFileName(), is(equalTo("fileName45")));
    }

    @Test
    public void testParseOldFormat() {
        final Md5.Entry entry = Md5.parse("98f6bcd4621d373cade4e832627b4f6  testdir/testfile1.txt");

        assertThat(entry, is(notNullValue()));
        assertThat(entry.getMd5Hash(), is(equalTo("98f6bcd4621d373cade4e832627b4f6")));
        assertThat(entry.getETag(), is(equalTo("98f6bcd4621d373cade4e832627b4f6")));
        assertThat(entry.getFileName(), is(equalTo("testdir/testfile1.txt")));
    }

    @Test
    public void testToStringAndParse() {
        final Md5.Entry entry = new Md5.Entry("md5Sum", "eTag45", "fileName48");

        assertThat(entry.toString(), is(equalTo("md5Sum eTag45 fileName48")));
        assertThat(Md5.parse(entry.toString()), is(equalTo(entry)));
    }

    @Test
    public void testSkipInvalidFormat() {
        assertThat(Md5.parseIfPossible("invalid").isPresent(), is(not(true)));
    }

}