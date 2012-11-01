package be.klak.rhino;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RhinoContext {
	public final static LoadingCache<String, String> BUNDLE_FILES = CacheBuilder
			.newBuilder().build(new CacheLoader<String, String>() {
				@Override
				public String load(final String key) throws Exception {
					final String filename = "bundle/" + key;
					try {
						final InputStream input = Thread.currentThread()
								.getContextClassLoader()
								.getResourceAsStream(filename);
						final File temp = File.createTempFile("rhinoLoader",
								".temp");
						temp.deleteOnExit();
						final OutputStream output = new FileOutputStream(temp);
						IOUtils.copy(input, output);
						IOUtils.closeQuietly(input);
						IOUtils.closeQuietly(output);
						return temp.getAbsolutePath();
					} catch (final Exception e) {
						System.out
								.println("Unable to find file on the classpath: "
										+ filename);
						throw e;
					}
				}
			});

	private final Context jsContext;
	private final Scriptable jsScope;

	public RhinoContext() {
		this.jsContext = createJavascriptContext();
		this.jsScope = createJavascriptScopeForContext(this.jsContext);
	}

	public RhinoContext(final Scriptable sharedScope) {
		this.jsContext = createJavascriptContext();
		final Scriptable newScope = this.jsContext.newObject(sharedScope);
		newScope.setPrototype(sharedScope);
		newScope.setParentScope(null);

		this.jsScope = newScope;
	}

	private RhinoContext createNewRhinoContextBasedOnPrevious() {
		return new RhinoContext(this.jsScope);
	}

	public void runAsync(final RhinoRunnable runnable) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final RhinoContext newRhinoContextBasedOnPrevious = createNewRhinoContextBasedOnPrevious();
				try {
					runnable.run(newRhinoContextBasedOnPrevious);
				} finally {
					newRhinoContextBasedOnPrevious.exit();
				}
			}
		}).start();
	}

	public Object evalJS(final String js) {
		return this.jsContext.evaluateString(this.jsScope, js, "script", 1,
				null);
	}

	@SuppressWarnings("unchecked")
	public <T extends ScriptableObject> T createClassInJS(
			final Class<T> classToExport) {
		exportClass(classToExport);
		final T newObj = (T) jsContext.newObject(jsScope,
				classToExport.getSimpleName());
		return newObj;
	}

	public void setProperty(final String objectToReceiveProperty,
			final String property, final Object value) {
		final Object obj = evalJS(objectToReceiveProperty);
		if (obj == null || !(obj instanceof ScriptableObject)) {
			throw new IllegalStateException(
					"object to receive property is no ScriptableObject but a "
							+ (obj == null ? "" : obj.getClass()
									.getSimpleName()));
		}

		final ScriptableObject objectToReceive = (ScriptableObject) obj;
		objectToReceive.put(property, objectToReceive, value);
	}

	private void exportClass(
			final Class<? extends ScriptableObject> classToExport) {
		try {
			ScriptableObject.defineClass(this.jsScope, classToExport);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void load(final String path, final String... jsFiles) {
		for (final String jsFile : jsFiles) {
			load(path + jsFile);
		}
	}

	public void load(final String fileName) {
		evalJS("load('" + StringEscapeUtils.escapeEcmaScript(fileName) + "')");
//		 Main.processFile(this.jsContext, this.jsScope, fileName);
	}

	public Object executeFunction(final ScriptableObject object,
			final String fnName, final Object[] arguments) {
		Object fnPointer = object.get(fnName, object);
		if (fnPointer == null || !(fnPointer instanceof Function)) {
			fnPointer = object.getPrototype().get(fnName, object);
		}

		return ((Function) fnPointer).call(jsContext, jsScope, object,
				arguments);
	}

	public Object executeFunction(final ScriptableObject object,
			final String fnName) {
		return executeFunction(object, fnName, new Object[] {});
	}

	public Context getJsContext() {
		return jsContext;
	}

	public Scriptable getJsScope() {
		return jsScope;
	}

	public void loadEnv() {
		// TODO ensure rhino 1.7R3 instead of R2 -> geen shim nodig + paths
		// gedoe in orde zetten hier
		try {
			load(BUNDLE_FILES.get("lib/es5-shim-0.0.4.min.js"));
			load(BUNDLE_FILES.get("lib/env.rhino.1.2.js"));
			load(BUNDLE_FILES.get("lib/env.utils.js"));
			load(BUNDLE_FILES.get("envJsOptions.js"));
		} catch (final ExecutionException e) {
			throw new RuntimeException("Failed to load environment", e);
		}

	}

	private Global createJavascriptScopeForContext(final Context jsContext) {
		try {
			final Field scriptCache = Main.class
					.getDeclaredField("scriptCache");
			scriptCache.setAccessible(true);
			((Map<?, ?>) scriptCache.get(null)).clear();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final Global scope = new Global();
		scope.init(jsContext);
		return scope;
	}

	private Context createJavascriptContext() {
		final Context jsContext = Context.enter();
		jsContext.setOptimizationLevel(-1);
		jsContext.setLanguageVersion(Context.VERSION_1_5); // TODO 1.8 plx
		jsContext.setErrorReporter(new ChainedErrorReporter(jsContext
				.getErrorReporter()));
		return jsContext;
	}

	public void exit() {
		Context.exit();
	}

}
