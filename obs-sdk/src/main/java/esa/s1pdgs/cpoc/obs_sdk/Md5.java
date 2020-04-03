package esa.s1pdgs.cpoc.obs_sdk;

public class Md5 {

    public static final String MD5SUM_SUFFIX = ".md5sum";


    public static String md5KeyFor(ObsObject object) {
        return md5KeyFor(object.getKey());
    }

    public static String md5KeyFor(String key) {
        return identifyMd5File(key);
    }


    private static String identifyMd5File(final String prefixKey) {
        int index = prefixKey.length();
        if (!(prefixKey.contains("raw") || prefixKey.contains("DSIB"))) {
            index = prefixKey.indexOf('/');
            if (index == -1) {
                index = prefixKey.length();
            }
        }
        return (prefixKey.substring(0, index) + MD5SUM_SUFFIX);
    }

}
