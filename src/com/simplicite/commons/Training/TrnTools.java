package com.simplicite.commons.Training;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.tools.SyntaxTool;

/**
 * Shared code TrnTools
 */
public class TrnTools implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private static final Pattern SSH_URL_PATTERN = Pattern.compile(
			"^[\\p{IsAlphabetic}\\d]+@[\\p{IsAlphabetic}\\.:\\/\\d-_]+$");

	public static String toSnake(String str) {
		return SyntaxTool.forceCase(StringUtils.stripAccents(str), SyntaxTool.SNAKE, true);
	}

	public static String path2Front(String path) {
		return path.replaceAll("(CTG|LSN)_[0-9]+_", "");
	}

	public static boolean isUiMode() throws TrnConfigException {
		try {
			return "UI".equals(getContentEdition().optString("mode"));
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static boolean isFileSystemMode() throws TrnConfigException {
		try {
			return "FILESYSTEM".equals(getContentEdition().optString("mode"));
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static boolean isElasticSearchMode() throws TrnConfigException {
		try {
			return "elasticsearch".equals(getIndexEngine());
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static boolean isSimpliciteMode() throws TrnConfigException {
		try {
			return "simplicite".equals(getIndexEngine());
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	private static JSONObject getTrnConfig() throws TrnConfigException {
		try {
			return new JSONObject(Grant.getSystemAdmin().getParameter("TRN_CONFIG"));
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static JSONObject getContentEdition() throws TrnConfigException {
		try {
			return getTrnConfig().getJSONObject("content_edition");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	private static JSONObject getContentIndexation() throws TrnConfigException {
		try {
			return getTrnConfig().getJSONObject("content_indexation");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String getIndexEngine() throws TrnConfigException {
		try {
			return getContentIndexation().getString("engine");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	private static JSONObject getEsConfig() throws TrnConfigException {
		try {
			return getContentIndexation().getJSONObject("es_config");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static boolean hasEsFrontInstance() throws TrnConfigException {
		try {
			return getEsConfig().has("front_instance");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String getEsFrontInstance() throws TrnConfigException {
		try {
			return getEsConfig().getString("front_instance");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String getEsUrl() throws TrnConfigException {
		try {
			return getEsConfig().getString("instance");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String getEsIndex() throws TrnConfigException {
		try {
			return getEsConfig().getString("index");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String getEsCredentials() throws TrnConfigException {
		try {
			return getEsConfig().getString("public_credentials");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	private static JSONObject getGitConfig() throws TrnConfigException {
		try {
			return getContentEdition().getJSONObject("git_checkout_service");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String getGitUrl() throws TrnConfigException {
		try {
			return getGitConfig().getJSONObject("repository").getString("uri");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	// return true if url is ssh url, false if other
	public static boolean isGitUrlSSH() throws TrnConfigException {
		try {
			Matcher matcher = SSH_URL_PATTERN.matcher(getGitUrl());
			boolean res = false;
			if (matcher.find()) {
				res = true;
			}
			return res;
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String getGitBranch() throws TrnConfigException {
		try {
			return getGitConfig().getJSONObject("repository").getString("branch");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static JSONObject getGitCredentials() throws TrnConfigException {
		try {
			return getGitConfig().getJSONObject("repository").getJSONObject("creds");
		} catch (Exception e) {
			throw new TrnConfigException(e.getMessage());
		}
	}

	public static String[] getLangs(Grant g) {
		return g.getListOfValues("LANG_ALL").getCodesArray("LANG_ALL");
	}

	public static String[] getLangs(Grant g, boolean includeDefault) {
		if (includeDefault)
			return getLangs(g);
		else {
			List<String> langs = new LinkedList<String>(Arrays.asList(getLangs(g)));
			langs.remove(getDefaultLang());
			return langs.toArray(new String[0]);
		}
	}

	public static String getDefaultLang() {
		return "ANY";
	}
}
