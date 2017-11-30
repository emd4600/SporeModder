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

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.MemoryOutputStream;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FastConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.dbpf.RefPackCompression.CompressorOutput;
import sporemodder.userinterface.ErrorManager;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Project;
import sporemodder.utilities.Project.EditorsPackages;
import sporemodder.utilities.names.SimpleNameRegistry;

public class DBPFPackingTask extends SwingWorker<Void, Void> {
	
	private static final int BUFFER_SIZE = 8192;
	
	private static final String EDITORSPACKAGES_BP2 = "/sporemodder/files/resources/BoosterPack2.prop";
	private static final String EDITORSPACKAGES_EP1 = "/sporemodder/files/resources/ExpansionPack1.prop";
	
	private Window parentWindow;
	private int nCompressThreshold = -1;
	
	private String inputPath;
	private String outputPath;
	
	private HashMap<File, Exception> failedFiles;
	private boolean bSuccess = false;
	private EditorsPackages embedEditorsPackages;
	
	private final List<ConvertAction> converters = new ArrayList<ConvertAction>();
	private final List<FastConvertAction> fastConverters = new ArrayList<FastConvertAction>();
	
	/** A memory stream to temporarily store the index data. This is more efficient than creating new objects for every single item. */
	private final MemoryOutputStream indexStream = new MemoryOutputStream();
	
	/** A stream for writing this DBPF. */
	private OutputStreamAccessor outputStream;
	
	private int nItemsCount;
	
	private DebugInformation debugInfo;
	
	// Temporary things
	private String currentFolderName;
	private String currentFileName;
	private String currentExtension;
	
	private int currentGroupID;
	private int currentInstanceID;
	private int currentTypeID;
	
	private byte[] currentInputData;
	private int nCurrentInputLength;
	
	private final CompressorOutput compressOut = new CompressorOutput();
	
	public DBPFPackingTask(Project project, Window parentWindow, boolean bDebugInformation) {
		super();
		this.parentWindow = parentWindow;
		this.nCompressThreshold = project.getCompressingLimit();
		this.inputPath = project.getProjectPath().getAbsolutePath();
		this.outputPath = project.getPackageFile().getAbsolutePath();
		this.embedEditorsPackages = project.getEmbeddedEditorPackages();
		
		addConverters(project.getPackingConverters());
		
		if (bDebugInformation) {
			debugInfo = new DebugInformation(project.getProjectName(), inputPath);
		}
	}
	
	public DBPFPackingTask(String outputPath, String inputPath, int nCompressThreshold, List<ConvertAction> converters, Window parentWindow) {
		super();
		this.parentWindow = parentWindow;
		this.nCompressThreshold = nCompressThreshold;
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		
		addConverters(converters);
	}
	
	private void addConverters(List<ConvertAction> list) {
		if (list != null) {
			for (ConvertAction action : list) {
				if (action instanceof FastConvertAction) {
					fastConverters.add((FastConvertAction) action);
				}
				else {
					converters.add(action);
				}
			}
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		try (FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			
			outputStream = out;
			

			boolean alreadyHasEditorsPackages = false;
			
			DBPFHeader header = new DBPFHeader();
			
			// The header will be written after, when we have all the positions
			outputStream.writePadding(96);
			
			// Write the default index information
			indexStream.writeLEInt(4);
			indexStream.writeInt(0);
			
			File[] folders = new File(inputPath).listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.isDirectory();
				}
				
			});
			System.out.println(folders.length + " folders in project");
			
			
			failedFiles = new HashMap<File, Exception>();
			
			float inc = 100.0f / folders.length;
			float progress = 0;
			
			if (Hasher.UsedNames != null) {
				Hasher.UsedNames = new SimpleNameRegistry();
			}
			
