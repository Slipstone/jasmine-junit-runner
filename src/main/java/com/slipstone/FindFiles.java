package com.slipstone;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 * @author kbranton
 * 
 */
public class FindFiles {
	public static String[] findFiles(final String folder,
			final String[] includes, final String[] excludes) {
		return findFiles(folder, convertToPattern(includes),
				convertToPattern(excludes));
	}

	public static String[] findFiles(final String folder,
			final Pattern[] includes, final Pattern[] excludes) {
		if (includes.length == 0 && excludes.length == 0) {
			return new String[0];
		}
		final List<String> results = Lists.newArrayList();
		final File start = new File(folder);
		addFiles(results, start.getAbsolutePath().length() + 1, start);
		for (final Iterator<String> i = results.iterator(); i.hasNext();) {
			final String filename = i.next();
			if (!matches(filename, includes) || matches(filename, excludes)) {
				i.remove();
			}
		}
		final String[] finalResult = results
				.toArray(new String[results.size()]);
		Arrays.sort(finalResult);
		return finalResult;
	}

	private static void addFiles(final List<String> results,
			final int prefixLength, final File current) {
		for (final File file : current.listFiles()) {
			if (file.isDirectory()) {
				addFiles(results, prefixLength, file);
			} else {
				results.add(file.getAbsolutePath().substring(prefixLength).replace('\\', '/'));
			}
		}
	}

	private static boolean matches(final String filename,
			final Pattern[] patterns) {
		for (final Pattern pattern : patterns) {
			if (pattern.matcher(filename).matches()) {
				return true;
			}
		}
		return false;
	}

	private static Pattern[] convertToPattern(final String[] patterns) {
		final Pattern[] results = new Pattern[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			results[i] = Pattern.compile(StringUtils.replace(StringUtils
					.replace(escRegEx(patterns[i]), "\\*\\*/", "(.*/)?"),
					"\\*", "[^/]*"));
		}
		return results;
	}

	private static String escRegEx(final String inStr) {
		return inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1");
	}
}
