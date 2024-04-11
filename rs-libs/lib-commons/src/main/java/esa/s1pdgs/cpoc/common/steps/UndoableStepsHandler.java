/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
