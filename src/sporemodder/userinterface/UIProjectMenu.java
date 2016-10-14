package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.userinterface.dialogs.UIProjectSettings;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.utilities.Project;
import sporemodder.utilities.ProjectTreeNode;

public class UIProjectMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4077919078688485637L;
	
	private JMenuItem mntmNewFile;
	private JMenuItem mntmNewFolder;
	private JMenuItem mntmRenameFile;
	private JMenuItem mntmImportExternal;
	private JMenuItem mntmModifyFile;
	private JMenuItem mntmRemoveFile;
	private JMenuItem mntmCompareFile;
	private JMenuItem mntmOpenSourceFolder;
	private JMenuItem mntmOpenModFolder;
	private JMenuItem mntmRefresh;
	
	private JMenuItem mntmProjectSettings;
	
	public UIProjectMenu(String name) {
		super(name);
		
		mntmNewFile = new JMenuItem("New file");
		mntmNewFile.setMnemonic(KeyEvent.VK_N);
		mntmNewFile.setEnabled(false);
		mntmNewFile.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK));
		mntmNewFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newFileAction();
			}
		});
		
		mntmNewFolder = new JMenuItem("New folder");
		mntmNewFolder.setMnemonic(KeyEvent.VK_F);
		mntmNewFolder.setEnabled(false);
		mntmNewFolder.setAccelerator(KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
		mntmNewFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newFolderAction();
			}
		});
		
		mntmRenameFile = new JMenuItem("Rename file");
		mntmRenameFile.setMnemonic(KeyEvent.VK_R);
		mntmRenameFile.setEnabled(false);
		mntmRenameFile.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK));
		mntmRenameFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.renameFile(MainApp.getCurrentProject().getModFile(MainApp.getActiveFilePath()));
			}
		});
		
		mntmImportExternal = new JMenuItem("Import external file");
		mntmImportExternal.setMnemonic(KeyEvent.VK_I);
		mntmImportExternal.setEnabled(false);
		mntmImportExternal.setAccelerator(KeyStroke.getKeyStroke('I', InputEvent.CTRL_DOWN_MASK));
		mntmImportExternal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.importExternal(MainApp.getActiveNode());
			}
		});
		
		mntmModifyFile = new JMenuItem("Import from source");
		mntmModifyFile.setMnemonic(KeyEvent.VK_M);
		mntmModifyFile.setEnabled(false);
		mntmModifyFile.setAccelerator(KeyStroke.getKeyStroke('M', InputEvent.CTRL_DOWN_MASK));
		mntmModifyFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.modifyActiveFile();
			}
		});
		
		mntmRemoveFile = new JMenuItem("Delete file");
		mntmRemoveFile.setMnemonic(KeyEvent.VK_D);
		mntmRemoveFile.setEnabled(false);
		mntmRemoveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.removeActiveFile();
			}
		});
		
		mntmCompareFile = new JMenuItem("Compare file");
		mntmCompareFile.setMnemonic(KeyEvent.VK_C);
		mntmCompareFile.setEnabled(false);
		mntmCompareFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.compareActiveFile();
			}
		});
		
		mntmOpenSourceFolder = new JMenuItem("Open source folder");
		mntmOpenSourceFolder.setMnemonic(KeyEvent.VK_O);
		mntmOpenSourceFolder.setEnabled(false);
		mntmOpenSourceFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.openSourceFolder();
			}
		});
		
		mntmOpenModFolder = new JMenuItem("Open mod folder");
		mntmOpenModFolder.setMnemonic(KeyEvent.VK_P);
		mntmOpenModFolder.setEnabled(false);
		mntmOpenModFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileUtils.openModFolder();
			}
		});
		
		mntmRefresh = new JMenuItem("Refresh");
		mntmRefresh.setMnemonic(KeyEvent.VK_R);
		mntmRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		mntmRefresh.setEnabled(false);
		mntmRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UIProjectPanel.refreshActiveNode();
			}
		});
		
		mntmProjectSettings = new JMenuItem("Project Settings");
		mntmProjectSettings.setMnemonic(KeyEvent.VK_S);
		mntmProjectSettings.setEnabled(false);
		mntmProjectSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new UIProjectSettings(MainApp.getCurrentProject(), UIProjectSettings.Mode.NONE);
			}
		});
		
		add(mntmNewFile);
		add(mntmNewFolder);
		add(mntmRenameFile);
		add(mntmImportExternal);
		add(mntmModifyFile);
		add(mntmRemoveFile);
		add(mntmCompareFile);
		add(mntmOpenSourceFolder);
		add(mntmOpenModFolder);
		add(mntmRefresh);
		add(new JSeparator());
		add(mntmProjectSettings);
	}
	
	public void update(FileView fileView) {
		if (fileView != null) {
			String filePath = MainApp.getActiveFilePath();
			
			Project currentProject = MainApp.getCurrentProject();
			Project project = currentProject.getProjectByFile(filePath);
			File file = project.getFile(filePath);
			
			boolean hasSource = currentProject.hasSource(filePath);
			boolean isMod = project == currentProject;
			mntmNewFile.setEnabled(isMod);
			mntmNewFolder.setEnabled(isMod);
			mntmRenameFile.setEnabled(isMod);
			mntmCompareFile.setEnabled(file.isFile() && hasSource && isMod);
			mntmImportExternal.setEnabled(isMod);
			mntmModifyFile.setEnabled(file.isFile() && hasSource && !isMod);
			mntmRemoveFile.setEnabled(isMod);
			mntmOpenSourceFolder.setEnabled(hasSource);
			mntmOpenModFolder.setEnabled(isMod);
			mntmRefresh.setEnabled(true);
			
		} 
		else {
			TreePath treePath = MainApp.getUserInterface().getProjectPanel().getTree().getSelectionPath();
			int pathCount = -1;
			if (treePath != null) {
				pathCount = treePath.getPathCount();
			}
			mntmNewFile.setEnabled(false);
			mntmNewFolder.setEnabled(pathCount == 1);
			mntmRenameFile.setEnabled(false);
			mntmCompareFile.setEnabled(false);
			mntmImportExternal.setEnabled(false);
			mntmModifyFile.setEnabled(false);
			mntmRemoveFile.setEnabled(false);
			mntmOpenSourceFolder.setEnabled(false);
			mntmRefresh.setEnabled(true);
			mntmOpenModFolder.setEnabled(pathCount == 1);
		}
		
		mntmProjectSettings.setEnabled(MainApp.getCurrentProject() != null);
	}
	
	
