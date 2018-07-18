package int_.esa.s1pdgs.cpoc.common.errors.k8s;

/**
 * @author Viveris Technologies
 */
public class K8sUnknownResourceException extends K8sEntityException {

    /**
     * UUID
     */
    private static final long serialVersionUID = 1694390566100800259L;

    /**
     * @param message
     */
    public K8sUnknownResourceException(final String message) {
        super(ErrorCode.K8S_UNKNOWN_RESOURCE, message);
    }
}
