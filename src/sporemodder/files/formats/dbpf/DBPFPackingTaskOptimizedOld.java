package sporemodder.files.formats.dbpf;

import java.awt.Window;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.dbpf.RefPackCompression.CompressorOutput;
import sporemodder.files.formats.prop.PROPMain;
import sporemodder.userinterface.ErrorManager;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Project;
import sporemodder.utilities.Project.EditorsPackages;
import sporemodder.utilities.names.SimpleNameRegistry;
import sporemodder.utilities.performance.Profiler;

public class DBPFPackingTaskOptimizedOld {
	
	private static final int INDEX_PROGRESS = 20;
	private static final int BUFFER_SIZE = 8192;
	
	private static final String EDITORSPACKAGES_BP2 = "/sporemodder/files/resources/BoosterPack2.prop";
	private static final String EDITORSPACKAGES_EP1 = "/sporemodder/files/resources/ExpansionPack1.prop";
	
	
	private Window parent;
	private List<ConvertAction> converters;
	private int compressLimit = -1;
	private String inputPath;
	private HashMap<File, Exception> failedFiles;
	private DBPFMain dbpf;
	private boolean success = false;
	private EditorsPackages embedEditorsPackages;
	
	private final Profiler profiler = new Profiler();
	
	public DBPFPackingTaskOptimizedOld(DBPFMain dbpf, Project project, List<ConvertAction> converters, Window parent) {
		super();
		this.parent = parent;
		this.converters = converters;
		this.compressLimit = project.getCompressingLimit();
		this.inputPath = project.getProjectPath().getAbsolutePath();
		this.dbpf = dbpf;
		this.embedEditorsPackages = project.getEmbeddedEditorPackages();
	}
	
	public DBPFPackingTaskOptimizedOld(DBPFMain dbpf, String inputPath, int compressLimit, List<ConvertAction> converters, Window parent) {
		super();
		this.parent = parent;
		this.converters = converters;
		this.compressLimit = compressLimit;
		this.inputPath = inputPath;
		this.dbpf = dbpf;
	}
	
	private static String createAutolocaleFile(FileFormatStructure struct, String folder, String name) {
		if (struct instanceof PROPMain) {
			PROPMain prop = (PROPMain) struct;
			//int nameID = Hasher.getFileHash("auto_" + folder + "_" + name);
			int nameID = Hasher.stringToFNVHash("auto_" + folder + "_" + name);
			return prop.createAutolocaleFile(nameID);
		}
		return null;
	}
	
	private static void writeAutolocaleFile(DBPFMain dbpf, String autoLocale, String folder, String name) throws IOException {
		if (autoLocale != null) {
			int nameID = Hasher.getFileHash("auto_" + folder + "_" + name);
			DBPFItem item = new DBPFItem();
			item.key.setGroupID(Hasher.getFileHash("locale~"));
			item.key.setInstanceID(nameID);
			item.key.setTypeID(Hasher.getTypeHash("locale"));
			item.chunkOffset = dbpf.source.getFilePointer();
			dbpf.source.write(autoLocale.getBytes("US-ASCII"));
			item.memSize = dbpf.source.getFilePointer() - item.chunkOffset;
			item.compressedSize = item.memSize;
			item.isCompressed = false;
			
			dbpf.index.items.add(item);
		}
	}

