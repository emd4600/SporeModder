package sporemodder.files.formats.dbpf;

import java.awt.Window;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
import sporemodder.utilities.names.SimpleNameRegistry;

public class DBPFPackingTask extends SwingWorker<Void, Void> {
	
	private static final int INDEX_PROGRESS = 20;
	
	private Window parent;
	private List<ConvertAction> converters;
	private int compressLimit = -1;
	private String inputPath;
	private HashMap<File, Exception> failedFiles;
	private DBPFMain dbpf;
	private boolean success = false;
	
	public DBPFPackingTask(DBPFMain dbpf, Project project, List<ConvertAction> converters, Window parent) {
		super();
		this.parent = parent;
		this.converters = converters;
		this.compressLimit = project.getCompressingLimit();
		this.inputPath = project.getProjectPath().getAbsolutePath();
		this.dbpf = dbpf;
	}
	
	public DBPFPackingTask(DBPFMain dbpf, String inputPath, int compressLimit, List<ConvertAction> converters, Window parent) {
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
			item.group = Hasher.getFileHash("locale~");
			item.name = nameID;
			item.type = Hasher.getTypeHash("locale");
			item.chunkOffset = dbpf.source.getFilePointer();
			dbpf.source.write(autoLocale.getBytes("US-ASCII"));
			item.memSize = dbpf.source.getFilePointer() - item.chunkOffset;
			item.compressedSize = item.memSize;
			item.isCompressed = false;
			
			dbpf.index.items.add(item);
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			
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
			
			File[] folders = new File(inputPath).listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.isDirectory();
				}
				
			});
			System.out.println(folders.length + " folders in project");
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
					String fileName = name.substring(0, dotIndex);
					String extension = name.substring(dotIndex + 1);
					
					for (ConvertAction converter : converters) {
						if (converter.isValid(file)) {
							int nameHash = Hasher.getFileHash(fileName);
							int typeHash = converter.getOutputExtensionID(extension);
							
							
							item = new DBPFItem();
							item.group = folderHash;
							item.name = nameHash;
							item.type = typeHash;
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
						
						item.group = folderHash;
						item.name = nameHash;
						item.type = typeHash;
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
					
					dbpf.index.items.add(item);
				}
				
				progress += inc;
				setProgress((int) progress);
			}
			
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
					item.group = Hasher.getFileHash("sporemaster");
					item.name = Hasher.getFileHash("names");
					item.type = Hasher.getTypeHash("txt");
					item.chunkOffset = dbpf.source.getFilePointer();
					dbpf.source.write(array);
					item.memSize = dbpf.source.getFilePointer() - item.chunkOffset;
					item.compressedSize = item.memSize;
					
					dbpf.index.items.add(item);
				}
			}
			
			
			dbpf.header.indOffset = dbpf.source.getFilePointer();
			System.out.println(dbpf.header.indOffset);
			dbpf.index.write(dbpf.source);
			
			//TODO do this dynamically?
			progress += INDEX_PROGRESS;
			setProgress((int) progress);
			
			dbpf.header.indSize = dbpf.source.getFilePointer() - dbpf.header.indOffset;
			dbpf.header.indCount = dbpf.index.items.size();
			dbpf.source.seek(0);
			dbpf.header.write(dbpf.source);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Error writing DBPF: \n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}
	
	@Override
	public void done() {

		if (failedFiles.size() > 0) {
			new UIErrorsDialog(failedFiles);
			success = false;
		}
		else
		{
			success = true;
		}
		
		parent.dispose();
	}
	
	public boolean wasSuccessful() {
		return success;
	}
}
