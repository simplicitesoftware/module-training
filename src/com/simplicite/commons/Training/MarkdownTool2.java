/*
 * SIMPLICITE - runtime & framework
 * http://www.simplicite.fr
 * Copyright (c)2006-2022 Simplicite Software. All rights reserved.
 */
package com.simplicite.commons.Training;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.lock.qual.MayReleaseLocks;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.simplicite.bpm.Processus;
import com.simplicite.util.AppLog;
import com.simplicite.util.DocumentDB;
import com.simplicite.util.Globals;
import com.simplicite.util.Grant;
import com.simplicite.util.ModuleDB;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.ObjectField;
import com.simplicite.util.Resource;
import com.simplicite.util.Tool;
import com.simplicite.util.tools.HTMLTool;
import com.simplicite.util.tools.FileTool;

/**
 * Markdown toolbox
 * <br>This class only provides static variables and methods
 */
public class MarkdownTool2
{
	private static final Pattern PATTERN_ANCHOR_ANCHORED_AND_HEADING = Pattern.compile("(#+|\\b)\\s?(.+)(.+\\s\\{#)\\}");
	/** Hidden default constructor */
	private MarkdownTool2()
	{
		// Does nothing
	}

	/** @deprecated User toHTMLPage */
	@Deprecated(forRemoval = false, since = Globals.LEGACY_VERSION_4)
	public static String toHTML(String title, String md)
	{
		AppLog.deprecation(MarkdownTool2.class, "toHTML", "toHTMLPage", false);
		return toHTMLPage(title, md);
	}

	/**
	 * Convert a markdown string to an HTML page (parsing done on client side)
	 * @param title Title
	 * @param md Markdown string
	 */
	public static String toHTMLPage(String title, String md)
	{
		return HTMLTool.openSimplePage(title, new String[] { HTMLTool.docCSS(), HTMLTool.highlightCSS() }, new String[] { HTMLTool.jqueryJS(), HTMLTool.highlightJS() })
				+ "<div class=\"doc\">" + toHTML(md) + "</div>"
				+ HTMLTool.jsBlock("$(document).ready(function() { hljs.highlightAll(); });")
			+ HTMLTool.closePage();
	}

	/** @deprecated User toHTMLPage */
	@Deprecated(forRemoval = false, since = Globals.LEGACY_VERSION_4)
	public static String toHTML(String title, File file, String encoding) throws Exception
	{
		AppLog.deprecation(MarkdownTool2.class, "toHTML", "toHTMLPage", false);
		return toHTMLPage(title, file, encoding);
	}

	/**
	 * Convert a markdown file (in specified encoding) to HTML page (parsing done on client side)
	 * @param title Title
	 * @param file Markdown file
	 * @param encoding Encoding
	 */
	public static String toHTMLPage(String title, File file, String encoding) throws Exception
	{
		return toHTMLPage(title, FileTool.readFile(file, encoding));
	}

	/** @deprecated User toHTMLPage */
	@Deprecated(forRemoval = false, since = Globals.LEGACY_VERSION_4)
	public static String toHTML(String title, File file) throws Exception
	{
		AppLog.deprecation(MarkdownTool2.class, "toHTML", "toHTMLPage", false);
		return toHTMLPage(title, file);
	}

	/**
	 * Convert a markdown file (in default encoding) to HTML page (parsing done on client side)
	 * @param title Title
	 * @param file Markdown file
	 */
	public static String toHTMLPage(String title, File file) throws Exception
	{
		return toHTMLPage(title, FileTool.readFile(file, Globals.encoding()));
	}

	private static String title(String title, char c)
	{
		StringBuilder t = new StringBuilder(title);
		t.append("\n").append(Tool.rpadString(null, c, title.length())).append("\n\n");
		return t.toString();
	}

	/**
	 * Get ASCII logo as Markdown comments
	 * @param complement Additonal string to append to ASCII logo
	 */
	public static String getASCIILogo(String complement)
	{
		return "<!--\n" + (Globals.ASCII_LOGO + (complement!=null ? complement : "")) + "\n-->\n";
	}

	/**
	 * Title
	 * @param title Title
	 * @param level Level
	 */
	public static String title(String title, int level)
	{
		switch (level)
		{
		case 1:
			return title1(title);
		case 2:
			return title2(title);
		case 3:
			return title3(title);
		case 4:
			return title4(title);
		case 5:
			return title5(title);
		default:
			return title;
		}
	}

	/**
	 * Level 1 titel
	 * @param title Title
	 */
	public static String title1(String title)
	{
		return title(title, '=');
	}

