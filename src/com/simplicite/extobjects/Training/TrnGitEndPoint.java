package com.simplicite.extobjects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.webapp.services.RESTServiceExternalObject ;

import java.io.IOException;
import java.io.File;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.Git.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.api.errors.*;

/**
 * External object TrnGitEndPoint
 */
public class TrnGitEndPoint extends RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object post(Parameters params) {
		get(params);
		return null;
	}
	
	@Override
	public Object get(Parameters params) {
		AppLog.info(getClass(), "get", "Training Git Endpoint was hit by the git repository", getGrant());
		String localRepoPath = getGrant().getParameter("TRN_LOCAL_REPO_PATH");
		if (isRepositoryCloned(localRepoPath)) {
			AppLog.info(getClass(), "get", "GIT REPOSITORY WAS FOUND IN : " + localRepoPath, getGrant());
			//pullTrainingContentInformation(localRepoPath);
			//updateDatabaseWithNewContent();
		} else {
			AppLog.info(getClass(), "get", "TRAINING CONTENT REPOSITORY IS NOT CLONED IN : " + localRepoPath , getGrant());
			//cloneTrainingRepository(localRepoPath);
			//importAllContent();
		}
		
		return "Training endpoint was hit";
	}
	
	private boolean isRepositoryCloned(String repositoryPath) {
		if (!new File(repositoryPath).isDirectory()) return false;
		return FileKey.isGitRepository(new File(repositoryPath), FS.DETECTED);
	}
	
	private boolean cloneTrainingRepository(String repositoryPath) {
		try {
			Git git = Git.cloneRepository()
				// .setURI("mbelgaid@git.simplicite.io:/var/git/misc/training-content.git")
				.setURI("https://github.com/maxbelgaid/fake-content.git")
				.setDirectory(new File(repositoryPath))
				.call();
			return true;
		} catch(GitAPIException e) {
			AppLog.error(getClass(), "cloneTrainingRepository", "GitAPIException while cloning the repository", e, getGrant());
			return false;
		}
	}
	
	private void addSshCredentials(){
		
	}
	
	private void pullTrainingContentInformation(String repositoryPath) {
		// https://download.eclipse.org/jgit/site/5.8.0.202006091008-r/apidocs/org/eclipse/jgit/api/Git.html
		try {
			FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
			Repository repository = repositoryBuilder.setGitDir(new File(repositoryPath + "/.git"))
			                .readEnvironment() // scan environment GIT_* variables
			                .findGitDir() // scan up the file system tree
			                .setMustExist(true)
			                .build();
			new Git(repository).pull().call();
		} catch(GitAPIException e) {
			AppLog.error(getClass(), "pullTrainingContentInformation", "GitAPIException while pulling latest commit", e, getGrant());
		} catch(IOException e) {
			AppLog.error(getClass(), "pullTrainingContentInformation", "IOException while pulling latest commit", e, getGrant());
		}
		
	}
	
	private void updateDatabaseWithNewContent() {
		
	}
	
	private void determineLessonsToUpdate() {
		
	}
	
	private void importAllContent() {
		
	}
}
