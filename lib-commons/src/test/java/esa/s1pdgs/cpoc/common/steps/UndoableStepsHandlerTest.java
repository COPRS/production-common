package esa.s1pdgs.cpoc.common.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UndoableStepsHandlerTest {

    @Mock
    UndoableStep firsStep;

    @Mock
    UndoableStep secondStep;

    @Mock
    UndoableStep thirdStep;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        when(firsStep.toString()).thenReturn("first step");
        when(secondStep.toString()).thenReturn("second step");
        when(thirdStep.toString()).thenReturn("third step");
    }

    @Test
    public void performFirstStepFails() {
        doThrow(new RuntimeException("first step failed")).when(firsStep).perform();

        assertThrows(RuntimeException.class,
                () -> new UndoableStepsHandler(asList(firsStep, secondStep, thirdStep)).perform());

        verify(firsStep, times(1)).perform();
        verify(firsStep, times(1)).undo();

        verifyNoInteractions(secondStep, thirdStep);
    }

    @Test
    public void performSecondStepFails() {
        doThrow(new RuntimeException("second step failed")).when(secondStep).perform();

        assertThrows(RuntimeException.class,
                () -> new UndoableStepsHandler(asList(firsStep, secondStep, thirdStep)).perform());

        verify(firsStep, times(1)).perform();
        verify(firsStep, times(1)).undo();

        verify(secondStep, times(1)).perform();
        verify(secondStep, times(1)).undo();

        verifyNoInteractions(thirdStep);
    }

    @Test
    public void performThirdStepFails() {
        doThrow(new RuntimeException("third step failed")).when(thirdStep).perform();

        assertThrows(RuntimeException.class,
                () -> new UndoableStepsHandler(asList(firsStep, secondStep, thirdStep)).perform());

        verify(firsStep, times(1)).perform();
        verify(firsStep, times(1)).undo();

        verify(secondStep, times(1)).perform();
        verify(secondStep, times(1)).undo();

        verify(thirdStep, times(1)).perform();
        verify(thirdStep, times(1)).undo();
    }

    @Test
    public void performAllStepsSucceed() {

        new UndoableStepsHandler(asList(firsStep, secondStep, thirdStep)).perform();

        verify(firsStep, times(1)).perform();
        verify(firsStep, times(0)).undo();

        verify(secondStep, times(1)).perform();
        verify(secondStep, times(0)).undo();

        verify(thirdStep, times(1)).perform();
        verify(thirdStep, times(0)).undo();
    }

    @Test
    public void performThirdStepFailsSecondRollbackFails() {
        doThrow(new RuntimeException("third step failed")).when(thirdStep).perform();
        doThrow(new RuntimeException("undo of second step failed")).when(secondStep).undo();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> new UndoableStepsHandler(asList(firsStep, secondStep, thirdStep)).perform());

        assertThat(exception.getMessage(), is(equalTo("undo of second step failed")));

        verify(firsStep, times(1)).perform();
        verify(firsStep, times(0)).undo();

        verify(secondStep, times(1)).perform();
        verify(secondStep, times(1)).undo();

        verify(thirdStep, times(1)).perform();
        verify(thirdStep, times(1)).undo();
    }
}