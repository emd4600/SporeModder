package sporemodder.files.formats.dbpf;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import sporemodder.MainApp;
import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.ReadWriteStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.dbpf.RefPackCompression.CompressorOutput;
import sporemodder.files.formats.prop.PROPMain;
import sporemodder.files.formats.prop.PropertyBool;
import sporemodder.files.formats.prop.PropertyInt32;
import sporemodder.files.formats.prop.PropertyText;
import sporemodder.userinterface.ErrorManager;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Project;

public class DBPFMain implements AutoCloseable {

	public static final String[] BANNED_PACKAGE_NAMES = new String[] {
		"Spore_Game.package", "Spore_Graphics.package", "Spore_Audio1.package", "Spore_Audio2.package", "Spore_Content.package",
		"Spore_Pack_03.package", "PatchData.package", "EP1_PatchData.package", "Spore_EP1_Content_01.package", "Spore_EP1_Content_02.package",
		"Spore_EP1_Locale_01.package", "Spore_EP1_Data.package", "BoosterPack_01.package"
	}; 
	
	protected DBPFHeader header;
	protected DBPFIndex index;
	private PROPMain packageProperties;
	private boolean hasPackageProperties;

	protected ReadWriteStreamAccessor source;
	protected int basePos; //TODO must use it when accessing file process
	
	public DBPFMain() {}
	
	//TODO Do we need both ways?
	public DBPFMain(ReadWriteStreamAccessor in) throws IOException {
		source = in;
		basePos = in.getFilePointer();
		read(true);
	}
	
	public DBPFMain(ReadWriteStreamAccessor in, boolean read) throws IOException, FileStructureException {
		source = in;
		basePos = in.getFilePointer();
		if (read) {
			read(true);
		}
	}
	
	public ReadWriteStreamAccessor getSource() {
		return source;
	}
	
	public void setSource(ReadWriteStreamAccessor in) throws IOException {
		source = in;
		basePos = in.getFilePointer();
	}
	
