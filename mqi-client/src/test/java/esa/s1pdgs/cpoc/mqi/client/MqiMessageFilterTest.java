package esa.s1pdgs.cpoc.mqi.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

public class MqiMessageFilterTest {
	@Test
	public void familyNotDefined_ShallAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter(null, null, null);
		final ProductionEvent event = newProductionEvent(null, ProductFamily.PLAN_AND_REPORT);
		assertTrue(uut.accept(event));
	}
	
	@Test
	public void differentFamily_ShallAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter(null, null, ProductFamily.AUXILIARY_FILE);
		final ProductionEvent event = newProductionEvent(null, ProductFamily.PLAN_AND_REPORT);
		assertTrue(uut.accept(event));
	}
	
	@Test
	public void sameFamilyAndNoRegex_ShallAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter(null, null, ProductFamily.AUXILIARY_FILE);
		final ProductionEvent event = newProductionEvent(null, ProductFamily.AUXILIARY_FILE);
		assertTrue(uut.accept(event));
	}
	
	@Test
	public void matchingAllowRegex_ShallAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter("^S1[ABCD]_OPER_REP__SUP___.*$", null, ProductFamily.PLAN_AND_REPORT);
		final ProductionEvent event = newProductionEvent("S1A_OPER_REP__SUP___1234", ProductFamily.PLAN_AND_REPORT);
		assertTrue(uut.accept(event));
	}
	
	@Test
	public void notMatchingAllowRegex_ShallNotAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter("^S1[ABCD]_OPER_REP__SUP___.*$", null, ProductFamily.PLAN_AND_REPORT);
		final ProductionEvent event = newProductionEvent("notallowed", ProductFamily.PLAN_AND_REPORT);
		assertFalse(uut.accept(event));
	}
	
	@Test
	public void matchingDisallowRegex_ShallNotAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter(null, "^S1[ABCD]_OPER_REP__SUP___.*$", ProductFamily.PLAN_AND_REPORT);
		final ProductionEvent event = newProductionEvent("S1A_OPER_REP__SUP___1234", ProductFamily.PLAN_AND_REPORT);
		assertFalse(uut.accept(event));
	}
	
	@Test
	public void notMatchingDisallowRegex_ShallAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter(null, "^S1[ABCD]_OPER_REP__SUP___.*$", ProductFamily.PLAN_AND_REPORT);
		final ProductionEvent event = newProductionEvent("accepted", ProductFamily.PLAN_AND_REPORT);
		assertTrue(uut.accept(event));
	}
	
	@Test
	public void notMatchingDisallowRegexAndMatchingAllowRegex_ShallAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter("accepted", "^S1[ABCD]_OPER_REP__SUP___.*$", ProductFamily.PLAN_AND_REPORT);
		final ProductionEvent event = newProductionEvent("accepted", ProductFamily.PLAN_AND_REPORT);
		assertTrue(uut.accept(event));
	}
	
	@Test
	public void matchingDisallowRegexAndNonMatchingAllowRegex_ShallNotAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter("accepted", "^S1[ABCD]_OPER_REP__SUP___.*$", ProductFamily.PLAN_AND_REPORT);
		final ProductionEvent event = newProductionEvent("S1A_OPER_REP__SUP___1234", ProductFamily.PLAN_AND_REPORT);
		assertFalse(uut.accept(event));
	}
	
	@Test
	public void matchingDisallowRegexAndMatchingAllowRegex_ShallNotAccept() {
		final MqiMessageFilter uut = newMqiMessageFilter("^S1[ABCD]_OPER_REP__SUP___.*$", "^S1[ABCD]_OPER_REP__SUP___.*$", ProductFamily.PLAN_AND_REPORT);
		final ProductionEvent event = newProductionEvent("S1A_OPER_REP__SUP___1234", ProductFamily.PLAN_AND_REPORT);
		assertFalse(uut.accept(event));
	}
	
	private MqiMessageFilter newMqiMessageFilter(final String allow, final String disallow, final ProductFamily family) {
		final MqiMessageFilter f = new MqiMessageFilter();
		f.setProductFamily(family);
		f.setAllowRegex(allow);
		f.setDisallowRegex(disallow);
		return f;
	}
	
	private final ProductionEvent newProductionEvent(final String name, final ProductFamily family) {
		final ProductionEvent pe = new ProductionEvent();
		pe.setKeyObjectStorage(name);
		pe.setProductFamily(family);
		return pe;
	}
	
}
