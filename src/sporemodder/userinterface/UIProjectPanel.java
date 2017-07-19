package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import sporemodder.MainApp;
import sporemodder.MainApp.SaveMode;
import sporemodder.userinterface.contextmenu.UITreePopClickListener;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.userinterface.fileview.TextFileView;
import sporemodder.utilities.ProjectTreeModel;
import sporemodder.utilities.Project;
import sporemodder.utilities.ProjectTreeNode;
import sporemodder.utilities.SearchSpec;

public class UIProjectPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6538241486910841498L;
	
	private JPanel panel;
	
	private JTextField tfSearch;
	private String lastSearchText = "";
	private JCheckBox cbShowMod;
	
	private JScrollPane scrollPane;
	// We must add a placeholder name or we won't be able to change it otherwise
	private ProjectTreeNode rootNode = new ProjectTreeNode("My Project", true);
	private ProjectTreeModel treeModel = new ProjectTreeModel(rootNode);
	private final JTree tree = new JTree(treeModel);
	
	public UIProjectPanel() {
		setLayout(new GridLayout(1, 1));
		
		panel = new JPanel();
		panel.setLayout(new BorderLayout(2, 10));
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		tfSearch = new JTextField();
		tfSearch.setColumns(16);
		tfSearch.getDocument().addDocumentListener(new DLSearch());
		
		cbShowMod = new JCheckBox("Show only modded files");
		cbShowMod.addItemListener(new ILShowMod());
		
		northPanel.add(tfSearch);
		northPanel.add(cbShowMod);
		
		scrollPane = new JScrollPane(tree);
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.INSERT);
		tree.setTransferHandler(new TreeTransferHandler());
		tree.setCellRenderer(new ProjectTreeCellRenderer());
		tree.addMouseListener(new UITreePopClickListener(tree));
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent arg0) 
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();
				
//				tree.clearSelection();
//				tree.setSelectionPath(arg0.getPath());
				
				if (node == null) return;
				
				MainApp.processAutoSave();
				
				TreePath path = arg0.getPath();
				if (path.getPathCount() > 1) {
					String file = Project.getRelativePath(path);
					
					MainApp.setActiveFile(file);
				}
				else {
					MainApp.setActiveFile(null);
				}
				
				tree.requestFocusInWindow();
			}
			
		});
		
		panel.add(northPanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		add(panel);
	}
	
	public void updateProjectName() {
		rootNode.name = MainApp.getCurrentProject().getProjectName();
		repaintTree();
	}
	
	public void loadProject(Project project) {
//		tree.add
//		rootNode.name = project.getProjectName();
		
		// make visible so there's no trouble when removing it
//		setChildNodesVisible(rootNode, false);
//		
//		int size = rootNode.getChildCount();
//		for (int i = size-1; i >= 0; i--) {
//			ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(i, false);
//			treeModel.removeNodeFromParent(node);
//		}
		
//		rootNode = new ProjectTreeNode(project.getProjectName(), true);
//		treeModel = new FilteredTreeModel(rootNode);
//		tree.setModel(treeModel);
//		
//		project.loadNodesEx(treeModel, rootNode);
		
		//TODO only for testing
		rootNode = new ProjectTreeNode(project.getProjectName(), true);
		treeModel = new ProjectTreeModel(rootNode);
		tree.setModel(treeModel);
		
		project.loadNodesFastEx(treeModel, rootNode);
		////////////////////////////////
		
		
//		project.loadTreeNodes();
//		List<DefaultMutableTreeNode> nodes = project.getTreeNodes();
//		int i = 0;
//		for (DefaultMutableTreeNode node : nodes) {
//			treeModel.insertNodeInto(node, rootNode, i++);
//		}
		
		tree.expandRow(0);
	}
	
	private static ProjectTreeNode findChild(ProjectTreeNode node, String[] components, int index) {
		if (components.length == index) return node;
		
		for (int i = 0; i < node.getChildCount(); i++) {
			ProjectTreeNode n = (ProjectTreeNode) node.getChildAt(i);
			
			if (n.name.equals(components[index])) {
				return findChild(n, components, index + 1);
			}
		}
		return null;
	}
	
	public ProjectTreeNode getNodeForPath(String path) {
		if (path == null) {
			return rootNode;
		}
		String[] components = path.split("\\\\");
		
		return findChild(rootNode, components, 0);
	}

	public ProjectTreeModel getTreeModel() {
		return treeModel;
	}
	
	public JTree getTree() {
		return tree;
	}
	
	
