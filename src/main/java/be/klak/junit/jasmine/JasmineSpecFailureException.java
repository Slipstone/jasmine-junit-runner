package be.klak.junit.jasmine;

import org.mozilla.javascript.NativeObject;

class JasmineSpecFailureException extends Exception {
	private static final long serialVersionUID = 1L;

	private final String message;

	public JasmineSpecFailureException(final NativeObject specResultItem) {
		this.message = (String) specResultItem.get("message", specResultItem);
	}

	@Override
	public String getMessage() {
		return message;
	}

}
