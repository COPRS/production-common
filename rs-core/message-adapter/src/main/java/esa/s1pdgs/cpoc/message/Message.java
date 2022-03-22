package esa.s1pdgs.cpoc.message;

public interface Message<M> {

    M data();

    Object internalMessage();

}