			final DBPFItem item = new DBPFItem();
			
			
			for (File folder : folders) {
				
				currentFolderName = folder.getName();
				currentGroupID = Hasher.getFileHash(currentFolderName);
				
				File[] files = folder.listFiles();
				
				for (File file : files) {
					
					boolean bUsesConverter = false;
					boolean bConvertedSuccessfully = false;
					
					String name = file.getName();
					file = getNestedFile(file, name);
					
					// Skip if there was a problem
					if (file == null) continue;
					
					String[] splits = name.split("\\.", 2);
					currentFileName = splits[0];
					currentExtension = splits.length > 1 ? splits[1] : null;
					
					currentInstanceID = Hasher.getFileHash(currentFileName);
					
					
					for (FastConvertAction converter : fastConverters) {
						if (converter.isValid(file)) {
							
							bConvertedSuccessfully = convertFile(item, converter, true, file);
							bUsesConverter = true;
							
							break;
						}
					}
					
					if (!bUsesConverter) {
						for (ConvertAction converter : converters) {
							if (converter.isValid(file)) {
								
								bConvertedSuccessfully = convertFile(item, converter, false, file);
								bUsesConverter = true;
								
								break;
							}
						}
					}
					
					
					if (!bUsesConverter) {
						
						currentTypeID = Hasher.getTypeHash(currentExtension);
						
						currentInputData = Files.readAllBytes(file.toPath());
						nCurrentInputLength = currentInputData.length;
					}
					else if (!bConvertedSuccessfully) {
						// Don't write the file if there was a problem while converting
						continue;
					}
					
					item.key.setGroupID(currentGroupID);
					item.key.setInstanceID(currentInstanceID);
					item.key.setTypeID(currentTypeID);
					
					
					writeFile(item, currentInputData, nCurrentInputLength);
					addFile(item);
					
					// Add debug information
					if (debugInfo != null 
							&& !bUsesConverter) {  // We cannot get the files from disk in Spore if they needed to be converted
						debugInfo.addFile(currentFolderName, name, currentGroupID, currentInstanceID, currentTypeID);
					}
				}
				
				if (!alreadyHasEditorsPackages && currentGroupID == 0x40404000) {
					alreadyHasEditorsPackages = true;
				}
				
				progress += inc;
				setProgress((int) progress);
			}
			
			/// Save additional files ///
			
			// Create sporemaster/names.txt
			writeNamesList(item);
			
			// Embed editorsPackages
			writeEditorsPackages(item, alreadyHasEditorsPackages);
			
			// Save debug information
			if (debugInfo != null) {
				debugInfo.saveInformation(this);
			}
			
			
			/// Write the index and the header ///
			
			header.indOffset = outputStream.getFilePointer();
			indexStream.writeInto(outputStream);
			
			header.indSize = indexStream.length();
			header.indCount = nItemsCount;
			
