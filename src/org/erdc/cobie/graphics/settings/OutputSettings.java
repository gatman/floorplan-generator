package org.erdc.cobie.graphics.settings;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.erdc.cobie.graphics.string.Default;
import org.erdc.cobie.shared.Common;
import org.erdc.cobie.shared.utility.StringUtils;

public class OutputSettings extends Settings
{
	public static class FileInfo
	{
		public final String format;
		public final String path;

		public FileInfo(String path, String format)
		{
			this.path = path;
			this.format = format;
		}

		public String getExtension()
		{
			String extension = format;

			if (!extension.startsWith(Common.FILE_EXTENSION_PREFIX.toString()))
			{
				extension = Common.FILE_EXTENSION_PREFIX.toString() + extension;
			}

			return extension;
		}
	}

	public static class NamedFileInfo extends FileInfo
	{
		public String name;

		public NamedFileInfo(String name, String path, String format)
		{
			super(path, format);
			this.name = name;
		}

		public String getFilePath()
		{
			return path + name + Common.FILE_EXTENSION_PREFIX.toString() + format;
		}
	}

	// TODO change the name.replaceAll with a correct regular expression using name.matches().
	public static String checkName(String name, String extension)
	{
		String checkedName = Common.EMPTY_STRING.toString();

		if (StringUtils.isNullOrEmpty(name))
		{
			defaultFileNamesUsed++;

			checkedName = Default.DEFAULT_FILENAME.toString() + Common.FILE_NAME_DELIMITER.toString() + defaultFileNamesUsed;
			getLogger().warn(Default.EMPTY_FILENAME.toString());
		}

		else
		{
			checkedName = name.replaceAll(ILLEGAL_FILENAME_CHARACTERS, Common.FILE_NAME_DELIMITER.toString());

			if (!name.equals(checkedName))
			{
				getLogger().warn(Default.ILLEGAL_CHARACTERS_IN_FILENAME.format(Common.FILE_NAME_DELIMITER.toString()));
			}
		}

		return checkedName + extension;
	}

	private static final String ILLEGAL_FILENAME_CHARACTERS = "[:\\\\/*?|<>]";

	public static final String PARENT_DIRECTORY = Common.PATH_PARENT.toString();
	public static final NamedFileInfo DEFAULT_JSON_INFO = new NamedFileInfo("cobie", "html/", "json");
	
	public static final FileInfo DEFAULT_HTML_INFO = new FileInfo("html/", "html");
	public static final FileInfo DEFAULT_IMAGE_INFO = new FileInfo("images/", "png");
	public static final FileInfo DEFAULT_SCRIPT_INFO = new FileInfo("scripts/", "js");
	public static final FileInfo DEFAULT_STYLE_INFO = new FileInfo("styles/", "css");
	public static final FileInfo DEFAULT_TEMPLATE_INFO = new FileInfo("templates/", "template");

	public static int defaultFileNamesUsed = 0;

	private Map<String, File> resourceFiles = new HashMap<String, File>();
	private final FileInfo htmlInfo;
	private final FileInfo imageInfo;
	private final FileInfo jsonInfo;
	private final FileInfo scriptsInfo;
	private final FileInfo stylesInfo;
	private final FileInfo templateInfo;

	public OutputSettings()
	{
		this(
			DEFAULT_HTML_INFO, 
			DEFAULT_IMAGE_INFO, 
			DEFAULT_SCRIPT_INFO, 
			DEFAULT_STYLE_INFO, 
			DEFAULT_TEMPLATE_INFO, 
			DEFAULT_JSON_INFO);
	}

	public OutputSettings(FileInfo htmlInfo, FileInfo imageInfo, FileInfo scriptsInfo, FileInfo stylesInfo, FileInfo templateInfo, FileInfo jsonInfo)
	{
		this.htmlInfo = htmlInfo;
		this.imageInfo = imageInfo;
		this.scriptsInfo = scriptsInfo;
		this.stylesInfo = stylesInfo;
		this.templateInfo = templateInfo;
		this.jsonInfo = jsonInfo;
	}

	public final FileInfo getHtmlInfo()
	{
		return htmlInfo;
	}

	public final FileInfo getImageInfo()
	{
		return imageInfo;
	}

	public final FileInfo getJSONInfo()
	{
		return jsonInfo;
	}

	/**
	 * Gets the resource that matches the supplied {@code URI}.
	 * 
	 * NOTE: This method makes a "best guess" to find the correct resource, because
	 * the resources are actually stored with absolute paths. This should only be a
	 * problem for very unusual cases.
	 * 
	 * @param relativePath The relative {@code URI} of the {@code File} to retrieve.
	 * @return The {@code File} that matches the supplied {@code URI}.
	 */
	public File getResource(URI relativePath)
	{
		File matchingFile = null;

		for (String key : getResources().keySet())
		{
			if (key.contains(relativePath.toString()))
			{
				matchingFile = getResources().get(key);
				break;
			}
		}

		return matchingFile;
	}

	public final Map<String, File> getResources()
	{
		return resourceFiles;
	}

	public final FileInfo getScriptInfo()
	{
		return scriptsInfo;
	}

	public final FileInfo getStyleInfo()
	{
		return stylesInfo;
	}

	public final FileInfo getTemplateInfo()
	{
		return templateInfo;
	}
	
	public final void setResourceFiles(Map<String, File> resources)
	{
		this.resourceFiles = resources;
	}
}
