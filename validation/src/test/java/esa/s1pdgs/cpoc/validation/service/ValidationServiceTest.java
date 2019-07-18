package esa.s1pdgs.cpoc.validation.service;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class ValidationServiceTest {

	@Before
	public void setUp() {
		
	}
	
	@Test
	public void test() {
		ValidationService vs = new ValidationService();
		vs.process(new Date(), new Date());
	}
	
}
