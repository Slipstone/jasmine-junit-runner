package be.klak.junit.jasmine;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.EvaluatorException;

import be.klak.junit.jasmine.classes.JasmineTestRunnerExceptionInJSCode;
import be.klak.junit.jasmine.classes.JasmineTestRunnerExceptionInSpec;
import be.klak.junit.jasmine.classes.JasmineTestRunnerFailingSpec;

@RunWith(MockitoJUnitRunner.class)
public class JasmineFailingSpecsTest {

	@Mock
	private RunNotifier notifierMock;

	@Test
	public void shouldNotifyOfSingleFailure() {
		new JasmineTestRunner(JasmineTestRunnerFailingSpec.class)
				.run(notifierMock);

		final ArgumentCaptor<Failure> failureCaptor = ArgumentCaptor
				.forClass(Failure.class);
		final ArgumentCaptor<Description> descriptionCaptor = ArgumentCaptor
				.forClass(Description.class);
		verify(notifierMock).fireTestStarted(descriptionCaptor.capture());
		verify(notifierMock).fireTestFailure(failureCaptor.capture());
		verifyNoMoreInteractions(notifierMock);

		final Failure failure = failureCaptor.getValue();
		final Description startedDescription = descriptionCaptor.getValue();

		assertThat(failure.getDescription()).isEqualTo(startedDescription);
		assertThat(failure.getDescription().getDisplayName()).startsWith(
				"will always fail");
		assertThat(failure.getMessage())
				.isEqualTo("Expected true to be false.");
	}

	@Test
	public void shouldNotifyOfSingleExceptionWithinSpecFunction() {
		new JasmineTestRunner(JasmineTestRunnerExceptionInSpec.class)
				.run(notifierMock);

		final ArgumentCaptor<Failure> failureCaptor = ArgumentCaptor
				.forClass(Failure.class);
		final ArgumentCaptor<Description> descriptionCaptor = ArgumentCaptor
				.forClass(Description.class);
		verify(notifierMock).fireTestStarted(descriptionCaptor.capture());
		verify(notifierMock).fireTestFailure(failureCaptor.capture());
		verifyNoMoreInteractions(notifierMock);

		final Failure failure = failureCaptor.getValue();
		final Description startedDescription = descriptionCaptor.getValue();

		assertThat(failure.getDescription()).isEqualTo(startedDescription);
		assertThat(failure.getDescription().getDisplayName()).startsWith(
				"will always crash");
		assertThat(failure.getMessage())
				.isEqualTo(
						"ReferenceError: \"OEIWANU\" is not defined. in src/test/javascript/specs/crashingSpec.js (line 3)");
	}

	@Test(expected = EvaluatorException.class)
	public void shouldCrashWhileTryingToLoadFaultyJSSpecFile() {
		new JasmineTestRunner(JasmineTestRunnerExceptionInJSCode.class);
	}
}
