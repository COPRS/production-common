package int_.esa.s1pdgs.cpoc.common.errors.k8s;

/**
 * @author Viveris Technologies
 */
public class PodResourceException extends K8sEntityException {

    /**
     * UUID
     */
    private static final long serialVersionUID = -4025488986609198762L;

    /**
     * @param message
     */
    public PodResourceException(final String message) {
        super(ErrorCode.K8S_NO_TEMPLATE_POD, message);
    }

    /**
     * @param message
     * @param cause
     */
    public PodResourceException(final String message, final Throwable cause) {
        super(ErrorCode.K8S_NO_TEMPLATE_POD, message, cause);
    }

}
