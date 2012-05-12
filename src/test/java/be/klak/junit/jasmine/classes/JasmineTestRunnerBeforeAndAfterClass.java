package be.klak.junit.jasmine.classes;

import static junit.framework.Assert.*;
import static org.fest.assertions.Assertions.*;

import org.junit.After;
import org.junit.Before;

import be.klak.junit.jasmine.JasmineSuite;
import be.klak.rhino.RhinoContext;

@JasmineSuite(specInclude = { "emptySpec.js" })
public class JasmineTestRunnerBeforeAndAfterClass extends
		JasmineTestRunnerBeforeAndAfterSuperClass {

	@Before
	public void runMij() {
		assertThat(runs).isEmpty();
		runs.add(RUN_MIJ_FLAG);
	}

	@After
	public void runMijAfter() {
		assertThat(runs).containsOnly(RUN_MIJ_FLAG, RUN_MIJ_OOK_FLAG);
		runs.remove((Object) RUN_MIJ_FLAG);
	}

	@After
	public void runMijAfterOok(final RhinoContext context) {
		assertThat(runs).containsOnly(RUN_MIJ_OOK_FLAG);
		runs.remove((Object) RUN_MIJ_OOK_FLAG);
	}

	public void runMijNiet() {
		fail("should not be run");
	}
}
