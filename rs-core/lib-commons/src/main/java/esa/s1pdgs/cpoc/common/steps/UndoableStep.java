package esa.s1pdgs.cpoc.common.steps;

public interface UndoableStep {

    void perform();

    void undo();

}
