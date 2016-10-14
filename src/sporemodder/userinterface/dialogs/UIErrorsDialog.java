package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import sporemodder.MainApp;
import sporemodder.files.formats.dbpf.DBPFItem;
import sporemodder.userinterface.ErrorManager;

public class UIErrorsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7337243996237555672L;
	
	private static final int MAX_ERRORS = 7;
	
	private Component parent;
	private String str;
	private HashMap<File, Exception> exceptions;
	private HashMap<DBPFItem, Exception> exceptions2;
	private JPanel pnlMain;
	private JLabel lblInfo;
	private JPanel pnlOptions;
	private JButton btnClose;
	
	private void init() {
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Errors");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		
		pnlMain = new JPanel();
		pnlMain.setBorder(BorderFactory.createEmptyBorder(4, 3, 4, 3));
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		
		lblInfo = new JLabel(str + "\nThe following files were skipped:");
		lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlMain.add(lblInfo);
		
		int index = 0;
		int count = 0;
		int remaining = 0;
		if (exceptions != null) {
			count = exceptions.size();
			for (Map.Entry<File, Exception> entry : exceptions.entrySet()) {
				if (index >= MAX_ERRORS) {
					remaining = count - index;
					break;
				}
				
				JButton button = new JButton();
			    button.setText("<HTML><FONT color=\"#000099\"><U>" + entry.getKey().getAbsolutePath() + "</U></FONT></HTML>");
			    button.setAlignmentX(Component.LEFT_ALIGNMENT);
			    button.setHorizontalAlignment(SwingConstants.LEFT);
			    button.setBorderPainted(false);
			    button.setOpaque(false);
			    button.setBackground(Color.WHITE);
			    button.setToolTipText("View errors");
			    button.addActionListener(new OpenUrlAction(entry.getKey(), entry.getValue()));
			    pnlMain.add(button);
			    
			    index++;
			}
		} else if (exceptions2 != null) {
			count = exceptions.size();
			for (Map.Entry<DBPFItem, Exception> entry : exceptions2.entrySet()) {
				if (index >= MAX_ERRORS) {
					remaining = count - index;
					break;
				}
				
				JButton button = new JButton();
			    button.setText("<HTML><FONT color=\"#000099\"><U>" + entry.getKey().toString() + "</U></FONT></HTML>");
			    button.setAlignmentX(Component.LEFT_ALIGNMENT);
			    button.setHorizontalAlignment(SwingConstants.LEFT);
			    button.setBorderPainted(false);
			    button.setOpaque(false);
			    button.setBackground(Color.WHITE);
			    button.setToolTipText("View errors");
			    button.addActionListener(new OpenUrlAction(entry.getKey(), entry.getValue()));
			    pnlMain.add(button);
			    
			    index++;
			}
		}
		
		if (remaining > 0) {
			JLabel label = new JLabel("And " + remaining + " effects more.");
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			pnlMain.add(label);
		}
	    
	    pnlOptions = new JPanel();
	    pnlOptions.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    
	    btnClose = new JButton("Close");
	    btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
	    	
	    });
	    
	    pnlOptions.add(btnClose);
	    
	    getContentPane().add(pnlMain, BorderLayout.CENTER);
	    getContentPane().add(pnlOptions, BorderLayout.SOUTH);
	    
	    pack();
	    setLocationRelativeTo(null);
	    setVisible(true);
	}
	
	public UIErrorsDialog(HashMap<File, Exception> exceptions) {
		super(MainApp.getUserInterface());
		this.exceptions = exceptions;
		this.str = "";
		init();
	}
	
	public UIErrorsDialog(HashMap<DBPFItem, Exception> exceptions, String str) {
		super(MainApp.getUserInterface());
		this.exceptions2 = exceptions;
		this.str = str;
		init();
	}
	
	private class OpenUrlAction implements ActionListener {
		private File file;
		private DBPFItem item;
		private Exception e;
		OpenUrlAction(File file, Exception e) {
			this.e = e;
			this.file = file;
			this.item = null;
		}
		OpenUrlAction(DBPFItem item, Exception e) {
			this.e = e;
			this.item = item;
			this.file = null;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (file != null) {
				JOptionPane.showMessageDialog(UIErrorsDialog.this, file.getAbsolutePath() + "\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(UIErrorsDialog.this, item.toString() + "\n" + ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}
}
