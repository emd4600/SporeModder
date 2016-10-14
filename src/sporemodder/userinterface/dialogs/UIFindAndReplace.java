package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

import sporemodder.MainApp;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.userinterface.fileview.TextFileView;

public class UIFindAndReplace extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8709512936833731208L;
	
	public static final int FIND = 0;
	public static final int REPLACE = 1;

	private JTabbedPane tabbedPane;
	
	private JPanel panelFindContainer;
	private JPanel panelFind;
	private JTextField tffFind;
	private JPanel pnlfOptions;
	private JCheckBox cbfCaseSensitive;
	private JCheckBox cbfWrapSearch;
	private ButtonGroup buttonGroupFind;
	private JRadioButton rbfForward;
	private JRadioButton rbfBackward;
	private JPanel pnlfButtons;
	private JButton btnfFindNext;
	private JButton btnfFindAll;
	private JLabel lblfInfo;
	private JButton btnfClose;
	
	private JPanel panelReplaceContainer;
	private JPanel panelReplace;
	private JTextField tfrFind;
	private JTextField tfrReplaceWith;
	private JPanel pnlrOptions;
	private JCheckBox cbrCaseSensitive;
	private JCheckBox cbrWrapSearch;
	private ButtonGroup buttonGroupReplace;
	private JRadioButton rbrForward;
	private JRadioButton rbrBackward;
	private JPanel pnlrButtons;
	private JButton btnrFindNext;
	private JButton btnrFindAll;
	private JButton btnrReplaceNext;
	private JButton btnrReplaceAll;
	private JLabel lblrInfo;
	private JButton btnrClose;
	
	private TextFileView fileView;
	private JTextPane textPane;

	public UIFindAndReplace(int initialPanel) {
		super(MainApp.getUserInterface());
		
		FileView view = MainApp.getActiveFileView();
		if (view == null || view.getViewType() != TextFileView.VIEWTYPE_TEXT) {
			dispose();
			return;
		}
		fileView = (TextFileView) view;
		if (fileView.getFindDialog() != null) {
			dispose();
			return;
		}
		fileView.setFindDialog(this);
		textPane = fileView.getTextPane();
		if (textPane == null) {
			dispose();
			return;
		}
		
		setLocationRelativeTo(null);
		setTitle("Find");
		setResizable(false);
		// setAlwaysOnTop(true);
		setModal(true);
		setModalityType(ModalityType.MODELESS);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		initFindPanel();
		
		/**
		 * Replacing has been removed since I had some difficulties implementing it. 
		 * When replacing the entire text, each line of text is registered as an undoable action.
		 * The replaced text is not selected.
		 * The order in which replaces the words doesn't seem to work very well...
		 */
		
		// initReplacePanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Find", panelFindContainer);
		// tabbedPane.addTab("Find/Replace", panelReplaceContainer);
		
		tabbedPane.setSelectedIndex(initialPanel);
		
		getContentPane().add(tabbedPane);
		
		pack();
		setVisible(true);
		
		tffFind.requestFocusInWindow();
	}
	
	private void initFindPanel() {
		/* -- FIND -- */
		
		tffFind = new JTextField();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(new JLabel("Find: "), BorderLayout.WEST);
		panel.add(tffFind, BorderLayout.CENTER);
		
		cbfCaseSensitive = new JCheckBox("Case sensitive");
		cbfWrapSearch = new JCheckBox("Wrap search", true);
		rbfForward = new JRadioButton("Forward", true);
		rbfBackward = new JRadioButton("Backward");
		buttonGroupFind = new ButtonGroup();
		buttonGroupFind.add(rbfForward);
		buttonGroupFind.add(rbfBackward);
		
		pnlfOptions = new JPanel();
		pnlfOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlfOptions.setBorder(BorderFactory.createTitledBorder("Options"));
		pnlfOptions.setLayout(new GridLayout(2, 2));
		pnlfOptions.add(cbfCaseSensitive);
		pnlfOptions.add(cbfWrapSearch);
		pnlfOptions.add(rbfForward);
		pnlfOptions.add(rbfBackward);
		
		lblfInfo = new JLabel();
		
		btnfFindNext = new JButton("Find Next");
		btnfFindNext.addActionListener(new ALFindNext(tffFind, cbfCaseSensitive, cbfWrapSearch, rbfForward, rbfBackward, lblfInfo));
		
		btnfFindAll = new JButton("Find All");
		btnfFindAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// set search terms repaints, so the order is important
				fileView.setSearchCaseSensitive(cbfCaseSensitive.isSelected());
				fileView.setSearchTerm(tffFind.getText());
			}
			
		});
		
		pnlfButtons = new JPanel();
		pnlfButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlfButtons.setLayout(new GridLayout(1, 2));
		pnlfButtons.add(btnfFindNext);
		pnlfButtons.add(btnfFindAll);
		
		panelFind = new JPanel(); 
		panelFind.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
		panelFind.setLayout(new BoxLayout(panelFind, BoxLayout.Y_AXIS));
		panelFind.add(panel);
		panelFind.add(pnlfOptions);
		panelFind.add(pnlfButtons);
		
		btnfClose = new JButton("Close");
		btnfClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout());
		buttonsPanel.add(lblfInfo, BorderLayout.WEST);
		buttonsPanel.add(btnfClose, BorderLayout.EAST);
		
		panelFindContainer = new JPanel();
		panelFindContainer.setLayout(new BorderLayout());
		panelFindContainer.add(panelFind, BorderLayout.NORTH);  // we don't want it to be resized, so use NORTH
		panelFindContainer.add(buttonsPanel, BorderLayout.SOUTH);
		
		tffFind.addActionListener(new ALFindNext(tffFind, cbfCaseSensitive, cbfWrapSearch, rbfForward, rbfBackward, lblfInfo));
	}
	
	private void initReplacePanel() {
		/* -- FIND -- */
		
		tfrFind = new JTextField();
		tfrReplaceWith = new JTextField();
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.25;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Find: "), gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Replace: "), gbc);
		
		gbc.weightx = 0.75;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(tfrFind, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		panel.add(tfrReplaceWith, gbc);
		
		cbrCaseSensitive = new JCheckBox("Case sensitive");
		cbrWrapSearch = new JCheckBox("Wrap search", true);
		rbrForward = new JRadioButton("Forward", true);
		rbrBackward = new JRadioButton("Backward");
		buttonGroupReplace = new ButtonGroup();
		buttonGroupReplace.add(rbrForward);
		buttonGroupReplace.add(rbrBackward);
		
		pnlrOptions = new JPanel();
		pnlrOptions.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlrOptions.setBorder(BorderFactory.createTitledBorder("Options"));
		pnlrOptions.setLayout(new GridLayout(2, 2));
		pnlrOptions.add(cbrCaseSensitive);
		pnlrOptions.add(cbrWrapSearch);
		pnlrOptions.add(rbrForward);
		pnlrOptions.add(rbrBackward);
		
		lblrInfo = new JLabel();
		
		btnrFindNext = new JButton("Find Next");
		btnrFindNext.addActionListener(new ALFindNext(tfrFind, cbrCaseSensitive, cbrWrapSearch, rbrForward, rbrBackward, lblrInfo));
		
		btnrFindAll = new JButton("Find All");
		//TODO Find all
		
		btnrReplaceNext = new JButton("Replace Next");
		btnrReplaceNext.addActionListener(new ALReplaceNext(tfrFind, tfrReplaceWith, cbrCaseSensitive, cbrWrapSearch, rbrForward, rbrBackward, lblrInfo));
		
		btnrReplaceAll = new JButton("Replace All");
		
		pnlrButtons = new JPanel();
		pnlrButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlrButtons.setLayout(new GridLayout(2, 2));
		pnlrButtons.add(btnrFindNext);
		pnlrButtons.add(btnrFindAll);
		pnlrButtons.add(btnrReplaceNext);
		pnlrButtons.add(btnrReplaceAll);
		
		panelReplace = new JPanel(); 
		panelReplace.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
		panelReplace.setLayout(new BoxLayout(panelReplace, BoxLayout.Y_AXIS));
		panelReplace.add(panel);
		panelReplace.add(pnlrOptions);
		panelReplace.add(pnlrButtons);
		
		btnrClose = new JButton("Close");
		btnrClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout());
		buttonsPanel.add(lblrInfo, BorderLayout.WEST);
		buttonsPanel.add(btnrClose, BorderLayout.EAST);
		
		panelReplaceContainer = new JPanel();
		panelReplaceContainer.setLayout(new BorderLayout());
		panelReplaceContainer.add(panelReplace, BorderLayout.CENTER);
		panelReplaceContainer.add(buttonsPanel, BorderLayout.SOUTH);
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
	
	private class ALFindNext implements ActionListener {
		
		private JTextField textField;
		private JCheckBox cbCaseSensitive;
		private JCheckBox cbWrapSearch;
		private JRadioButton rbForward;
		private JRadioButton rbBackward;
		private JLabel lblInfo;
		private ALFindNext(JTextField textField, JCheckBox cbCaseSensitive, JCheckBox cbWrapSearch, JRadioButton rbForward, JRadioButton rbBackward, JLabel lblInfo) {
			this.textField = textField;
			this.cbCaseSensitive = cbCaseSensitive;
			this.cbWrapSearch = cbWrapSearch;
			this.rbForward = rbForward;
			this.rbBackward = rbBackward;
			this.lblInfo = lblInfo;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//String text = textPane.getText();
			String text = "";
			try {
				text = textPane.getDocument().getText(0, textPane.getDocument().getLength());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String searchText = textField.getText();
			int caret = textPane.getCaretPosition();
			
			if (!cbCaseSensitive.isSelected()) {
				text = text.toLowerCase();
				searchText = searchText.toLowerCase();
			}
			find(textPane, text, searchText, caret, rbForward.isSelected(), cbWrapSearch.isSelected(), lblInfo);
		}
	}
	
	public static void find(JTextPane textPane, String text, String searchText, int caret, boolean forward, boolean wrapSearch, JLabel lblInfo) {
		if (forward) {
			int indexOf = text.indexOf(searchText, caret);
			if (indexOf != -1) {
				textPane.setCaretPosition(indexOf);
				textPane.setSelectionStart(indexOf );
				textPane.setSelectionEnd(indexOf + searchText.length());
			}
			else {
				if (wrapSearch && caret != (forward ? 0 : text.length())) {
					find(textPane, text, searchText, 0, forward, wrapSearch, lblInfo);
				} else {
					if (lblInfo != null) {
						lblInfo.setText("String Not Found");
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		} else {
			int selectionStart = textPane.getSelectionStart();
			int indexOf = 0;
			if (selectionStart == caret) {
				// nothing selected
				indexOf = text.lastIndexOf(searchText, caret - 1);
			} else {
				int selectionEnd = textPane.getSelectionEnd();
				indexOf = text.lastIndexOf(searchText, selectionEnd - searchText.length() - 1);
			}
			
			if (indexOf != -1) {
				textPane.setCaretPosition(indexOf);
				textPane.setSelectionStart(indexOf);
				textPane.setSelectionEnd(indexOf  + searchText.length());
			}
			else {
				if (wrapSearch && caret != (forward ? 0 : text.length())) {
					find(textPane, text, searchText, text.length(), forward, wrapSearch, lblInfo);
				} else {
					if (lblInfo != null) {
						lblInfo.setText("String Not Found");
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		}
	}
	
//	public static void find(JTextPane textPane, String text, String searchText, int caret, boolean forward, boolean wrapSearch, JLabel lblInfo) {
//		int lineCountCaret = lineCount(text, caret);
//		if (forward) {
//			int indexOf = text.indexOf(searchText, caret + lineCountCaret);
//			if (indexOf != -1) {
//				int lineCount = lineCount(text, indexOf);
//				textPane.setCaretPosition(indexOf - lineCount);
//				textPane.setSelectionStart(indexOf - lineCount);
//				textPane.setSelectionEnd(indexOf - lineCount + searchText.length());
//			}
//			else {
//				if (wrapSearch && caret != (forward ? 0 : text.length())) {
//					find(textPane, text, searchText, 0, forward, wrapSearch, lblInfo);
//				} else {
//					if (lblInfo != null) {
//						lblInfo.setText("String Not Found");
//						Toolkit.getDefaultToolkit().beep();
//					}
//				}
//			}
//		} else {
//			int selectionStart = textPane.getSelectionStart();
//			int indexOf = 0;
//			if (selectionStart == caret) {
//				// nothing selected
//				indexOf = text.lastIndexOf(searchText, caret + lineCountCaret - 1);
//			} else {
//				int selectionEnd = textPane.getSelectionEnd();
//				int lineCountSelection = lineCount(text, selectionEnd);
//				indexOf = text.lastIndexOf(searchText, selectionEnd + lineCountSelection - searchText.length() - 1);
//			}
//			
//			if (indexOf != -1) {
//				int lineCount = lineCount(text, indexOf);
//				textPane.setCaretPosition(indexOf - lineCount);
//				textPane.setSelectionStart(indexOf - lineCount);
//				textPane.setSelectionEnd(indexOf - lineCount + searchText.length());
//			}
//			else {
//				if (wrapSearch && caret != (forward ? 0 : text.length())) {
//					find(textPane, text, searchText, text.length(), forward, wrapSearch, lblInfo);
//				} else {
//					if (lblInfo != null) {
//						lblInfo.setText("String Not Found");
//						Toolkit.getDefaultToolkit().beep();
//					}
//				}
//			}
//		}
//	}
	
	private class ALReplaceNext implements ActionListener {
		
		private JTextField tfFind;
		private JTextField tfReplaceWith;
		private JCheckBox cbCaseSensitive;
		private JCheckBox cbWrapSearch;
		private JRadioButton rbForward;
		private JRadioButton rbBackward;
		private JLabel lblInfo;
		private ALReplaceNext(JTextField tfFind, JTextField tfReplaceWith, JCheckBox cbCaseSensitive, JCheckBox cbWrapSearch, JRadioButton rbForward, JRadioButton rbBackward, JLabel lblInfo) {
			this.tfFind = tfFind;
			this.tfReplaceWith = tfReplaceWith;
			this.cbCaseSensitive = cbCaseSensitive;
			this.cbWrapSearch = cbWrapSearch;
			this.rbForward = rbForward;
			this.rbBackward = rbBackward;
			this.lblInfo = lblInfo;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String text = textPane.getText();
			String originalText = text;
			String findText = tfFind.getText();
			String replaceText = tfReplaceWith.getText();
			int caret = textPane.getCaretPosition();
			
			if (!cbCaseSensitive.isSelected()) {
				text = text.toLowerCase();
				findText = findText.toLowerCase();
			}
			StringBuilder sb = new StringBuilder();
			replaceNext(sb, originalText, text, findText, replaceText, caret, rbForward.isSelected(), cbWrapSearch.isSelected(), lblInfo);
			textPane.setText(sb.toString());
		}
	}
	
	private void replaceNext(
			StringBuilder sb,  // the output StringBuilder where the text will be written
			String originalText,  // the original, unmodified text
			String text,  // the text where the string will be searched, lowercase if ignoreCase
			String findText,  // the text to find, lowercase if ignoreCase
			String replaceText,  // the text to replace with
			int caret,  // the starting position to find
			boolean forward,  // search forward or search backward?
			boolean wrapSearch,  // wrap the search if the end is reached?
			JLabel lblInfo  // the label to put info in
			) {
		
		int lineCountCaret = lineCount(text, caret);
		int indexOf = -1;
		if (forward) {
			indexOf = text.indexOf(findText, caret + lineCountCaret);
		} else {
			int selectionStart = textPane.getSelectionStart();
			if (selectionStart == caret) {
				// nothing selected
				indexOf = text.lastIndexOf(findText, caret + lineCountCaret - 1);
			} else {
				int selectionEnd = textPane.getSelectionEnd();
				int lineCountSelection = lineCount(text, selectionEnd);
				indexOf = text.lastIndexOf(findText, selectionEnd + lineCountSelection - findText.length() - 1);
			}
		}
		
		if (indexOf != -1) {
			sb.append(originalText.substring(0, indexOf));
			sb.append(replaceText);
			sb.append(originalText.substring(indexOf + findText.length()));
			
			int lineCount = lineCount(text, indexOf);
			textPane.setCaretPosition(indexOf - lineCount);
			textPane.setSelectionStart(indexOf - lineCount);
			textPane.setSelectionEnd(indexOf - lineCount + replaceText.length());
		}
		else {
			if (wrapSearch && caret != (forward ? 0 : text.length())) {
				replaceNext(sb, originalText, text, findText, replaceText, 0, forward, wrapSearch, lblInfo);
			} else {
				lblInfo.setText("String Not Found");
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}
	
	private class ALFindAll implements ActionListener {
		private JTextField tfFind;
		private ALFindAll(JTextField tfFind) {
			this.tfFind = tfFind;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (fileView != null) {
				fileView.setSearchTerm(tfFind.getText());
			}
		}
	}
}