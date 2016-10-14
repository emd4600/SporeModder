package sporemodder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import sporemodder.files.formats.dbpf.DBPFPacker;
import sporemodder.files.formats.dbpf.DBPFUnpacker;
import sporemodder.files.formats.effects.EffectPacker;
import sporemodder.files.formats.effects.EffectUnpacker;
import sporemodder.files.formats.pctp.PctpToTxt;
import sporemodder.files.formats.pctp.TxtToPctp;
import sporemodder.files.formats.prop.PropToXml;
import sporemodder.files.formats.prop.XmlToProp;
import sporemodder.files.formats.rast.DDStoRast;
import sporemodder.files.formats.rast.RastToDDS;
import sporemodder.files.formats.renderWare4.DDSToRw4;
import sporemodder.files.formats.renderWare4.Rw4ToDDS;
import sporemodder.files.formats.spui.SpuiToTxt;
import sporemodder.files.formats.spui.TxtToSpui;
import sporemodder.files.formats.tlsa.TlsaToTxt;
import sporemodder.files.formats.tlsa.TxtToTlsa;
import sporemodder.userinterface.ErrorManager;
import sporemodder.userinterface.ProjectTreeCellRenderer;
import sporemodder.userinterface.UIMainApp;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.userinterface.fileview.ImageFileView;
import sporemodder.userinterface.fileview.TextFileView;
import sporemodder.utilities.Project;
import sporemodder.utilities.Project.GamePathType;
import sporemodder.utilities.ProjectTreeNode;
import sporemodder.utilities.SporeGame;
import sporemodder.utilities.names.NameRegistry;

public class MainApp {
	
	public static class VersionInfo {
		public static final String ALPHA = "alpha";
		public static final String BETA = "beta";
		
		public int majorVersion;
		public int minorVersion;
		public int buildVersion;
		public String buildTag;
		
		public VersionInfo(int majorVersion, int minorVersion,
				int buildVersion, String buildTag) {
			this.minorVersion = minorVersion;
			this.majorVersion = majorVersion;
			this.buildVersion = buildVersion;
			this.buildTag = buildTag;
		}
		
		@Override
		public String toString() {
			return "v" + majorVersion + "." + minorVersion + "." + buildVersion + (buildTag == null ? "" : ("-" + buildTag));
		}
		
		public boolean isLastRelease(VersionInfo lastRelease) {
			if (majorVersion < lastRelease.majorVersion) {
				return false;
			}
			else if (majorVersion > lastRelease.majorVersion) {
				return true;
			}
			else {
				if (minorVersion < lastRelease.minorVersion) {
					return false;
				}
				else if (minorVersion > lastRelease.minorVersion) {
					return true;
				}
				else {
					return buildVersion >= lastRelease.buildVersion;
				}
			}
		}
		
		public static VersionInfo fromString(String str) {
			int minorVersion = 0;
			int majorVersion = 0;
			int buildVersion = 0;
			String buildTag = null;
			
			if (str.startsWith("v")) {
				str = str.substring(1);
			}
			
			String[] splits = str.split("-");
			if (splits.length > 1) {
				buildTag = splits[1];
			}
			String[] versions = splits[0].split("\\.");
			
			majorVersion = Integer.parseInt(versions[0]);
			
			if (versions.length > 1) {
				minorVersion = Integer.parseInt(versions[1]);
				
				if (versions.length > 2) {
					buildVersion = Integer.parseInt(versions[2]);
				}
			}
			
			return new VersionInfo(majorVersion, minorVersion, buildVersion, buildTag);
		}
	}
	
	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 1, VersionInfo.BETA);
	
	private static final String PROPERTY_SAVE_MODE = "saveMode";
	private static final String PROPERTY_SEARCHABLE_EXT = "searchableExtensions";
	private static final String PROPERTY_LAST_PATH = "lastPath";
	private static final String PROPERTY_PROJECTS_PATH = "projectsPath";
	private static final String PROPERTY_PRESET = "preset";
	private static final String PROPERTY_GAME_PATH_TYPE = "gamePathType";
	private static final String PROPERTY_GAME_PATH = "gamePath";
	private static final String PROPERTY_GAME_COMMANDS = "gameCommands";
	private static final String PROPERTY_COLOR_SOURCE = "colorSource";
	private static final String PROPERTY_COLOR_SOURCEMOD = "colorSourceMod";
	private static final String PROPERTY_COLOR_MOD = "colorMod";
	private static final String SETTINGS_FILE = "settings.properties";
	
	private static final int MAX_PRESETS = 10;
	
	//TODO disable when compiling