//	private static class RenameDialog extends JDialog {
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = -81589368414647524L;
//		
//		private JTextField tfName;
//		private JButton btnAccept;
//		private JButton btnCancel;
//		private String path;
//		private File file;
//		
//		private RenameDialog() {
//			super(MainApp.getUserInterface());
//			
//			setModalityType(ModalityType.TOOLKIT_MODAL);
//			setResizable(false);
//			getContentPane().setLayout(new BorderLayout());
//			
//			Project project = MainApp.getCurrentProject();
//			path = MainApp.getActiveFilePath();
//			file = project.getModFile(MainApp.getActiveFilePath());
//			if (file == null || !file.exists()) {
//				JOptionPane.showMessageDialog(this, "File doesn't exist.", "Error", JOptionPane.ERROR_MESSAGE);
//				dispose();
//				return;
//			}
//			
//			tfName = new JTextField(file.getName());
//			
//			getContentPane().add(tfName, BorderLayout.CENTER);
//			
//			{
//				JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//				
//				btnAccept = new JButton("Accept");
//				btnAccept.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent arg0) {
//						String newName = tfName.getText();
//						File newFile = new File(file.getAbsolutePath().replace(file.getName(), newName));
//						if (file != null && file.exists() && !newFile.exists()) {
//							if (!file.renameTo(newFile)) {
//								JOptionPane.showMessageDialog(RenameDialog.this, "Couldn't rename file.", "Error", JOptionPane.ERROR_MESSAGE);
//								dispose();
//								return;
//							}
//						}
//						else {
//							JOptionPane.showMessageDialog(RenameDialog.this, "Couldn't rename file.", "Error", JOptionPane.ERROR_MESSAGE);
//						}
//						
//						Project project = MainApp.getCurrentProject();
//						UIProjectPanel panel = MainApp.getUserInterface().getProjectPanel();
//						ProjectTreeNode node = panel.getNodeForPath(path);
//						
//						if (node.name != newName) {
//							
//							TreeNode[] nodePath = node.getPath();
//							Object[] newNodePath = new Object[nodePath.length];
//							System.arraycopy(nodePath, 0, newNodePath, 0, newNodePath.length);
//							newNodePath[newNodePath.length - 1] = newName;
//							String newPath = Project.getSimplePath(newNodePath);
//							
//							boolean hasSource = project.hasSource(newPath);
//							ProjectTreeNode finalNode = null;
//						
//							if (hasSource) {
//								// a node with that name already exists as a source, we'll use it
//								ProjectTreeNode sourceNode = panel.getNodeForPath(newPath);
//								sourceNode.name = newName;
//								UIProjectPanel.setIsMod(sourceNode, true);
//								
//								finalNode = sourceNode;
//							}
//							else {
//								// there's no existing node with that name, we create a new one
//								ProjectTreeNode newNode = new ProjectTreeNode();
//								newNode.name = newName;
//								newNode.isSource = false;  // we've already checked if it hasSource
//								
//								panel.getTreeModel().insertNode(newNode, (ProjectTreeNode)node.getParent());
//								
//								UIProjectPanel.setIsMod(newNode, true);
//								
//								finalNode = newNode;
//							}
//							
//							if (node.isSource) {
//								// we must turn the old node into a source-only node
//								UIProjectPanel.setIsMod(node, false);
//							}
//							else {
//								// the original node was mod-only, so we can remove it now
//								panel.getTreeModel().removeNodeFromParent(node);
//							}
//							
//							panel.repaintTree();
//							
//							panel.getTree().setSelectionPath(new TreePath(finalNode.getPath()));
//							
//							MainApp.setActiveFile(newPath);
//						}
//						
//						dispose();
//					}
//				});
//				
//				btnCancel = new JButton("Cancel");
//				btnCancel.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent arg0) {
//						dispose();
//					}
//				});
//				
//				buttonsPanel.add(btnAccept);
//				buttonsPanel.add(btnCancel);
//				
//				getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
//			}
//			
//			pack();
//			setVisible(true);
//		}
//	}
	
	private static String getChildPath(ProjectTreeNode originalNode, String newName) {
		TreeNode[] nodePath = originalNode.getPath();
		Object[] newNodePath = new Object[nodePath.length];
		System.arraycopy(nodePath, 0, newNodePath, 0, newNodePath.length);
		newNodePath[newNodePath.length - 1] = newName;
		return Project.getRelativePath(newNodePath);
	}
	
	
	private static ProjectTreeNode getParentNode() {
		ProjectTreeNode activeNode = MainApp.getUserInterface().getProjectPanel().getNodeForPath(MainApp.getActiveFilePath());
		File folder = MainApp.getCurrentProject().getModFile(Project.getRelativePath(activeNode.getPath()));
		ProjectTreeNode parentNode = null;
		if (folder.isDirectory()) {
			// using node.getChildCount() > 0 is incorrect, since empty folders don't have children neither
			parentNode = activeNode;
		}
		else {
			if (activeNode.getParent() != null) {
				parentNode = (ProjectTreeNode) activeNode.getParent();
			}
			else {
				// what??? if it isn't a directory, how do you create a file here?
				parentNode = activeNode;
			}
		}
		return parentNode;
	}
	
	private void newFileAction() {
		ProjectTreeNode parentNode = getParentNode();
		
		String newName = (String) JOptionPane.showInputDialog(MainApp.getUserInterface(), "Insert new file name: ", "Create new file", JOptionPane.PLAIN_MESSAGE,
				null, null, "untitled.prop.xml");
		
		if (newName != null) {
			File parentFolder = MainApp.getCurrentProject().getModFile(Project.getRelativePath(parentNode.getPath()));
			
			if (parentFolder == null || !parentFolder.exists() || parentFolder.isFile()) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't create new file.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String path = parentFolder.getAbsolutePath();
			File newFile = new File(path, newName);
			try {
				if (!newFile.createNewFile()) {
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't create new file.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (HeadlessException | IOException e) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't create new file.\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String newPath = getChildPath(parentNode, newName);
			ProjectTreeNode node = new ProjectTreeNode();
			node.name = newName;
			node.isMod = true;
			node.isSource = MainApp.getCurrentProject().hasSource(newPath);
			
			UIProjectPanel panel = MainApp.getUserInterface().getProjectPanel();
			panel.getTreeModel().insertNode(node, parentNode);
			
			panel.getTree().setSelectionPath(new TreePath(node.getPath()));
			
			panel.repaintTree();
			
			// MainApp.setActiveFile(newPath);
		}
	}
	
	private void newFolderAction() {
		ProjectTreeNode parentNode = getParentNode();
		
		String newName = (String) JOptionPane.showInputDialog(MainApp.getUserInterface(), "Insert new folder name: ", "Create new folder", JOptionPane.PLAIN_MESSAGE,
				null, null, "untitled_folder");
		
		if (newName != null) {
			File parentFolder = MainApp.getCurrentProject().getModFile(Project.getRelativePath(parentNode.getPath()));
			
			if (parentFolder == null || !parentFolder.exists() || parentFolder.isFile()) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't create new folder.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String path = parentFolder.getAbsolutePath();
			File newFile = new File(path, newName);
			try {
				if (!newFile.mkdir()) {
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't create new folder.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (HeadlessException e) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't create new folder.\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String newPath = getChildPath(parentNode, newName);
			ProjectTreeNode node = new ProjectTreeNode();
			node.name = newName;
			node.isMod = true;
			node.isSource = MainApp.getCurrentProject().hasSource(newPath);
			
			UIProjectPanel panel = MainApp.getUserInterface().getProjectPanel();
			panel.getTreeModel().insertNode(node, parentNode);
			
			panel.getTree().setSelectionPath(new TreePath(node.getPath()));
			
			panel.repaintTree();
			
			// MainApp.setActiveFile(newPath);
		}
	}
	
	private void importExternalAction() {
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, MainApp.getUserInterface(), JFileChooser.FILES_ONLY, true, ChooserType.OPEN);
		String result = chooser.launch();
		if (result != null) {
			String[] files = result.split("\\|");
			ProjectTreeNode parentNode = getParentNode();
			File parentFolder = MainApp.getCurrentProject().getModFile(Project.getRelativePath(parentNode.getPath()));
			UIProjectPanel panel = MainApp.getUserInterface().getProjectPanel();
			
			for (int i = 0; i < files.length; i++) {
				File chosenFile = new File(files[i]);
				String newName = chosenFile.getName();
				String path = parentFolder.getAbsolutePath();
				File newFile = new File(path, newName);
				
				try {
					Files.copy(chosenFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't import external file.\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
				}
				
				String newPath = getChildPath(parentNode, newName);
				ProjectTreeNode node = new ProjectTreeNode();
				node.name = newName;
				node.isMod = true;
				node.isSource = MainApp.getCurrentProject().hasSource(newPath);
				
				panel.getTreeModel().insertNode(node, parentNode);
				
				if (i == files.length - 1) {
					panel.getTree().setSelectionPath(new TreePath(node.getPath()));
					
					panel.repaintTree();
					
					// MainApp.setActiveFile(newPath);
				}
			}
		}
	}
}
