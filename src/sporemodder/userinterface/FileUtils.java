package sporemodder.userinterface;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.utilities.FilteredTreeModel;
import sporemodder.utilities.Project;
import sporemodder.utilities.ProjectTreeNode;

public class FileUtils {
	
	public static void modifyActiveFile() {
		Project project = MainApp.getCurrentProject();
		String filePath = MainApp.getActiveFilePath();
		File sourceFile = project.getSourceFile(filePath);
		File modFile = new File(project.getProjectPath(), filePath);
		
		if (!modFile.getParentFile().exists()) {
			modFile.getParentFile().mkdirs();
		}
		
		//TODO Update node tree
		
		try {
			Files.copy(sourceFile.toPath(), modFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			// Update UI
			MainApp.setActiveFile(filePath);
			TreeNode n = MainApp.getUserInterface().getProjectPanel().getNodeForPath(filePath);
			if (n != null) {
				ProjectTreeNode treeNode = (ProjectTreeNode)n;
				treeNode.isMod = true;
				UIProjectPanel.setIsMod(treeNode, true);
			}
			
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error copying the file:\n" + e1.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
		
		MainApp.getUserInterface().getProjectPanel().repaintTree();
	}
	
	public static void compareActiveFile() {
		Project project = MainApp.getCurrentProject();
		String filePath = MainApp.getActiveFilePath();
		File sourceFile = project.getSourceFile(filePath);
		File modFile = project.getModFile(filePath);
		
		if (!sourceFile.exists()) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Source file doesn't exist", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			String path = MainApp.getProgramPath().getAbsolutePath();
			if (!path.endsWith("\\")) {
				path += "\\";
			}
			Runtime.getRuntime().exec(path + "WinMerge\\WinMergeU.exe \"" + sourceFile.getAbsolutePath() + "\" \"" +
					modFile.getAbsolutePath() + "\"");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void removeActiveFile() {
		UIProjectPanel projectPanel = MainApp.getUserInterface().getProjectPanel();
		Project project = MainApp.getCurrentProject();
		String filePath = MainApp.getActiveFilePath();
		File file = project.getModFile(filePath);
		
		if (file == null) return;
		
		//TODO Ask if user is sure he wants to delete the file
		
		if (file.isFile()) file.delete();
		else deleteDirectory(file);
		
		//TODO Update node tree
		
		ProjectTreeNode treeNode = (ProjectTreeNode) projectPanel.getNodeForPath(filePath);
		boolean removeParentFile = file.getParentFile().list().length == 0;
		
		if (removeParentFile) {
			// remove parent folder too
			file.getParentFile().delete();
		}
		
//		ProjectTreeNode treeNode = (ProjectTreeNode)(n.getUserObject());
		treeNode.isMod = false;
		UIProjectPanel.setIsMod(treeNode, false);
		treeNode.isSource = project.hasSource(filePath);
		// This file's source has been deleted; there's no need to keep the node
		if (!treeNode.isSource) {
			FilteredTreeModel treeModel = projectPanel.getTreeModel();
			TreeNode parentNode = treeNode.getParent();
			treeModel.removeNodeFromParent(treeNode);
			if (parentNode.getChildCount() == 0) {
				treeModel.removeNodeFromParent((MutableTreeNode) parentNode);
			}
		}
		
		if (treeNode.isSource) {
			// Update active file to use source one
			MainApp.setActiveFile(filePath);
		} else {
			MainApp.setActiveFile(null);
		}
		
		projectPanel.repaintTree();
	}
	
	public static void openSourceFolder() {
		if (Desktop.isDesktopSupported()) {
			String filePath = MainApp.getActiveFilePath();
			File file = new File(MainApp.getCurrentProject().getSourceByFile(filePath).getProjectPath(), filePath);
			try {
				if (file.isDirectory()) {
					Desktop.getDesktop().open(file);
				}
				else {
					Desktop.getDesktop().open(file.getParentFile());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public static void openModFolder() {
		if (Desktop.isDesktopSupported()) {
			String filePath = MainApp.getActiveFilePath();
			File file = null;
			if (filePath == null) {
				// check if the root node is selected, so we open the project folder
				int pathCount = MainApp.getUserInterface().getProjectPanel().getTree().getSelectionPath().getPathCount();
				if (pathCount == 1) {
					file = MainApp.getCurrentProject().getProjectPath();
				}
			} else {
				file = MainApp.getCurrentProject().getModFile(filePath);
			}
			if (file != null) {
				try {
					if (file != null) {
						if (file.isDirectory()) {
							Desktop.getDesktop().open(file);
						}
						else {
							Desktop.getDesktop().open(file.getParentFile());
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

//	private static void copyFile(File inputFile, File outputFile) throws IOException {
//		if (outputFile.exists()) {
//			outputFile.createNewFile();
//		}
//		
//		FileChannel in = null;
//		FileChannel out = null;
//		try {
//			in = new FileInputStream(inputFile).getChannel();
//			out = new FileOutputStream(outputFile).getChannel();
//			
//			out.transferFrom(in, 0, in.size());
//		} finally {
//			if (in != null) in.close();
//			if (out != null) out.close();
//		}
//	}
	
	/**
	 * Deletes a dir recursively deleting anything inside it.
	 * @param dir The dir to delete
	 * @return true if the dir was successfully deleted
	 */
	private static boolean deleteDirectory(File dir) {
	    if(! dir.exists() || !dir.isDirectory())    {
	        return false;
	    }

	    String[] files = dir.list();
	    for(int i = 0, len = files.length; i < len; i++)    {
	        File f = new File(dir, files[i]);
	        if(f.isDirectory()) {
	            deleteDirectory(f);
	        }else   {
	            f.delete();
	        }
	    }
	    return dir.delete();
	}
	
	public static String changePathLeaf(ProjectTreeNode originalNode, String newName) {
		TreeNode[] nodePath = originalNode.getPath();
		Object[] newNodePath = new Object[nodePath.length];
		System.arraycopy(nodePath, 0, newNodePath, 0, newNodePath.length);
		newNodePath[newNodePath.length - 1] = newName;
		return Project.getRelativePath(newNodePath);
	}
	
	public static String getChildPath(ProjectTreeNode parentNode, String childName) {
		TreeNode[] nodePath = parentNode.getPath();
		Object[] newNodePath = new Object[nodePath.length + 1];
		System.arraycopy(nodePath, 0, newNodePath, 0, nodePath.length);
		newNodePath[newNodePath.length - 1] = childName;
		return Project.getRelativePath(newNodePath);
	}
	
	
	public static void renameFile(File file) {

		Project project = MainApp.getCurrentProject();
//		File file = project.getModFile(MainApp.getActiveFilePath());
		if (file == null || !file.exists()) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "File doesn't exist.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String newName = (String) JOptionPane.showInputDialog(MainApp.getUserInterface(), "Insert new file name: ", "Rename file", JOptionPane.PLAIN_MESSAGE,
				null, null, file.getName());
		
		if (newName != null) {
			File newFile = new File(file.getParentFile(), newName);
			if (!newFile.exists()) {
				if (!file.renameTo(newFile)) {
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't rename file.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't rename file. A file with that name already exists in that mod folder.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			UIProjectPanel panel = MainApp.getUserInterface().getProjectPanel();
			ProjectTreeNode node = panel.getNodeForPath(MainApp.getActiveFilePath());
			
			if (!node.name.equals(newName)) {
				String newPath = changePathLeaf(node, newName);
				
				boolean hasSource = project.hasSource(newPath);
				ProjectTreeNode finalNode = null;
			
				if (hasSource) {
					// a node with that name already exists as a source, we'll use it
					finalNode = panel.getNodeForPath(newPath);
					finalNode.name = newName;
					UIProjectPanel.setIsMod(finalNode, true);
					
				}
				else {
					// there's no existing node with that name, we create a new one
					finalNode = new ProjectTreeNode(node);
					finalNode.copyChildren(node);
					finalNode.name = newName;
					finalNode.isSource = false;  // we've already checked if it hasSource
					finalNode.isMod = true;
					
					panel.getTreeModel().insertNode(finalNode, (ProjectTreeNode)node.getParent());
					
					//UIProjectPanel.setIsMod(finalNode, true);
					
				}
				
				// does this new name match the search?
				finalNode.searchInName();
				
				if (finalNode.getChildCount() > 0) {
					UIProjectPanel.refresh(finalNode);
				}
				
				if (node.isSource) {
					// we must turn the old node into a source-only node
					// UIProjectPanel.setIsMod(node, false);
					UIProjectPanel.refresh(node);
				}
				else {
					// the original node was mod-only, so we can remove it now
					panel.getTreeModel().removeNodeFromParent(node);
				}
				
				panel.repaintTree();
				
				panel.getTree().setSelectionPath(new TreePath(finalNode.getPath()));
				
				// MainApp.setActiveFile(newPath);
			}
		}
	}

	public static ProjectTreeNode getParentNode(ProjectTreeNode node) {
//		ProjectTreeNode activeNode = MainApp.getUserInterface().getProjectPanel().getNodeForPath(MainApp.getActiveFilePath());
		File folder = MainApp.getCurrentProject().getModFile(Project.getRelativePath(node.getPath()));
		ProjectTreeNode parentNode = null;
		if (folder.isDirectory()) {
			// using node.getChildCount() > 0 is incorrect, since empty folders don't have children neither
			parentNode = node;
		}
		else {
			if (node.getParent() != null) {
				parentNode = (ProjectTreeNode) node.getParent();
			}
			else {
				// what??? if it isn't a directory, how do you create a file here?
				parentNode = node;
			}
		}
		return parentNode;
	}
	
	public static void importExternal(ProjectTreeNode activeNode) {
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, MainApp.getUserInterface(), JFileChooser.FILES_ONLY, true, ChooserType.OPEN);
		String result = chooser.launch();
		if (result != null) {
			String[] filePaths = result.split("\\|");
			ProjectTreeNode parentNode = getParentNode(activeNode);
			File parentFolder = MainApp.getCurrentProject().getModFile(Project.getRelativePath(parentNode.getPath()));
			UIProjectPanel panel = MainApp.getUserInterface().getProjectPanel();
			String path = parentFolder.getAbsolutePath();
			
			File[] choosenFiles = new File[filePaths.length];
			File[] resultingFiles = new File[filePaths.length];
			List<File> existingFiles = new ArrayList<File>();
			
			for (int i = 0; i < filePaths.length; i++) {
				choosenFiles[i] = new File(filePaths[i]);
				resultingFiles[i] = new File(path, choosenFiles[i].getName());
				
				if (resultingFiles[i].exists()) {
					existingFiles.add(resultingFiles[i]);
				}
			}
			
			boolean replaceExisting = false;
			
			if (existingFiles.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("The following " + existingFiles.size() + " files already exist. Do you want to overwrite them?\n");
				
				for (File f : existingFiles) {
					sb.append(f.getName());
					sb.append("\n");
				}
						
				int replaceResult = JOptionPane.showConfirmDialog(MainApp.getUserInterface(), sb.toString(), "Overwrite existing?", JOptionPane.YES_NO_OPTION);
				replaceExisting = replaceResult == JOptionPane.YES_OPTION;
			}
			
			for (int i = 0; i < resultingFiles.length; i++) {
				File newFile = resultingFiles[i];
				if (!replaceExisting && existingFiles.contains(newFile)) {
					continue;
				}
				
				try {
					Files.copy(choosenFiles[i].toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Couldn't import external file.\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
				}
				
				String newPath = getChildPath(parentNode, newFile.getName());
				
				ProjectTreeNode node = panel.getNodeForPath(newPath);
				boolean isNewNode = false;
				
				if (node == null) {
					node = new ProjectTreeNode();
					isNewNode = true;
					node.name = newFile.getName();
					panel.getTreeModel().insertNode(node, parentNode);
				}
				
				node.isMod = true;
				node.isSource = MainApp.getCurrentProject().hasSource(newPath);
				node.searchInName();
				
				if (i == resultingFiles.length - 1 && !isNewNode) {
					panel.getTree().setSelectionPath(new TreePath(node.getPath()));
					
					panel.repaintTree();
				}
			}
		}
	}
	
	
}
