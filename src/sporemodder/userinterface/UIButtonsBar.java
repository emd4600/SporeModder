package sporemodder.userinterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.utilities.Project;

public class UIButtonsBar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5764996941811039265L;
	private JButton btnCompare;
	private JButton btnModify;
	private JButton btnRemove;
	private JButton btnOpenSourceFolder;
	private JButton btnOpenModFolder;
	
	public UIButtonsBar()
	{
		super("File Buttons");
		
		setRollover(true);
		
		btnCompare = new JButton("Compare");
		btnCompare.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.compareActiveFile();
			}
		});
		
		btnModify = new JButton("Import from source");
		btnModify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.modifyActiveFile();
			}
		});
		
		btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.removeActiveFile();
			}
		});
		
		btnOpenSourceFolder = new JButton("Open source folder");
		btnOpenSourceFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.openSourceFolder();
			}
		});
		
		btnOpenModFolder = new JButton("Open mod folder");
		btnOpenModFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.openModFolder();
			}
		});
		
		btnCompare.setEnabled(false);
		btnModify.setEnabled(false);
		btnRemove.setEnabled(false);
		btnOpenSourceFolder.setEnabled(false);
		btnOpenModFolder.setEnabled(false);
		
		add(btnCompare);
		add(btnModify);
		add(btnRemove);
		add(btnOpenSourceFolder);
		add(btnOpenModFolder);
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
			btnCompare.setEnabled(file.isFile() && hasSource && isMod);
			btnModify.setEnabled(file.isFile() && hasSource && !isMod);
			btnRemove.setEnabled(isMod);
			btnOpenSourceFolder.setEnabled(hasSource);
			btnOpenModFolder.setEnabled(isMod);
			
		}
		else {
			
			TreePath treePath = MainApp.getUserInterface().getProjectPanel().getTree().getSelectionPath();
			int pathCount = -1;
			if (treePath != null) {
				pathCount = treePath.getPathCount();
			}
			
			btnCompare.setEnabled(false);
			btnModify.setEnabled(false);
			btnRemove.setEnabled(false);
			btnOpenSourceFolder.setEnabled(false);
			btnOpenModFolder.setEnabled(pathCount == 1);
			
		}
	}
}