//	protected static void hideSourceNodes(ProjectTreeNode node) {
//		boolean isVisible = false;
//		
//		for (int i = 0; i < node.getChildCount(); i++) 
//		{
//			ProjectTreeNode n = (ProjectTreeNode) node.getChildAt(i);
//			
//			if (n.isLeaf()) {
////				if (!n.isMod) {
////					n.isVisible = false;
////				}
//				
//				if (!n.isMod) {
//					n.isVisible = false;
//				}
//			}
//			else {
//				hideSourceNodes(n);
//			}
//			
//			
//			isVisible = isVisible || n.isVisible;
//		}
//		
//		// hide node if it has no visible childs
//		if (!node.isRoot) {
//			node.isVisible = isVisible;
//		}
//	}
//	
//	protected static void setNodesVisible(ProjectTreeNode node, boolean hideSources) {
//		boolean isVisible = false;
//		
//		for (int i = 0; i < node.getChildCount(); i++) 
//		{
//			ProjectTreeNode n = (ProjectTreeNode) node.getChildAt(i);
////			ProjectTreeNode p = (ProjectTreeNode) n.getUserObject();
//			
//			if (n.isLeaf()) {
////				n.isVisible = hideSources && !n.isMod ? false : true;
//				if (n.matchedSearch()) {
//					n.isVisible = true;
//				}
//			}
//			else {
//				setNodesVisible(n, hideSources);
//			}
//			
//			isVisible = isVisible || n.isVisible;
//		}
//		
////		node.isVisible = hideSources && !node.isRoot ? isVisible : true;
////		if (node.matchedSearch()) {
////			node.isVisible = true;
////		}
//	}
//	
//	
//	// Sets all children nodes visible
//	// Sets this node visible unless it has no visible childs
//	// Returns if the given node is visible or not
//	public static boolean setChildNodesVisible(ProjectTreeNode parentNode, boolean hideSources) {
//		boolean isVisible = false;
//		
//		int count = parentNode.getChildCount();
//		for (int i = 0; i < count; i++) 
//		{
//			ProjectTreeNode n = (ProjectTreeNode) parentNode.getChildAt(i);
//			
//			if (n.isLeaf()) {
//				n.isVisible = hideSources ? n.isMod : true;
//			}
//			else {
//				setChildNodesVisible(n, hideSources);
//			}
//			
//			isVisible = isVisible || n.isVisible;
//		}
//		
//		if (parentNode.isRoot()) {
//			parentNode.isVisible = true;
//			return true;
//		}
//		
//		if (!parentNode.isLeaf()) {
//			if (hideSources) {
//				parentNode.isVisible = parentNode.isMod || isVisible;
//			}
//			else {
//				parentNode.isVisible = isVisible;
//			}
//		}
//		return parentNode.isVisible;
//	}
//	
//	protected static void setParentNodesVisible(ProjectTreeNode node) {
//		ProjectTreeNode n = (ProjectTreeNode) node.getParent();
//		if (n == null) return;
//		
//		n.isVisible = true;
//		setParentNodesVisible(n);
//	}
	
	protected static boolean checkIsMod(DefaultMutableTreeNode node) {
		boolean result = false;
		
		for (int i = 0; i < node.getChildCount(); i++) {
			ProjectTreeNode n = (ProjectTreeNode) node.getChildAt(i);
//			ProjectTreeNode p = (ProjectTreeNode) n.getUserObject();
			
			if (n.isLeaf()) {
				result = result || n.isMod;
				if (result == true) break;
			}
			else {
				result = result || checkIsMod(n);
				if (result == true) break;
			}
		}
		
		return result;
	}
	
	private static void setIsModParents(ProjectTreeNode node, boolean isMod) {
		TreeNode n = (TreeNode) node.getParent();
		if (n.getParent() == null) return;
		
		ProjectTreeNode treeNode = (ProjectTreeNode)n;
		if (isMod) {
			treeNode.isMod = isMod;
		} else {
			treeNode.isMod = checkIsMod(treeNode);
		}
		setIsModParents(treeNode, isMod);
	}
	private static void setIsModChilds(ProjectTreeNode node, boolean isMod) {
		for (int i = 0; i < node.getChildCount(); i++) {
			ProjectTreeNode n = (ProjectTreeNode) node.getChildAt(i);
			n.isMod = isMod;
			
			if (!n.isLeaf()) {
				setIsModChilds(n, isMod);
			}
		}
	}
	
	protected static void setIsMod(ProjectTreeNode node, boolean isMod) {
		setIsModParents(node, isMod);
		setIsModChilds(node, isMod);
		node.isMod = isMod;
	}
	
