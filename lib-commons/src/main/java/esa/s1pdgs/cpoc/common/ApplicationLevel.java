package esa.s1pdgs.cpoc.common;

/**
 * Application levels
 * @author Viveris Technologies
 *
 */
public enum ApplicationLevel {
	L0(ProductFamily.L0_JOB), 
	L0_SEGMENT(ProductFamily.L0_SEGMENT_JOB),
	L1(ProductFamily.L1_JOB), 
	L2(ProductFamily.L2_JOB),
	SPP_OBS(ProductFamily.SPP_OBS_JOB),
	S3_L0(ProductFamily.S3_JOB),
	S3_L1(ProductFamily.S3_JOB),
	S3_L2(ProductFamily.S3_JOB);
	
	private final ProductFamily jobFamily;
	
	private ApplicationLevel(final ProductFamily jobFamily) {
		this.jobFamily = jobFamily;
	}

	public ProductFamily toFamily() {
		return jobFamily;
	}
}
