package esa.s1pdgs.cpoc.obs_sdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}