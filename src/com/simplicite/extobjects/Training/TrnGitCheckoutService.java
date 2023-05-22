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
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.CredentialItem.Username;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.TransportCommand;
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
            String branch = TrnTools.getGitBranch();
            File localDir = getContentDir();
            if (!localDir.exists()) {
                return performClone(TrnTools.getGitUrl(), branch, localDir);
            } else {
                return performPull(branch, localDir);
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
        cloneCommand.setBranch(branch);
        cloneCommand.setCloneSubmodules(false);
        cloneCommand.setDepth(1);
        gitAuthentication(cloneCommand);

        try (Git git = cloneCommand.call()) {
            return "Shallow clone completed successfully.";
        }
    }

    private static String performPull(String branch, File localDir) throws IOException, GitAPIException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(localDir, ".git"))
                                                .readEnvironment()
                                                .findGitDir()
                                                .build();

        try (Git git = new Git(repository)) {
            PullCommand pullCommand = git.pull();
            pullCommand.setRemote("origin");
            pullCommand.setRemoteBranchName(branch);
            gitAuthentication(pullCommand);

            pullCommand.call();

            return "Pull completed successfully.";
        }
    }

    private static <T extends TransportCommand<?, ?>> void gitAuthentication(T command) {
        JSONObject credentials = TrnTools.getGitCredentials();
        if(credentials!=null) {
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                credentials.getString("username"), credentials.getString("token"));
            command.setCredentialsProvider(credentialsProvider);
        }
    }

    @Override
    public Object del(Parameters params) {
        try {
            File repositoryDir = getContentDir();
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

    private File getContentDir() {
        return new File(Platform.getContentDir()+"/test-content");
    }
}