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
    private static final String CONTENT_FOLDER_NAME = "content";

	@Override
	public Object post(Parameters params) {
        try {
            String branch = TrnTools.getGitBranch();
            File contentDir = getContentDir();
            if (!contentDir.exists()) {
                return performClone(TrnTools.getGitUrl(), branch, contentDir);
            } else {
                return performPull(branch, contentDir);
            }
        } catch (IOException | GitAPIException | TrnConfigException e) {
            AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
            return "ERROR : " + e.getMessage();
        }
	}

    private String performClone(String remoteUrl, String branch, File contentDir) throws IOException, GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(remoteUrl);
        cloneCommand.setDirectory(contentDir);
        cloneCommand.setCloneAllBranches(false);
        cloneCommand.setBranch(branch);
        cloneCommand.setCloneSubmodules(false);
        cloneCommand.setDepth(1);
        gitAuthentication(cloneCommand);

        try (Git git = cloneCommand.call()) {
            return "Shallow clone completed successfully.";
        }
    }

    private String performPull(String branch, File contentDir) throws IOException, GitAPIException, TrnConfigException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(contentDir, ".git"))
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
        } catch(GitAPIException e) {
            // improve log ??
            AppLog.warning(getClass(), "performPull", 
                "Unable to pull. The pull command fallback will try to delete and clone back the repository", e, getGrant());
            return pullCommandFallback(TrnTools.getGitUrl(), branch, contentDir); 
        }
    }

    private String pullCommandFallback(String remoteUrl, String branch, File contentDir) throws IOException, GitAPIException {
        deleteRepository();
        performClone(remoteUrl, branch, contentDir);
        return "The pull command has failed probably due to a conflict between your local and remote content. The pull command fallback has deleted and cloned back the repository";
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
            return deleteRepository();
        } catch (IOException e) {
            return "ERROR : " + e.getMessage();
        }
    }

    private String deleteRepository() throws IOException {
        File repositoryDir = getContentDir();
        if (repositoryDir.exists()) {
            FileUtils.delete(repositoryDir, FileUtils.RECURSIVE);
            return "Repository deleted successfully.";
        } else {
            return "Repository directory does not exist.";
        }
    }

    private File getContentDir() {
        return new File(Platform.getContentDir(), CONTENT_FOLDER_NAME);
    }
}