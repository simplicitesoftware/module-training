package com.simplicite.tests.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.engine.Platform;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.simplicite.commons.Training.TrnFsSyncTool;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.commons.Training.TrnEsiHelper;
/**
 * Unit tests TrnTests
 */
public class TrnTests {
	
	@Test
	public void testCheckout() throws InterruptedException{
			String command = "git clone --filter=blob:none https://github.com/simplicitesoftware/module-pizzeria.git ../../test-git";
            SystemTool.ExecResult res = SystemTool.exec("git clone --filter=blob:none https://github.com/simplicitesoftware/module-pizzeria.git" , null, Platform.getTmpDir());
            AppLog.info(res.getStdoutAsString(), Grant.getSystemAdmin());
	}
	
	/*@Test
	public void testSync() {
		try {
            if(TrnTools.isFileSystemMode()) {
                TrnFsSyncTool.dropDbData();
                TrnFsSyncTool.deleteStore();
                TrnFsSyncTool.triggerSync();
            }
            if(TrnTools.isElasticSearchMode()) {
                TrnEsiHelper eh = TrnEsiHelper.getEsHelper(Grant.getSystemAdmin());
                eh.deleteIndex();
                eh.createIndex();
            }
		} catch (Exception e) {
			AppLog.error(getClass(), "testSync", e.getMessage(), e, Grant.getSystemAdmin());
			fail(e.getMessage());
		}
	}*/
	
	/*@Test
	public void testDockerVolume() {
		try {
			AppLog.info(FileTool.readFile("/usr/local/training-content/glo.ser"), Grant.getSystemAdmin());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}*/
}
