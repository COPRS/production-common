package esa.s1pdgs.cpoc.common.steps;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UndoableStepsHandler {

    private static final Logger LOG = LogManager.getLogger(UndoableStepsHandler.class);

    final Queue<UndoableStep> stepsTodo;
    final Stack<UndoableStep> stepsDone = new Stack<>();

    public UndoableStepsHandler(final List<UndoableStep> stepsTodo) {
        this.stepsTodo = new ArrayDeque<>(stepsTodo);
    }

    public UndoableStepsHandler(UndoableStep... stepsTodo) {
        this.stepsTodo = new ArrayDeque<>(Arrays.asList(stepsTodo));
    }

    public void perform() {
        try {
            perform(stepsTodo, stepsDone);
        } catch (Exception e) {
            LOG.error("error during step execution: {}", stepsTodo.peek(), e);
            rollback(stepsDone);
            throw e;
        }
    }

    private void perform(Queue<UndoableStep> tasksTodo, Stack<UndoableStep> tasksDone) {
        while (!tasksTodo.isEmpty()) {
            final UndoableStep currentStep = tasksTodo.poll();
            tasksDone.add(currentStep);
            LOG.info("performing step: {}", currentStep);
            currentStep.perform();
        }
    }

    private void rollback(Stack<UndoableStep> tasksDone) {
        try {
            while (!tasksDone.isEmpty()) {
                UndoableStep currentStep = tasksDone.peek(); // don't remove head, is used for fail scenario
                LOG.info("rolling back step: {}", currentStep);
                currentStep.undo();
                tasksDone.pop();
            }
        } catch (Exception e) {
            LOG.error("error during rollback of step: {}", tasksDone.peek(), e);
            throw e;
        }
    }

}
