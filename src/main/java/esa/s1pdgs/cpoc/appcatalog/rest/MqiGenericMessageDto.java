package esa.s1pdgs.cpoc.appcatalog.rest;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Generic message object used by the REST applicative catalog
 * 
 * @author Viveris Technologies
 * @param <T>
 */
public class MqiGenericMessageDto<T> extends MqiLightMessageDto {

    /**
     * Dto object: class according the category
     */
    protected T dto;

    /**
     * Default constructor
     */
    public MqiGenericMessageDto() {
        super();
    }

    /**
     * @param category
     */
    public MqiGenericMessageDto(final ProductCategory category) {
        super(category);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiGenericMessageDto(final ProductCategory category,
            final long identifier, final String topic, final int partition,
            final long offset) {
        super(category, identifier, topic, partition, offset);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     * @param dto
     */
    public MqiGenericMessageDto(final ProductCategory category,
            final long identifier, final String topic, final int partition,
            final long offset, final T dto) {
        super(category, identifier, topic, partition, offset);
        this.dto = dto;
    }

    /**
     * @return the dto
     */
    public T getDto() {
        return dto;
    }

    /**
     * @param dto
     *            the dto to set
     */
    public void setDto(final T dto) {
        this.dto = dto;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{%s, dto: %s}", toStringForExtend(), dto);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, dto);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            MqiGenericMessageDto<?> other = (MqiGenericMessageDto<?>) obj;
            ret = super.equals(other) && Objects.equals(dto, other.dto);
        }
        return ret;
    }
}
