package sporemodder.userinterface;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import sporemodder.utilities.FilteredTreeModel;
import sporemodder.utilities.ProjectTreeNode;

public class TreeTransferHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3220336576270527993L;
	DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    ProjectTreeNode[] nodesToRemove;
  
    public TreeTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                sporemodder.utilities.ProjectTreeNode[].class.getName() +
                              "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }
  
    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) {
            return false;
        }
        
        JTree tree = (JTree)support.getComponent();
        int[] selRows = tree.getSelectionRows();
        TreePath path = tree.getPathForRow(selRows[0]);
        ProjectTreeNode firstNode =
            (ProjectTreeNode)path.getLastPathComponent();
        
        // can only drag mod files
        if (!firstNode.isMod) {
        	support.setShowDropLocation(false);
        	return false;
        }
        
        
        support.setShowDropLocation(true);
        if(!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        int dropRow = tree.getRowForPath(dl.getPath());
        for(int i = 0; i < selRows.length; i++) {
            if(selRows[i] == dropRow) {
                return false;
            }
        }
        
        // Do not allow MOVE-action drops if a non-leaf node is
        // selected unless all of its children are also selected.
        int action = support.getDropAction();
        if(action == MOVE) {
            // return haveCompleteNode(tree);
        	return true;
        }
        
        TreePath dest = dl.getPath();
        ProjectTreeNode target =
            (ProjectTreeNode)dest.getLastPathComponent();
        
        // Do not allow a non-leaf node to be copied to a level
        // which is less than its source level.
        if(firstNode.getChildCount() > 0 &&
               target.getLevel() < firstNode.getLevel()) {
            return false;
        }
        
        return true;
    }
  
    private boolean haveCompleteNode(JTree tree) {
        int[] selRows = tree.getSelectionRows();
        TreePath path = tree.getPathForRow(selRows[0]);
        ProjectTreeNode first =
            (ProjectTreeNode)path.getLastPathComponent();
        int childCount = first.getChildCount();
        // first has children and no children are selected.
        if(childCount > 0 && selRows.length == 1)
            return false;
        // first may have children.
        for(int i = 1; i < selRows.length; i++) {
            path = tree.getPathForRow(selRows[i]);
            ProjectTreeNode next =
                (ProjectTreeNode)path.getLastPathComponent();
            if(first.isNodeChild(next)) {
                // Found a child of first.
                if(childCount > selRows.length-1) {
                    // Not all children of first are selected.
                    return false;
                }
            }
        }
        return true;
    }
  
    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree)c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<ProjectTreeNode> copies =
                new ArrayList<ProjectTreeNode>();
            List<ProjectTreeNode> toRemove =
                new ArrayList<ProjectTreeNode>();
            ProjectTreeNode node =
                (ProjectTreeNode)paths[0].getLastPathComponent();
            ProjectTreeNode copy = copy(node);
            copies.add(copy);
            toRemove.add(node);
            for(int i = 1; i < paths.length; i++) {
            	ProjectTreeNode next =
                    (ProjectTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else if(next.getLevel() > node.getLevel()) {  // child node
                    copy.add(copy(next));
                    // node already contains child
                } else {                                        // sibling
                    copies.add(copy(next));
                    toRemove.add(next);
                }
            }
            ProjectTreeNode[] nodes =
                copies.toArray(new ProjectTreeNode[copies.size()]);
            nodesToRemove =
                toRemove.toArray(new ProjectTreeNode[toRemove.size()]);
            return new NodesTransferable(nodes);
        }
        return null;
    }
  
    /** Defensive copy used in createTransferable. */
    private ProjectTreeNode copy(ProjectTreeNode node) {
        return new ProjectTreeNode(node);
    }
  
    protected void exportDone(JComponent source, Transferable data, int action) {
        if((action & MOVE) == MOVE) {
            JTree tree = (JTree)source;
            FilteredTreeModel model = (FilteredTreeModel)tree.getModel();
            // Remove nodes saved in nodesToRemove in createTransferable.
            for(int i = 0; i < nodesToRemove.length; i++) {
                model.removeNodeFromParent(nodesToRemove[i]);
            }
        }
    }
  
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
  
    public boolean importData(TransferHandler.TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        ProjectTreeNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (ProjectTreeNode[])t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
        } catch(java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
        // Get drop location info.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        ProjectTreeNode parent =
            (ProjectTreeNode)dest.getLastPathComponent();
        JTree tree = (JTree)support.getComponent();
        FilteredTreeModel model = (FilteredTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        for(int i = 0; i < nodes.length; i++) {
            model.insertNodeInto(nodes[i], parent, index++);
        }
        return true;
    }
  
    public String toString() {
        return getClass().getName();
    }
  
    public class NodesTransferable implements Transferable {
    	ProjectTreeNode[] nodes;
  
        public NodesTransferable(ProjectTreeNode[] nodes) {
            this.nodes = nodes;
         }
  
        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return nodes;
        }
  
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
  
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
