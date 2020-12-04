package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

import static org.junit.Assert.*;

public class TestAuxipInboxFilter {

	private final static String AUX_ZIP_REGEX = "^S1.*(AUX_|AMH_|AMV_|MPL_).*$";
	private final static WhitelistRegexRelativePathInboxFilter WHITELIST_FILTER_AUX_ZIP = new WhitelistRegexRelativePathInboxFilter(
			Pattern.compile(AUX_ZIP_REGEX));

	private final static List<String> AUX_ZIPS;
	private final static String AUX_OPER_MPL_ORBPRE_ZIP = "S1A_OPER_MPL_ORBPRE_20201121T200312_20201128T200312_0001.EOF.zip";
	private final static String AUX_OPER_PREORB_OPOD_ZIP = "S1A_OPER_AUX_PREORB_OPOD_20201118T183424_V20201118T172742_20201119T000242.EOF";

	static {
		AUX_ZIPS = new ArrayList<>();
		AUX_ZIPS.add(AUX_OPER_MPL_ORBPRE_ZIP);
		AUX_ZIPS.add(AUX_OPER_PREORB_OPOD_ZIP);
	}

	private final static String PLANS_REPORTS_ZIP_REGEX = "^S1.*(_OPER_REP_MP_MP__PDMC_|_OPER_REP__SUP___|_OPER_MPL_SP.{4}_PDMC_|_OPER_REP_STNACQ_.{4}_|_OPER_REP_STNUNV_.{4}_|_OPER_AM[VH_]_FAILUR_MPC__|_OPER_REP__MACP__).*\\.(zip|ZIP)$";
	private final static WhitelistRegexRelativePathInboxFilter WHITELIST_FILTER_PLANS_REPORTS_ZIP = new WhitelistRegexRelativePathInboxFilter(
			Pattern.compile(PLANS_REPORTS_ZIP_REGEX));

	private final static List<String> PLANS_REPORTS_ZIPS;
	private final static String MISSION_PLAN_REPORT_ZIP = "S1B_OPER_REP_MP_MP__PDMC_20200303T091119_V20200303T170000_20200326T190000.xml.zip";
	private final static String FOS_SATELLITE_UNAVAILABLITIY_REPORT_ZIP = "S1A_OPER_REP__SUP___20181208T070000_20181208T111500_0001.EOF.zip";
	private final static String MANOEUVRE_ACCELERATION_PROFILE_ZIP = "S1A_OPER_REP__MACP__20130712T015902_20130712T050411_0001.TGZ.zip";
	private final static String FAILURE_MATRICES_ZIP = "S1B_OPER_AM__FAILUR_MPC__20170126T091213_V20170116T220000_99999999T999999.EOF.zip";
	private final static String STATION_ACQUISITION_REPORT_ZIP = "S1A_OPER_REP_STNACQ_MPS__20200304T192845_V20200304T191631_20200304T192629.EOF.zip";
	private final static String STATION_ACQUISITION_PLAN_ZIP = "S1B_OPER_MPL_SPMPS__PDMC_20200302T091444_V20200302T170000_20200314T190000.EOF.zip";

	static {
		PLANS_REPORTS_ZIPS = new ArrayList<>();
		PLANS_REPORTS_ZIPS.add(MISSION_PLAN_REPORT_ZIP);
		PLANS_REPORTS_ZIPS.add(MANOEUVRE_ACCELERATION_PROFILE_ZIP);
		PLANS_REPORTS_ZIPS.add(FOS_SATELLITE_UNAVAILABLITIY_REPORT_ZIP);
		PLANS_REPORTS_ZIPS.add(FAILURE_MATRICES_ZIP);
		PLANS_REPORTS_ZIPS.add(STATION_ACQUISITION_REPORT_ZIP);
		PLANS_REPORTS_ZIPS.add(STATION_ACQUISITION_PLAN_ZIP);
	}

