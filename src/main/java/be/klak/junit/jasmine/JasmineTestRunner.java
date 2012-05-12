package be.klak.junit.jasmine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.tools.debugger.Main;

import be.klak.rhino.RhinoContext;

import com.slipstone.FindFiles;

public class JasmineTestRunner extends Runner {

	private static final int SLEEP_TIME_MILISECONDS = 50;
	private static final String JASMINE_LIB_DIR = "lib/jasmine-1.0.2/";

	private JasmineDescriptions jasmineSuite;

	protected final RhinoContext rhinoContext;
	protected final JasmineSuite suiteAnnotation;
	protected final Class<?> testClass;

	@JasmineSuite
	private class DefaultSuite {
	}

	public JasmineTestRunner(final Class<?> testClass) {
		this.testClass = testClass;
		this.suiteAnnotation = getJasmineSuiteAnnotationFromTestClass();

		Main debugger = null;
		if (this.suiteAnnotation.debug()) {
			debugger = createDebugger();
		}

		this.rhinoContext = setUpRhinoScope();

		if (this.suiteAnnotation.debug()) {
			debugger.doBreak();
		}
	}

	private RhinoContext setUpRhinoScope() {
		final RhinoContext context = new RhinoContext();

		context.loadEnv();
		setUpJasmine(context);

		pre(context);

		context.load(
				suiteAnnotation.mockDir() + "/",
				FindFiles.findFiles(suiteAnnotation.mockDir(),
						suiteAnnotation.mockInclude(),
						suiteAnnotation.mockExclude()));
		context.load(suiteAnnotation.sourceDir() + "/", FindFiles.findFiles(
				suiteAnnotation.sourceDir(), suiteAnnotation.sourceInclude(),
				suiteAnnotation.sourceExclude()));
		context.load(suiteAnnotation.specDir() + "/",
				getJasmineSpecs(suiteAnnotation));
		return context;
	}

	private String[] getJasmineSpecs(final JasmineSuite suiteAnnotation) {
		return FindFiles.findFiles(suiteAnnotation.specDir(),
				suiteAnnotation.specInclude(), suiteAnnotation.specExclude());
	}

	protected void pre(final RhinoContext context) {
	}

	private void setUpJasmine(final RhinoContext context) {
		try {
			context.load(RhinoContext.BUNDLE_FILES.get(JASMINE_LIB_DIR
					+ "jasmine.js"));
			context.load(RhinoContext.BUNDLE_FILES.get(JASMINE_LIB_DIR
					+ "jasmine.delegator_reporter.js"));

			context.evalJS("jasmine.getEnv().addReporter(new jasmine.DelegatorJUnitReporter());");
		} catch (final ExecutionException e) {
			throw new RuntimeException("Failed to load Jasmine", e);
		}
	}

	private Main createDebugger() {
		final Main debugger = new Main("JS Debugger");

		debugger.setExitAction(new Runnable() {
			@Override
			public void run() {
				System.exit(0);
			}
		});

		debugger.attachTo(ContextFactory.getGlobal());
		debugger.pack();
		debugger.setSize(600, 460);
		debugger.setVisible(true);

		return debugger;
	}

	private JasmineSuite getJasmineSuiteAnnotationFromTestClass() {
		JasmineSuite suiteAnnotation = testClass
				.getAnnotation(JasmineSuite.class);
		if (suiteAnnotation == null) {
			suiteAnnotation = DefaultSuite.class
					.getAnnotation(JasmineSuite.class);
		}
		return suiteAnnotation;
	}

	private void resetEnvjsWindowSpace() {
		try {
			this.rhinoContext.evalJS("window.location = '"
					+ RhinoContext.BUNDLE_FILES.get("lib/blank.html") + "';");
		} catch (final ExecutionException e) {
			throw new RuntimeException(
					"Failed to load blank.html into window.location", e);
		}
	}

	private JasmineDescriptions getJasmineDescriptions() {
		if (this.jasmineSuite == null) {
			final NativeArray baseSuites = (NativeArray) rhinoContext
					.evalJS("jasmine.getEnv().currentRunner().suites()");
			this.jasmineSuite = new JasmineJSSuiteConverter(rhinoContext)
					.convertToJunitDescriptions(testClass, baseSuites);
		}
		return this.jasmineSuite;
	}

	@Override
	public Description getDescription() {
		return getJasmineDescriptions().getRootDescription();
	}

	@Override
	public void run(final RunNotifier notifier) {
		generateSpecRunnerIfNeeded();

		for (final JasmineSpec spec : getJasmineDescriptions().getSpecs()) {
			final Object testClassInstance = createTestClassInstance();
			fireMethodsWithSpecifiedAnnotationIfAny(testClassInstance,
					Before.class);

			try {
				notifier.fireTestStarted(spec.getDescription());
				spec.execute(rhinoContext);
				while (!spec.isDone()) {
					waitALittle();
				}

				reportSpecResultToNotifier(notifier, spec);
				resetEnvjsWindowSpace();
			} finally {
				fireMethodsWithSpecifiedAnnotationIfAny(testClassInstance,
						After.class);
			}
		}

		after();
	}

	protected void after() {
		this.rhinoContext.exit();
	}

	private Object createTestClassInstance() {
		try {
			return testClass.newInstance();
		} catch (final Exception ex) {
			throw new RuntimeException(
					"Unable to create a new instance of testClass "
							+ testClass.getSimpleName()
							+ " using a no-arg constructor", ex);
		}
	}

	private void fireMethodsWithSpecifiedAnnotationIfAny(
			final Object testClassInstance,
			final Class<? extends Annotation> annotation) {
		for (final Method method : testClass.getMethods()) {

			try {
				if (method.getAnnotation(annotation) != null) {
					method.setAccessible(true);
					final Class<?>[] parameterTypes = method
							.getParameterTypes();
					if (parameterTypes.length == 0) {
						method.invoke(testClassInstance, (Object[]) null);
					} else if (parameterTypes.length == 1
							&& RhinoContext.class
									.isAssignableFrom(parameterTypes[0])) {
						method.invoke(testClassInstance,
								new Object[] { this.rhinoContext });
					} else {
						throw new IllegalStateException(
								"Annotated method does not have zero or rhinoContext as parameterTypes");
					}
				}
			} catch (final Exception ex) {
				throw new RuntimeException("Exception while firing "
						+ annotation.getSimpleName() + " method: "
						+ method.getName(), ex);
			}
		}
	}

	private void generateSpecRunnerIfNeeded() {
		if (suiteAnnotation.generateSpecRunner()) {
			final String[] jasmineSpecs = getJasmineSpecs(suiteAnnotation);
			new JasmineSpecRunnerGenerator(jasmineSpecs, suiteAnnotation,
					suiteAnnotation.runnersDir(), testClass.getSimpleName()
							+ "Runner.html").generate();
		}
	}

	private void reportSpecResultToNotifier(final RunNotifier notifier,
			final JasmineSpec spec) {
		if (spec.isPassed(rhinoContext)) {
			notifier.fireTestFinished(spec.getDescription());
		} else if (spec.isFailed(rhinoContext)) {
			notifier.fireTestFailure(spec.getJunitFailure(rhinoContext));
		} else {
			throw new IllegalStateException("Unexpected spec status received: "
					+ spec);
		}
	}

	private void waitALittle() {
		try {
			Thread.sleep(SLEEP_TIME_MILISECONDS);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