//	public static boolean DEBUG = false;
	
	public static ClassLoader Loader = MainApp.class.getClassLoader();
	
	public enum SaveMode { AUTOSAVE, MANUAL };
	
	// Alternative paths for name registries
	public static File FILE_REG_PATH = null;
	public static File TYPE_REG_PATH = null;
	public static File PROP_REG_PATH = null;
	public static File SPUI_REG_PATH = null;
	
	private static UIMainApp userInterface;
	
	private static List<Project> projects;
	private static HashMap<String, Project> projectsMap = new HashMap<String, Project>();
	private static Project currentProject;
	private static File programPath;
	private static File projectsPath;
	
	// Complete path is currentProject.path + activeFile
	private static String activeFile;
	private static FileView activeFileView;
	private static List<String> searchStrings;
	
	private static File lastFileChooserPath;
	
	private static HashMap<String, NameRegistry> registries;
	
	private static SaveMode saveMode;
	private static List<String> searchableExtensions;
	private static HashMap<String, List<String>> sourcePresets = new HashMap<String, List<String>>();
	
	private static GamePathType gamePathType = GamePathType.CUSTOM;
	private static String gamePath = "";
	private static String gameCommandLine = "";
	
	public static UIMainApp getUserInterface() {
		MainApp.processAutoSave();
		return userInterface;
	}
	public static void setUserInterface(UIMainApp userInterface) {
		MainApp.userInterface = userInterface; 
	}
	
	public static File getProgramPath() {
		return programPath;
	}
	
	public static File getProjectsPath() {
		return projectsPath;
	}
	
	public static String getRelativeProjectsPath() {
		String pPath = programPath.getAbsolutePath();
		String path = projectsPath.getAbsolutePath();
		
		path = path.replace(pPath, "//");
		if (path.startsWith("//\\")) {
			path = "//" + path.substring(3);
		}
		
		return path;
	}
	
	public static void setProjectsPath(String path) {
		File newProjectsPath = null;
		if (path.startsWith("//")) {
			newProjectsPath = new File(programPath, path.substring(2));
		}
		else {
			newProjectsPath = new File(path);
		}
		if (!newProjectsPath.equals(projectsPath)) {
			projectsPath = newProjectsPath;
			System.out.println(projectsPath);
			loadProjects();
		}
	}
	
	public static Project getCurrentProject() {
		return currentProject;
	}
	
	public static void setCurrentProject(Project project) {
		processAutoSave();
		//TODO Update last projects
		currentProject = project;
		
		//TODO Update Recent projects too
		project.setLastTimeUsed();
		project.writeProperties();
		
		setActiveFile(null);
		
		if (userInterface != null) {
			userInterface.update();
			userInterface.getProjectPanel().loadProject(project);
			userInterface.getProjectPanel().setShowModSelected(false);
			userInterface.getProjectPanel().setSearchFieldText("");
			userInterface.setTitle("SporeModder - " + project.getProjectName());
		}
	}
	
	
	public static List<Project> getProjects() {
		return projects;
	}
	
	public static boolean projectExists(Project project) {
		return projects.contains(project);
	}
	
	public static List<Project> getProjectsByNames(List<String> names) {
		//TODO This might need optimization
		List<Project> result = new ArrayList<Project>();

//		for (Project project : projects)
//		{
//			for (String name : names) {
//				if (project.getProjectName().equals(name))
//				{
//					result.add(project);
//				}
//			}
//		}
		
		for (String name : names) {
			Project p = projectsMap.get(name);
			if (p != null) result.add(p);
		}
		
		return result;
	}
	
	public static List<Project> getProjectsByNames(List<String> names, List<Project> dst) {
		//TODO This might need optimization
		for (String name : names) {
			Project p = projectsMap.get(name);
			if (p != null) dst.add(p);
		}
		
		return dst;
	}
	
	public static List<Project> getLastProjects(int count) {
		List<Project> list = new ArrayList<Project>(projects);
		Collections.sort(list, new Comparator<Project>() {
			@Override
			public int compare(Project arg0, Project arg1) {
				return -Long.compare(arg0.getLastTimeUsed(), arg1.getLastTimeUsed());
			}
		});
		
		
		return list.subList(0, Math.min(count, list.size()));
	}
	
	public static String getActiveFilePath() {
		return activeFile;
	}
	
	public static File getActiveFile() {
		return currentProject.getFile(activeFile);
	}
	
	public static void setActiveFile(String file) {
		// close Find dialog if exists
		if (activeFileView != null && activeFileView.getViewType() == TextFileView.VIEWTYPE_TEXT) {
			((TextFileView) activeFileView).closeFindDialog();
		}
		
		activeFile = file;
		if (activeFile != null) {
			File f = currentProject.getFile(activeFile); 
			if (f != null && f.exists()) {
				boolean found = false;
				if (f.isFile()) {
					for (String extension : ImageFileView.VALID_EXTENSIONS) {
						if (file.endsWith(extension)) {
							activeFileView = new ImageFileView(activeFile, f);
							found = true;
							break;
						}
					}
				}
				if (!found) {
					activeFileView = new TextFileView(activeFile, f);
				}
			} else {
				activeFileView = null;
			}
		} else {
			activeFileView = null;
		}
		userInterface.getDisplayPanel().setCurrentFileView(activeFileView);
		userInterface.update();
//		userInterface.getProjectMenu().update(activeFileView);
	}
	
	public static void reloadActiveFileView() {
		setActiveFile(activeFile);
	}
	
	public static FileView getActiveFileView() {
		return activeFileView;
	}
	
	public static ProjectTreeNode getActiveNode() {
		return userInterface.getProjectPanel().getNodeForPath(activeFile);
	}
	
	// saves if Autosave is enabled
	public static void processAutoSave() {
		if (MainApp.getSaveMode() == SaveMode.AUTOSAVE) {
			FileView view = MainApp.getActiveFileView();
			if (view != null && view instanceof TextFileView) {
				((TextFileView)view).save();
			}
		}
	}
	
