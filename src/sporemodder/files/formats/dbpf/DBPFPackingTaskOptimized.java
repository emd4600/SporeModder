package sporemodder.files.formats.dbpf;

import java.awt.Window;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.MemoryOutputStream;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.ReadWriteStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FastConvertAction;
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

public class DBPFPackingTaskOptimized {
	
	private static final int INDEX_PROGRESS = 20;
	private static final int BUFFER_SIZE = 8192;
	
	private static final String EDITORSPACKAGES_BP2 = "/sporemodder/files/resources/BoosterPack2.prop";
	private static final String EDITORSPACKAGES_EP1 = "/sporemodder/files/resources/ExpansionPack1.prop";
	
	
	private Window parent;
	private int compressLimit = -1;
	private String inputPath;
	private HashMap<File, Exception> failedFiles;
	private boolean success = false;
	private EditorsPackages embedEditorsPackages;
	
	private final Profiler profiler = new Profiler();
	
	private final List<ConvertAction> converters = new ArrayList<ConvertAction>();
	private final List<FastConvertAction> fastConverters = new ArrayList<FastConvertAction>();
	
	/** A memory stream to temporarily store the index data. This is more efficient than creating new objects for every single item. */
	private final MemoryOutputStream indexStream = new MemoryOutputStream();
	
	/** A stream for writing this DBPF. */
	private OutputStreamAccessor outputStream;
	
	/** If our output stream is a file on the system, we can use Java's NIO for better performance. */
	private FileChannel outputChannel;
	
	public boolean bConvertInMemory;
	public boolean bUseFastConverters;
	
	
	public DBPFPackingTaskOptimized(OutputStreamAccessor out, Project project, List<ConvertAction> converters, Window parent) {
		super();
		this.parent = parent;
		this.compressLimit = project.getCompressingLimit();
		this.inputPath = project.getProjectPath().getAbsolutePath();
		this.outputStream = out;
		this.embedEditorsPackages = project.getEmbeddedEditorPackages();
		
		addConverters(converters);
	}
	
