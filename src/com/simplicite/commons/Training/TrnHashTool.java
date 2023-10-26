package com.simplicite.commons.Training;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.simplicite.objects.Training.TrnSyncSupervisor;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.Tool;

/**
 * Shared code TrnHashTool
 */
public class TrnHashTool implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private static final String HASHSTORE_FILENAME = "glo.ser";
	private HashMap<String, String> hashStore;
	private final File hashStoreFile;

	Grant g;

	public TrnHashTool(Grant g, String baseContentDir) {
		this.g = g;
		hashStoreFile = new File(baseContentDir, HASHSTORE_FILENAME);
	}

	public void loadHashes() throws TrnSyncException {
		if (!hashStoreFile.exists() && !isTrainingDbEmpty())
			throw new TrnSyncException("TRN_SYNC_NO_HASHSTORE",
					"The glo.ser file could not be found. Please empty the training objects manually before syncronization with filesystem.");
		else if (!hashStoreFile.exists()) {
			hashStore = new HashMap<>();
		} else {
			hashStore = (HashMap<String, String>) Tool.fileToObject(hashStoreFile);
		}
	}

	// compares hashes from the last successfull synchronization with the current
	// content to synchronize
	// and returns the calculated hash if no hash has been found in glo.ser, or if
	// old hash is different from the new hash
	// returns null if the hashes are the same (no modification of docs content)
	public String getNewHash(String relativePath, File dir) throws TrnSyncException {
		String oldHash = hashStore.get(relativePath);
		String newHash = "";
		try {
			newHash = calculateHash(dir);
		} catch (Exception e) {
			throw new TrnSyncException("TRN_SYNC_ERROR_HASHING_FILES", relativePath);
		}

		if (oldHash == null || !oldHash.equals(newHash)) {
			return newHash;
		} else {
			return null;
		}
	}

	public void deleteHashStore() throws TrnSyncException {
		try {
			Files.deleteIfExists(hashStoreFile.toPath());
            AppLog.info("HashStore deleted", Grant.getSystemAdmin());
		} catch (Exception e) {
			AppLog.error("Unable to delete store: "+e.getMessage(), e, Grant.getSystemAdmin());
			throw new TrnSyncException("TRN_SYNC_DELETE_STORE", hashStoreFile);
		}
	}

	public void storeHashes() {
		Tool.objectToFile(hashStore, hashStoreFile);
	}

	public Set<String> getStoreKeySet() {
		return hashStore.keySet();
	}

	public void addPathToStore(String path, String newHash) {
		hashStore.put(path, newHash);
	}

	public void removePathFromStore(String path) {
		hashStore.remove(path);
	}

    public void clearAllPath(ArrayList<String> pathList) {
        for(String path : pathList) {
            removePathFromStore(path);
        }
    }

	private static String calculateHash(File fileOrFolder) throws IOException {
		String contentToHash = getFolderContents(fileOrFolder);
		return DigestUtils.md5Hex(contentToHash);
	}

	// create a String from the content of a given folder / file
	private static String getFolderContents(File fileOrFolder) throws IOException {
		StringBuilder contentToHash = new StringBuilder();
		if (fileOrFolder.isFile()) {
			contentToHash.append(readFileContents(fileOrFolder.toPath()));
		} else if (fileOrFolder.isDirectory()) {
			File[] files = fileOrFolder.listFiles();
			if (files != null) {
				for (File file : files) {
					if(file.isFile()) {
						contentToHash.append(readFileContents(file.toPath()));
					} else if(file.isDirectory()) {
						contentToHash.append(file.getName());
					}
				}
			}
		}
		return contentToHash.toString();
	}

	private static String readFileContents(Path path) throws IOException {
		byte[] bytes = Files.readAllBytes(path);
		return new String(bytes);
	}

	private boolean isTrainingDbEmpty() {
		return 0 == (int) g.simpleQueryAsDouble("select count(*) from trn_category");
	}

}
