package com.simplicite.tests.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.simplicite.commons.Training.TrnFsSyncTool;
/**
 * Unit tests TrnTests
 */
public class TrnTests {
	@Test
	public void testSync() {
		try {
			TrnFsSyncTool.dropDbData();
			TrnFsSyncTool.deleteStore();
			TrnFsSyncTool.triggerSync();
		} catch (Exception e) {
			AppLog.error(getClass(), "testSync", e.getMessage(), e, Grant.getSystemAdmin());
			fail(e.getMessage());
		}
	}
	
	/*@Test
	public void testDockerVolume() {
		try {
			AppLog.info(FileTool.readFile("/usr/local/training-content/glo.ser"), Grant.getSystemAdmin());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}*/
}
