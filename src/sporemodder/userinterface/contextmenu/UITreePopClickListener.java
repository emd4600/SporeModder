package sporemodder.userinterface.contextmenu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class UITreePopClickListener extends MouseAdapter {
	private JTree tree;
	
	public UITreePopClickListener(JTree tree) {
		this.tree = tree;
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			int x = e.getX();
			int y = e.getY();
			TreePath path = tree.getPathForLocation(x, y);
			tree.setSelectionPath(path);
			
			if (path != null) {
				UIFileContextMenu contextMenu = new UIFileContextMenu(path);
				contextMenu.show(e.getComponent(), x, y);
			}
		}
	}
}
