package esa.s1pdgs.cpoc.dissemination.worker.path;

import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class MbuPathEvaluatorTest {
	
	@Test
	public void outputPath() {
		
		String key =  "s1b-wv1-mbu-vv-20190628t190957-20190628t191000-016900-01fce3-001_C26C.bufr";
		
		ObsObject obsObject = new ObsObject(ProductFamily.SPP_MBU, key);
		
		MbuPathEvaluator uut = new MbuPathEvaluator();
		
		Path outputPath = uut.outputPath("/data/public/METEO/", obsObject);
		
		Assert.assertEquals("/data/public/METEO/190628", outputPath.toString());
		
	}
	
	@Test
	public void outputFilename() {
		
		String key =  "s1b-wv1-mbu-vv-20190628t190957-20190628t191000-016900-01fce3-001_C26C.bufr";
		
		ObsObject obsObject = new ObsObject(ProductFamily.SPP_MBU, key);
		
		MbuPathEvaluator uut = new MbuPathEvaluator();
		
		String outputFilename = uut.outputFilename(null, obsObject);
		
		Assert.assertEquals(key, outputFilename);
		
	}

}
