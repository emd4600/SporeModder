package sporemodder.userinterface.dialogs;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JTextField;

public class DropTargetTextField extends DropTarget {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4528122831625911394L;
	
	private JTextField target;
	//TODO this is for the output file; is it necessary?
//	private boolean singleFile;
	
	public DropTargetTextField(JTextField target, boolean singleFile) {
		this.target = target;
//		this.singleFile = singleFile;
	}
	
	public synchronized void drop(DropTargetDropEvent event) {
		event.acceptDrop(DnDConstants.ACTION_COPY);
		try {
			@SuppressWarnings("unchecked")
			List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			
			int count = droppedFiles.size();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < droppedFiles.size(); i++) {
				sb.append(droppedFiles.get(i));
				
				if (i != count-1) {
					sb.append("|");
				}
			}
			
			target.setText(sb.toString());
			
		} catch (UnsupportedFlavorException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