	// --------------------------------------------------------------------------

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue_forAuxZip() {
		boolean acceptsAll = true;
		for (int i = 0; i < AUX_ZIPS.size(); i++) {
			final String filename = AUX_ZIPS.get(i);
			final boolean accepts = TestAuxipInboxFilter.WHITELIST_FILTER_AUX_ZIP
					.accept(newAuxipInboxEntry(filename, ProductFamily.AUXILIARY_FILE_ZIP));
			System.out.println((i + 1) + "/" + AUX_ZIPS.size() + " " + filename + ":\n    expect: accepted  ->  is: "
					+ (accepts ? "accepted" : "not accepted"));
			if (!accepts) {
				acceptsAll = false;
			}
		}
		assertTrue(acceptsAll);
	}

	@Test
	public final void testAccept_OnMatchingRegex_ShallReturnTrue_forPlanReportZips() {
		boolean acceptsAll = true;
		for (int i = 0; i < PLANS_REPORTS_ZIPS.size(); i++) {
			final String filename = PLANS_REPORTS_ZIPS.get(i);
			final boolean accepts = TestAuxipInboxFilter.WHITELIST_FILTER_PLANS_REPORTS_ZIP
					.accept(newAuxipInboxEntry(filename, ProductFamily.PLAN_AND_REPORT_ZIP));
			System.out.println((i + 1) + "/" + PLANS_REPORTS_ZIPS.size() + " " + filename
					+ ":\n    expect: accepted  ->  is: " + (accepts ? "accepted" : "not accepted"));
			if (!accepts) {
				acceptsAll = false;
			}
		}
		assertTrue(acceptsAll);
	}

	@Test
	public final void testAuxRegex() {
		boolean matchAll = true;
		for (int i = 0; i < AUX_ZIPS.size(); i++) {
			final String filename = AUX_ZIPS.get(i);
			final boolean matches = Pattern.matches(AUX_ZIP_REGEX, filename);
			System.out.println((i + 1) + "/" + AUX_ZIPS.size() + " " + filename + ":\n    expect: matches  ->  is: "
					+ (matches ? "matches" : "does not match"));
			if (!matches) {
				matchAll = false;
			}
		}
		assertTrue(matchAll);
	}

	@Test
	public final void testPlansReportsRegex() {
		boolean matchAll = true;
		for (int i = 0; i < PLANS_REPORTS_ZIPS.size(); i++) {
			final String filename = PLANS_REPORTS_ZIPS.get(i);
			final boolean matches = Pattern.matches(PLANS_REPORTS_ZIP_REGEX, filename);
			System.out.println((i + 1) + "/" + PLANS_REPORTS_ZIPS.size() + " " + filename
					+ ":\n    expect: matches  ->  is: " + (matches ? "matches" : "does not match"));
			if (!matches) {
				matchAll = false;
			}
		}
		assertTrue(matchAll);
	}

	//@Test
	public final void testPlansReportsNotMatchAuxRegex() {
		// making sure the product is not ingested twice
		boolean matchNone = true;
		for (int i = 0; i < PLANS_REPORTS_ZIPS.size(); i++) {
			final String filename = PLANS_REPORTS_ZIPS.get(i);
			final boolean matches = Pattern.matches(AUX_ZIP_REGEX, filename);
			System.out.println((i + 1) + "/" + PLANS_REPORTS_ZIPS.size() + " " + filename
					+ ":\n    expect: does not match  ->  is: " + (matches ? "matches" : "does not match"));
			if (matches) {
				matchNone = false;
			}
		}
		assertTrue(matchNone);
	}

	@Test
	public final void testAuxNotMatchPlansReportsRegex() {
		// making sure the product is not ingested twice
		boolean matchNone = true;
		for (int i = 0; i < AUX_ZIPS.size(); i++) {
			final String filename = AUX_ZIPS.get(i);
			final boolean matches = Pattern.matches(PLANS_REPORTS_ZIP_REGEX, filename);
			System.out.println((i + 1) + "/" + AUX_ZIPS.size() + " " + filename
					+ ":\n    expect: does not match  ->  is: " + (matches ? "matches" : "does not match"));
			if (matches) {
				matchNone = false;
			}
		}
		assertTrue(matchNone);
	}

	// --------------------------------------------------------------------------

	private static InboxEntry newAuxipInboxEntry(final String filename, final ProductFamily productFamily) {
		return new InboxEntry(UUID.randomUUID().toString(), filename, "https://prip.odata/", new Date(), 123456, null,
				"auxip", productFamily.name(), "PRIP");
	}

}