//	public static String getCompleteFilePath(String file) {
//		return projectsPath + currentProject.getProjectName() + "\\" + file;
//	}
	
	public static File getLastFileChooserPath() {
		return lastFileChooserPath;
	}
	public static void setLastFileChooserPath(File file) {
		lastFileChooserPath = file;
		writeSettings();
	}
	
	public static NameRegistry getRegistry(String name) {
		return registries.get(name);
	}
	
	
	public static GamePathType getGamePathType() {
		return gamePathType;
	}
	public static String getCustomGamePath() {
		return gamePath;
	}
	public static String getGameCommandLine() {
		return gameCommandLine;
	}
	public static void setGamePathType(GamePathType gamePathType) {
		MainApp.gamePathType = gamePathType;
	}
	public static void setGamePath(String gamePath) {
		MainApp.gamePath = gamePath;
	}
	public static void setGameCommandLine(String gameCommandLine) {
		MainApp.gameCommandLine = gameCommandLine;
	}
	
	
	public static Project getProjectByName(String name) {
//		for (Project project : projects) {
//			if (project.getProjectName().equals(name)) {
//				return project;
//			}
//		}
//		return null;
		
		return projectsMap.get(name);
	}
	
	public static Project setProject(Project project, String name) {
//		int size = projects.size();
//		for (int i = 0; i < size; i++)
//		{
//			Project p = projects.get(i);
//			if (p.getProjectName().equals(name)) {
//				projects.set(i, project);
//				return p;
//			}
//		}
//		return null;
		
		return projectsMap.put(name, project);
	}
	
	public static void addProject(Project project) {
		//TODO Update project when it already exists?
		projects.add(project);
		projectsMap.put(project.getProjectName(), project);
		if (userInterface != null) {
			userInterface.updateProjects();
		}
	}
	
	public static String getNewProjectName() {
		int i = 0;
		while(true) {
			String name = "Project " + (projects.size() + i++);
			if (!checkProjectName(name)) return name;
		}
	}
	
	public static boolean checkProjectName(String name) {
//		for (Project project : projects) {
//			if (project.getProjectName().equals(name)) {
//				return true;
//			}
//		}
//		return false;
		
		return projectsMap.containsKey(name);
	}
	
	public static boolean checkProjectPackageName(String name) {
		for (Project project : projects) {
			if (project.getPackageName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static HashMap<String, List<String>> getSourcePresets() {
		return sourcePresets;
	}
	
	public static SaveMode getSaveMode() {
		return saveMode;
	}
	public static List<String> getSearchableExtensions() {
		return searchableExtensions;
	}
	
	public static void setSaveMode(SaveMode saveMode) {
		MainApp.saveMode = saveMode;
	}
	public static void setSearchableExtensions(List<String> searchableExtensions) {
		MainApp.searchableExtensions = searchableExtensions;
	}
	
	// doens't modify the tree
//	public static boolean searchInternal(List<String> strings) {
//		
//		if (userInterface == null) {
//			throw new UnsupportedOperationException("Program must have an interface to search files.");
//		}
//		
//		UIProjectPanel projectPanel = userInterface.getProjectPanel();
//		ProjectTreeNode rootNode = (ProjectTreeNode) projectPanel.getTreeModel().getRoot();
//		
//		boolean result = rootNode.search(strings, projectPanel.isShowModSelected(), false, false, false);
//		
////		projectPanel.repaintTree();
//		
//		return result;
//	}
	
	public static List<String> getSearchStrings() {
		return searchStrings;
	}
	
	public static void setSearchStrings(List<String> searchStrings) {
		MainApp.searchStrings = searchStrings;
		if (activeFileView != null && activeFileView instanceof TextFileView) {
			((TextFileView) activeFileView).setSearchTerms(searchStrings);
		}
	}
	
	public static List<String> getSearchStrings(String text) {
		String[] splits = text.toLowerCase().split(" ");
		List<String> strings = new ArrayList<String>();
		
		for (int i = 0; i < splits.length;) 
		{
			System.out.println(splits[i]);
			if (splits[i].length() == 0) {
				i++;
				continue;
			}
			if (splits[i].startsWith("\""))
			{
				StringBuilder sb = new StringBuilder();
				while (i < splits.length) {
					if (splits[i].endsWith("\"") && (!splits[i].equals("\"") || sb.length() > 0)) {
						System.out.println(splits[i]);
						sb.append(splits[i].substring(splits[i].startsWith("\"") ? 1 : 0));
						i++;
						break;
					}
					else {
						sb.append(splits[i].startsWith("\"") ? splits[i].substring(1) : splits[i]);
						if (i + 1 < splits.length) sb.append(" ");
						i++;
					}
				}
				strings.add(sb.toString());
			}
			else {
				strings.add(splits[i++]);
			}
		}
		
		System.out.println(strings.toString());
		
		return strings;
	}
	
	public static void runGame() {
		String path = null;
		String[] args = new String[0];
		if (currentProject != null && !currentProject.isDefaultGamePath()) {
			SporeGame game = null;
			switch(currentProject.getGamePathType()) {
			case CUSTOM:
				path = currentProject.getCustomGamePath();
				break;
			case GALACTIC_ADVENTURES:
				game = SporeGame.getGalacticAdventures();
				if (game == null) {
					JOptionPane.showMessageDialog(userInterface, "Couldn't execute game:\nGalactic Adventures not found. "
							+ "Please change the game path to Custom and specify the path to the game manually.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				path = game.getGamePath();
				break;
			case SPORE:
				game = SporeGame.getSpore();
				if (game == null) {
					JOptionPane.showMessageDialog(userInterface, "Couldn't execute game:\nSpore not found. "
							+ "Please change the game path to Custom and specify the path to the game manually.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				path = game.getGamePath();
				break;
			default:
				break;
			}
			String commandLine = currentProject.getGameCommandLine();
			if (commandLine != null && commandLine.length() > 0) {
				args = currentProject.getGameCommandLine().split(" ");
			}
		}
		else {
			SporeGame game = null;
			switch(gamePathType) {
			case CUSTOM:
				path = gamePath;
				break;
			case GALACTIC_ADVENTURES:
				game = SporeGame.getGalacticAdventures();
				if (game == null) {
					JOptionPane.showMessageDialog(userInterface, "Couldn't execute game:\nGalactic Adventures not found. "
							+ "Please change the game path to Custom and specify the path to the game manually.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				path = game.getGamePath();
				break;
			case SPORE:
				game = SporeGame.getSpore();
				if (game == null) {
					JOptionPane.showMessageDialog(userInterface, "Couldn't execute game:\nSpore not found. "
							+ "Please change the game path to Custom and specify the path to the game manually.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				path = game.getGamePath();
				break;
			default:
				break;
			}
			if (gameCommandLine != null && gameCommandLine.length() > 0) {
				args = gameCommandLine.split(" ");
			}
		}
		
		try {
			SporeGame.execute(path, args);
		} catch (IOException | URISyntaxException e) {
			JOptionPane.showMessageDialog(userInterface, "Couldn't execute game:\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	private static void processSettings() {
		Properties settings = null;
		File file = new File(SETTINGS_FILE);
		if (file.exists()) {
			settings = new Properties();
			try (FileInputStream in = new FileInputStream(file)) {
				settings.load(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			settings = getDefaultSettings();
			try (FileOutputStream out = new FileOutputStream(file)) {
				settings.store(out, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		readSettings(settings);
	}
	
	public static void writeSettings() {
		Properties settings = new Properties();
		
		settings.setProperty(PROPERTY_SAVE_MODE, saveMode.toString());
		
		{
			StringBuilder sb = new StringBuilder();
			for (String s : searchableExtensions) {
				sb.append(s + " ");
			}
			settings.setProperty(PROPERTY_SEARCHABLE_EXT, sb.toString());
		}
		
		if (lastFileChooserPath != null) {
			settings.setProperty(PROPERTY_LAST_PATH, lastFileChooserPath.getAbsolutePath());
		}
		
//		String path = projectsPath.getAbsolutePath();
//		path.replace(programPath.getAbsolutePath(), "//");
//		
//		settings.setProperty(PROPERTY_PROJECTS_PATH, path);
		
		settings.setProperty(PROPERTY_PROJECTS_PATH, getRelativeProjectsPath());
		
		int i = 0;
		for (Map.Entry<String, List<String>> entry : sourcePresets.entrySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey());
			List<String> list = entry.getValue();
			int size = list.size();
			for (int j = 0; j < size; j++) {
				sb.append('|');
				sb.append(list.get(j));
			}
			settings.setProperty(PROPERTY_PRESET + i++, sb.toString());
		}
		
		settings.setProperty(PROPERTY_GAME_PATH, gamePath);
		settings.setProperty(PROPERTY_GAME_PATH_TYPE, gamePathType.toString());
		settings.setProperty(PROPERTY_GAME_COMMANDS, gameCommandLine);
		
		settings.setProperty(PROPERTY_COLOR_SOURCE, ProjectTreeCellRenderer.generateColorString(ProjectTreeCellRenderer.COLOR_SOURCE));
		settings.setProperty(PROPERTY_COLOR_SOURCEMOD, ProjectTreeCellRenderer.generateColorString(ProjectTreeCellRenderer.COLOR_SOURCE_MOD));
		settings.setProperty(PROPERTY_COLOR_MOD, ProjectTreeCellRenderer.generateColorString(ProjectTreeCellRenderer.COLOR_MOD));
		
		try {
			try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
				settings.store(out, null);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't write program settings:\n" + ErrorManager.getStackTraceString(e), 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private static void readSettings(Properties settings) {
		
		saveMode = SaveMode.valueOf(settings.getProperty(PROPERTY_SAVE_MODE, "AUTOSAVE"));
		
		String str = settings.getProperty(PROPERTY_SEARCHABLE_EXT, "xml locale txt spui_t tlsa_t pctp_t pfx");
		String[] splits = str.split(" ");
		searchableExtensions = new ArrayList<String>(splits.length);
		for (String s : splits) {
			searchableExtensions.add(s);
		}
		
		String lastPath = settings.getProperty(PROPERTY_LAST_PATH);
		if (lastPath != null) {
			lastFileChooserPath = new File(settings.getProperty(PROPERTY_LAST_PATH));
		}
		
		String projectsPath = settings.getProperty(PROPERTY_PROJECTS_PATH);
		if (projectsPath != null) {
			if (projectsPath.startsWith("//")) {
				MainApp.projectsPath = new File(programPath, projectsPath.substring(2));
			}
			else {
				MainApp.projectsPath = new File(projectsPath);
			}
		}
		
		for (int i = 0; i < MAX_PRESETS; i++) {
			String presetStr = settings.getProperty(PROPERTY_PRESET + i);
			if (presetStr != null) {
				String[] presetSplits = presetStr.split("\\|");
				if (presetSplits.length > 0) {
					String presetName = presetSplits[0];
					List<String> presetProjects = new ArrayList<String>();
					for (int j = 1; j < presetSplits.length; j++) {
						presetProjects.add(presetSplits[j]);
					}
					sourcePresets.put(presetName, presetProjects);
				}
			}
		}
		
		gamePath = settings.getProperty(PROPERTY_GAME_PATH, "");
		String gamePathTypeStr = settings.getProperty(PROPERTY_GAME_PATH_TYPE);
		if (gamePathTypeStr == null) {
			if (SporeGame.hasGalacticAdventures()) {
				gamePathType = GamePathType.GALACTIC_ADVENTURES;
			} else if (SporeGame.hasSpore()) {
				gamePathType = GamePathType.SPORE;
			} else {
				gamePathType = GamePathType.CUSTOM;
			}
		}
		else {
			gamePathType = GamePathType.valueOf(gamePathTypeStr);
		}
		gameCommandLine = settings.getProperty(PROPERTY_GAME_COMMANDS, "");
		
		String colorSource = settings.getProperty(PROPERTY_COLOR_SOURCE);
		if (colorSource != null) ProjectTreeCellRenderer.COLOR_SOURCE = ProjectTreeCellRenderer.hex2Rgb(colorSource);
		
		String colorSourceMod = settings.getProperty(PROPERTY_COLOR_SOURCEMOD);
		if (colorSourceMod != null) ProjectTreeCellRenderer.COLOR_SOURCE_MOD = ProjectTreeCellRenderer.hex2Rgb(colorSourceMod);
		
		String colorMod = settings.getProperty(PROPERTY_COLOR_MOD);
		if (colorMod != null) ProjectTreeCellRenderer.COLOR_MOD = ProjectTreeCellRenderer.hex2Rgb(colorMod);
		
	}
	
	private static Properties getDefaultSettings() {
		Properties p = new Properties();
		
		p.setProperty(PROPERTY_SAVE_MODE, SaveMode.AUTOSAVE.toString());
		p.setProperty(PROPERTY_SEARCHABLE_EXT, "xml locale txt spui_t tlsa_t pctp_t pfx");
		p.setProperty(PROPERTY_PROJECTS_PATH, "//Projects\\");
		if (SporeGame.hasGalacticAdventures()) {
			p.setProperty(PROPERTY_GAME_PATH_TYPE, GamePathType.GALACTIC_ADVENTURES.toString());
		} else if (SporeGame.hasSpore()) {
			p.setProperty(PROPERTY_GAME_PATH_TYPE, GamePathType.SPORE.toString());
		}
		p.setProperty(PROPERTY_COLOR_SOURCE, ProjectTreeCellRenderer.generateColorString(ProjectTreeCellRenderer.COLOR_SOURCE));
		p.setProperty(PROPERTY_COLOR_SOURCEMOD, ProjectTreeCellRenderer.generateColorString(ProjectTreeCellRenderer.COLOR_SOURCE_MOD));
		p.setProperty(PROPERTY_COLOR_MOD, ProjectTreeCellRenderer.generateColorString(ProjectTreeCellRenderer.COLOR_MOD));
		
		return p;
	}
	
	public static boolean processArguments(String[] args) throws IOException
	{
		if (args.length > 0) 
		{
			//JOptionPane.showMessageDialog(null, Arrays.toString(args));
			if (args[0].equals("-convert")) {
				switch(args[1]) {
				case "PropToXml": 
					PropToXml.processCommand(args);
					break;
				case "XmlToProp": 
					XmlToProp.processCommand(args);
					break;
				case "TlsaToTxt": 
					TlsaToTxt.processCommand(args);
					break;
				case "TxtToTlsa": 
					TxtToTlsa.processCommand(args);
					break;
				case "PctpToTxt": 
					PctpToTxt.processCommand(args);
					break;
				case "TxtToPctp": 
					TxtToPctp.processCommand(args);
					break;
				case "SpuiToTxt": 
					SpuiToTxt.processCommand(args);
					break;
				case "TxtToSpui": 
					TxtToSpui.processCommand(args);
					break;
				case "Rw4ToDDS":
					Rw4ToDDS.processCommand(args);
					break;
				case "DDSToRw4":
					DDSToRw4.processCommand(args);
					break;
				case "DDStoRast":
					DDStoRast.processCommand(args);
					break;
				case "RastToDDS":
					RastToDDS.processCommand(args);
					break;
				case "EffectUnpacker":
					EffectUnpacker.processCommand(args);
					break;
				case "EffectPacker":
					EffectPacker.processCommand(args);
					break;
				case "DBPFUnpacker":
					DBPFUnpacker.processCommand(args);
					break;
				case "DBPFPacker-packModHere":
					DBPFPacker.processCommand(args);
					break;
				case "DBPFPacker-packModAs":
					DBPFPacker.processCommandFindOutput(args);
					break;
				}
				
				
			} else {
				System.err.println("Unknown command!");
				System.in.read();
			}
			
			return true;
		}
		
		return false;
	}
	
	public static void loadProjects()
	{
		try {
			projects = Project.loadProjects(getProjectsPath());
			
			projectsMap.clear();
			
			for (Project p : projects) {
				projectsMap.put(p.getProjectName(), p);
			}

			for (Project project : projects) {
				project.updateSources();
			}
			
			if (userInterface != null) {
				userInterface.updateProjects();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Project p : projects) {
			System.out.println(p.getProjectName() + "\t" + p.getProjectPath());
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		init();
		if (!processArguments(args)) {
			loadProjects();
			
			UIMainApp.init();
		}
	}
	
	private static void initPaths() {
		programPath = new File(System.getProperty("user.dir"));
		if (programPath == null || !programPath.exists())
		{
			try {
				programPath = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		projectsPath = new File(programPath, "Projects");
	}
	
	private static void initRegistries() throws IOException {
		registries = new HashMap<String, NameRegistry>();
		if (FILE_REG_PATH == null) FILE_REG_PATH = new File(MainApp.getProgramPath(), "reg_file.txt");
		if (TYPE_REG_PATH == null) TYPE_REG_PATH = new File(MainApp.getProgramPath(), "reg_type.txt");
		if (PROP_REG_PATH == null) PROP_REG_PATH = new File(MainApp.getProgramPath(), "reg_property.txt");
		if (SPUI_REG_PATH == null) SPUI_REG_PATH = new File(MainApp.getProgramPath(), "reg_spui.txt");
		
		registries.put(NameRegistry.NAME_FILE, new NameRegistry(FILE_REG_PATH));
		registries.put(NameRegistry.NAME_TYPE, new NameRegistry(TYPE_REG_PATH));
		registries.put(NameRegistry.NAME_PROP, new NameRegistry(PROP_REG_PATH));
		registries.put(NameRegistry.NAME_SPUI, new NameRegistry(SPUI_REG_PATH));
	}
	
	public static void init() {
		System.out.println(VERSION_INFO);
		long time1 = System.currentTimeMillis();
		SporeGame.init();
		//TODO read properties?
		initPaths();
		processSettings();
		System.out.println(projectsPath);
		try {
			initRegistries();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long resTime = System.currentTimeMillis() - time1;
		
		System.out.println("-- " + resTime/1000f + " s --");
	}
	
	public static void setLoader(ClassLoader loader) {
		Loader = loader;
	}
	
	public static boolean isLastUpdate(String releaseTag) {
		VersionInfo lastRelease = VersionInfo.fromString(releaseTag);
		
		System.out.println("Release version: " + VERSION_INFO);
		System.out.println("Last release: " + lastRelease);
		
		return VERSION_INFO.isLastRelease(lastRelease);
	}
	
}
