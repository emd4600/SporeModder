package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.utilities.Project;

public class UIDisplayPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -732751395523773443L;
	
	private JPanel panel;
	
	private JTabbedPane tabbedPane;
	
	private JPanel bottomPanel;
	private JLabel lblCurrentFile;
	
	protected JPanel makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
	
	public UIDisplayPanel()
	{
		setLayout(new GridLayout(1, 1));
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		add(panel);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("", makeTextPanel("Time to get modding!"));
		
		bottomPanel = new JPanel(new BorderLayout());
		
		lblCurrentFile = new JLabel();
		lblCurrentFile.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
		bottomPanel.add(lblCurrentFile, BorderLayout.WEST);
		
		panel.add(tabbedPane, BorderLayout.CENTER);
		panel.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	public void setCurrentFileView(FileView fileView) {
		String filePath = MainApp.getActiveFilePath();
		if (fileView != null && filePath != null) {
			fileView.setParentTabbedPane(tabbedPane);
			tabbedPane.setTitleAt(0, fileView.getName());

			try {
				tabbedPane.setComponentAt(0, fileView.getPanel());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't create file view:\n" + ErrorManager.getStackTraceString(e), 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			
			
			Project currentProject = MainApp.getCurrentProject();
			//TODO we're checking the project sources twice here; one to get the project, and another to see if it is in the source too
			Project project = currentProject.getProjectByFile(filePath);
			File file = project.getFile(filePath);
			
			boolean hasSource = currentProject.hasSource(filePath);
			boolean isMod = project == currentProject;
			fileView.setEditable(isMod);
//			btnCompare.setEnabled(file.isFile() && hasSource && isMod);
//			btnModify.setEnabled(file.isFile() && hasSource && !isMod);
//			btnRemove.setEnabled(isMod);
//			btnOpenSourceFolder.setEnabled(hasSource);
//			btnOpenModFolder.setEnabled(isMod);
			
			lblCurrentFile.setText("<html>" + project.getProjectName() + " --- <strong>" + filePath + "</strong></html>");
		}
		else {
			tabbedPane.setTitleAt(0, "");
			tabbedPane.setComponentAt(0, new JPanel());
			
//			btnCompare.setEnabled(false);
//			btnModify.setEnabled(false);
//			btnRemove.setEnabled(false);
//			btnOpenSourceFolder.setEnabled(false);
//			btnOpenModFolder.setEnabled(false);
			
			lblCurrentFile.setText("");
		}
	}
	
	protected void update(FileView fileView) {
		if (fileView != null) {
			String filePath = MainApp.getActiveFilePath();
			
			Project currentProject = MainApp.getCurrentProject();
			Project project = currentProject.getProjectByFile(filePath);
			File file = project.getFile(filePath);
			
			boolean hasSource = currentProject.hasSource(filePath);
			boolean isMod = project == currentProject;
			fileView.setEditable(isMod);
//			btnCompare.setEnabled(file.isFile() && hasSource && isMod);
//			btnModify.setEnabled(file.isFile() && hasSource && !isMod);
//			btnRemove.setEnabled(isMod);
//			btnOpenSourceFolder.setEnabled(hasSource);
//			btnOpenModFolder.setEnabled(isMod);
			
			lblCurrentFile.setText("<html>" + project.getProjectName() + " --- <strong>" + filePath + "</strong></html>");
		}
		else {
			tabbedPane.setTitleAt(0, "");
			tabbedPane.setComponentAt(0, new JPanel());
			
			TreePath treePath = MainApp.getUserInterface().getProjectPanel().getTree().getSelectionPath();
			int pathCount = -1;
			if (treePath != null) {
				pathCount = treePath.getPathCount();
			}
			
//			btnCompare.setEnabled(false);
//			btnModify.setEnabled(false);
//			btnRemove.setEnabled(false);
//			btnOpenSourceFolder.setEnabled(false);
//			btnOpenModFolder.setEnabled(pathCount == 1);
			
			lblCurrentFile.setText("");
		}
	}
	
}

