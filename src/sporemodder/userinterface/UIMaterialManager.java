package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.files.formats.renderWare4.RW4Main;
import sporemodder.files.formats.renderWare4.RW4Section.SectionInfo;
import sporemodder.files.formats.renderWare4.RW4TexMetadata;
import sporemodder.files.formats.shaders.MaterialAssignments;
import sporemodder.files.formats.shaders.MaterialAssignments.MaterialAssignment;
import sporemodder.files.formats.shaders.material.SporeMaterial;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.syntaxpane.MtlView.MtlEditorKit;
import sporemodder.userinterface.syntaxpane.TextLineNumber;
import sporemodder.utilities.FilteredTree;
import sporemodder.utilities.FilteredTreeModel;
import sporemodder.utilities.FilteredTreeModel.TreeFilter;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.SearchSpec;

public class UIMaterialManager extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
//	private DefaultListModel<MaterialAssignment> materialsListModel;
//	private JList<MaterialAssignment> materialsList;
	
	//private JTable materialsList;
	
	private FilteredTreeModel materialsListModel;
	private FilteredTree materialsList;
	private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Materials");
	
	private final List<SearchSpec> searchSpecs = new ArrayList<SearchSpec>();
	private JTextField tfSearch;
	
	private JTextPane textPane;
	private JScrollPane viewerScrollPane;
	
	private List<MaterialAssignment> materialAssignments;
	private List<RW4TexMetadata> compiledStates;
	
	private InputStreamAccessor compiledStatesInput;
	
	public UIMaterialManager() {
		setSize(800, 600);
		setTitle("Spore Materials Viewer");
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
//		materialsListModel = new DefaultListModel<MaterialAssignment>();
//		
//		materialsList = new JList<MaterialAssignment>(materialsListModel);
//		materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		materialsList.addListSelectionListener(new ListSelectionListener() {
//
//			@Override
//			public void valueChanged(ListSelectionEvent arg0) {
//				try {
//					listSelectionChanged();
//				} catch (IOException e) {
//					JOptionPane.showMessageDialog(UIMaterialManager.this, "Error: the material could not be read.", "Error", JOptionPane.ERROR_MESSAGE);
//					e.printStackTrace();
//				}
//			}
//			
//		});
		
		tfSearch = new JTextField();
		tfSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				searchAction(tfSearch.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				searchAction(tfSearch.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				searchAction(tfSearch.getText());
			}
		});
		
		materialsListModel = new FilteredTreeModel(rootNode);
		materialsListModel.setFilter(new TreeFilter() {
			@Override
			public boolean accept(DefaultMutableTreeNode node) {
				
				// always accept the root node
				if (node.getParent() == null) {
					return true;
				}
				
				String str = node.getUserObject().toString().toLowerCase();
				for (SearchSpec spec : searchSpecs) {
					if (!str.contains(spec.getLowercaseString())) {
						return false;
					}
				}
				return true;
			}
		});
		materialsListModel.setFilterEnabled(true);
		
		materialsList = new FilteredTree(materialsListModel);
		materialsList.setMinimumSize(new Dimension(200, materialsList.getMinimumSize().height));
		
		materialsList.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		materialsList.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				try {
					listSelectionChanged();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(UIMaterialManager.this, "Error: the material could not be read.", "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		});
		
		JScrollPane listScrollPane = new JScrollPane(materialsList);
		
		
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BorderLayout());
		westPanel.add(tfSearch, BorderLayout.NORTH);
		westPanel.add(listScrollPane, BorderLayout.CENTER);
		
		contentPanel.add(westPanel, BorderLayout.WEST);
		
		JPanel viewerPanel = new JPanel();
		contentPanel.add(viewerPanel, BorderLayout.CENTER);
		viewerPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel buttonsPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonsPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		viewerPanel.add(buttonsPanel, BorderLayout.SOUTH);
		
		JButton btnExportMaterial = new JButton("Export Material");
		btnExportMaterial.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					exportMaterialAction();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(UIMaterialManager.this, "Error: the compiled state could not be exported.", "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
			
		});
		buttonsPanel.add(btnExportMaterial);
		
		JButton btnExportCompiledState = new JButton("Export Compiled State");
		btnExportCompiledState.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					exportCompiledStateAction();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(UIMaterialManager.this, "Error: the compiled state could not be exported.", "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
			
		});
		buttonsPanel.add(btnExportCompiledState);
		
		textPane = new JTextPane() {
			@Override
		    public boolean getScrollableTracksViewportWidth() {
		        return getUI().getPreferredSize(this).width
		                        <= getParent().getSize().width;
		    }
		};
		textPane.setEditable(true);
		textPane.setEditorKit(new MtlEditorKit(null));
		
		viewerScrollPane = new JScrollPane(textPane);
		viewerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		viewerPanel.add(viewerScrollPane, BorderLayout.CENTER);
		
		TextLineNumber textLineNumber = new TextLineNumber(textPane);
		textLineNumber.setUpdateFont(true);
		viewerScrollPane.setRowHeaderView(textLineNumber);
		
	}
	
	private void searchAction(String text) {
		searchSpecs.clear();
		
		if (text != null && text.length() > 0) {
			List<String> strings = MainApp.getSearchStrings(text);
			
			for (String str : strings) {
				searchSpecs.add(new SearchSpec(str.toLowerCase()));
			}
			
		}
		
		materialsList.updateUI();
		materialsList.repaint();
	}
	
	private void saveCompiledState(File file, int index) throws IOException {
		SectionInfo sectionInfo = compiledStates.get(index).sectionInfo;
		compiledStatesInput.seek(sectionInfo.pos);
		
		byte[] arr = new byte[sectionInfo.size];
		compiledStatesInput.read(arr);
		
		Files.write(file.toPath(), arr);
	}
	
	private MaterialAssignment getSelectedAssignment() {
		if (!materialsList.isSelectionEmpty()) {
			Object obj = ((DefaultMutableTreeNode) materialsList.getSelectionPath().getLastPathComponent()).getUserObject();
			
			if (obj instanceof MaterialAssignment) {
				return (MaterialAssignment) obj;
			} else {
				return null;
			}
		}

		return null;
	}
	
	private void exportCompiledStateAction() throws IOException {
		//MaterialAssignment assignment = (MaterialAssignment) materialsList.getValueAt(materialsList.getSelectedRow(), 0);
		//MaterialAssignment assignment = materialsList.getSelectedValue();
		
		MaterialAssignment assignment = getSelectedAssignment();
		
		if (assignment == null) {
			return;
		}
		
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, this, JFileChooser.FILES_ONLY, false, /* Hasher.getFileName(assignment.materialID) + ".bin" ,*/ 
				AdvancedFileChooser.ChooserType.SAVE);
		
		String path = chooser.launch();
		
		if (path != null && path.length() > 0) {
			
			if (assignment.numCompiledStates > 1) {
				File file = new File(path);
				String name = file.getName();
				String[] splits = name.split(".", 2);
				
				for (int i = 0; i < assignment.numCompiledStates; i++) {
					
					saveCompiledState(new File(file.getParentFile(), splits[0] + "-" + Integer.toString(i) + "." + splits[1]), assignment.compiledStateIndex + i);
				}
			}
			else {
				// just write the only one we have
				saveCompiledState(new File(path), assignment.compiledStateIndex);
			}
		}
	}
	
	private void exportMaterialAction() throws IOException {
		if (textPane.getText() == null) {
			return;
		}
		
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, this, JFileChooser.FILES_ONLY, false, /* Hasher.getFileName(assignment.materialID) + ".bin" ,*/ 
				AdvancedFileChooser.ChooserType.SAVE);
		
		String path = chooser.launch();
		
		if (path != null && path.length() > 0) {
			
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
				bw.write(textPane.getText());
			}
		}
	}
	
	private void listSelectionChanged() throws IOException {
		//MaterialAssignment assignment = (MaterialAssignment) materialsList.getValueAt(materialsList.getSelectedRow(), 0);
		//MaterialAssignment assignment = materialsList.getSelectedValue();
		MaterialAssignment assignment = getSelectedAssignment();
		
		if (assignment == null) {
			return;
		}
		
		ArgScript as = new ArgScript();
		
		String[] blockNames = new String[assignment.numCompiledStates];
		
		for (int i = 0; i < assignment.numCompiledStates; i++) {
			SectionInfo sectionInfo = compiledStates.get(assignment.compiledStateIndex + i).sectionInfo;
			compiledStatesInput.seek(sectionInfo.pos);
			
			SporeMaterial material = new SporeMaterial();
			material.read(compiledStatesInput);
			
			blockNames[i] = "material-" + Integer.toString(assignment.compiledStateIndex + i);
			
			as.putBlock(material.toBlock(blockNames[i]));
			as.addBlankLine();
		}
		
		String[] textureNames = new String[assignment.textureNames.length];
		for (int i = 0; i < textureNames.length; i++) {
			String name = Hasher.getFileName(assignment.textureNames[i], "0x");
			
			if (assignment.textureGroups[i] != 0) {
				name = Hasher.getFileName(assignment.textureGroups[i], "0x") + "!" + name;
			}
			
			textureNames[i] = name;
		}
		
		ArgScriptCommand c = new ArgScriptCommand("exportMaterial", Hasher.getFileName(assignment.materialID, "0x"));

		if (blockNames.length > 0) {
			c.putOption(new ArgScriptOption("materials", blockNames));
		}
		
		if (textureNames.length > 0) {
			c.putOption(new ArgScriptOption("textures", textureNames));
		}
		
		as.putCommand(c);
		
		textPane.setText(as.toString());
		textPane.setCaretPosition(0);
		viewerScrollPane.revalidate();
	}
	
	public boolean initialize(InputStreamAccessor assignmentsInput, InputStreamAccessor compiledStatesInput) throws IOException, InstantiationException, IllegalAccessException {
		
		this.compiledStatesInput = compiledStatesInput;
		
		materialAssignments = MaterialAssignments.read(assignmentsInput);
		if (materialAssignments == null) {
			JOptionPane.showMessageDialog(this, "Error: The material assignments input could not be read.", "Error", JOptionPane.ERROR_MESSAGE);
			dispose();
			return false;
		}
		
		for (MaterialAssignment mat : materialAssignments) {
			// materialsListModel.addElement(mat);
			materialsListModel.insertNodeInto(new DefaultMutableTreeNode(mat), rootNode, rootNode.getChildCount());
		}
		
		
		RW4Main rw4 = new RW4Main();
		rw4.readHeader(compiledStatesInput);
		rw4.readSections(compiledStatesInput);
		
		compiledStates = rw4.getSections(RW4TexMetadata.class);
		
		
		materialsList.expandRow(0);
		
		return true;
	}
	
	public static UIMaterialManager showChooserDialog() throws IOException, InstantiationException, IllegalAccessException {
		
		//JOptionPane dialog = new JOptionPane((Object) mainPanel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		//dialog.createDialog(MainApp.getUserInterface(), "Select files");
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JLabel label = new JLabel("<html>The first file is usually located in the #40212000 folder.<br>"
				+ "The second file is usually located in the #40212001 folder.<br>"
				+ "#00000003 is the highest quality, followed by #00000002, etc</html>");
		
		mainPanel.add(label, BorderLayout.NORTH);
		
		JTextField tfAssignmentsPath = new JTextField();
		JTextField tfCompiledStatePath = new JTextField();
			
		{
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout());
			
			tfAssignmentsPath.setColumns(100);
			
			JButton btnAssignmentsPath = new JButton("Find file");
			btnAssignmentsPath.addActionListener(new AdvancedFileChooser(tfAssignmentsPath, MainApp.getUserInterface(), JFileChooser.FILES_ONLY, false, AdvancedFileChooser.ChooserType.OPEN));
			
			panel.add(tfAssignmentsPath);
			panel.add(btnAssignmentsPath);
			
			mainPanel.add(panel, BorderLayout.CENTER);
		}
		
		{
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout());
			
			tfCompiledStatePath.setColumns(100);
			
			JButton btnCompiledStatePath = new JButton("Find file");
			btnCompiledStatePath.addActionListener(new AdvancedFileChooser(tfCompiledStatePath, MainApp.getUserInterface(), JFileChooser.FILES_ONLY, false, AdvancedFileChooser.ChooserType.OPEN));
			
			panel.add(tfCompiledStatePath);
			panel.add(btnCompiledStatePath);
			
			mainPanel.add(panel, BorderLayout.SOUTH);
		}
		
		int result = JOptionPane.showConfirmDialog(MainApp.getUserInterface(), (Object) mainPanel, "Select files", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			
			String assignmentsInputPath = tfAssignmentsPath.getText();	
			String compiledStatesInputPath = tfCompiledStatePath.getText();
			
			if (assignmentsInputPath == null || assignmentsInputPath.length() == 0
					|| compiledStatesInputPath == null || compiledStatesInputPath.length() == 0) {
				return null;
			}
			
			
			try (InputStreamAccessor assignmentsInput = new FileStreamAccessor(tfAssignmentsPath.getText(), "r"); 
					InputStreamAccessor compiledStatesInput = new FileStreamAccessor(tfCompiledStatePath.getText(), "r")) {
				
				UIMaterialManager dialog = new UIMaterialManager();
				//dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
				
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setModal(true);
				
				dialog.initialize(assignmentsInput, compiledStatesInput);
				
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			}
		}
		
		return null;
	}

	/**
	 * Launch the application.
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		
		MainApp.init();
		
		String inputAssignmentsPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\GA Shaders\\#40212000\\#00000003.rw4";
		String inputCompiledStatePath = "E:\\Eric\\Spore DLL Injection\\Shaders\\GA Shaders\\#40212001\\#00000003.rw4\\raw.rw4";
		
		try (InputStreamAccessor assignmentsInput = new FileStreamAccessor(inputAssignmentsPath, "r"); 
				InputStreamAccessor compiledStatesInput = new FileStreamAccessor(inputCompiledStatePath, "r")) {
			
			UIMaterialManager dialog = new UIMaterialManager();
			//dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
			
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			
			dialog.initialize(assignmentsInput, compiledStatesInput);
			
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		}
	}
}
