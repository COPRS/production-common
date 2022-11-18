package esa.s1pdgs.cpoc.preparation.worker.type.s3;

import esa.s1pdgs.cpoc.common.ProductFamily;

public enum S3Timeliness {

	NRT, STC, NTC, NONE;

	public static S3Timeliness fromFamily(ProductFamily family) {

		switch (family) {

		case S3_L1_NRT:
		case S3_L2_NRT:
			return NRT;
		case S3_L1_STC:
		case S3_L2_STC:
			return STC;
		case S3_L1_NTC:
		case S3_L2_NTC:
			return NTC;
		default:
			return NONE;
		}

	}

}
