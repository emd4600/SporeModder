package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import sporemodder.MainApp;
import sporemodder.utilities.Project;

public class UIOpenProject extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7914133740382335909L;
	
	private final JPanel contentPanel = new JPanel();
	private JList<String> list;
	private DefaultListModel<String> listModel;
	
	private JButton okButton;
	
	private Project project;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIOpenProject dialog = new UIOpenProject(null, "Choose a project", null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	private UIOpenProject(Window parent, String title, String text) {
		super(parent);
		
		setTitle(title);
		setBounds(100, 100, 450, 300);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setIconImage(null);
		
		getContentPane().setLayout(new BorderLayout());
		//contentPanel.setLayout(new FlowLayout());
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			if (text != null) {
				JLabel label = new JLabel(text);
				label.setAlignmentX(LEFT_ALIGNMENT);
				contentPanel.add(label);
			}
			
			listModel = new DefaultListModel<String>();
			
			list = new JList<String>(listModel);
			list.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					okButton.setEnabled(list.getSelectedIndices().length > 0);
				}
				
			});
			list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
			        int index = -1;
			        if (evt.getClickCount() == 2) {

			            // Double-click detected
			            index = list.locationToIndex(evt.getPoint());
			        } else if (evt.getClickCount() == 3) {

			            // Triple-click detected
			            index = list.locationToIndex(evt.getPoint());
			        }
			        
			        if (index != -1) {
			        	project = MainApp.getProjectByName(listModel.elementAt(index));
						dispose();
			        }
			    }
			});
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			List<Project> projects = MainApp.getProjects();
			for (Project p : projects) {
				listModel.addElement(p.getProjectName());
			}
			
			JScrollPane scrollPane = new JScrollPane(list);
			scrollPane.setAlignmentX(LEFT_ALIGNMENT);
			
			contentPanel.add(scrollPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setEnabled(false);
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						project = MainApp.getProjectByName(list.getSelectedValue());
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
						project = null;
						dispose();
					}
					
				});
				buttonPane.add(cancelButton);
			}
		}
		
		setVisible(true);
	}

	public static Project createOpenProjectDialog(Window parent, String title, String text) {
		UIOpenProject dialog = new UIOpenProject(parent, title, text);
		return dialog.project;
	}
}
