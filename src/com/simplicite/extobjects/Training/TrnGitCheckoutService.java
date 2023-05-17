package com.simplicite.extobjects.Training;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONObject;

import com.simplicite.commons.Training.TrnConfigException;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.engine.Platform;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnGitCheckoutService
 */
public class TrnGitCheckoutService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object post(Parameters params) {
        try {
            String gitUrl = TrnTools.getGitUrl();
            String branch = TrnTools.getGitBranch();
            File localDir = new File("/home/trainingrec/test-content");
            AppLog.info(Platform.getContentDir(), getGrant());
            if (!localDir.exists()) {
                return performClone(gitUrl, branch, localDir);
            } else {
                return performPull(gitUrl, branch, localDir);
            }
        } catch (IOException | GitAPIException | TrnConfigException e) {
            AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
            return "ERROR : " + e.getMessage();
        }
	}

    private static String performClone(String remoteUrl, String branch, File localDir) throws IOException, GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(remoteUrl);
        cloneCommand.setDirectory(localDir);
        cloneCommand.setCloneAllBranches(false);
        cloneCommand.setBranch(branch); // Replace with the branch you want to clone
        cloneCommand.setCloneSubmodules(false);
        cloneCommand.setDepth(1);

        try (Git git = cloneCommand.call()) {
            return "Shallow clone completed successfully.";
        }
    }

    private static String performPull(String remoteUrl, String branch, File localDir) throws IOException, GitAPIException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(localDir, ".git"))
                                                .readEnvironment() // Read environment variables like GIT_DIR
                                                .findGitDir() // Find the Git repository
                                                .build();

        try (Git git = new Git(repository)) {
            PullCommand pullCommand = git.pull();
            pullCommand.setRemote("origin");
            pullCommand.setRemoteBranchName(branch); // Change branch name if needed

            pullCommand.call();

            return "Pull completed successfully.";
        }
    }

    @Override
    public Object del(Parameters params) {
        try {
            File repositoryDir = new File("/home/trainingrec/test-content");
            if (repositoryDir.exists()) {
                return deleteRepository(repositoryDir);
            } else {
                return "Repository directory does not exist.";
            }
        } catch (IOException e) {
            return "ERROR : " + e.getMessage();
        }
    }

    private static String deleteRepository(File repositoryDir) throws IOException {
        FileUtils.delete(repositoryDir, FileUtils.RECURSIVE);
        return "Repository deleted successfully.";
    }
}