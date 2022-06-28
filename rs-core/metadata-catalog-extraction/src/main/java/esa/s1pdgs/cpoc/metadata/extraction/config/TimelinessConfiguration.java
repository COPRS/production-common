package esa.s1pdgs.cpoc.metadata.extraction.config;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "timeliness")
public class TimelinessConfiguration extends HashMap<String, Integer> {
	
	public static final String NRT = "NRT";
	public static final String FAST24 = "FAST24";
	public static final String PT = "PT";
	
	public static final String S1_SESSION = "S1_SESSION";
	public static final String S1_NRT = "S1_NRT";
	public static final String S1_FAST24 = "S1_FAST24";
	public static final String S1_PT = "S1_PT";
	
	public static final String S2_SESSION = "S2_SESSION";
	public static final String S2_L0 = "S2_L0";
	public static final String S2_L1 = "S1_L1";
	public static final String S2_L2 = "S2_L2";
	
	public static final String S3_NRT = "S3_NRT";
	public static final String S3_NTC = "S3_NTC";
	public static final String S3_STC = "S3_STC";
	
	public static final Pattern FILE_PATTERN_S1_GP_HK = Pattern.compile("^S1[AB]_(GP|HK)_RAW__.*$");

}
