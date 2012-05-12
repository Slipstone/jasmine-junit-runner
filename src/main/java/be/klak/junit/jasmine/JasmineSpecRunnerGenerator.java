package be.klak.junit.jasmine;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

class JasmineSpecRunnerGenerator {

	private enum TemplatePlaceholders {
		RELATIVE_PATH("<!--RelativePath-->"), SOURCE_FILES_TO_INCLUDE(
				"<!--SourceFileIncludes-->"), SPEC_FILES_TO_INCLUDE(
				"<!--SpecFileIncludes-->");

		private final String placeholder;

		private TemplatePlaceholders(final String placeholder) {
			this.placeholder = placeholder;
		}

		public String getPlaceholder() {
			return placeholder;
		}

	}

	private final JasmineSuite suite;
	private final String[] jasmineSpecs;
	private final String outputPath;
	private final String outputFileName;

	public JasmineSpecRunnerGenerator(final String[] jasmineSpecs,
			final JasmineSuite suite, final String outputPath,
			final String outputFileName) {
		this.jasmineSpecs = jasmineSpecs;
		this.suite = suite;
		this.outputPath = outputPath;
		this.outputFileName = outputFileName;
	}

	public void generate() {
		// TODO hardcoded relative path stuff wat configureerbaar maken
		String template = loadTemplate();
		template = replaceRelativePathsForLibs(template);
		template = template.replaceAll(
				TemplatePlaceholders.SOURCE_FILES_TO_INCLUDE.getPlaceholder(),
				getJavascriptFileIncludes("./../../../main/webapp/js",
						suite.sourceInclude()));
		template = template.replaceAll(
				TemplatePlaceholders.SPEC_FILES_TO_INCLUDE.getPlaceholder(),
				getJavascriptFileIncludes("./../specs", jasmineSpecs));

		try {
			FileUtils.writeStringToFile(new File(outputPath + "/"
					+ outputFileName), template);
		} catch (final IOException e) {
			throw new RuntimeException(
					"unable to write spec runner contents to destination", e);
		}
	}

	private String replaceRelativePathsForLibs(final String template) {
		return template.replaceAll(
				TemplatePlaceholders.RELATIVE_PATH.getPlaceholder(),
				suite.specDir());
	}

	private String getJavascriptFileIncludes(final String path,
			final String[] jsFiles) {
		final StringBuilder sourceFileIncludes = new StringBuilder();
		for (final String sourceFile : jsFiles) {
			sourceFileIncludes
					.append("\t\t<script type='text/javascript' src='" + path
							+ "/" + sourceFile + "'></script>\r\n");
		}
		return sourceFileIncludes.toString();
	}

	private String loadTemplate() {
		String template = null;
		try {
			template = IOUtils.toString(Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream("bundle/lib/specRunner.tpl"));
		} catch (final IOException e) {
			throw new RuntimeException("spec runner template file not found!",
					e);
		}
		return template;
	}
}
