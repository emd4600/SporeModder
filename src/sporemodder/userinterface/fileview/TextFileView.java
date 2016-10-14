package sporemodder.userinterface.fileview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.undo.UndoManager;

import sporemodder.MainApp;
import sporemodder.userinterface.dialogs.UIFindAndReplace;
import sporemodder.userinterface.syntaxpane.SpuiView.SpuiEditorKit;
import sporemodder.userinterface.syntaxpane.TxtView.TxtEditorKit;
import sporemodder.userinterface.syntaxpane.PfxView.PfxEditorKit;
import sporemodder.userinterface.syntaxpane.TextLineNumber;
import sporemodder.userinterface.syntaxpane.TlsaView.TlsaEditorKit;
import sporemodder.userinterface.syntaxpane.XmlView.XmlEditorKit;;

public class TextFileView extends FileView {
	
	public static final int VIEWTYPE_TEXT = 1;
	
	public static final Color SEARCH_COLOR = Color.red;
	public static final String COMMENT_KEYWORD = "//";
	public static final Color COMMENT_COLOR = Color.gray;

	// JTextPane contents, used for saving
//	private String contents;
	private boolean isSaved = true; // we don't set it to false until we change it
	
	private UndoManager undoManager;
	
	private JTextPane textPane;
	private JScrollPane scrollPane;
	
	private List<String> searchTerms = new ArrayList<String>();
	private boolean searchCaseSensitive;
	
	// Don't let the user edit it until we know it's 100% text
	private boolean isAscii;
	
	private UIFindAndReplace findDialog;
	
	public TextFileView(String path) {
		super(path);
	}
	
	public TextFileView(String path, File file) {
		super(path, file);
	}
	
	
	public String read() {
		File file = MainApp.getCurrentProject().getFile(path);
		String contents = null;
		try {
			contents = new String(Files.readAllBytes(file.toPath()), "US-ASCII");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			contents = "SporeModder - ERROR READING THE FILE";
		}
		isAscii = checkAscii(contents);
		
		return contents;
	}
	
	public void write() {
//		File file = MainApp.getCurrentProject().getFile(path);
		try 
		{
			if (!file.exists()) file.createNewFile();
			
			PrintWriter out = null;
			try {
				out = new PrintWriter(file);
				out.write(textPane.getText());
			} finally {
				if (out != null) out.close();
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error saving file " + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	private void initPanes() {
		searchTerms = MainApp.getSearchStrings();
		
		textPane = new JTextPane();
//		textPane.getDocument().addDocumentListener(new DLTextFile());
//		textPane.getDocument().addDocumentListener(new DocumentListener() {
//
//			@Override
//			public void changedUpdate(DocumentEvent e) {
//				System.out.println("change");
//			}
//
//			@Override
//			public void insertUpdate(DocumentEvent e) {
//				System.out.println("insert");
//			}
//
//			@Override
//			public void removeUpdate(DocumentEvent e) {
//				System.out.println("remove");
//			}
//			
//		});
		scrollPane = new JScrollPane(textPane);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		TextLineNumber textLineNumber = new TextLineNumber(textPane);
		textLineNumber.setUpdateFont(true);
		scrollPane.setRowHeaderView(textLineNumber);
		
		if (path.endsWith(".xml")) {
			textPane.setContentType("text/xml");
			textPane.setEditorKit(new XmlEditorKit(this));
		}
		else if (path.endsWith(".spui_t")) {
			textPane.setContentType("text/spui_t");
			textPane.setEditorKit(new SpuiEditorKit(this));
		}
		else if (path.endsWith(".tlsa_t")) {
			textPane.setContentType("text/tlsa_t");
			textPane.setEditorKit(new TlsaEditorKit(this));
		}
		else if (path.endsWith(".pfx")) {
			textPane.setContentType("text/pfx");
			textPane.setEditorKit(new PfxEditorKit(this));
		}
		else {
			textPane.setContentType("text/txt");
			textPane.setEditorKit(new TxtEditorKit(this));
		}
		
		textPane.setText(read());
		textPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		textPane.setCaretPosition(0);  // move caret to the beginning of the file
		
		// this is a source file: read-only
		if (!isAscii || MainApp.getCurrentProject().getModFile(path) == null) {
			textPane.setEditable(false);
		}
		else {
			textPane.setEditable(true);
		}
		
		undoManager = new UndoManager();
		textPane.getDocument().addUndoableEditListener(undoManager);
		textPane.addKeyListener(new KLTextFile());
		textPane.addMouseWheelListener(new MWLZoom());
		textPane.getDocument().addDocumentListener(new DLTextFile());
		
		textPane.getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
		setTabSize(2);
		
		if (searchTerms != null && searchTerms.size() > 0) {
			// move caret to first occurrence
			String text = "";
			try {
				text = textPane.getDocument().getText(0, textPane.getDocument().getLength()).toLowerCase();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int newCaret = text.length();
			for (String s : searchTerms) {
				int ind = text.indexOf(s);
				if (ind != -1 && ind < newCaret) {
					newCaret = ind;
					break;
				}
			}
			if (newCaret != text.length()) {
				textPane.setCaretPosition(newCaret);
			}
		}
	}
	
	@Override
	public JComponent getPanel() {
		//TODO is it correct to handle this here?
		if (file == null || file.isDirectory()) {
			return new JPanel();
		}
		
		if (textPane == null || scrollPane == null) {
			initPanes();
		}
		
		return scrollPane;
	}
	
	public JTextPane getTextPane() {
		return textPane;
	}
	
	@Override
	public boolean isEditable() {
		if (textPane == null) return false;
		if (!isAscii) return false;
		return textPane.isEditable();
	}
	
	@Override
	public void setEditable(boolean isMod) {
		if (textPane != null) {
			textPane.setEditable(isAscii && isMod);
		}
	}
	
	private void updateTitle() {
		parentTabbedPane.setTitleAt(tabIndex, (isSaved ? "" : "*") + name);
	}
	
	private class DLTextFile implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			// insert is called multiple times when the text is loaded
			// this is a very bad solution
			isSaved = true;
			updateTitle();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			// we only need to update if it's been saved
			if (isSaved) {
				isSaved = false;
				updateTitle();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			// we only need to update if it's been saved
			if (isSaved) {
				isSaved = false;
				updateTitle();
			}
		}
	}
	
	public void save() {
		if (file != null && file.isFile() && isAscii) {
			write();
			isSaved = true;
			updateTitle();
		}
	}
	
	private class KLTextFile implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
				System.out.println("Saving");
				save();
			}
			
			if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
				if (undoManager.canUndo()) undoManager.undo();
			}
			if ((e.getKeyCode() == KeyEvent.VK_Y) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
				if (undoManager.canRedo()) undoManager.redo();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}
	
