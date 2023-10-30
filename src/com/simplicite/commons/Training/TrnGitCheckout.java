package com.simplicite.commons.Training;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.json.JSONObject;

import com.simplicite.objects.Training.TrnSyncSupervisor;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.engine.Platform;

/**
 * Shared code TrnGitCheckout
 */
public class TrnGitCheckout implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
    private static final String CONTENT_FOLDER_NAME = "docs";
    private static UsernamePasswordCredentialsProvider credentialsProvider;
    private Grant g;

    public TrnGitCheckout(Grant g) {
        this.g = g;
    }

	public String checkout() throws Exception {
        File contentDir = getContentDir();
        try {
            String branch = TrnTools.getGitBranch();
           
            AppLog.info("Starting git checkout process on branch: "+branch+" on directory: "
                +contentDir.getAbsolutePath(), g);
            String msg;
            setAuthentication();
            if (!contentDir.exists()) {
                String gitUrl = TrnTools.getGitUrl();
                performClone(gitUrl, branch, contentDir);
                msg = "Result: " + gitUrl + " has been successfully cloned. Current branch: " + branch + ".";
            } else {
                performPull(branch, contentDir);
                msg = "The content has been successfully updated. Current branch: " + branch + ".";
            }
            TrnFsSyncTool.triggerSyncFromCheckout();
            TrnSyncSupervisor.logSync(true, "GIT", null, getLatestCommitInfo(contentDir));
            return msg;
        } catch(Exception e) {
            TrnSyncSupervisor.logSync(false, "GIT", "Unable to check out: " + e.getMessage(), getLatestCommitInfo(contentDir));
            throw new Exception(e);
        }
    }

    private void performClone(String remoteUrl, String branch, File contentDir) throws GitAPIException {
		AppLog.info("Attempting to clone " + remoteUrl + " on branch " + branch, g);
		CloneCommand cloneCommand = Git.cloneRepository()
				.setURI(remoteUrl)
				.setDirectory(contentDir)
				.setCloneAllBranches(false)
				.setBranch(branch)
				.setCloneSubmodules(false)
				.setDepth(1)
				.setCredentialsProvider(credentialsProvider);

		try (Git git = cloneCommand.call()) {
			AppLog.info("Shallow clone completed successfully.", g);
		}
	}

	private void performPull(String branch, File contentDir) throws IOException, GitAPIException, TrnConfigException {
		AppLog.info("Attempting to pull on branch " + branch, g);
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
					.setCredentialsProvider(credentialsProvider)
					.call();
			AppLog.info("Successfully fetched from remote repository", g);

			if (branchExistsLocally(git, branch)) {
				// Switch to the existing local branch
				git.checkout()
						.setName(branch)
						.call();
				AppLog.info("Checkout on existing branch: " + branch, g);
			} else {
				// Create a new branch from the remote branch
				git.checkout()
						.setCreateBranch(true)
						.setName(branch)
						.setStartPoint("origin/" + branch)
						.call();
				AppLog.info("Checkout on new branch: " + branch, g);
			}

			git.pull()
					.setRemote("origin")
					.setRemoteBranchName(branch)
					.setCredentialsProvider(credentialsProvider)
					.setContentMergeStrategy(ContentMergeStrategy.THEIRS)
					.call();
			AppLog.info("Remote content has been pulled", g);

			AppLog.info("Checkout done.", g);
		} catch (GitAPIException e) {
			AppLog.warning("Unable to pull. The pull command fallback will try to delete and clone back the repository", e, g);
			pullCommandFallback(TrnTools.getGitUrl(), branch, contentDir);
		}
	}

	private static boolean branchExistsLocally(Git git, String branch) throws GitAPIException {
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
		AppLog.info("The pull command fallback has deleted and cloned back the repository", g);
	}

    private String getLatestCommitInfo(File repoPath) 
    {
        String commitId = "no commit id";
        try (Git git = Git.open(repoPath)) {
            Repository repository = git.getRepository();
            try (RevWalk revWalk = new RevWalk(repository)) {
                Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
                for (RevCommit commit : commits) {
                    // The latest commit ID
                    commitId = commit.getId().getName();
                }
            }
        } catch (IOException | GitAPIException e) {
            AppLog.warning(getClass(), "getLatestCommitInfo", "Unable to get latest commit info", e, g);
            commitId = "no commit id";
        }
        return commitId;
    }

	private void setAuthentication() throws TrnConfigException {
		if (TrnTools.isGitUrlSSH()) {
			credentialsProvider = null;
			new org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder()
					.setPreferredAuthentications("publickey")
					.setHomeDirectory(org.eclipse.jgit.util.FS.DETECTED.userHome())
					.setSshDirectory(new File(org.eclipse.jgit.util.FS.DETECTED.userHome(), "/.ssh"))
					.build(null);
		} else {
			JSONObject jsonCreds = TrnTools.getGitCredentials();
			credentialsProvider = new UsernamePasswordCredentialsProvider(
					jsonCreds.getString("username"),
					jsonCreds.getString("token"));
		}
	}

	public String deleteRepository() throws IOException {
		File repositoryDir = getContentDir();
		String msg;
		if (repositoryDir.exists()) {
			FileUtils.delete(repositoryDir, FileUtils.RECURSIVE);
			msg = "Repository deleted successfully.";
			AppLog.info(msg, g);
			return msg;
		} else {
			msg = "Repository directory does not exist.";
			AppLog.info(msg, g);
			return msg;
		}
	}

    private static File getContentDir() {
		return new File(Platform.getContentDir(), CONTENT_FOLDER_NAME);
	}
}
