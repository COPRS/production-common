package esa.s1pdgs.cpoc.jobgenerator;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class Test {

	public static void main(String[] args) {
		new Test().execute();
	}
	
	private void execute() {
		List<ProductFamily> list = new ArrayList<>();
		list.add(ProductFamily.L0_SLICE);
		list.add(ProductFamily.L1_SLICE);
		
		System.out.println(list.contains(ProductFamily.L0_SLICE));
		System.out.println(list.contains(ProductFamily.L1_SLICE));
		System.out.println(list.contains("L0_SLICE"));
		System.out.println(list.contains(ProductFamily.L2_SLICE));
	}

}
