package esa.s1pdgs.cpoc.ingestor;

import java.io.File;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;

public class FileUtils {

    /**
     * Path of the test directory
     */
    public static final String TEST_DIR_PATH = "build";

    /**
     * Test directory
     */
    public static final File TEST_DIR = new File(TEST_DIR_PATH);

    /**
     * Absolute path of the test directory with the last separator
     */
    public static final String TEST_DIR_ABS_PATH_SEP =
            TEST_DIR.getAbsolutePath() + File.separator;

    /**
     * Relative paths of auxiliary file
     */
    public static final String RPATH_AUX_INS =
            "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE";
    public static final String RPATH_AUX_INS_MANIFEST =
            "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/manifest.safe";
    public static final String RPATH_AUX_INS_SUPPORT_XSD =
            "S1A_AUX_INS_V20171017T080000_G20171013T101216.SAFE/support/s1-aux-ins.xsd";
    public static final String RPATH_AUX_PP1 =
            "S1B_AUX_PP1_V20160422T000000_G20160922T094703.SAFE";
    public static final String RPATH_AUX_PP1_MANIFEST =
            "S1B_AUX_PP1_V20160422T000000_G20160922T094703.SAFE/manifest.safe";
    public static final String RPATH_AUX_PP1_SUPPORT =
            "S1B_AUX_PP1_V20160422T000000_G20160922T094703.SAFE/support";
    public static final String RPATH_AUX_PP1_SUPPORT_XSD =
            "S1B_AUX_PP1_V20160422T000000_G20160922T094703.SAFE/support/s1-aux-pp1.xsd";
    public static final String RPATH_AUX_CAL =
            "S1B_AUX_CAL_V20160422T000000_G20170116T134142.SAFE";
    public static final String RPATH_AUX_CAL_MANIFEST =
            "S1B_AUX_CAL_V20160422T000000_G20170116T134142.SAFE/manifest.safe";
    public static final String RPATH_MPL_ORBPRE =
            "S1A_OPER_MPL_ORBPRE_20171215T200330_20171222T200330_0001.EOF";
    public static final String RPATH_MPL_ORBSCT =
            "S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF";
    public static final String RPATH_AUX_OBMEMC =
            "S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml";
    public static final String RPATH_AUX_RESORB =
            "S1A_OPER_AUX_RESORB_OPOD_20171214T042134_V20171213T233734_20171214T025504.EOF";
    public static final String RPATH_AUX_NO_MATCH =
            "S1A_OPER_AUX_RESORB_OPOD_20171214T042134_V20171213T233734_20171214T025504.EO";

    /**
     * Relative paths of EDRS sessions
     */
    public static final String RPATH_SESSION_RAW =
            "S1A/707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw";
    public static final String RPATH_SESSION_XML =
            "S1B/707000180/ch02/DCS_02_L20171109175634707000180_ch2_DSIB.xml";
    public static final String RPATH_SESSION_XML_IIF =
            "S1B/707000180/ch02/DCS_02_L20171109175634707000180_ch2_DSIBiif_.xml";
    public static final String RPATH_SESSION_NO_MATCH =
            "S1B/707000180/ch01/DCS_02_L20171109175634707000182_ch1_DSIB.xml";

    /**
     * Get the file descriptor according the relative path
     * 
     * @param str
     * @return
     */
    public static FileDescriptor getFileDescriptorForAuxiliary(String str) {
        FileDescriptor desc = new FileDescriptor();
        desc.setRelativePath(str);
        desc.setKeyObjectStorage(str);
        if (RPATH_AUX_INS_MANIFEST.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_AUX_INS, true, "S1", "A");
        } else if (RPATH_AUX_INS_SUPPORT_XSD.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_AUX_INS, false, "S1", "A");
        } else if (RPATH_AUX_PP1_MANIFEST.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_AUX_PP1, true, "S1", "B");
        } else if (RPATH_AUX_PP1_SUPPORT_XSD.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_AUX_PP1, false, "S1", "B");
        } else if (RPATH_AUX_CAL_MANIFEST.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_AUX_CAL, true, "S1", "B");
        } else if (RPATH_MPL_ORBPRE.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_MPL_ORBPRE, true, "S1",
                    "A");
        } else if (RPATH_MPL_ORBSCT.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_MPL_ORBSCT, true, "S1",
                    "A");
        } else if (RPATH_AUX_OBMEMC.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_AUX_OBMEMC, true, "S1",
                    "B");
        } else if (RPATH_AUX_RESORB.equals(str)) {
            setFileDescriptorFromParams(desc, RPATH_AUX_RESORB, true, "S1",
                    "A");
        } else {
            throw new IllegalArgumentException(
                    "The relative path shall be a known file");
        }
        return desc;
    }

    /**
     * Get the file descriptor according the relative path
     * 
     * @param str
     * @return
     */
    public static FileDescriptor getFileDescriptorForEdrsSession(String str) {
        FileDescriptor desc = new FileDescriptor();
        desc.setRelativePath(str);
        desc.setKeyObjectStorage(str);
        if (RPATH_SESSION_RAW.equals(str)) {
            setFileDescriptorFromParams(desc,
                    "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw", true,
                    "S1", "A", FileExtension.RAW, EdrsSessionFileType.RAW, 1);
        } else if (RPATH_SESSION_XML.equals(str)) {
            setFileDescriptorFromParams(desc,
                    "DCS_02_L20171109175634707000180_ch2_DSIB.xml", true, "S1",
                    "B", FileExtension.XML, EdrsSessionFileType.SESSION, 2);
        } else {
            throw new IllegalArgumentException(
                    "The relative path shall be a known file");
        }
        return desc;
    }

    /**
     * Set attributes values of a FileDescriptor
     * 
     * @param desc
     * @param productName
     * @param hasToBePub
     * @param missionId
     * @param satelliteId
     */
    private static void setFileDescriptorFromParams(final FileDescriptor desc,
            final String productName, final boolean hasToBePub,
            final String missionId, final String satelliteId) {
        desc.setProductName(productName);
        desc.setHasToBePublished(hasToBePub);
        desc.setMissionId(missionId);
        desc.setSatelliteId(satelliteId);
    }

    /**
     * Set attributes values of a FileDescriptor
     * 
     * @param desc
     * @param productName
     * @param hasToBePub
     * @param missionId
     * @param satelliteId
     */
    private static void setFileDescriptorFromParams(final FileDescriptor desc,
            final String productName, final boolean hasToBePub,
            final String missionId, final String satelliteId,
            final FileExtension extension,
            final EdrsSessionFileType productType, final int channelId) {
        setFileDescriptorFromParams(desc, productName, hasToBePub, missionId,
                satelliteId);
        desc.setExtension(extension);
        desc.setProductType(productType);
        desc.setChannel(channelId);
    }
}