//	private void search(ProjectTreeNode node, String text) {
//		//TODO Search "animate" and then erase it. No nodes are shown!
//		boolean isVisible = false;
//		
//		for (int i = 0; i < node.getChildCount(); i++) 
//		{
//			ProjectTreeNode n = (ProjectTreeNode) node.getChildAt(i);
//			
//			// if n matches the search criteria
//			
//			if (n.matchesCriteria(text)) {
////				setNodesVisible(n, cbShowMod.isSelected());
////				setNodesVisible(n, false);
//				setNodesVisible(n, true);
//			}
//			else {
//				if (n.isLeaf()) {
//					n.isVisible = false;
//				}
//				else {
//					search(n, text);
//				}
//			}
//			
//			
//			isVisible = isVisible || n.isVisible;
//		}
//		
//		// hide node if it has no visible childs
//		if (!node.isRoot) {
//			node.isVisible = isVisible;
//		}
//	}
	
	public void repaintTree() {
		tree.updateUI();
		//tree.revalidate();
		tree.repaint();
	}
	
	private class ILShowMod implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			
//			if (cbShowMod.isSelected()) {
//				hideSourceNodes(rootNode);
//			}
//			//TODO adapt this to the search system
//			// if there's something being searched, 
//			else {
////				setNodesVisible(rootNode, false);
//				
//				// search again, but only in those nodes that weren't 
//				// not really, because those nodes are now invisible
//				if (tfSearch.getText().length() > 0) {
//					rootNode.search(MainApp.getSearchStrings(tfSearch.getText().toLowerCase()), cbShowMod.isSelected(), false, false, true);
//				}
//				else {
//					setChildNodesVisible(rootNode, cbShowMod.isSelected());
//				}
//			}
			
			treeModel.setFilterMod(cbShowMod.isSelected());
			
			repaintTree();
		}
		
	}
	
//	private void search(boolean dontSearchInMods, boolean searchOnlyVisible) {
//		long time1 = System.currentTimeMillis(); 
//		
//		String searchText = tfSearch.getText().toLowerCase();
//		
//		if (searchText != null && searchText.length() > 0) {
//			List<String> searchStrings = MainApp.getSearchStrings(searchText);
//			MainApp.setSearchStrings(searchStrings);
//			rootNode.search(searchStrings, cbShowMod.isSelected(), dontSearchInMods, searchOnlyVisible, true);
//		}
//		else {
//			setChildNodesVisible(rootNode, cbShowMod.isSelected());
//			MainApp.setSearchStrings(new ArrayList<String>());
//		}
//		repaintTree();
//		
//		time1 = System.currentTimeMillis() - time1;
//		System.out.println("Search old time: " + time1);
//	}
//	private void search(boolean searchOnlyVisible) {
//		search(false, searchOnlyVisible);
//	}
	
	private void searchFast(boolean dontSearchInMods, boolean searchOnlyVisible) {
		long time1 = System.currentTimeMillis(); 
				
		String searchText = tfSearch.getText().toLowerCase();
		
		if (searchText != null && searchText.length() > 0) {
			treeModel.setFilterSearch(true);
			// has the character been inserted at the end ?
			//TODO is this a good method to check it?
			if (searchOnlyVisible == true && !searchText.startsWith(lastSearchText)) {
				searchOnlyVisible = false;
			}
			
			List<String> searchStrings = MainApp.getSearchStrings(searchText);
			MainApp.setSearchStrings(searchStrings);
			//rootNode.searchFast(SearchSpec.generateSearchSpecs(searchStrings), cbShowMod.isSelected(), dontSearchInMods, searchOnlyVisible, true);
			rootNode.searchFast(SearchSpec.generateSearchSpecs(searchStrings), true);
		}
		else {
			//setChildNodesVisible(rootNode, cbShowMod.isSelected());
			treeModel.setFilterSearch(false);
			MainApp.setSearchStrings(new ArrayList<String>());
		}
		repaintTree();
		
		time1 = System.currentTimeMillis() - time1;
		System.out.println("Search new time: " + time1);
		
		lastSearchText = searchText;
	}
	
	private void searchFast(boolean searchOnlyVisible) {
		searchFast(false, searchOnlyVisible);
	}
	
	private class DLSearch implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent arg0) {
//			search(false);
//			
//			setChildNodesVisible(rootNode, cbShowMod.isSelected());
			searchFast(false);
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			// we search only in the visible nodes, the ones that were found previously
//			search(true);
//			
//			setChildNodesVisible(rootNode, cbShowMod.isSelected());
			searchFast(true);
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
//			search(false);
//
//			setChildNodesVisible(rootNode, cbShowMod.isSelected());
			searchFast(false);
		}
		
	}
	
	
	public void setShowModSelected(boolean isSelected) {
		cbShowMod.setSelected(isSelected);
	}
	
	public boolean isShowModSelected() {
		return cbShowMod.isSelected();
	}
	
	public void setSearchFieldText(String text) {
		tfSearch.setText(text);
	}
	
