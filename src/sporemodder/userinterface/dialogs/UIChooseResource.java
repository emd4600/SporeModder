package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BoxLayout;

import sporemodder.files.formats.ResourceKey;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIChooseResource extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 936359986391991233L;
	
	
	private final JPanel contentPanel = new JPanel();
	private JTextField tfGroup;
	private JTextField tfInstance;
	private JTextField tfType;
	
	private boolean canceled;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIChooseResource dialog = new UIChooseResource(null, "Choose Atlas");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public UIChooseResource(Window parent, String title) {
		super(parent);
		
		setTitle(title);
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 376, 120);
		setLocationRelativeTo(null);
		
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JLabel lblNewLabel = new JLabel("Specify groupID!instanceID.typeID");
			contentPanel.add(lblNewLabel);
		}
		{
			JPanel fieldsPanel = new JPanel();
			fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			contentPanel.add(fieldsPanel);
			{
				tfGroup = new JTextField();
				fieldsPanel.add(tfGroup);
				tfGroup.setColumns(10);
			}
			{
				JLabel label = new JLabel("!");
				fieldsPanel.add(label);
			}
			{
				tfInstance = new JTextField();
				fieldsPanel.add(tfInstance);
				tfInstance.setColumns(10);
			}
			{
				JLabel label = new JLabel(".");
				fieldsPanel.add(label);
			}
			{
				tfType = new JTextField();
				fieldsPanel.add(tfType);
				tfType.setColumns(10);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						canceled = false;
						dispose();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						canceled = true;
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		
		pack();
		setVisible(true);
	}
	
	public ResourceKey getResourceKey() {
		ResourceKey key = new ResourceKey();
		key.setGroupID(tfGroup.getText());
		key.setInstanceID(tfInstance.getText());
		key.setTypeID(tfType.getText());
		
		return key;
	}
	
	
	public static ResourceKey createResourceChooser(Window parent, String title) {
		UIChooseResource dialog = new UIChooseResource(parent, title);
		if (dialog.canceled) return null;
		return dialog.getResourceKey();
	}
}
