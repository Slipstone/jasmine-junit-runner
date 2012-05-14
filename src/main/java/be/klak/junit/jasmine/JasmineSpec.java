package be.klak.junit.jasmine;

import static junit.framework.Assert.*;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import be.klak.rhino.RhinoContext;
import be.klak.rhino.RhinoRunnable;

// TODO rhinoContext als field zetten ipv altijd mee te geven?
class JasmineSpec {

	public enum JasmineSpecStatus {
		PASSED, FAILED, SKIPPED
	}

	private final Description description;
	private final NativeObject spec;

	JasmineSpec(final NativeObject spec) {
		this.spec = spec;
		final String descriptionString = (String) spec.get("description", spec);
		this.description = Description
				.createSuiteDescription(descriptionString);
	}

	public Description getDescription() {
		return description;
	}

	public NativeObject getSpec() {
		return spec;
	}

	public boolean isPassed(final RhinoContext context) {
		return getSpecResultStatus(context) == JasmineSpecStatus.PASSED;
	}

	public boolean isFailed(final RhinoContext context) {
		return getSpecResultStatus(context) == JasmineSpecStatus.FAILED;
	}

	public JasmineSpecStatus getSpecResultStatus(final RhinoContext context) {
		assertTrue(isDone());

		final NativeObject results = getSpecResults(context);
		final boolean passed = (Boolean) context.executeFunction(results,
				"passed");
		final boolean skipped = (Boolean) results.get("skipped", results);

		if (skipped) {
			return JasmineSpecStatus.SKIPPED;
		}
		return passed ? JasmineSpecStatus.PASSED : JasmineSpecStatus.FAILED;
	}

	public Failure getJunitFailure(final RhinoContext context) {
		assertTrue(isFailed(context));
		return new Failure(description, getFirstFailedStacktrace(context));
	}

	private Throwable getFirstFailedStacktrace(final RhinoContext context) {
		final NativeArray resultItems = (NativeArray) context.executeFunction(
				getSpecResults(context), "getItems");
		for (final Object resultItemId : resultItems.getIds()) {
			final NativeObject resultItem = (NativeObject) resultItems.get(
					(Integer) resultItemId, resultItems);

			if (!((Boolean) context.executeFunction(resultItem, "passed"))) {
				return new JasmineSpecFailureException(resultItem);
			}
		}

		return null;
	}

	private NativeObject getSpecResults(final RhinoContext context) {
		return (NativeObject) context.executeFunction(spec, "results");
	}

	public boolean isDone() {
		final Object doneResult = spec.get("done", spec);
		return doneResult instanceof Boolean && ((Boolean) doneResult);
	}

	public void execute(final RhinoContext baseContext) {
		baseContext.runAsync(new RhinoRunnable() {

			@Override
			public void run(final RhinoContext context) {
				context.executeFunction(spec, "execute");
			}
		});
	}

	@Override
	public String toString() {
		return description.getDisplayName();
	}
}