//	public boolean containsWord(String word) {
//		List<String> list = new ArrayList<String>();
//		list.add(word);
//		return rootNode.searchFast(SearchSpec.generateSearchSpecs(list), false, false, false, false);
//	}
//	
//	public void findWord(String word) {
//		List<String> list = new ArrayList<String>();
//		list.add(word);
//		return rootNode.searchFast(SearchSpec.generateSearchSpecs(list),  cbShowMod.isSelected(), false, false, true);
//	}
	
	private void removeAllChildren(ProjectTreeNode node) {
		int count = node.getChildCount();
		int[] childIndices = new int[count];
		Object[] removedChildren = new Object[count];
		
		for (int i = 0; i < count; i++) {
			childIndices[i] = i;
			removedChildren[i] = node.getChildAt(i);
		}
		
		node.removeAllChildren();
		treeModel.nodesWereRemoved(node, childIndices, removedChildren);
	}
	
	/**
	 * Updates the given node. This determines again if it's source or mod. All the node children are recalculated and removed/added if necessary.
	 * @param node
	 */
	public void refresh(Project project, ProjectTreeNode node) {
		if (project == null || node == null) {
			return;
		}
		if (node.isLeaf() && node.getParent() != null)
		{
			node = (ProjectTreeNode) node.getParent();
		}
		
		// remove all children
		removeAllChildren(node);
		
		TreeNode[] treePath = node.getPath();
		String relativePath = Project.getRelativePath(node.getPath());
		int level = treePath.length - 1;
		
		// if there are sources it will be updated
		node.isSource = false;
		// if there is mod it will be updated
		node.isMod = false;
		
		List<Project> sources = project.getSources();
		ListIterator<Project> iterable = sources.listIterator(sources.size());
		
		// We'll load the least important sources first 
		while (iterable.hasPrevious())
		{
			Project source = iterable.previous();
			
			File rootFolder = new File(source.getProjectPath(), relativePath);
			if (rootFolder.exists()) {
				node.isSource = true;
				Project.loadNodesFast(treeModel, rootFolder, node, false, level, true);
			}
		}
		
		File rootFolder = new File(project.getProjectPath(), relativePath);
		if (rootFolder.exists()) {
			node.isMod = true;
			Project.loadNodesFast(treeModel, rootFolder, node, true, level, true);
		}
		
		tree.expandPath(new TreePath(treePath));
	}
	
	public static void refreshActiveNode() {
		UIMainApp userInterface = MainApp.getUserInterface();
		if (userInterface != null) {
			
			UIProjectPanel panel = userInterface.getProjectPanel();
			panel.refresh(MainApp.getCurrentProject(), (ProjectTreeNode) panel.getTree().getLastSelectedPathComponent());
		}
	}
	
	public static void refresh(ProjectTreeNode node) {
		UIMainApp userInterface = MainApp.getUserInterface();
		if (userInterface != null) {
			
			UIProjectPanel panel = userInterface.getProjectPanel();
			panel.refresh(MainApp.getCurrentProject(), node);
		}
	}
}
