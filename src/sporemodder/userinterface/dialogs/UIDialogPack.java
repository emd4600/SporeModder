package sporemodder.userinterface.dialogs;

import java.awt.Frame;
import java.awt.Dialog.ModalityType;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.formats.dbpf.DBPFMain;
import sporemodder.files.formats.dbpf.DBPFPackingTask;
import sporemodder.userinterface.ErrorManager;
import sporemodder.utilities.Project;

public class UIDialogPack extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -397485895476738587L;
	
	private JProgressBar progressBar;
	private DBPFPackingTask task;
	private Project project;
	private DBPFMain dbpf;

	public UIDialogPack() {
		this(MainApp.getUserInterface());
	}
	public UIDialogPack(Frame parent) {
		super(parent);
		
		MainApp.processAutoSave();
		project = MainApp.getCurrentProject();
		String packageName = project.getPackageName();
		
		//TODO check if project packing name doesn't collide with Spore ones
		for (String s : DBPFMain.BANNED_PACKAGE_NAMES) {
			if (packageName.equals(s)) {
				int result = JOptionPane.showConfirmDialog(parent, "The current package name collides with one used by Spore (" + s + 
						"). Packing this is heavily discouraged since it might cause irreversible problems to your game. Are you sure you want to continue?", 
						"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				
				if (result != JOptionPane.YES_OPTION) {
					dispose();
					return;
				}
			}
		}
		
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Packing '" + project.getProjectName() + "'");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		//progressBar.setStringPainted(true);
		
		add(progressBar);
		
		
		try {
			dbpf = new DBPFMain(new FileStreamAccessor(project.getPackageFile(), "rw", true), false);
			task = new DBPFPackingTask(dbpf, project, project.getPackingConverters(), this);
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
	
	
	public static boolean createPackDialog() {
		UIDialogPack dialog = new UIDialogPack();
		if (dialog.task == null) return false;
		return dialog.task.wasSuccessful();
	}
}
