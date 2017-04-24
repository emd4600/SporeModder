package sporemodder.utilities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

public class SporeGame {
	private static boolean is64bit;
	
	private static SporeGame SPORE = null;
	private static SporeGame GALACTIC_ADVENTURES = null;
	
	private String installLoc;
	private String sporebinFolder;
	
	private SporeGame(String path, String sporebinFolder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		String regPath = "SOFTWARE\\" + (is64bit ? "Wow6432Node\\" : "") + "Electronic Arts\\" + path;
		
		String str = WinRegistry.valueForKey(WinRegistry.HKEY_LOCAL_MACHINE, regPath, "InstallLoc");
		if (str == null) {
			// the recent patch changed InstallLoc to Install Dir. I don't know if the information it has is the same
			str = WinRegistry.valueForKey(WinRegistry.HKEY_LOCAL_MACHINE, regPath, "Install Dir");
		}
		installLoc = str.substring(1, str.length() - 1);
		this.sporebinFolder = sporebinFolder;
	}
	
	public String getInstallLoc() {
		return installLoc;
	}
	
	public String getDataDir() {
		return installLoc + "\\Data";
	}
	
	public String getGamePath() {
		return installLoc + "\\" + sporebinFolder + "\\SporeApp.exe";
	}
	
	public Process execute() throws IOException {
		if (installLoc == null) return null;
		return new ProcessBuilder(installLoc + "\\" + sporebinFolder + "\\SporeApp.exe").start();
	}
	
	public Process execute(String ... args) throws IOException {
		if (installLoc == null) return null;
		
		String[] newArgs = new String[args.length + 1];
		newArgs[0] = installLoc + "\\" + sporebinFolder + "\\SporeApp.exe";
		System.arraycopy(args, 0, newArgs, 1, args.length);
		return new ProcessBuilder(newArgs).start();
	}
	
	public static Process execute(String path, String ... args) throws IOException, URISyntaxException {
		if (path == null) return null;
		
		String[] newArgs = new String[args.length + 1];
//		newArgs[0] = path;
//		newArgs[0] = new File(path).toURI().getPath();
		System.arraycopy(args, 0, newArgs, 1, args.length);
		//return new ProcessBuilder(newArgs).start();
		
		//return Runtime.getRuntime().exec(newArgs);
		
		if (new File(path).exists()) {
			newArgs[0] = path;
			System.out.println(newArgs[0]);
			return new ProcessBuilder(newArgs).start();
		}
		else {
			// For Steam URIs
			System.out.println(path);
			Desktop.getDesktop().browse(new URI(path));
			return null;
		}
		
	}
	
	public static boolean hasSpore() {
		return SPORE != null;
	}
	
	public static boolean hasGalacticAdventures() {
		return GALACTIC_ADVENTURES != null;
	}
	
	public static SporeGame getSpore() {
		return SPORE;
	}
	
	public static SporeGame getGalacticAdventures() {
		return GALACTIC_ADVENTURES;
	}
	
	public static void init() {
		is64bit = false;
		if (System.getProperty("os.name").contains("Windows")) {
		    is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
		    is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}
		
		try {
			SPORE = new SporeGame("SPORE", "Sporebin");
		} catch (Exception e) {
			System.err.println("Couldn't find Spore!");
			e.printStackTrace();
			SPORE = null;
		}
		
		try {
			GALACTIC_ADVENTURES = new SporeGame("SPORE_EP1", "SporebinEP1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Couldn't find Galactic Adventures!");
			e.printStackTrace();
			GALACTIC_ADVENTURES = null;
		}
	}
}