			outputStream.seek(0);
			header.write(outputStream);
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parentWindow, "Error writing DBPF: \n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
		}
		finally {
			indexStream.close();
		}
		
		return null;
	}
	
	
	private boolean convertFile(DBPFItem item, ConvertAction converter, boolean bIsFastConverter, File file) throws IOException {
		currentTypeID = converter.getOutputExtensionID(currentExtension);
		
		item.key.setGroupID(currentGroupID);
		item.key.setInstanceID(currentInstanceID);
		item.key.setTypeID(currentTypeID);
		
		MemoryOutputStream stream = null;
		boolean result = false;
		
		try {
				
			if (bIsFastConverter) {
				stream = ((FastConvertAction) converter).fastConvert(file, this);
			}
			else {
				stream = new MemoryOutputStream();
				
				FileFormatStructure struct = converter.process(file);
				struct.write(stream);
			}
			
			currentInputData = stream.getRawData();
			nCurrentInputLength = stream.length();
			
			result = true;
			
		} catch (Exception e) {
			// e.printStackTrace();
			failedFiles.put(file, e);
		}
		finally {
			if (stream != null) {
				stream.close();
			}
		}
		
		return result;
	}
	
	private void writeEditorsPackages(DBPFItem item, boolean alreadyHasEditorsPackages) throws IOException {
		if (embedEditorsPackages != EditorsPackages.NONE && !alreadyHasEditorsPackages) {
			
			item.key.setGroupID(0x40404000);
			item.key.setTypeID(0x00B1B104);
			item.chunkOffset = outputStream.getFilePointer();
			
			String path = null;
			
			if (embedEditorsPackages == EditorsPackages.BOT_PARTS) {
				item.key.setInstanceID(Hasher.getFileHash("BoosterPack2"));
				path = EDITORSPACKAGES_BP2;
			}
			else if (embedEditorsPackages == EditorsPackages.PATCH51) {
				item.key.setInstanceID(Hasher.getFileHash("ExpansionPack1"));
				path = EDITORSPACKAGES_EP1;
			}
			
			try (InputStream is = DBPFPackingTaskOptimized.class.getResourceAsStream(path)) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int n;
				
				while ((n = is.read(buffer)) > 0) {
					outputStream.write(buffer, 0, n);
				}
			}
			
			item.isCompressed = false;
			item.memSize = outputStream.getFilePointer() - item.chunkOffset;
			item.compressedSize = item.memSize;
			
			addFile(item);
		}
	}
	
	private void writeNamesList(DBPFItem item) throws IOException {
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
			
			if (array != null) {
				item.key.setGroupID(Hasher.getFileHash("sporemaster"));
				item.key.setInstanceID(Hasher.getFileHash("names"));
				item.key.setTypeID(Hasher.getTypeHash("txt"));
				item.chunkOffset = outputStream.getFilePointer();
				outputStream.write(array);
				
				writeFile(item, array, array.length);
				addFile(item);
			}
		}
	}
	
	public void addFile(DBPFItem item) throws IOException {
		item.writeInfo(indexStream);
		nItemsCount++;
	}
	
	public boolean writeFile(DBPFItem item, byte[] data, int length) throws IOException {
		
		item.chunkOffset = outputStream.getFilePointer();
		
		if (nCompressThreshold != -1 && length > nCompressThreshold) {
			
			RefPackCompression.compress(data, length, compressOut);

			outputStream.write(compressOut.data, 0, compressOut.lengthInBytes);
			item.isCompressed = true;
			item.memSize = length;
			item.compressedSize = compressOut.lengthInBytes;
		}
		else {
			
			outputStream.write(data, 0, length);
			item.isCompressed = false;
			item.memSize = length;
			item.compressedSize = item.memSize;
		}
		

		return item.isCompressed;
	}
	
	private File getNestedFile(File file, String name) {
		if (!file.isFile()) {
			if (name.contains(".") && !name.endsWith(".effdir.unpacked")) {
				File newFile = new File(file, name);
				if (!newFile.exists()) {
					failedFiles.put(file, new UnsupportedOperationException("Couldn't find file " + name + " inside subfolder " + name));
					return null;
				}
				file = newFile;
			}
			else if (!name.endsWith(".effdir.unpacked")) {
				failedFiles.put(file, new UnsupportedOperationException("Nested subfolders are not supported. File: " + name));
				return null;
			}
		}
		
		return file;
	}
	
	@Override
	public void done() {

		if (failedFiles.size() > 0) {
			new UIErrorsDialog(failedFiles);
			bSuccess = false;
		}
		else
		{
			bSuccess = true;
		}
		
		if (parentWindow != null) {
			parentWindow.dispose();
		}
	}
	
	public boolean wasSuccessful() {
		return bSuccess;
	}

	public String getInputPath() {
		return inputPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public String getCurrentFolderName() {
		return currentFolderName;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}

	public String getCurrentExtension() {
		return currentExtension;
	}

	public int getCurrentGroupID() {
		return currentGroupID;
	}

	public int getCurrentInstanceID() {
		return currentInstanceID;
	}

	public int getCurrentTypeID() {
		return currentTypeID;
	}
	
	
}
