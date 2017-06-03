package sporemodder.userinterface;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import sporemodder.MainApp;

public class ConsoleDialog {
	
	private JTextPane textPane;
	private JDialog dialog;
	
	@SuppressWarnings("resource")
	public static ConsoleDialog redirectConsole() {
		ConsoleDialog dialog = new ConsoleDialog();
		dialog.textPane = new JTextPane();
		
		System.setOut(new PrintStream(dialog.new ConsoleStream(Color.BLACK)));
		System.setErr(new PrintStream(dialog.new ConsoleStream(Color.RED)));
		
		return dialog;
	}
	
	public void toggleDialog() {
		if (dialog == null) {
			dialog = showDialog();
		}
		else {
			if (dialog.isVisible()) {
				dialog.dispose();
			}
			else {
				dialog.setVisible(true);
			}
		}
	}
	
	public JDialog showDialog() {
		JDialog dialog = new JDialog(MainApp.getUserInterface());
		dialog.setTitle("SporeModder Console");
		dialog.setContentPane(new JScrollPane(textPane));
		dialog.setSize(800, 600);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		dialog.setLocationRelativeTo(null);
		
		dialog.setVisible(true);
		
		return dialog;
	}
	
	private void appendToPane(String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = textPane.getDocument().getLength();
        textPane.setCaretPosition(len);
        textPane.setCharacterAttributes(aset, false);
        textPane.replaceSelection(msg);
    }

	private class ConsoleStream extends OutputStream {
		private Color color;
		
		private ConsoleStream(Color color) {
			super();
			this.color = color;
		}

		@Override
		public void write(int b) throws IOException {
			// redirects data to the text area
			appendToPane(String.valueOf((char) b), color);
			// scrolls the text area to the end of data
			textPane.setCaretPosition(textPane.getDocument().getLength());
		}
		
	}
}
