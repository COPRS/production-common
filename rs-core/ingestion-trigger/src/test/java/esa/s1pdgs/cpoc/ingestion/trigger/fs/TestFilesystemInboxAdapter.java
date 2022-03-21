package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.BlacklistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.WhitelistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactoryImpl;

public class TestFilesystemInboxAdapter {
	private InboxEntryFactory factory;
	private final File testDir = FileUtils.createTmpDir();	
	private FilesystemInboxAdapter uut;

    @BeforeEach
    public final void init() {
		final ProcessConfiguration processConfiguration = new ProcessConfiguration();
		processConfiguration.setHostname("ingestor-01");
		factory  = new InboxEntryFactoryImpl(processConfiguration);
    	uut = new FilesystemInboxAdapter(factory, testDir.toPath().toUri(), null, null, ProductFamily.AUXILIARY_FILE);
	}

	@AfterEach
	public final void tearDown() {
		FileUtils.delete(testDir.getPath());
	}

	//@Test
	public final void testRead_OnEmptyDirectory_ShallReturnNoElements() throws IOException {
		Assertions.assertEquals(0, uut.read(InboxFilter.ALLOW_ALL).size());
	}
	
	//@Test
	public final void testRegex() {
		final Pattern patternToTest = Pattern.compile("(WILE|MTI_|SGS_|INU_)/S1(A|B)/([A-Za-z0-9]+)/ch0?(1|2)/.*\\.(xml|raw)",Pattern.CASE_INSENSITIVE);
		Assertions.assertTrue(patternToTest.matcher("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw").matches());
	}

	@Test
	public final void testRead_WithConfiguredFilters_ShallReturnProperProducts() throws IOException, URISyntaxException {
		// create some content in test directory
		final File product1 = newTestProduct("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml");
		Assertions.assertTrue(product1.createNewFile());
		final File product2 = newTestProduct("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSDB_00027.raw");
		Assertions.assertTrue(product2.createNewFile());
		final File product3 = newTestProduct("AUX/S1__AUX_ICE_V20160501T120000_G20160502T043607.SAFE/foo.txt");		
		Assertions.assertTrue(product3.createNewFile());
		final File product4 = newTestProduct("shouldBeIgnored/S1__AUX_ICE_V20160501T120000_G20160502T043607.SAFE/foo.txt");		
		Assertions.assertTrue(product4.createNewFile());
		final File product5 = newTestProduct("AUX/foo.txt");		
		Assertions.assertTrue(product5.createNewFile());
		
		final InboxFilter edrsFilter = new JoinedFilter(
				new BlacklistRegexRelativePathInboxFilter(Pattern.compile("(^\\..*|.*\\.tmp$|db.*|^lost\\+found$)")),
				new WhitelistRegexRelativePathInboxFilter(Pattern.compile("(WILE|MTI_|SGS_|INU_)/S1(A|B)/([A-Za-z0-9]+)/ch0?(1|2)/(.+DSIB\\.(xml|XML)|.+DSDB.*\\.(raw|RAW|aisp|AISP))"))
		);
		final FilesystemInboxAdapter uutEdrs = new FilesystemInboxAdapter(
				factory,
				testDir.toPath().toUri(),
				null,
				"S1",
				ProductFamily.EDRS_SESSION
		);
		final Collection<InboxEntry> actualEdrs = uutEdrs.read(edrsFilter);
		Assertions.assertEquals(2, actualEdrs.size());

		final InboxFilter auxFilter = new JoinedFilter(
				new BlacklistRegexRelativePathInboxFilter(Pattern.compile("(^\\..*|.*\\.tmp$|db.*|^lost\\+found$)")),
				new WhitelistRegexRelativePathInboxFilter(Pattern.compile("AUX/[0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z_]_((OPER|TEST|REPR)_)?(AUX_OBMEMC|AUX_PP1|AUX_PP2|AUX_CAL|AUX_INS|AUX_RESORB|AUX_WND|AUX_SCS|AMV_ERRMAT|AMH_ERRMAT|AUX_ICE|AUX_WAV|MPL_ORBPRE|MPL_ORBSCT|MSK__LAND)_.*\\.(xml|XML|EOF|SAFE)"))
		);
		final FilesystemInboxAdapter uutAux = new FilesystemInboxAdapter(factory, testDir.toPath().toUri(), null, null, ProductFamily.AUXILIARY_FILE);
		final Collection<InboxEntry> actualAux = uutAux.read(auxFilter);
		Assertions.assertEquals(1, actualAux.size());
	}
	
	private File newTestProduct(final String name) {
		final File product = new File(testDir, name);
		product.getParentFile().mkdirs();
		return product;
	}
}
