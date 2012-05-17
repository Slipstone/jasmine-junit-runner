package be.klak.junit.jasmine;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import be.klak.junit.jasmine.classes.JasmineTestRunnerSuccessSpec;

@RunWith(MockitoJUnitRunner.class)
public class JasmineFinishedSpecsTest {

	@Mock
	private RunNotifier notifierMock;

	@Test
	public void shouldNotifyOfSingleSuccess() {
		new JasmineTestRunner(JasmineTestRunnerSuccessSpec.class)
				.run(notifierMock);

		final ArgumentCaptor<Description> descriptionStartedCaptor = ArgumentCaptor
				.forClass(Description.class);
		final ArgumentCaptor<Description> descriptionFinishedCaptor = ArgumentCaptor
				.forClass(Description.class);
		verify(notifierMock)
				.fireTestStarted(descriptionStartedCaptor.capture());
		verify(notifierMock).fireTestFinished(
				descriptionFinishedCaptor.capture());
		verifyNoMoreInteractions(notifierMock);

		final Description startedDescription = descriptionStartedCaptor
				.getValue();
		final Description finishedDescription = descriptionFinishedCaptor
				.getValue();

		assertThat(startedDescription).isSameAs(finishedDescription);
		assertThat(startedDescription.getDisplayName()).startsWith(
				"will always run");
	}

}
