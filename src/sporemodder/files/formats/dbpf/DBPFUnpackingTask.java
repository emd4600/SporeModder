package sporemodder.files.formats.dbpf;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Project;
import sporemodder.utilities.names.SimpleNameRegistry;

public class DBPFUnpackingTask extends SwingWorker<Void, Void> {
	
	private static final int INDEX_PROGRESS = 15;
	private List<ConvertAction> converters;
	private int extractedFiles = 0;
	private int fileCount = 0;
	private int errorCount;
	
	private String inputPath;
	private String outputPath;
	private Window parent;
	
//	private List<List<FileStructureError>> errors = new ArrayList<List<FileStructureError>>();
	//private List<DBPFItem> errors = new ArrayList<DBPFItem>();
	private HashMap<DBPFItem, Exception> exceptions = new HashMap<DBPFItem, Exception>();
	
	public DBPFUnpackingTask(String inputPath, String outputPath, List<ConvertAction> converters, Window parent) {
		this.inputPath = inputPath;
		this.converters = converters;
		this.outputPath = outputPath;
		if (!this.outputPath.endsWith("\\")) {
			this.outputPath += "\\";
		}
		this.parent = parent;
	}
	
	public DBPFUnpackingTask(String inputPath, Project project, List<ConvertAction> converters, Window parent) {
		this.inputPath = inputPath;
		this.converters = converters;
		this.outputPath = project.getProjectPath().getAbsolutePath();
		if (!this.outputPath.endsWith("\\")) {
			this.outputPath += "\\";
		}
		this.parent = parent;
	}
	
	private static void findNamesFile(List<DBPFItem> items, InputStreamAccessor in) throws IOException {
		int group = Hasher.getFileHash("sporemaster");
		int name = Hasher.getFileHash("names");
		for (DBPFItem item : items) {
			if (item.key.getGroupID() == group && item.key.getInstanceID() == name) {
				//TODO this forces it to be always uncompressed
				byte[] arr = new byte[item.memSize];
				in.seek(item.chunkOffset);
				in.read(arr);
				try (ByteArrayInputStream arrayStream = new ByteArrayInputStream(arr);
						BufferedReader reader = new BufferedReader(new InputStreamReader(arrayStream))) {
					Hasher.UsedNames = new SimpleNameRegistry(reader);
				}
			}
		}
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		long initialTime = System.currentTimeMillis();
		
		try (DBPFMain dbpf = new DBPFMain(new FileStreamAccessor(inputPath, "r"))) 
		{
			//TODO This is too slow!!!!
//			DBPFMain dbpf = new DBPFMain(in);
			setProgress(INDEX_PROGRESS);
			
			DBPFIndex index = dbpf.getIndex();
			fileCount = index.items.size();
			firePropertyChange("extractedFiles", extractedFiles, extractedFiles);
			
			float inc = (100.0f - INDEX_PROGRESS) / fileCount;
			float progress = INDEX_PROGRESS;
			
			// First search sporemaster/names.txt, and use it if it exists
			findNamesFile(index.items, dbpf.getSource());
			
			for (DBPFItem item : index.items) 
			{
				String fileName = Hasher.getFileName(item.key.getInstanceID()) ;
				
				// skip autolocale files
				if (item.key.getGroupID() == 0x02FABF01 && fileName.startsWith("auto_")) {
					continue;
				}
				
				String extension = Hasher.getTypeName(item.key.getTypeID());
				String folderPath = outputPath + Hasher.getFileName(item.key.getGroupID()) + "\\";
				String path = folderPath + fileName + 
						"." + extension;
				
				File folder = new File(folderPath);
				if (!folder.exists()) folder.mkdir();
				
//				long time1 = System.currentTimeMillis();
				try (ByteArrayStreamAccessor stream = item.processFile(dbpf.getSource())) 
				{
//					System.out.println((System.currentTimeMillis() - time1) + " ms processing file " + path);
					
//					long time2 = System.currentTimeMillis();
					boolean convert = false;
					for (ConvertAction converter : converters) {
						if (converter.isValid(item.key)) 
						{
//							FileStreamAccessor out = new FileStreamAccessor(path + "." + converter.getOutputExtension(extension), "rw");
							String outputExtension = converter.getOutputExtension(extension);
							FileFormatStructure struct;
							if (outputExtension == null) {
								struct = converter.convert(stream, path);
							}
							else {
								struct = converter.convert(stream, path + "." + outputExtension);
							}
							
							if (struct == null) {
								// this will write the file if there was an error
								convert = false;
								break;
							}
							
							List<FileStructureError> err = struct.getAllErrors();
							
							if (err != null && err.size() > 0) {
//								errors.add(item);
								exceptions.put(item, new IOException(FileStructureError.getErrorsString(err)));
//								firePropertyChange("errorCount", errorCount, errorCount++);
							}
							
							convert = true;
							break;
						}
					}
//					System.out.println((System.currentTimeMillis() - time2) + " ms converting file " + path);
					
					if (!convert) {
						stream.writeToFile(path);
					}
				} catch (Exception e) {
//					errors.add(item);
//					e.printStackTrace();
					exceptions.put(item, e);
//					firePropertyChange("errorCount", errorCount, errorCount++);
				}
				
				progress += inc;
				setProgress((int) progress);
				
//				firePropertyChange("extractedFiles", extractedFiles, extractedFiles++);
			}
			
			// disable extra names
			Hasher.UsedNames = null;
		}
		
		long time = System.currentTimeMillis() - initialTime;
		
		if (exceptions.size() > 0) {
//			StringBuilder sb = new StringBuilder("Unpacked in " + (time / 1000.0f) + " seconds with " + errors.size() + "errors.\n");
//			sb.append("The following files could not be converted: \n");
//			
//			for (int i = 0; i < errors.size(); i++) 
//			{
//				DBPFItem item = errors.get(i);
//				sb.append(Hasher.getFileName(item.group) + "\\" + Hasher.getFileName(item.name) +
//						"." + Hasher.getTypeName(item.type) + "\n");
//			}
//			
//			JOptionPane.showMessageDialog(parent, sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			
			new UIErrorsDialog(exceptions, "Unpacked in " + (time / 1000.0f) + " seconds with " + exceptions.size() + " errors. ");
		}
		else {
			JOptionPane.showMessageDialog(parent, "Successfully unpacked in " + (time / 1000.0f) + " seconds with no errors.", 
					"Successfully unpacked", JOptionPane.INFORMATION_MESSAGE);
		}
		
		return null;
	}
	
	@Override
	public void done() {
		parent.dispose();
	}
	
	public int getFileCount() {
		return fileCount;
	}
	
	public int getErrorCount() {
		return errorCount;
	}
}