	private boolean updateHeader() {
		if (source != null) {
			try {
				source.seek(basePos);
				header = new DBPFHeader();
				header.read(source);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean updateIndex() {
		if (source != null) {
			try {
				if (header == null) {
					updateHeader();
				}
				source.seek(basePos + header.indOffset);
				index = new DBPFIndex();
				index.read(source, header.type);
				index.readItemsInfo(source, header.indCount);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean updateHasPackageProperties() {
		if (index == null) {
			updateIndex();
		}
		int smallestPos = 0x7FFFFFFF; // header size
		for (DBPFItem item : index.items) {
			if (item.chunkOffset < smallestPos) {
				smallestPos = item.chunkOffset;
			}
		}
		
		if (smallestPos > 96) {
			hasPackageProperties = true;
		} else {
			hasPackageProperties = false;
		}
		return true;
	}
	
	private boolean updatePackageProperties() {
		if (!hasPackageProperties) {
			updateHasPackageProperties();
		}
		if (hasPackageProperties) {
			try {
				int oldBaseOffset = source.getBaseOffset();
				source.setBaseOffset(96);
				source.seek(0);
				ByteArrayStreamAccessor out = new ByteArrayStreamAccessor(RefPackCompression.getDecompressedSize(source)); 
				try {
					RefPackCompression.decompress(source, out);
					out.seek(0);
					packageProperties = new PROPMain();
					packageProperties.readProp(out);
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException
						| SecurityException | NoSuchFieldException
						| FileStructureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					out.close();
				}
				source.setBaseOffset(oldBaseOffset);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Get the main information of this DBPF, which is contained in a DBPFHeader object. If the file hasn't been read, the header of the DBPF will be read
	 * and returned; if there's no source file for the DBPF or there's an error while reading the header, this method will return null.
	 * 
	 * @return DBPF's header or null if there's no source or there was an error while reading the header.
	 */
	public DBPFHeader getHeader() {
		if (header == null) 
		{
			updateHeader();
		}
		return header;
	}

	/** 
	 * Get the index of this DBPF, which is contained in a DBPFIndex object. If the file hasn't been read, the index of the DBPF will be read, including its items' info; 
	 * if there's no source file for the DBPF or there's an error while reading the index, this method will return null.
	 * 
	 * @return DBPF's index or null if there's no source or there was an error while reading the index.
	 */
	public DBPFIndex getIndex() {
		if (index == null)
		{
			updateIndex();
		}
		
		return index;
	}
	
	/**
	 * Tells whether this DBPF has package properties or not. If the DBPF hasn't been read yet, the header and the index will be read to check if there are any package properties; 
	 * if the DBPF has no package properties or there's an error while reading it, this method will return false.
	 * 
	 * @return true if the DBPF has package properties, false if not or there was an error while reading the file.
	 */
	public boolean hasPackageProperties() {
		if (packageProperties == null) {
			updateHasPackageProperties();
		}
		return hasPackageProperties;
	}

	/**
	 * Get the package properties of this DBPF, in the form of a property file (PROPMain). If the DBPF hasn't been read yet, the header and the index will be read to check if there
	 * are any package properties and, if tehre are, those will be read too; if the DBPF has no package properties or there's an error while reading it, this method will return null.
	 * @return
	 */
	public PROPMain getPackageProperties() {
		if (packageProperties == null) {
			if (!hasPackageProperties) {
				updateHasPackageProperties();
			}
			updatePackageProperties();
		}
		return packageProperties;
	}
	
	/** 
	 * Read the DBPF metadata (header and index) from the StreamAccessor in, starting at the source's file pointer when this was passed into the constructor.
	 * 
	 * 
	 * @param readItems If true, it will read and store all the items' metadata.
	 * 
	 * @throws IOException If there was a problem with the file, e.g. it ended before than expected.
	 * @throws FileStructureException If the given file doesn't follow the supported structure.
	 * 
	 */
	public void read(boolean readItems) throws IOException {
		header = new DBPFHeader();
		header.read(source);
		index = new DBPFIndex();
		index.read(source, header.type);
		
		if (readItems) {
			index.readItemsInfo(source, header.indCount);
		}
	}
	
	/** 
	 * Searches a file whose group, name and type match with the given ones. It returns the file metadata (DBPFItem) which then can be used to unpack the item content.
	 * 
	 * @param group Hash of the file group.
	 * @param name Hash of the file name.
	 * @param type Hash of the file type.
	 * @return Returns the found DBPFItem or null, if the file wasn't found.
	 * @throws IOException If the items' metadata wasn't read.
	 */
	public DBPFItem getFile(int group, int name, int type) throws IOException {
		if (index.items == null) throw new IOException("DBPF Items' metadata isn't initialized");
		for (DBPFItem item : index.items) {
			if (item.key.getGroupID() == group && item.key.getInstanceID() == name && item.key.getTypeID() == type) {
				return item;
			}
		}
		
		return null;
	}
	/** 
	 * Searches all the files whose type matches the given type.
	 * 
	 * @param type Hash of the type used as filter.
	 * @return Returns a list with all the items matching the filter.
	 * @throws IOException If the items' metadata wasn't read.
	 */
	public List<DBPFItem> getFilesByType(int type) throws IOException {
		if (index.items == null) throw new IOException("DBPF Items' metadata isn't initialized");
		
		List<DBPFItem> items = new ArrayList<DBPFItem>();
		for (DBPFItem item : index.items) {
			if (item.key.getTypeID() == type) {
				items.add(item);
			}
		}
		
		return items;
	}
	
	public static void parseArgs(String[] args) throws IOException {
		String inPath = null;
		String outPath = null;
		boolean merge = false;
		boolean keepOld = false;
		boolean decode = false;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dbpf")) {
				decode = args[++i].equals("-d") ? true : false;
				inPath = args[++i];
				outPath = args[++i];
				if (decode) {
					merge = Boolean.parseBoolean(args[++i]);
					keepOld = Boolean.parseBoolean(args[++i]);
				}
			}
		}
		
		if (decode) {
			try (DBPFMain dbpf = new DBPFMain()) {
				outPath += ".unpacked"; //to avoid files with the same name
				if (!outPath.endsWith("\\")) outPath += "\\";
				dbpf.setSource(new FileStreamAccessor(inPath, "r"));
				dbpf.read(true);
				dbpf.writeProject(outPath, merge, keepOld);
			} catch (IllegalArgumentException | SecurityException
					| TransformerFactoryConfigurationError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.in.read();
			}
		}
	}
	
	private static void getFileInfo() throws IOException
	{
//		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore\\Data\\";
		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore Aventuras Galacticas\\Data\\";
//		String packageName = "Spore_Pack_03.package";
//		String packageName = "PatchData.package";
		String packageName = "!!!SPUI_Test.package";
//		String packageName = "Spore_Graphics.package";
//		String packageName = "EP1_PatchData.package";
		
//		int[] desiredFile = new int[] {Hasher.getFileHash("editor_rigblocks~"), Hasher.getFileHash("SRNS-ce_test_bones1"), Hasher.getTypeHash("rw4")};  // EAX == 84397611
//		int[] desiredFile = new int[] {Hasher.getFileHash("editor_rigblocks~"), Hasher.getFileHash("ce_grasper_radial_01"), Hasher.getTypeHash("rw4")};
//		int[] desiredFile = new int[] {Hasher.getFileHash("animations~"), Hasher.getFileHash("#7A305113"), Hasher.getTypeHash("rw4")};
//		int[] desiredFile = new int[] {Hasher.getFileHash("#40212002"), Hasher.getFileHash("#00000000"), Hasher.getTypeHash("cpp")};
		int[] desiredFile = new int[] {Hasher.getFileHash("layouts_atlas~"), Hasher.getFileHash("#5B168EB1"), Hasher.getTypeHash("spui")};
		
		FileStreamAccessor in = new FileStreamAccessor(sporeDataPath + packageName, "r");
		try (DBPFMain dbpf = new DBPFMain(in)){
			DBPFItem item = dbpf.getFile(desiredFile[0], desiredFile[1], desiredFile[2]);
			
			dbpf.header.print();
			System.out.println();
			item.print();
			
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	private static void getChunkOffsets() throws IOException
	{
//		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore\\Data\\";
		String sporeDataPath = "C:\\Program Files (x86)\\Electronic Arts\\SPORE_BP1\\";
//		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore Aventuras Galacticas\\Data\\";
		String packageName = "BoosterPack_01.package";
		
		FileStreamAccessor in = new FileStreamAccessor(sporeDataPath + packageName, "r");
		
		try {
			DBPFMain dbpf = new DBPFMain(in);
			List<Integer> chunkOffsets = new ArrayList<Integer>(dbpf.header.indCount);
			for (DBPFItem item : dbpf.index.items)
			{
				chunkOffsets.add(item.chunkOffset);
			}
			
			Collections.sort(chunkOffsets);

			System.out.println("First chunkOffset: " + chunkOffsets.get(0));
			
		} finally {
			in.close();
		}
	}
	
	private static void printPackageProperties() throws IOException {
		String[] sporeDataPath = new String[] {"C:\\Program Files (x86)\\Juegos\\Spore Aventuras Galacticas\\Data\\",
				"C:\\Program Files (x86)\\Juegos\\Spore\\Data\\",
				"C:\\Program Files (x86)\\Electronic Arts\\SPORE_BP1\\"};
		
		for (String dataFolder : sporeDataPath) {
			File[] files = new File(dataFolder).listFiles();
			
			for (File f : files) {
				if (f.isFile() && f.getName().endsWith(".package")) {
					FileStreamAccessor in = new FileStreamAccessor(f, "r");
					//System.out.println(f.getName());
					try {
						DBPFMain dbpf = new DBPFMain(in);
						if (dbpf.hasPackageProperties()) {
							PROPMain prop = dbpf.getPackageProperties();
	
							System.out.println("Package name: " + f.getName() + "\n\t" + 
									"Expansion pack \"" + prop.<PropertyText>getArrayProperty("packageTitle", PropertyText.class).getValues().get(0).getText()
									+ "\";  \tpriority: " + (prop.contains("packagePriority") ? 
											prop.<PropertyInt32>getProperty("packagePriority", PropertyInt32.class).getValue() : "None")
									+ "\tblessCheck: " + (prop.contains("packageBlessCheck") ? 
											prop.<PropertyBool>getProperty("packageBlessCheck", PropertyBool.class).getValue() : "None"));
						}
						
					} finally {
						in.close();
					}
				}
			}
		}
	}
	
	private static void getFileNames() throws IOException
	{
//		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore\\Data\\";
//		String sporeDataPath = "C:\\Program Files (x86)\\Electronic Arts\\SPORE_BP1\\";
		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore Aventuras Galacticas\\Data\\";
		String packageName = "BP2_Data_test.package";
		
		FileStreamAccessor in = new FileStreamAccessor(sporeDataPath + packageName, "r");
		
		try {
			DBPFMain dbpf = new DBPFMain(in);
			for (DBPFItem item : dbpf.index.items)
			{
				System.out.println(Hasher.getFileName(item.name) + "!" + Hasher.getFileName(item.group) + "." + Hasher.getFileName(item.type) + 
						"\tchunkOffset: " + item.chunkOffset);
			}
		} finally {
			in.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.init();
		
//		printPackageProperties();
		getFileInfo();
//		packDBPF();
//		getFileNames();
		
//		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore Aventuras Galacticas\\Data\\";
//		String packageName = "BP2_Data.package";
//		
//		FileStreamAccessor in = new FileStreamAccessor(sporeDataPath + packageName, "r");
//		//FileStreamAccessor out = new FileStreamAccessor("C:\\Users\\Eric\\Desktop\\BP2_Data.prop", "rw");
//		
//		try {
//			DBPFMain dbpf = new DBPFMain(in);
//			dbpf.getHeader().print();
//			//DBPFItem item = dbpf.getFile(Hasher.getFileHash("editorsPackages~"), Hasher.getFileHash("BoosterPack2"), Hasher.getTypeHash("prop"));
//			//item.processNormalFile(in, out);
//			
//		} catch (FileStructureException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			in.close();
//			//out.close();
//		}
		
		//getChunkOffsets();
////		String sporeDataPath = "C:\\Program Files (x86)\\Electronic Arts\\SPORE_BP1\\";
//		String sporeDataPath = "C:\\Program Files (x86)\\Juegos\\Spore Aventuras Galacticas\\Data\\";
////		String packageName = "BoosterPack_01.package";
//		String packageName = "BP2_Data.package";
//		
//		FileStreamAccessor in = new FileStreamAccessor(sporeDataPath + packageName, "r");
//		FileStreamAccessor out = new FileStreamAccessor("C:\\Users\\Eric\\Desktop\\packageProperties.xml", "rw");
//		
//		try {
//			DBPFMain dbpf = new DBPFMain(in);
//			System.out.println("hasPackageProperties: " + dbpf.hasPackageProperties());
//			
//			PROPMain prop = dbpf.getPackageProperties();
//			
//			prop.writeXML(out);
//			
//		} catch (FileStructureException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			in.close();
//			out.close();
//		}
	}

	@Override
	public void close() throws IOException {
		if (source != null) source.close();
	}
	
	
//	public static DBPFPackingTask packProject(JDialog parent, List<ConvertAction> converters, Project project) throws Exception {
//
//		try (DBPFMain dbpf = new DBPFMain(new FileStreamAccessor(project.getPackageFile(), "rw"), false)) 
//		{
//			return dbpf.new DBPFPackingTask(parent, converters, project);
//		} catch (Exception e) {
//			throw e;
//		}
//		
//	}
}
