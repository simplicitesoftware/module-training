package com.simplicite.commons.Training;

import java.io.File;
import java.io.FileInputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.codec.digest.DigestUtils;

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
      newHash = hashDirectoryFiles(dir);
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
    } catch (Exception e) {
      AppLog.error(getClass(), "deleteStore", e.getMessage(), e, Grant.getSystemAdmin());
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

  // Non-recursive version of https://stackoverflow.com/a/46899517/1612642 =>
  // ignores directories
  private static String hashDirectoryFiles(File file) throws java.io.IOException {
    Vector<FileInputStream> fileStreams = new Vector<>();

    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File child : files)
          if (!child.isDirectory())
            fileStreams.add(new FileInputStream(child));
      }
    } else {
      fileStreams.add(new FileInputStream(file));
    }

    try (SequenceInputStream sequenceInputStream = new SequenceInputStream(fileStreams.elements())) {
      return DigestUtils.md5Hex(sequenceInputStream);
    }
  }

  private boolean isTrainingDbEmpty() {
    return 0 == (int) g.simpleQueryAsDouble("select count(*) from trn_category");
  }

}
