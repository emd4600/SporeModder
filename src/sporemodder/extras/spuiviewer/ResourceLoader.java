package sporemodder.extras.spuiviewer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Project;

public class ResourceLoader {
	
	private static final HashMap<SPUIFileResource, BufferedImage> loadedImages = new HashMap<SPUIFileResource, BufferedImage>();

	public static BufferedImage loadImage(String path) throws IOException {
		return ImageIO.read(new File(path));
	}
	
	// parentFolder is the folder where the SPUI is contained
	public static BufferedImage loadImage(SPUIFileResource fileResource, File parentFolder) throws IOException {
		
		BufferedImage image = loadedImages.get(fileResource);
		if (image != null) {
			return image;
		}
		
		// First we check if there's such file in the parentFolder
		String folderName = Hasher.getFileName(fileResource.getGroupID());
		String fileName = Hasher.getFileName(fileResource.getFileID()) + "." + Hasher.getTypeName(fileResource.getTypeID());
		File file = new File(new File(parentFolder, folderName), fileName);
		
		if (!file.exists()) {
			// Check it in the parent folder of parentFolder
			file = new File(new File(parentFolder.getParentFile(), folderName), fileName);
		}
		if (!file.exists()) {
			// Check in project
			Project project = MainApp.getCurrentProject();
			if (project != null) {
				file = project.getFile(folderName + "/" + fileName);
			}
		}
		
		if (file == null) {
			throw new FileNotFoundException("Image " + fileResource.getStringSimple() + " not found.");
		}
		
		image = ImageIO.read(file);
		loadedImages.put(fileResource, image);
		return image;
	}
	
	public static SPUIMain loadSpui(File file) throws IOException {
		try (InputStreamAccessor in = new FileStreamAccessor(file, "r")) {
			SPUIMain spui = new SPUIMain();
			spui.read(in);
			return spui;
		}
	}
	
	public static SPUIMain loadSpuiText(File file) throws IOException, ArgScriptException {
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			SPUIMain spui = new SPUIMain();
			spui.parse(in);
			return spui;
		}
	}
	
	public static void saveSpui(SPUIMain spui, File file) throws IOException {
		try (OutputStreamAccessor out = new FileStreamAccessor(file, "rw")) {
			spui.write(out);
		}
	}
	
	public static void saveSpuiText(SPUIMain spui, File file) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			spui.toArgScript().write(out);
		}
	}
}
