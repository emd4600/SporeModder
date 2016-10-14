package net.tomahawk; 

import java.util.*; 
import java.io.*; 


public class ExtensionsFilter extends javax.swing.filechooser.FileFilter 
{
	static final char SPACE = ' ';
	static final char ASTERISK = '*';
	static final char SEMICOLON = ';';
	static final char VBAR = '|';
	static final char OPEN_PAR = '(';
	static final char CLOSE_PAR = ')';
	static final String POINT = ".";
	java.util.List<String> extensions;
	String shortDescription;
	String description;


// constructor 1 (from a List)
public ExtensionsFilter(String shortDescription, List<String> extensions) 
	{
		this.extensions = extensions;
		this.shortDescription = shortDescription;
		description = shortDescription;
		
//		description = shortDescription +SPACE+OPEN_PAR;
//		for (String ext : extensions) 
//		{
//			this.description += ASTERISK + POINT + ext + SEMICOLON;
//		}
//		description=description.substring(0,description.length()-1);
//		description+=CLOSE_PAR;
	}

// key: the accept method 
	public boolean accept(File f) 
	{
		if (f.isDirectory()) return true;
		if (extensions == null) return true;
		if (extensions.isEmpty()) return true;
		String filename = f.getName().toLowerCase();
		if (filename == null) return false;
		for (int i = 0; i < extensions.size(); i++) {
			String ext = extensions.get(i);
			if (filename.endsWith(POINT + ext)) return true;
		}
		return false;
	}

// description 
	public String getDescription() {
		return description;
	}


// get the filter string for native filedialog 
//desc0|*.ext00;...;*.ext0n|desc1|*.ext10;...;*.ext1n...
	public static String getNativeString(ExtensionsFilter... filters) 
	{
		String filterString = "";
		for (ExtensionsFilter extensionsFilter : filters) 
		{
			filterString += extensionsFilter.description + VBAR;

			java.util.List<String> exts = extensionsFilter.extensions;
			int count = exts.size();
			for (int j = 0; j < count - 1; j++) 
			{
			filterString += ASTERISK + POINT + exts.get(j) + SEMICOLON;
			}
			filterString += ASTERISK + POINT + exts.get(count - 1);
			filterString += VBAR;
		}
		System.out.println("Native filter string: " + filterString);
		return filterString;
	}

}