	/**
	 * Level 2 titel
	 * @param title Title
	 */
	public static String title2(String title)
	{
		return title(title, '-');
	}

	/**
	 * Level 3 titel
	 * @param title Title
	 */
	public static String title3(String title)
	{
		return "### " + title + "\n\n";
	}

	/**
	 * Level 4 titel
	 * @param title Title
	 */
	public static String title4(String title)
	{
		return "#### " + title + "\n\n";
	}

	/**
	 * Level 5 titel
	 * @param title Title
	 */
	public static String title5(String title)
	{
		return "##### " + title + "\n\n";
	}

	/**
	 * Code fragment
	 * @param code Code
	 */
	public static String code(String code)
	{
		return "`" + code + "`";
	}

	/**
	 * Code block
	 * @param code Code
	 * @param language Code language
	 */
	public static String code(String code, String language)
	{
		return "```" + (language!=null ? " " + language : "") + "\n" + code + (code.endsWith("\n") ? "" : "\n") + "```\n\n";
	}

	/**
	 * Quite block
	 * @param text Text to quote
	 */
	public static String quote(String text)
	{
		try
		{
			StringBuilder out = new StringBuilder();
			BufferedReader in = new BufferedReader(new StringReader(text));
			String l;
			while ((l = in.readLine()) != null) out.append("> " + l + "\n");
			in.close();
			return out.toString();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Link
	 * @param url URL
	 * @param label Label
	 */
	public static String link(String url, String label)
	{
		return "[" + (label!=null ? label : "") + "](" + url + ")";
	}

	/**
	 * Image
	 * @param url Image URL (can be a data URL)
	 * @param alt Alternative text for image
	 */
	public static String image(String url, String alt)
	{
		return "!" + link(url, alt);
	}

	/**
	 * Convert a the anchor links of a markdown into html elements
	 * @param md Markdown string
	 * @return A markdown with HTML headers
	 */
	public static String toHTMLWithAnchors(String md) {
		BufferedReader br = new BufferedReader(new StringReader(md));
		StringBuffer inputBuffer = new StringBuffer();
		String line;
		try {
			while((line = br.readLine()) != null) {
				line = line.replaceAll("(\\-+)", "");
				Matcher matcher = PATTERN_ANCHOR_ANCHORED_AND_HEADING.matcher(line);
				while(matcher.find())
				{
					int headingNbr = matcher.group(1).length();
					if(headingNbr == 0) ++headingNbr;
					String title = matcher.group(2);
					String id = matcher.group(3);
					line = "<h" + headingNbr + " id=\"" + id +"\">" + title + "</h" + headingNbr +">";
				}
				inputBuffer.append(line + "\n");
			}
		} catch(IOException e) {
			AppLog.error(e, Grant.getPublic());
		}
		String html = toHTML(inputBuffer.toString(), false);
		return html;
	}

	/**
	 * Convert a markdown string to HTML (parsing done on server side)
	 * @param md Markdown string
	 * @return HTML
	 */
	public static String toHTML(String md)
	{
		return toHTML(md, false);
	}

	/**
	 * Convert a markdown string to HTML (parsing done on server side)
	 * @param md Markdown string
	 * @param autolinks Auto-links?
	 * @return HTML
	 */
	public static String toHTML(String md, boolean autolinks)
	{
		List<Extension> extensions = Arrays.asList(
			TablesExtension.create(),
			StrikethroughExtension.create(),
			HeadingAnchorExtension.create(),
			InsExtension.create(),
			ImageAttributesExtension.create(),
			TaskListItemsExtension.create(),
			YamlFrontMatterExtension.create());

		if (autolinks)
			extensions.add(AutolinkExtension.create());

		Parser parser = Parser.builder().extensions(extensions).build();
		Node document = parser.parse(md);
		HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
		return renderer.render(document);
	}

	/**
	 * Convert a markdown file to HTML (parsing done on server side)
	 * @param file Markdown file
	 * @return HTML
	 */
	public static String toHTML(File file) throws Exception
	{
		return toHTML(file, false);
	}

	/**
	 * Convert a markdown file to HTML (parsing done on server side)
	 * @param file Markdown file
	 * @param autolinks Auto-links?
	 * @return HTML
	 */
	public static String toHTML(File file, boolean autolinks) throws Exception
	{
		return toHTML(FileTool.readFile(file, Globals.encoding()), autolinks);
	}

	/**
	 * Convert a markdown input stream to HTML and write it to output stream (parsing done on server side)
	 * @param in Markdown input stream
	 * @param out Markdown output stream
	 */
	public static void toHTML(InputStream in, OutputStream out) throws Exception
	{
		out.write(toHTML(Tool.readStream(in, Globals.encoding())).getBytes());
	}

	/**
	 * Documentation header
	 * @param g Grant
	 * @return Markdown fragment
	 */
	public static String docHeader(Grant g)
	{
		String url = null;
		Resource header = g.getResource(Resource.TYPE_IMAGE, "APIDOC_HEADER", null, null);
		if (header != null)
		{
			DocumentDB doc = header.getDocument(g);
			try
			{
				url = HTMLTool.getDataURL(doc.getMIME(), doc.getBytes(true));
			}
			catch (Exception e)
			{
				return "";
			}
		}
		else
			url = Globals.getPlatformResourcesURL() + "/logos/logo250.png";
		return image(url, "") + "\n* * *\n\n";
	}

	/** @deprecated Use docForModule with additional arguments */
	@Deprecated(forRemoval = true, since = Globals.LEGACY_VERSION_4)
	public static String docForModule(Grant g, String name, boolean header, boolean services, int titleLevel)
	{
		AppLog.deprecation(MarkdownTool2.class, "docForModule", "docForModule with additional arguments", true);
		return genMarkdownForModule(g, name, header, services, false, titleLevel, true);
	}

	/**
	 * Documentation for specified module
	 * @param g Grant
	 * @param name Module name
	 * @param header Include header?
	 * @param models Include models?
	 * @param services Service variant (without models)?
	 * @param titleLevel Title level
	 * @return Markdown fragment
	 */
	public static String docForModule(Grant g, String name, boolean header, boolean models, boolean services, int titleLevel)
	{
		return genMarkdownForModule(g, name, header, models, services, titleLevel, true);
	}

	/**
	 * Documentation for specified module
	 * @param g Grant
	 * @param name Module name
	 * @param header Include header?
	 * @param models Include models?
	 * @param services Service variant (without models)?
	 * @param titleLevel Title level
	 * @param rawMarkdown false to generate Simplicite-flavored markdown
	 * @return Markdown fragment
	 */
	public static String genMarkdownForModule(Grant g, String name, boolean header, boolean models, boolean services, int titleLevel, boolean rawMarkdown)
	{
		if (g == null)
			g = Grant.getSystemAdmin();

		StringBuilder md = new StringBuilder(header ? docHeader(g) : "");

		titleLevel = Math.max(1, Math.min(titleLevel, 3)); // Title level is >= 1 and <= 3

		ObjectDB mdl = g.getTmpObject("Module");
		mdl.select(ModuleDB.getModuleId(name));

		String moduleName = mdl.getFieldValue("mdl_name");
		md.append(title(code(moduleName) + " module " + (services ? "services" : "definition"), titleLevel));
		md.append(mdl.getFieldValue("mdl_comment").replaceAll("\\r", "") + "\n\n");

		if (!services && models)
		{
			ObjectDB mod = g.getTmpObject("Model");
			mod.resetFilters();
			mod.getField("row_module_id").setFilter(mdl.getRowId());
			List<String[]> ms = mod.search();
			if (ms.size() > 0)
			{
				md.append(title("Business models", titleLevel + 1));
				for (int i = 0; i < ms.size(); i++)
				{
					mod.setValues(ms.get(i));
					String modelName = mod.getFieldValue("mod_name");
					md.append(title(code(modelName) + " business model", titleLevel + 2));
					if (rawMarkdown)
					{
						try
						{
							DocumentDB img = mod.getField("mod_image").getDocument(g);
							if (img == null)
								throw new IOException("Unable to get image for " + modelName + " model");

							md.append(image(HTMLTool.getImageDataURL(img.getMIME(), img.getBytes(true)), modelName) + "\n\n");
						}
						catch (IOException e)
						{
							md.append("Error: " + e.getMessage());
						}
					}
					else
					{
						md.append("[MODEL:"+modelName+"]\n\n");
					}
				}
			}
		}

		ObjectDB obj = g.getTmpObject("ObjectInternal");
		obj.resetFilters();
		obj.resetOrders();
		obj.getField("obo_exportorder").setOrder(1);
		obj.getField("obo_name").setOrder(2);
		obj.getField("row_module_id").setFilter(mdl.getRowId());
		List<String[]> os = obj.search();
		for (int i = 0; i < os.size(); i++)
		{
			String objName = os.get(i)[obj.getFieldIndex("obo_name")];
			md.append(rawMarkdown ? docForObject(g, objName, false, services, titleLevel + 1) : "[OBJECTDOC:"+objName+"]\n\n");
		}

		ObjectDB pcs = g.getTmpObject("BPMProcess");
		pcs.resetFilters();
		pcs.getField("row_module_id").setFilter(mdl.getRowId());
		List<String[]> ps = pcs.search();
		for (int i = 0; i < ps.size(); i++)
		{
			String processName = ps.get(i)[pcs.getFieldIndex("pcs_name")];
			md.append(rawMarkdown ? docForProcess(g, processName, false, services, titleLevel + 1) : "[PROCESSDOC:"+processName+"]\n\n");
		}

		ObjectDB ext = g.getTmpObject("ObjectExternal");
		ext.resetFilters();
		ext.getField("row_module_id").setFilter(mdl.getRowId());
		List<String[]> es = ext.search();
		for (int i = 0; i < es.size(); i++)
		{
			md.append(docForExternalObject(g, es.get(i)[ext.getFieldIndex("obe_name")], false, services, titleLevel + 1));
		}

		return MarkdownTool2.getASCIILogo(null) + md.toString();
	}

	/**
	 * Documentation for specified object
	 * @param g Grant
	 * @param name Object name
	 * @param header Include header ?
	 * @param services Service output ?
	 * @param titleLevel Title level
	 * @return Markdown fragment
	 */
	public static String docForObject(Grant g, String name, boolean header, boolean services, int titleLevel)
	{
		try
		{
			Grant sys = Grant.getSystemAdmin();

			if (g == null)
				g = sys;

			titleLevel = Math.max(1, Math.min(titleLevel, 4)); // Title level is >= 1 and <= 4

			StringBuilder md = new StringBuilder(header ? docHeader(g) : "");

			ObjectDB obj = (g.accessObject("ObjectInternal") ? g : sys).getTmpObject("ObjectInternal");
			obj.resetFilters();
			String objId = ObjectDB.getObjectId(name);
			if (!obj.select(objId))
				throw new Exception("Unable to select business object " + name + " (row ID " + objId + ")");

			ObjectDB obf = (g.accessObject("ObjectFieldSystem") ? g : sys).getTmpObject("ObjectFieldSystem");
			int obf1 = obf.getFieldIndex("obf_field_id");
			int obf2 = obf.getFieldIndex("obf_field_id.fld_name");
			int obf3 = obf.getFieldIndex("obf_ref_object_id");
			int obf4 = obf.getFieldIndex("obf_ref_object_id.obo_name");
			int obf5 = obf.getFieldIndex("obf_ref_field_id");
			int obf6 = obf.getFieldIndex("obf_ref_field_id.fld_name");

			ObjectDB fld = (g.accessObject("Field") ? g : sys).getTmpObject("Field");

			ObjectDB flc = (g.accessObject("FieldListCode") ? g : sys).getTmpObject("FieldListCode");
			int flc1 = flc.getFieldIndex("lov_code");
			int flc2 = flc.getFieldIndex("lov_label");

			ObjectDB fct = (g.accessObject("Function") ? g : sys).getTmpObject("Function");
			int fct1 = fct.getFieldIndex("fct_action_id");

			ObjectDB act = (g.accessObject("Action") ? g : sys).getTmpObject("Action");

			String objectName = obj.getFieldValue("obo_name");
			md.append(title(code(objectName) + " business object " + (services ? "services" : "definition"), titleLevel));

			if (services)
			{
				md.append(title("API endpoint", titleLevel + 1));
				md.append(code(g.getContextURL() + "/api/rest/" + objectName, "text") +"\n\n");

				md.append(title("Description", titleLevel + 1));
			}
			ObjectField cmt = obj.getField("obo_comment", false);
			if (cmt!=null)
				md.append(cmt.getValue().replaceAll("\\r", "") + "\n\n");

			StringBuilder lmd = new StringBuilder();

			md.append(title("Fields", titleLevel + 1));

			int[] cs = new int[] { 60, 40, 8, 9, 8, 80 };
			String[] cl = new String[] { "Name", "Type", "Required", "Updatable", "Personal", "Description" };
			md.append("|");
			for (int c = 0; c < cs.length; c++)
				md.append(" " + Tool.rpadString(cl[c], ' ', cs[c]) + " |");
			for (int c = 0; c < cs.length; c++)
				md.append((c==0 ? "\n" : "-") + "|-" + Tool.rpadString(null, '-', cs[c]));
			md.append("-|\n");

			if (services || obj.isCustomRowId())
				md.append(
					"| " + Tool.rpadString("**" + code(obj.getRowIdField().getName()) + "**", ' ', cs[0]) +
					" | " + Tool.rpadString(ObjectField.getTypeLabel(ObjectField.TYPE_ID, 0, 0).toLowerCase(), ' ', cs[1]) +
					" | " + Tool.rpadString("*", ' ', cs[2]) +
					" | " + Tool.rpadString("", ' ', cs[3]) +
					" | " + Tool.rpadString("", ' ', cs[4]) +
					" | " + Tool.rpadString("Row ID", ' ', cs[5]) + " |\n");

			obf.resetFilters();
			obf.getField("obf_object_id").setFilter(obj.getRowId());
			HashMap<String, String> lovs = new HashMap<>();
			List<String[]> fs = obf.search(false);
			for (int j = 0; j < fs.size(); j++)
			{
				String[] f = fs.get(j);
				fld.resetFilters();
				String id = f[obf1];

				fld.select(id);
				if (fld.getField("fld_visible").getInt(ObjectField.VIS_BOTH)==ObjectField.VIS_FORBIDDEN) continue;

				boolean ref = !Tool.isEmpty(f[obf3]) && !Tool.isEmpty(f[obf5]);

				String n = "`" + (Tool.isEmpty(f[obf6]) ? "" : f[obf6] + (services ? Globals.DOT_REPLACEMENT : ".")) + f[obf2] + (!Tool.isEmpty(f[obf3]) && Tool.isEmpty(f[obf5]) ? "` link to **`" + f[obf4] + "`**" : "`");
				md.append("| " + Tool.rpadString((ref ? "_Ref. " : "") + n + (ref ? "_" : ""), ' ', cs[0]) + " | ");

				int t = fld.getField("fld_type").getInt(0);
				String lov = fld.getFieldValue("fld_list_id.lov_name");
				if ((t == ObjectField.TYPE_ENUM || t == ObjectField.TYPE_ENUM_MULTI) && !lovs.containsKey(lov))
				{
					lovs.put(lov, "");
					flc.resetFilters();
					flc.getField("lov_list_id.lov_name").setFilter(lov);
					List<String[]> ls = flc.search(false);
					lmd.append("* " + code(lov) + "\n");
					for (int k = 0; k < ls.size(); k++)
					{
						String[] l = ls.get(k);
						lmd.append("    - " + code(l[flc1]) + " " + l[flc2] + "\n");
					}
				}
				md.append(Tool.rpadString((ref ? "_" : "") +
					ObjectField.getTypeLabel(t, Tool.parseInt(fld.getFieldValue("fld_size")),
						Tool.parseInt(fld.getFieldValue("fld_precision"))).toLowerCase() + (lov.length() > 0 ? " using " + code(lov) + " list" : "") + (ref ? "_" : ""), ' ', cs[1]) + " | ");

				String req = null;
				if (!ref)
					req = (fld.getField("fld_required").getBoolean(false) ? "yes" : "") + (fld.getField("fld_fonctid").getBoolean(false) ? "*" : "");
				md.append(Tool.rpadString(req, ' ', cs[2]) + " | ");

				String upd = !ref && fld.getField("fld_updatable").getInt(ObjectField.UPD_ALWAYS)!=ObjectField.UPD_READ_ONLY ? "yes" : null;
				md.append(Tool.rpadString(upd, ' ', cs[3]) + " | ");

				String clsf = !fld.getField("fld_classification").isEmpty() ? "yes" : "";
				md.append(Tool.rpadString(clsf, ' ', cs[4]) + " | ");

				String c = fld.getFieldValue("fld_comment");
				md.append(Tool.rpadString(Tool.isEmpty(c) ? "-" : (ref ? "_" : "") + c.replaceAll("\\r", "").replaceAll("\\n", "<br/>") + (ref ? "_" : ""), ' ', cs[5]) + " |\n");
			}
			md.append("\n");
			if (services)
				md.append("_NB: The row ID is also part of the REST URL. The value to use for creation is empty or `0`._\n\n");

			if (lmd.length() > 0)
			{
				md.append(title("Lists", titleLevel + 1)).append(lmd).append("\n");
			}

			fct.resetFilters();
			fct.getField("fct_object_id").setFilter(obj.getRowId());
			fs = fct.search(false);

			StringBuilder fmd = new StringBuilder();
			for (int j = 0; j < fs.size(); j++)
			{
				String[] f = fs.get(j);
				if (!Tool.isEmpty(f[fct1]))
				{
					act.resetFilters();
					act.select(f[fct1]);
					if (!act.getField("act_method").isEmpty())
					{
						fmd.append("* " + code(act.getFieldValue("act_name")) + ": " + act.getFieldValue("act_comment").replaceAll("\\r", "") + "\n");
					}
				}
			}

			if (fmd.length() > 0)
			{
				md.append(title("Custom actions", titleLevel + 1)).append(fmd).append("\n");
			}

			return md.toString();
		}
		catch (Exception e)
		{
			AppLog.error(MarkdownTool2.class, "docForObject", null, e, g);
			return "\n> **Error**: Unable to generate documentation for business object " + name + "\n\n";
		}
	}

	/**
	 * Documentation for specified business process
	 * @param g Grant
	 * @param name Process name
	 * @param header Include header ?
	 * @param services Service output ?
	 * @param titleLevel Title level
	 * @return Markdown fragment
	 */
	public static String docForProcess(Grant g, String name, boolean header, boolean services, int titleLevel)
	{
		try
		{
			Grant sys = Grant.getSystemAdmin();

			if (g == null)
				g = sys;

			titleLevel = Math.max(1, Math.min(titleLevel, 4)); // Title level is >= 1 and <= 4

			StringBuilder md = new StringBuilder(header ? docHeader(g) : "");

			ObjectDB pcs = (g.accessObject("BPMProcess") ? g : sys).getTmpObject("BPMProcess");
			pcs.resetFilters();
			if (!pcs.select(Processus.getProcessId(name)))
				throw new Exception("Unable to select business process " + name);

			String processName = pcs.getFieldValue("pcs_name");
			md.append(title(code(processName) + " business process " + (services ? "services" : "definition"), titleLevel));

			ObjectField cmt = pcs.getField("pcs_comment", false);
			if (cmt!=null)
				md.append(cmt.getValue().replaceAll("\\r", "") + "\n\n");

			md.append(title("Activities", titleLevel + 1));

			ObjectDB acy = (g.accessObject("BPMActivity") ? g : sys).getTmpObject("BPMActivity");
			acy.resetFilters();
			acy.getField("acy_process_id").setFilter(pcs.getRowId());
			ObjectField acmt = acy.getField("acy_comment", false); // May not yet exists during upgrade

			List<String[]> rows = acy.search(false);
			for (int i=0; i<rows.size(); i++)
			{
				acy.setValues(rows.get(i), false);
				md.append("* " + code(acy.getFieldValue("acy_name")) + (acmt==null ? "" : ": " + acmt.getValue().replaceAll("\\r", "")) + "\n");
			}

			md.append("\n");
			return md.toString();
		}
		catch (Exception e)
		{
			AppLog.error(MarkdownTool2.class, "docForProcess", null, e, g);
			return "\n> **Error**: Unable to generate documentation for business process " + name + "\n\n";
		}
	}

	/**
	 * Documentation for specified external object
	 * @param g Grant
	 * @param name External object name
	 * @param header Include header ?
	 * @param services Service output ?
	 * @param titleLevel Title level
	 * @return Markdown fragment
	 */
	public static String docForExternalObject(Grant g, String name, boolean header, boolean services, int titleLevel)
	{
		try
		{
			Grant sys = Grant.getSystemAdmin();

			if (g == null)
				g = sys;

			titleLevel = Math.max(1, Math.min(titleLevel, 4)); // Title level is >= 1 and <= 4

			StringBuilder md = new StringBuilder(header ? docHeader(g) : "");

			ObjectDB ext = (g.accessObject("ObjectExternal") ? g : sys).getTmpObject("ObjectExternal");
			ext.resetFilters();
			if (!ext.select(ObjectDB.getObjectId(name)))
				throw new Exception("Unable to select external objects " + name);

			String extobjectName = ext.getFieldValue("obe_name");
			md.append(title(code(extobjectName) + " external object " + (services ? "services" : "definition"), titleLevel));

			ObjectField comments = ext.getField("obe_comment", false);
			if (comments!=null)
				md.append(comments.getValue().replaceAll("\\r", "") + "\n\n");

			md.append("\n");
			return md.toString();
		}
		catch (Exception e)
		{
			AppLog.error(MarkdownTool2.class, "docForExternalObject", null, e, g);
			return "\n> **Error**: Unable to generate documentation for business process " + name + "\n\n";
		}
	}
}