	public void execute() throws Exception {
		try {
			
			boolean alreadyHasEditorsPackages = false;
			
			int originalBasePos = dbpf.source.getBaseOffset();
			dbpf.source.setBaseOffset(originalBasePos + dbpf.basePos);
			
			dbpf.source.writePadding(96); // the header will be written after, when we have all the positions
			
			// If we don't have an index, we'll create it here, as we need to add the items to it
			if (dbpf.header == null) {
				dbpf.header = new DBPFHeader();
			}
			if (dbpf.index == null) {
				dbpf.index = new DBPFIndex();
			}
			if (dbpf.index.items == null) {
				dbpf.index.items = new ArrayList<DBPFItem>();
				dbpf.index.itemsPos = dbpf.source.getFilePointer();
			}
			
			profiler.startMeasure(PerformanceTest.PROFILER_GET_FOLDERS);
			
			File[] folders = new File(inputPath).listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.isDirectory();
				}
				
			});
			System.out.println(folders.length + " folders in project");
			
			profiler.endMeasure(PerformanceTest.PROFILER_GET_FOLDERS);
			
			failedFiles = new HashMap<File, Exception>();
			
			float inc = (100.0f - INDEX_PROGRESS) / folders.length;
			float progress = 0;
			
			// set this or sporemaster/names.txt
			Hasher.UsedNames = new SimpleNameRegistry();
			
			
			for (File folder : folders) {
				
				String folderName = folder.getName();
				int folderHash = Hasher.getFileHash(folderName);
				
				File[] files = folder.listFiles();
				
				for (File file : files) {
					
					profiler.startMeasure(PerformanceTest.PROFILER_PROCESS_NAMES);
					
					DBPFItem item = null;
					byte[] input = null;
					String name = file.getName();
					if (!file.isFile()) {
						if (name.contains(".") && !name.endsWith(".effdir.unpacked")) {
							File newFile = new File(file, name);
							if (!newFile.exists()) {
								failedFiles.put(file, new UnsupportedOperationException("Couldn't find file " + name + " inside subfolder " + name));
								continue;
							}
							file = newFile;
						}
					}
					
					int dotIndex = name.indexOf(".");
					String fileName;
					String extension;
					
					if (dotIndex != -1) {
						fileName = name.substring(0, dotIndex);
						extension = name.substring(dotIndex + 1);
					}
					else {
						fileName = name;
						extension = "";
					}
					
					profiler.endMeasure(PerformanceTest.PROFILER_PROCESS_NAMES);
					
					profiler.startMeasure(PerformanceTest.PROFILER_CONVERT);
					
					for (ConvertAction converter : converters) {
						if (converter.isValid(file)) {
							int nameHash = Hasher.getFileHash(fileName);
							int typeHash = converter.getOutputExtensionID(extension);
							
							
							item = new DBPFItem();
							item.key.setGroupID(folderHash);
							item.key.setInstanceID(nameHash);
							item.key.setTypeID(typeHash);
							item.chunkOffset = dbpf.source.getFilePointer();
							
							//TODO Known bug: these files won't be compressed
							try {
								// set base pointer for those formats that work with offsets (like RW4)
								int basePos = dbpf.source.getBaseOffset();
								dbpf.source.setBaseOffset(item.chunkOffset);
								//FileFormatStructure struct = converter.convert(file.getAbsolutePath(), dbpf.source);
								FileFormatStructure struct = converter.process(file);
								String autolocale = createAutolocaleFile(struct, folderName, fileName);
								struct.write(dbpf.source);
								
								dbpf.source.setBaseOffset(basePos);
								
								item.memSize = dbpf.source.getFilePointer() - item.chunkOffset;
								item.compressedSize = item.memSize;
								
								if (autolocale != null) {
									writeAutolocaleFile(dbpf, autolocale, folderName, fileName);
								}
								
							} catch (Exception e) {
								// e.printStackTrace();
								failedFiles.put(file, e);
							}
							
							input = new byte[0]; //TODO dirty solution!!!
							
							break;
						}
					}
					
					profiler.endMeasure(PerformanceTest.PROFILER_CONVERT);
					
					profiler.startMeasure(PerformanceTest.PROFILER_WRITE_FILE);
					
					if (input == null) {
						
						if (!file.isFile()) {
							if (name.contains(".") && !name.endsWith(".effdir")) {
								File newFile = new File(file, name);
								if (!newFile.exists()) {
									failedFiles.put(file, new UnsupportedOperationException("Couldn't find file " + name + " inside subfolder " + name));
									continue;
								}
								file = newFile;
							}
							else {
								failedFiles.put(file, new UnsupportedOperationException("Nested subfolders are not supported. File: " + name));
								continue;
							}
						}
						
						item = new DBPFItem();
						
						int nameHash = Hasher.getFileHash(fileName);
						int typeHash = Hasher.getTypeHash(extension);
						
						item.key.setGroupID(folderHash);
						item.key.setInstanceID(nameHash);
						item.key.setTypeID(typeHash);
						item.chunkOffset = dbpf.source.getFilePointer();
						
						input = Files.readAllBytes(file.toPath());
						
						//TODO do this for converted files too
						if (compressLimit != -1 && file.length() > compressLimit) {
							CompressorOutput compressOut = new CompressorOutput();
							RefPackCompression.compress(input, compressOut);

							dbpf.source.write(compressOut.data, 0, compressOut.lengthInBytes);
							item.isCompressed = true;
							item.memSize = input.length;
							item.compressedSize = dbpf.source.getFilePointer() - item.chunkOffset;
						}
						else {
							dbpf.source.write(input);
							item.memSize = dbpf.source.getFilePointer() - item.chunkOffset;
							item.compressedSize = item.memSize;
						}
					}
					
					profiler.endMeasure(PerformanceTest.PROFILER_WRITE_FILE);
					
					if (!alreadyHasEditorsPackages && item.key.getGroupID() == 0x40404000) {
						alreadyHasEditorsPackages = true;
					}
					
					dbpf.index.items.add(item);
				}
				
				progress += inc;
			}
			
			profiler.startMeasure(PerformanceTest.PROFILER_EXTRA_FILES);
			
			// create sporemaster/names.txt
			if (Hasher.UsedNames != null) {
				byte[] array = null;
				// by default it allocates a new array. There's no need for that
				try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream() {
					@Override
					public byte[] toByteArray() {
						return buf;
					}
				}) {
					// we try here because when the writer is closed, it will write it to the OutputStream
					try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(arrayOutputStream))) {
						Hasher.UsedNames.write(writer);
					}
					array = arrayOutputStream.toByteArray();
				}
				
				// disable name logging
				Hasher.UsedNames = null;
				
				if (array != null) {
					DBPFItem item = new DBPFItem();
					item.key.setGroupID(Hasher.getFileHash("sporemaster"));
					item.key.setInstanceID(Hasher.getFileHash("names"));
					item.key.setTypeID(Hasher.getTypeHash("txt"));
					item.chunkOffset = dbpf.source.getFilePointer();
					dbpf.source.write(array);
					item.memSize = dbpf.source.getFilePointer() - item.chunkOffset;
					item.compressedSize = item.memSize;
					
					dbpf.index.items.add(item);
				}
			}
			
			// embed editorsPackages
			if (embedEditorsPackages != EditorsPackages.NONE && !alreadyHasEditorsPackages) {
				
				DBPFItem item = new DBPFItem();
				item.key.setGroupID(0x40404000);
				item.key.setTypeID(0x00B1B104);
				item.chunkOffset = dbpf.source.getFilePointer();
				
				String path = null;
				
				if (embedEditorsPackages == EditorsPackages.BOT_PARTS) {
					item.key.setInstanceID(Hasher.getFileHash("BoosterPack2"));
					path = EDITORSPACKAGES_BP2;
				}
				else if (embedEditorsPackages == EditorsPackages.PATCH51) {
					item.key.setInstanceID(Hasher.getFileHash("ExpansionPack1"));
					path = EDITORSPACKAGES_EP1;
				}
				
				try (InputStream is = DBPFPackingTaskOptimizedOld.class.getResourceAsStream(path)) {
					byte[] buffer = new byte[BUFFER_SIZE];
					int n;
					
					while ((n = is.read(buffer)) > 0) {
						dbpf.source.write(buffer, 0, n);
					}
				}
				
				item.memSize = dbpf.source.getFilePointer() - item.chunkOffset;
				item.compressedSize = item.memSize;
				
				dbpf.index.items.add(item);
			}
			
			profiler.endMeasure(PerformanceTest.PROFILER_EXTRA_FILES);
			
			profiler.startMeasure(PerformanceTest.PROFILER_WRITE_INDEX);
			
			dbpf.header.indOffset = dbpf.source.getFilePointer();
			System.out.println(dbpf.header.indOffset);
			dbpf.index.write(dbpf.source);
			
			//TODO do this dynamically?
			progress += INDEX_PROGRESS;
			
			dbpf.header.indSize = dbpf.source.getFilePointer() - dbpf.header.indOffset;
			dbpf.header.indCount = dbpf.index.items.size();
			dbpf.source.seek(0);
			dbpf.header.write(dbpf.source);
			
			profiler.endMeasure(PerformanceTest.PROFILER_WRITE_INDEX);
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Error writing DBPF: \n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void done() {

		if (failedFiles.size() > 0) {
			new UIErrorsDialog(failedFiles);
			success = false;
		}
		else
		{
			success = true;
		}
		
		if (parent != null) {
			parent.dispose();
		}
	}
	
	public boolean wasSuccessful() {
		return success;
	}
	
	public Profiler getProfiler() {
		return profiler;
	}
}
