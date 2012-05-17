package be.klak.junit.jasmine;

import static org.fest.assertions.Assertions.*;

import org.junit.Test;
import org.junit.runner.Description;

public class DescriptionsRecursiveTreeInRunnerTest {

	@JasmineSuite(specInclude = { "recursiveSpec.js" })
	private class RecursiveTreeTest {
	}

	@Test
	public void buildDescriptionsRecursively() {
		final Description baseTestDescription = new JasmineTestRunner(
				RecursiveTreeTest.class).getDescription();
		assertThat(baseTestDescription.getDisplayName()).contains(
				RecursiveTreeTest.class.getSimpleName());

		assertThat(baseTestDescription.getChildren()).hasSize(2);
		final Description root = baseTestDescription.getChildren().get(0);
		assertThat(root.getDisplayName()).isEqualTo("root");
		assertThat(root.getChildren()).hasSize(3);

		assertThat(root.getChildren().get(0).getDisplayName()).startsWith(
				"rootTest");
		assertChild1AndChildren(root);
		assertChild2AndChildren(root);

		final Description root2 = baseTestDescription.getChildren().get(1);
		assertThat(root2.getDisplayName()).isEqualTo("root2");
		assertThat(root2.getChildren()).hasSize(1);

		assertThat(root2.getChildren().get(0).getDisplayName()).startsWith(
				"root2Test");
	}

	private void assertChild2AndChildren(final Description root) {
		final Description child2 = root.getChildren().get(2);
		assertThat(child2.getDisplayName()).isEqualTo("child2");

		assertThat(child2.getChildren()).hasSize(1);
		assertThat(child2.getChildren().get(0).getDisplayName()).startsWith(
				"child2Test");
	}

	private void assertChild1AndChildren(final Description root) {
		final Description child1 = root.getChildren().get(1);
		assertThat(child1.getDisplayName()).isEqualTo("child1");

		assertThat(child1.getChildren()).hasSize(2);
		assertThat(child1.getChildren().get(0).getDisplayName()).startsWith(
				"child1Test");
		final Description grandchild = child1.getChildren().get(1);
		assertThat(grandchild.getDisplayName()).isEqualTo("grandchild");

		assertThat(grandchild.getChildren()).hasSize(1);
		assertThat(grandchild.getChildren().get(0).getDisplayName())
				.startsWith("grandchildTest");
	}

}
