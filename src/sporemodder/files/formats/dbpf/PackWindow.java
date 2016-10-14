package sporemodder.files.formats.dbpf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.userinterface.ErrorManager;

public class PackWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5030678361780479334L;
	
	private JProgressBar progressBar;
	private DBPFPackingTask task;
	private DBPFMain dbpf;

	public PackWindow(File input, File output, List<ConvertAction> converters) {
		
		String packageName = output.getName();
		for (String s : DBPFMain.BANNED_PACKAGE_NAMES) {
			if (packageName.equals(s)) {
				int result = JOptionPane.showConfirmDialog(this, "The current package name collides with one used by Spore (" + s + 
						"). Packing this is heavily discouraged since it might cause irreversible problems to your game. Are you sure you want to continue?", 
						"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				
				if (result != JOptionPane.YES_OPTION) {
					dispose();
					return;
				}
			}
		}
		
		setTitle("Packing '" + output.getName() + "'");
		// setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		
		add(progressBar);
		
		
		try {
			dbpf = new DBPFMain(new FileStreamAccessor(output, "rw", true), false);
			task = new DBPFPackingTask(dbpf, input.getAbsolutePath(), -1, converters, this);
			task.addPropertyChangeListener(new TaskProgressListener());
			task.execute();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
			dispose();
			return;
		}
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	@Override
	public void dispose() {
		if (dbpf != null)
			try {
				dbpf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		super.dispose();
	}
	
	private class TaskProgressListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			progressBar.setValue(task.getProgress());
		}
	}
	
}
