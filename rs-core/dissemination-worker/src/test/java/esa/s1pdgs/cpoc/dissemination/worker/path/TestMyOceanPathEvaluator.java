package esa.s1pdgs.cpoc.dissemination.worker.path;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class TestMyOceanPathEvaluator {

	private static final MyOceanPathEvaluator MYO_PATH_EVALUATOR;

	private static final ObsObject L1_SLICE = new ObsObject(ProductFamily.L1_SLICE,	"S1A_S6_GRDH_1SSV_20200120T184828_20200120T184847_030888_038B81_7FE9.SAFE");
	private static final ObsObject L1_SLICE_ZIP = new ObsObject(ProductFamily.L1_SLICE_ZIP,	"S1A_S6_GRDH_1SSV_20200120T184828_20200120T184847_030888_038B81_7FE9.SAFE.zip");
	private static final ObsObject L1_SLICE_MANIFEST = new ObsObject(ProductFamily.L1_SLICE, "S1A_S6_GRDH_1SSV_20200120T184828_20200120T184847_030888_038B81_7FE9.SAFE/manifest.safe");

	private static final String BASE_PATH = "/data/public";
	private static final Path EXPECTED_OUTPUT_PATH = Paths.get(BASE_PATH, "2020", "01", "20");

	static {
		final PathEvaluator pathEvaluator = newPathEvaluator("myocean");
		if (pathEvaluator instanceof MyOceanPathEvaluator) {
			MYO_PATH_EVALUATOR = (MyOceanPathEvaluator) pathEvaluator;
		} else {
			throw new IllegalStateException(
					"path evaluator must be of type " + MyOceanPathEvaluator.class.getSimpleName());
		}
	}

	// --------------------------------------------------------------------------

	@Test
	public final void testOutputPathGenerationForL1Slice() {
		final Path outputPath = MYO_PATH_EVALUATOR.outputPath(BASE_PATH, L1_SLICE);

		assertTrue(EXPECTED_OUTPUT_PATH.equals(outputPath),
				"expected output path for " + L1_SLICE.getKey() + " was " + EXPECTED_OUTPUT_PATH + " but was " + outputPath);
	}

	@Test
	public final void testOutputPathGenerationForL1SliceZip() {
		final Path outputPath = MYO_PATH_EVALUATOR.outputPath(BASE_PATH, L1_SLICE_ZIP);

		assertTrue(EXPECTED_OUTPUT_PATH.equals(outputPath),
				"expected output path for " + L1_SLICE_ZIP.getKey() + " was " + EXPECTED_OUTPUT_PATH + " but was " + outputPath);
	}

	@Test
	public final void testOutputPathGenerationForL1SliceManifest() {
		final Path outputPath = MYO_PATH_EVALUATOR.outputPath(BASE_PATH, L1_SLICE_MANIFEST);

		assertTrue(EXPECTED_OUTPUT_PATH.equals(outputPath),
				"expected output path for " + L1_SLICE_MANIFEST.getKey() + " was " + EXPECTED_OUTPUT_PATH + " but was " + outputPath);
	}

	@Test
	public final void testOutputFilenameGenerationForL1SliceZip() {
		final String outputFilename = MYO_PATH_EVALUATOR.outputFilename(L1_SLICE, L1_SLICE_ZIP);
		final String expectedName = FilenameUtils.getName(L1_SLICE_ZIP.getKey());

		assertTrue(expectedName.equals(outputFilename), "expected output file name for " + L1_SLICE_ZIP.getKey()
		+ " was " + expectedName + " but was " + outputFilename);
	}

	@Test
	public final void testOutputFilenameGenerationForL1SliceManifest() {
		final String outputFilename = MYO_PATH_EVALUATOR.outputFilename(L1_SLICE, L1_SLICE_MANIFEST);
		final String expectedName = FilenameUtils.getName(L1_SLICE.getKey()) + ".manifest";

		assertTrue(expectedName.equals(outputFilename), "expected output file name for " + L1_SLICE_MANIFEST.getKey()
		+ " was " + expectedName + " but was " + outputFilename);
	}

	// --------------------------------------------------------------------------

	private static PathEvaluator newPathEvaluator(final String name) {
		final OutboxConfiguration outboxConfig = new OutboxConfiguration();
		outboxConfig.setPathEvaluator(name);

		return PathEvaluator.newInstance(outboxConfig);
	}

}
