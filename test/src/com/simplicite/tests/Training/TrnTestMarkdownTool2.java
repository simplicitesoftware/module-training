package com.simplicite.tests.Training;

import java.util.*;

import com.simplicite.commons.Training.MarkdownTool2;
import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests TrnTestMarkdownTool2
 */
public class TrnTestMarkdownTool2 {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String ENTRY_VALID_VALUES = "# My Title {#my-title}".concat(NEW_LINE)
	.concat("## My Title {#my-title}").concat(NEW_LINE)
	.concat("### My Title {#my-title}").concat(NEW_LINE)
	.concat("#### My Title {#my-title}").concat(NEW_LINE)
	.concat("##### My Title {#my-title}").concat(NEW_LINE)
	.concat("###### My Title {#my-title}").concat(NEW_LINE)
	.concat("My Title {#my-title}").concat(NEW_LINE)
	.concat("====================").concat(NEW_LINE)
	.concat("My Title {#my-title}").concat(NEW_LINE)
	.concat("---------------------------").concat(NEW_LINE);

	private static final String EXPECTED_VALID_VALUES = "<h1 id=\"my-title\">My Title</h1>".concat(NEW_LINE)
	.concat("<h2 id=\"my-title\">My Title</h2>").concat(NEW_LINE)
	.concat("<h3 id=\"my-title\">My Title</h3>").concat(NEW_LINE)
	.concat("<h4 id=\"my-title\">My Title</h4>").concat(NEW_LINE)
	.concat("<h5 id=\"my-title\">My Title</h5>").concat(NEW_LINE)
	.concat("<h6 id=\"my-title\">My Title</h6>").concat(NEW_LINE)
	.concat("<h1 id=\"my-title\">My Title</h1>").concat(NEW_LINE)
	.concat("<h2 id=\"my-title\">My Title</h2>").concat(NEW_LINE);
	
	// should remain the same after process
	private static final String ENTRY_UNVALID_VALUES = "My Title {#my title}";

	@Test
	public void testValidValues() {
		try {
			String html = MarkdownTool2.toHTMLWithAnchors(ENTRY_VALID_VALUES);
			String test = EXPECTED_VALID_VALUES;
			assertEquals(EXPECTED_VALID_VALUES, html);
		} catch (Exception e) {
			fail(e.getMessage());
			AppLog.error(e, Grant.getSystemAdmin());
		}
	}

	@Test
	public void testUnvalidValues() {
		try {
			String html = MarkdownTool2.toHTMLWithAnchors(ENTRY_UNVALID_VALUES);
			assertEquals(ENTRY_UNVALID_VALUES, html);
		} catch (Exception e) {
			fail(e.getMessage());
			AppLog.debug(e.getMessage());
		}
	}
}