	public DBPFPackingTaskOptimized(OutputStreamAccessor out, String inputPath, int compressLimit, List<ConvertAction> converters, Window parent) {
		super();
		this.parent = parent;
		this.compressLimit = compressLimit;
		this.inputPath = inputPath;
		this.outputStream = out;
		
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
	
	private static String createAutolocaleFile(FileFormatStructure struct, String folder, String name) {
		if (struct instanceof PROPMain) {
			PROPMain prop = (PROPMain) struct;
			//int nameID = Hasher.getFileHash("auto_" + folder + "_" + name);
			int nameID = Hasher.stringToFNVHash("auto_" + folder + "_" + name);
			return prop.createAutolocaleFile(nameID);
		}
		return null;
	}
	
	private int writeAutolocaleFile(DBPFItem item, String autoLocale, String folder, String name, int nItemsCount) throws IOException {
		if (autoLocale != null) {
			byte[] data = autoLocale.getBytes("US-ASCII");
			
			int nameID = Hasher.getFileHash("auto_" + folder + "_" + name);
			item.key.setGroupID(Hasher.getFileHash("locale~"));
			item.key.setInstanceID(nameID);
			item.key.setTypeID(Hasher.getTypeHash("locale"));
			item.chunkOffset = outputStream.getFilePointer();
			item.memSize = data.length;
			item.compressedSize = item.memSize;
			item.isCompressed = false;
			
			outputStream.write(data);
			
			writeIndexItem(item);
			nItemsCount += 1;
		}
		
		return nItemsCount;
	}

	public void execute() throws Exception {
		try {
			
			boolean alreadyHasEditorsPackages = false;
			
			int nItemsCount = 0;
			
			DBPFHeader header = new DBPFHeader();
			
			// the header will be written after, when we have all the positions
			outputStream.writePadding(96);
			
			
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
			if (Hasher.UsedNames != null) {
				Hasher.UsedNames = new SimpleNameRegistry();
			}
			
			final DBPFItem item = new DBPFItem();
			final CompressorOutput compressOut = new CompressorOutput();
			
			
			for (File folder : folders) {
				
				String folderName = folder.getName();
				int folderHash = Hasher.getFileHash(folderName);
				
				File[] files = folder.listFiles();
				
				for (File file : files) {
					
					boolean bFileIsWritten = false;
					
					profiler.startMeasure(PerformanceTest.PROFILER_PROCESS_NAMES);
					
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
					
					String fileName;
					String extension;
					
					String[] splits = name.split("\\.", 2);
					fileName = splits[0];
					extension = splits.length > 1 ? splits[1] : null;
					
					int nameHash = Hasher.getFileHash(fileName);
					
					profiler.endMeasure(PerformanceTest.PROFILER_PROCESS_NAMES);
					
					profiler.startMeasure(PerformanceTest.PROFILER_CONVERT);
					
					for (FastConvertAction converter : fastConverters) {
						if (converter.isValid(file)) {
							convertFile(item, converter, true, extension, folderHash, nameHash, file);
							
							bFileIsWritten = true;
							
							break;
						}
					}
					
					for (ConvertAction converter : converters) {
						if (converter.isValid(file)) {
							convertFile(item, converter, false, extension, folderHash, nameHash, file);
							
							bFileIsWritten = true;
							
							break;
						}
					}
					
					profiler.endMeasure(PerformanceTest.PROFILER_CONVERT);
					
					profiler.startMeasure(PerformanceTest.PROFILER_WRITE_FILE);
					
					if (!bFileIsWritten) {
						
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
						
						int typeHash = Hasher.getTypeHash(extension);
						
						item.key.setGroupID(folderHash);
						item.key.setInstanceID(nameHash);
						item.key.setTypeID(typeHash);
						item.chunkOffset = outputStream.getFilePointer();
						
						//TODO do this for converted files too
						if (compressLimit != -1 && file.length() > compressLimit) {
							byte[] input = Files.readAllBytes(file.toPath());
							
							RefPackCompression.compress(input, compressOut);

							outputStream.write(compressOut.data, 0, compressOut.lengthInBytes);
							item.isCompressed = true;
							item.memSize = input.length;
							item.compressedSize = compressOut.lengthInBytes;
						}
						else {
//							if (outputChannel != null) {
//								// we use NIO for better performance
//								item.memSize = (int) transferData(file);
//								item.isCompressed = false;
//								item.compressedSize = item.memSize;
//							}
//							else {
//								byte[] input = Files.readAllBytes(file.toPath());
//								
//								outputStream.write(input);
//								item.isCompressed = false;
//								item.memSize = input.length;
//								item.compressedSize = item.memSize;
//							}
							
							byte[] input = Files.readAllBytes(file.toPath());
							
							outputStream.write(input);
							item.isCompressed = false;
							item.memSize = input.length;
							item.compressedSize = item.memSize;
						}
					}
					
					profiler.endMeasure(PerformanceTest.PROFILER_WRITE_FILE);
					
					
					profiler.startMeasure(PerformanceTest.PROFILER_WRITE_INDEX);
					
					nItemsCount++;
					writeIndexItem(item);
					
					profiler.endMeasure(PerformanceTest.PROFILER_WRITE_INDEX);
				}
				
				if (!alreadyHasEditorsPackages && folderHash == 0x40404000) {
					alreadyHasEditorsPackages = true;
				}
				
				progress += inc;
			}
			
			profiler.startMeasure(PerformanceTest.PROFILER_EXTRA_FILES);
			{
				// create sporemaster/names.txt
				writeNamesList(item);
				
				// embed editorsPackages
				writeEditorsPackages(item, alreadyHasEditorsPackages);
			}
			profiler.endMeasure(PerformanceTest.PROFILER_EXTRA_FILES);
			
			profiler.startMeasure(PerformanceTest.PROFILER_WRITE_INDEX);
			{
				header.indOffset = outputStream.getFilePointer();
				indexStream.writeInto(outputStream);
				
				header.indSize = indexStream.length();
				header.indCount = nItemsCount;
				
				progress += INDEX_PROGRESS;
				
				
				outputStream.seek(0);
				header.write(outputStream);
			}
			profiler.endMeasure(PerformanceTest.PROFILER_WRITE_INDEX);
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Error writing DBPF: \n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
		}
		finally {
			indexStream.close();
		}
	}
	
	private void writeIndexItem(DBPFItem item) throws IOException {
		item.writeInfo(indexStream);
	}
	
	private void convertFile(DBPFItem item, ConvertAction converter, boolean bIsFastConverter, String extension, int folderHash, int nameHash, File file) throws IOException {
		int typeHash = converter.getOutputExtensionID(extension);
		
		item.key.setGroupID(folderHash);
		item.key.setInstanceID(nameHash);
		item.key.setTypeID(typeHash);
		item.chunkOffset = outputStream.getFilePointer();
		
		MemoryOutputStream stream = null;
		
		//TODO Known bug: these files won't be compressed
		try {
			
//			String autolocale = createAutolocaleFile(struct, folderName, fileName);
			
			if (bConvertInMemory) {
				
				if (bIsFastConverter && bUseFastConverters) {
					stream = ((FastConvertAction) converter).fastConvert(file);
				}
				else {
					stream = new MemoryOutputStream();
					
					FileFormatStructure struct = converter.process(file);
					struct.write(stream);
				}
				
				stream.writeInto(outputStream);
				
				item.isCompressed = false;
				item.memSize = stream.length();
				item.compressedSize = item.memSize;
			}
			else {
				// set base pointer for those formats that work with offsets (like RW4)
				int basePos = outputStream.getBaseOffset();
				outputStream.setBaseOffset(item.chunkOffset);
				
				FileFormatStructure struct = converter.process(file);
				struct.write(outputStream);
				
				outputStream.setBaseOffset(basePos);
				
				item.memSize = outputStream.getFilePointer() - item.chunkOffset;
				item.compressedSize = item.memSize;
			}
			
//			if (autolocale != null) {
//				nItemsCount = writeAutolocaleFile(item, autolocale, folderName, fileName, nItemsCount);
//			}
			
		} catch (Exception e) {
			// e.printStackTrace();
			failedFiles.put(file, e);
		}
		finally {
			if (stream != null) {
				stream.close();
			}
		}
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
			
//			nItemsCount++;
			writeIndexItem(item);
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
			
			// disable name logging
			// Hasher.UsedNames = null;
			
			if (array != null) {
				item.key.setGroupID(Hasher.getFileHash("sporemaster"));
				item.key.setInstanceID(Hasher.getFileHash("names"));
				item.key.setTypeID(Hasher.getTypeHash("txt"));
				item.chunkOffset = outputStream.getFilePointer();
				outputStream.write(array);
				item.isCompressed = false;
				item.memSize = array.length;
				item.compressedSize = item.memSize;
				
//				nItemsCount++;
				writeIndexItem(item);
			}
		}
	}
	
	private long transferData(File inputFile) throws IOException {
		
		FileInputStream in = null;
		FileChannel inChannel = null;
		long nTransferred = 0;
		
		try {
			
			in = new FileInputStream(inputFile);
			inChannel = in.getChannel();
			
			long nBytes = inChannel.size();
			
			while (nTransferred < nBytes) {
				
				nTransferred += inChannel.transferTo(nTransferred, nBytes, outputChannel);
			}
		}
		finally {
			
			if (in != null) {
				in.close();
			}
			else if (inChannel != null) {
				inChannel.close();
			}
		}

		return nTransferred;
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
