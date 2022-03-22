package esa.s1pdgs.cpoc.obs_sdk;

public class ObsValidationException extends Exception {
	   private static final long serialVersionUID = 5121673912582265431L;

	   public ObsValidationException(final String message, final Throwable cause, final Object... arguments) {
	      super(format(message, arguments), cause);
	   }

	   public ObsValidationException(final String message, final Object... arguments) {
	      this(message, null, arguments);
	   }

	   @Override
	   public String getMessage() {
	      return getDeepMessage(super.getMessage(), this.getCause());
	   }

	   static String getDeepMessage(final String msg, final Throwable cause) {
	      if(null != cause) {
	         return msg + ": " + getDeepMessage(cause.getMessage(), cause.getCause());
	      } else {
	         return msg;
	      }
	   }
	   
	   private static String format(final String message, final Object... arguments) {
	      return String.format(message.replaceAll("\\{\\}", "%s"), arguments);
	   }
}
