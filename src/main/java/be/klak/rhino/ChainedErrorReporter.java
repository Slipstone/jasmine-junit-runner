package be.klak.rhino;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.tools.ToolErrorReporter;

class ChainedErrorReporter implements ErrorReporter {

	private final List<ErrorReporter> chainedReporters = new ArrayList<ErrorReporter>();

	ChainedErrorReporter(final ErrorReporter chainedDefaultReporter) {
		chainedReporters.add(chainedDefaultReporter);
		chainedReporters.add(new ToolErrorReporter(true, System.err));
	}

	@Override
	public void error(final String message, final String sourceName,
			final int line, final String lineSource, final int lineOffset) {
		EvaluatorException ex = null;
		for (final ErrorReporter reporter : chainedReporters) {
			try {
				reporter.error(message, sourceName, line, lineSource,
						lineOffset);
			} catch (final EvaluatorException thrownByChainEx) {
				ex = thrownByChainEx;
			}
		}

		if (ex != null) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public EvaluatorException runtimeError(final String message,
			final String sourceName, final int line, final String lineSource,
			final int lineOffset) {
		EvaluatorException ex = null;
		for (final ErrorReporter reporter : chainedReporters) {
			final EvaluatorException returnedByChainEx = reporter.runtimeError(
					message, sourceName, line, lineSource, lineOffset);
			if (returnedByChainEx != null) {
				ex = returnedByChainEx;
			}
		}

		if (ex != null) {
			throw ex;
		}
		return null;
	}

	@Override
	public void warning(final String message, final String sourceName,
			final int line, final String lineSource, final int lineOffset) {
		for (final ErrorReporter reporter : chainedReporters) {
			reporter.warning(message, sourceName, line, lineSource, lineOffset);
		}
	}

}
