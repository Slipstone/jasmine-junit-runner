package be.klak.junit.jasmine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JasmineSuite {
	/**
	 * The location of the jasmine specs
	 */
	String specDir() default "src/test/javascript/specs";

	/**
	 * A list of ant-style filters of specs to include
	 */
	String[] specInclude() default {};

	/**
	 * A list of ant-style filters of specs to exclude
	 */
	String[] specExclude() default {};

	/**
	 * The location of the source javascript under test
	 */
	String sourceDir() default "src/main/webapp/js";

	/**
	 * A list of ant-style filters of sources to include
	 */
	String[] sourceInclude() default {};

	/**
	 * A list of ant-style filters of sources to exclude
	 */
	String[] sourceExclude() default {};

	/**
	 * The location of mocks to be loaded before the source under test
	 */
	String mockDir() default "src/test/javascript/mocks";

	/**
	 * A list of ant-style filters of mocks to include
	 */
	String[] mockInclude() default {};

	/**
	 * A list of ant-style filters of mocks to exclude
	 */
	String[] mockExclude() default {};

	/**
	 * Not sure what this does
	 */
	boolean generateSpecRunner() default false;

	/**
	 * The target location of the jasmine runners
	 */
	String runnersDir() default "src/test/javascript/runners";

	/**
	 * Enable rhino debug mode
	 */
	boolean debug() default false;
}
