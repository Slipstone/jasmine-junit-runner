package be.klak.junit.jasmine;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import be.klak.rhino.RhinoContext;

class JasmineJSSuiteConverter {

	private final RhinoContext context;

	public JasmineJSSuiteConverter(final RhinoContext context) {
		this.context = context;
	}

	public JasmineDescriptions convertToJunitDescriptions(
			final Class<?> testClass, final NativeArray baseSuites) {
		final Description rootDescription = Description
				.createSuiteDescription(testClass);
		final List<JasmineSpec> specs = convertSuiteArrayToDescriptions(
				baseSuites, rootDescription, new ArrayList<String>());
		return new JasmineDescriptions(rootDescription, specs);
	}

	private List<JasmineSpec> convertSuiteArrayToDescriptions(
			final NativeArray suiteArray, final Description rootDescription,
			final List<String> processed) {
		final List<JasmineSpec> specs = new ArrayList<JasmineSpec>();
		for (final Object idObj : suiteArray.getIds()) {
			final NativeObject suite = (NativeObject) suiteArray.get(
					(Integer) idObj, suiteArray);

			final String description = (String) suite.get("description", suite);
			if (!processed.contains(description)) {
				final Description suiteDescription = addSuiteToDescription(
						rootDescription, processed, description);
				specs.addAll(convertToJunitDescription(suite, suiteDescription));

				final NativeArray subSuites = (NativeArray) context
						.executeFunction(suite, "suites");
				specs.addAll(convertSuiteArrayToDescriptions(subSuites,
						suiteDescription, processed));
			}
		}

		return specs;
	}

	private Description addSuiteToDescription(final Description description,
			final List<String> processed, final String suiteName) {
		processed.add(suiteName);
		final Description suiteDescription = Description
				.createSuiteDescription(suiteName, (Annotation[]) null);
		description.addChild(suiteDescription);
		return suiteDescription;
	}

	private List<JasmineSpec> convertToJunitDescription(
			final NativeObject suite, final Description description) {
		final List<JasmineSpec> specsMap = new ArrayList<JasmineSpec>();
		final NativeArray specsArray = (NativeArray) context.executeFunction(
				suite, "specs");
		for (final Object idObj : specsArray.getIds()) {
			final NativeObject spec = (NativeObject) specsArray.get(
					(Integer) idObj, specsArray);

			final JasmineSpec jasmineSpec = new JasmineSpec(spec, description);
			specsMap.add(jasmineSpec);
			description.addChild(jasmineSpec.getDescription());
		}

		return specsMap;
	}

}
