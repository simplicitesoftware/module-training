package com.simplicite.extobjects.Training;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONObject;

import com.simplicite.commons.Training.TrnConfigException;
import com.simplicite.commons.Training.TrnFsSyncTool;
import com.simplicite.commons.Training.TrnSyncException;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.*;
import com.simplicite.util.engine.Platform;
import com.simplicite.util.tools.Parameters;

/**
 * External object TrnGitCheckoutService
 */
public class TrnGitCheckoutService extends com.simplicite.webapp.services.RESTServiceExternalObject {
  private static final long serialVersionUID = 1L;
  private static final String CONTENT_FOLDER_NAME = "docs";
  private UsernamePasswordCredentialsProvider credentials;

  @Override
  public Object post(Parameters params) {
    try {
      String branch = TrnTools.getGitBranch();
      File contentDir = getContentDir();
      String msg;
      setUsernameTokenCredentials();
      if (!contentDir.exists()) {
        String gitUrl = TrnTools.getGitUrl();
        performClone(gitUrl, branch, contentDir);
        msg = "Result: " + gitUrl + " has been successfully cloned. Current branch: " + branch + ".";
      } else {
        performPull(branch, contentDir);
        msg = "The content has been successfully updated. Current branch: " + branch + ".";
      }
      TrnFsSyncTool.triggerSync();
      return msg;
    } catch (IOException | GitAPIException | TrnConfigException | TrnSyncException e) {
      setHTTPStatus(500);
      AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
      return "ERROR : " + e.getMessage();
    }
  }

  private void performClone(String remoteUrl, String branch, File contentDir) throws GitAPIException {
    AppLog.info("Attempting to clone " + remoteUrl + " on branch " + branch, getGrant());
    CloneCommand cloneCommand = Git.cloneRepository()
        .setURI(remoteUrl)
        .setDirectory(contentDir)
        .setCloneAllBranches(false)
        .setBranch(branch)
        .setCloneSubmodules(false)
        .setDepth(1)
        .setCredentialsProvider(credentials);

    try (Git git = cloneCommand.call()) {
      AppLog.info("Shallow clone completed successfully.", getGrant());
    }
  }

  private void performPull(String branch, File contentDir) throws IOException, GitAPIException, TrnConfigException {
    AppLog.info("Attempting to pull on branch " + branch, getGrant());
    FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
    Repository repository = repositoryBuilder.setGitDir(new File(contentDir, ".git"))
        .readEnvironment()
        .findGitDir()
        .build();

    try (Git git = new Git(repository)) {

      git.fetch()
          .setRemote("origin")
          // map all the local branches with their remote tracking branches
          .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
          .setCredentialsProvider(credentials)
          .call();
      AppLog.info("Successfully fetched from remote repository", getGrant());

      if (branchExistsLocally(git, branch)) {
        // Switch to the existing local branch
        git.checkout()
            .setName(branch)
            .call();
        AppLog.info("Checkout on existing branch: " + branch, getGrant());
      } else {
        // Create a new branch from the remote branch
        git.checkout()
            .setCreateBranch(true)
            .setName(branch)
            .setStartPoint("origin/" + branch)
            .call();
        AppLog.info("Checkout on new branch: " + branch, getGrant());
      }

      git.pull()
          .setRemote("origin")
          .setRemoteBranchName(branch)
          .setCredentialsProvider(credentials)
          .setContentMergeStrategy(ContentMergeStrategy.THEIRS)
          .call();
      AppLog.info("Remote content has been pulled", getGrant());

      AppLog.info("Checkout done.", getGrant());
    } catch (GitAPIException e) {
      AppLog.warning(getClass(), "performPull",
          "Unable to pull. The pull command fallback will try to delete and clone back the repository", e, getGrant());
      pullCommandFallback(TrnTools.getGitUrl(), branch, contentDir);
    }
  }

  private boolean branchExistsLocally(Git git, String branch) throws GitAPIException {
    List<Ref> branches = git.branchList().call();
    boolean branchExistsLocally = false;
    for (Ref ref : branches) {
      if (ref.getName().equals("refs/heads/" + branch)) {
        branchExistsLocally = true;
        break;
      }
    }
    return branchExistsLocally;
  }

  private void pullCommandFallback(String remoteUrl, String branch, File contentDir)
      throws IOException, GitAPIException {
    deleteRepository();
    performClone(remoteUrl, branch, contentDir);
    AppLog.info("The pull command fallback has deleted and cloned back the repository", getGrant());
  }

  private void setUsernameTokenCredentials() throws TrnConfigException {
    JSONObject jsonCreds = TrnTools.getGitCredentials();
    credentials = new UsernamePasswordCredentialsProvider(
    	jsonCreds.getString("username"),
        jsonCreds.getString("token")
    );
    
    // quickfix use ssh  
    credentials = null; 
	SshdSessionFactory sshSessionFactory = new org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder()
	        .setPreferredAuthentications("publickey")
	        .setHomeDirectory(org.eclipse.jgit.util.FS.DETECTED.userHome())
	        .setSshDirectory(new File(org.eclipse.jgit.util.FS.DETECTED.userHome(), "/.ssh"))
	        .build(null);
  }

  @Override
  public Object del(Parameters params) {
    try {
      return deleteRepository();
    } catch (IOException e) {
      return "ERROR : " + e.getMessage();
    }
  }

  private String deleteRepository() throws IOException {
    File repositoryDir = getContentDir();
    String msg;
    if (repositoryDir.exists()) {
      FileUtils.delete(repositoryDir, FileUtils.RECURSIVE);
      msg = "Repository deleted successfully.";
      AppLog.info(msg, getGrant());
      return msg;
    } else {
      msg = "Repository directory does not exist.";
      AppLog.info(msg, getGrant());
      return msg;
    }
  }

  private File getContentDir() {
    return new File(Platform.getContentDir(), CONTENT_FOLDER_NAME);
  }
}