	private class MWLZoom implements MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown())
	        {
				Font current = textPane.getFont();
				int size = current.getSize();
				float wheel = (float) e.getPreciseWheelRotation();
				
				if (size - wheel >= 7 && size - wheel < 32) {
					textPane.setFont(current.deriveFont((float) (size - e.getPreciseWheelRotation()/* * 0.2 * size*/)));
				}
	        }
	        else
	        {
	            textPane.getParent().dispatchEvent(e);
	        }
		}
		
	}
	
	private void setTabSize(int size) {
		 	TabStop[] tabs = new TabStop[1];
	        tabs[0] = new TabStop(size, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
//	        tabs[1] = new TabStop(100, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
//	        tabs[2] = new TabStop(200, TabStop.ALIGN_CENTER, TabStop.LEAD_NONE);
//	        tabs[3] = new TabStop(300, TabStop.ALIGN_DECIMAL, TabStop.LEAD_NONE);
	        TabSet tabset = new TabSet(tabs);

	        StyleContext sc = StyleContext.getDefaultStyleContext();
	        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
	        StyleConstants.TabSet, tabset);
	        textPane.setParagraphAttributes(aset, false);
	}
	
	public void setSearchTerms(List<String> searchTerms) {
		this.searchTerms = searchTerms;
		if (textPane != null) textPane.repaint();
	}
	
	public void setSearchTerm(String searchTerm) {
		searchTerms = new ArrayList<String>(1);
		searchTerms.add(searchTerm);
		if (textPane != null) {
			textPane.repaint();
		}
	}
	
	public List<String> getSearchTerms() {
		return searchTerms;
	}
	
	public boolean isSearchCaseSensitive() {
		return searchCaseSensitive;
	}
	
	public void setSearchCaseSensitive(boolean searchCaseSensitive) {
		this.searchCaseSensitive = searchCaseSensitive;
	}
	
	public void closeFindDialog() {
		if (findDialog != null) {
			findDialog.dispose();
		}
	} 
	
	public UIFindAndReplace getFindDialog() {
		return findDialog;
	}

	public void setFindDialog(UIFindAndReplace findDialog) {
		this.findDialog = findDialog;
	}

	private static int lineCount(String text, int caretPos) {
		int num = 0;
		int indexOf = -1;
		while (true) {
			indexOf = text.indexOf('\n', indexOf + 1);
			if (indexOf != -1 && indexOf <= caretPos + num) {
				num++;
			} else {
				break;
			}
		}
		return num;
	}
	
	@Override
	public int getViewType() {
		return VIEWTYPE_TEXT;
	}

	public boolean isAscii() {
		return isAscii;
	}

	public void setAscii(boolean isAscii) {
		this.isAscii = isAscii;
	}
	
	public static boolean checkAscii(String text) {
		byte[] bytes = text.getBytes(Charset.forName("US-ASCII"));
		for (int i = 0; i < bytes.length; i++) {
			// if it's not a printable character, and it's not \t, \n or \r
			if (bytes[i] < 32 && bytes[i] != 0x9 && bytes[i] != 0xA && bytes[i] != 0xD) {
				return false;
			}
		}
		return true;
	}
}
