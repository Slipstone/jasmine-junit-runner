package be.klak.junit.jasmine.classes;

import be.klak.junit.jasmine.JasmineSuite;

@JasmineSuite(specInclude = { "spec1.js", "spec2.js" }, sourceInclude = {
		"source1.js", "source2.js" }, sourceDir = "src/test/javascript/sources/")
public class JasmineSuiteGeneratorClassWithoutRunner {

}
