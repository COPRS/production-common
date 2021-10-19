package esa.s1pdgs.cpoc.prip.model;

public enum ProductionType {
	
	SYSTEMATIC_PRODUCTION(0, "systematic_production"),
	ON_DEMAND_DEFAULT(1, "on_demand_default"),
	ON_DEMAND_NON_DEFAULT(2, "on_demand_non_default");
	
	private static final long serialVersionUID = -2974165362740296325L;
	
	private final int value;
	private final String name;
	
	private ProductionType(final int value, final String name) {
        this.value = value;
        this.name = name;
    }
	
	public int getValue() {
        return value;
    }

	public String getName() {
        return name;
    }

}
