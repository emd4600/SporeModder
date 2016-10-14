package sporemodder.files.formats.dbpf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import sporemodder.files.formats.ConvertAction;

/**
 * This <code>JFrame</code> unpacks the specified package and shows the progress in a <code>JProgressBar</code>. 
 * This class should only be used if there aren't any other windows open (for example, from Windows' Shell Shortcuts), otherwise use UIUnpackDialog.
 *
 */
public class UnpackWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8730395232828344822L;
	
	private JProgressBar progressBar;
	private JLabel lblExtractedFiles;
	private JLabel lblErrorsFound;
	
	private DBPFUnpackingTask task;
	
	public UnpackWindow(List<ConvertAction> converters, String inputPath, String outputPath) {
		setTitle("Unpacking DBPF");
		//setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
		setResizable(false);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		
		lblExtractedFiles = new JLabel();
		lblErrorsFound = new JLabel();
		
		getContentPane().add(progressBar);
		getContentPane().add(lblExtractedFiles);
		
		task = new DBPFUnpackingTask(inputPath, outputPath, converters, this);
		task.addPropertyChangeListener(new TaskProgressListener());
		task.execute();
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private class TaskProgressListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String name = event.getPropertyName();
			
			if (name.equals("extractedFiles")) {
				int newValue = (int) event.getNewValue();
				lblExtractedFiles.setText(newValue + "/" + task.getFileCount() + " files extracted");
			}
			else if (name.equals("errorCount")) {
				lblErrorsFound.setText(task.getErrorCount() + " errors found");
			}
			else if (name.equals("progress")) {
				progressBar.setValue(task.getProgress());
			}
		}
	}
}
