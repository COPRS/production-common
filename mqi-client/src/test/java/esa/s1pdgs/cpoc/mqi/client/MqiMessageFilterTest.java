package esa.s1pdgs.cpoc.mqi.client;

import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

public class MqiMessageFilterTest {

	@Test
	public void noFamily() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setAllowRegex("a");
		f.setDisallowRegex("d");

		ProductionEvent pe = new ProductionEvent();
		pe.setKeyObjectStorage("d");
		pe.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertTrue(f.accept(pe));
	}

	@Test
	public void wrongFamily() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.AUXILIARY_FILE);
		f.setAllowRegex("a");
		f.setDisallowRegex("d");

		ProductionEvent pe = new ProductionEvent();
		pe.setKeyObjectStorage("d");
		pe.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertTrue(f.accept(pe));
	}

	@Test
	public void rightFamily() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.AUXILIARY_FILE);
		f.setAllowRegex("a");
		f.setDisallowRegex("d");

		ProductionEvent pe = new ProductionEvent();
		pe.setKeyObjectStorage("d");
		pe.setProductFamily(ProductFamily.AUXILIARY_FILE);

		Assert.assertFalse(f.accept(pe));
	}
	
	@Test
	public void allowRegex_1() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.PLAN_AND_REPORT);
		f.setAllowRegex("^S1[ABCD]_OPER_REP__SUP___.*$");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("notallowed");
		pe1.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_OPER_REP__SUP___1234");
		pe2.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertTrue(f.accept(pe2));
	}

	@Test
	public void disallowRegex_1() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.PLAN_AND_REPORT);
		f.setDisallowRegex("^((?!^S1[ABCD]_OPER_REP__SUP___.*$).)*$");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("notallowed");
		pe1.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_OPER_REP__SUP___1234");
		pe2.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertTrue(f.accept(pe2));
	}
	
	@Test
	public void allowRegex_2() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.PLAN_AND_REPORT);
		f.setAllowRegex("^S1[ABCD_]_OPER_REP_MP_MP__PDMC.*$");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("notallowed");
		pe1.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_OPER_REP_MP_MP__PDMC1234");
		pe2.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertTrue(f.accept(pe2));
	}

	@Test
	public void disallowRegex_2() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.PLAN_AND_REPORT);
		f.setDisallowRegex("^((?!(^S1[ABCD_]_OPER_REP_MP_MP__PDMC.*$)).)*$");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("notallowed");
		pe1.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_OPER_REP_MP_MP__PDMC1234");
		pe2.setProductFamily(ProductFamily.PLAN_AND_REPORT);

		Assert.assertTrue(f.accept(pe2));
	}
	
	@Test
	public void allowRegex_3() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.L0_SEGMENT);
		f.setAllowRegex("S1._(GP|HK|RF).*_RAW.*\\.SAFE");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("notallowed");
		pe1.setProductFamily(ProductFamily.L0_SEGMENT);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_GP_RAW.SAFE");
		pe2.setProductFamily(ProductFamily.L0_SEGMENT);

		Assert.assertTrue(f.accept(pe2));
	}

	@Test
	public void disallowRegex_3() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.L0_SEGMENT);
		f.setDisallowRegex("S1._(?!GP|HK|RF).*_RAW.*\\.SAFE");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("S1A_XX_RAW.SAFE");
		pe1.setProductFamily(ProductFamily.L0_SEGMENT);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_GP_RAW.SAFE");
		pe2.setProductFamily(ProductFamily.L0_SEGMENT);

		Assert.assertTrue(f.accept(pe2));
	}
	
	@Test
	public void allowRegex_4() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.L0_SLICE);
		f.setAllowRegex("^S1[A-D]_(IW|WV|EW|S[1-6]).*_RAW__0S.*");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("notallowed");
		pe1.setProductFamily(ProductFamily.L0_SLICE);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_WV_RAW__0S123");
		pe2.setProductFamily(ProductFamily.L0_SLICE);

		Assert.assertTrue(f.accept(pe2));
	}

	@Test
	public void disallowRegex_4() {

		MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(ProductFamily.L0_SLICE);
		f.setDisallowRegex("^S1[A-D]_(?!IW|WV|EW|S[1-6]).*_RAW__0S.*");

		ProductionEvent pe1 = new ProductionEvent();
		pe1.setKeyObjectStorage("S1A_XX_RAW__0S");
		pe1.setProductFamily(ProductFamily.L0_SLICE);

		Assert.assertFalse(f.accept(pe1));

		ProductionEvent pe2 = new ProductionEvent();
		pe2.setKeyObjectStorage("S1A_IW_RAW__0S");
		pe2.setProductFamily(ProductFamily.L0_SLICE);

		Assert.assertTrue(f.accept(pe2));
	}
	
	
	
}
