package esa.s1pdgs.cpoc.dlq.manager.model.routing;

public enum ActionType {
	RESTART, DELETE, NO_ACTION;
	
	public static ActionType fromValue(final String value) {
		try {
			return ActionType.valueOf(value.trim().toUpperCase().replace('-', '_').replace("NOACTION", "NO_ACTION"));
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Invalid action type %s", value), e);
		}
	}
}
