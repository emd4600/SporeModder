package sporemodder.files.formats.dbpf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import sporemodder.utilities.performance.Profiler;
import sporemodder.utilities.performance.ProfilerBars;
import sporemodder.utilities.performance.ProfilerData;
import sporemodder.utilities.performance.ProfilerGraph;

public class PerformanceViewer extends JFrame {

	private JPanel contentPane;
	private JPanel panelLegend;
	
	private Profiler oldProfiler;
	private ProfilerGraph oldGraph;
	private ProfilerData oldData;
	
	private Profiler newProfiler;
	private ProfilerGraph newGraph;
	private ProfilerData newData;
	
	private ProfilerBars panelBars;
	
	private JLabel lblTotalTime;
	private JLabel lblTotalTime_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PerformanceViewer frame = new PerformanceViewer(null, null, null, null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PerformanceViewer(Profiler oldProfiler, Profiler newProfiler, String[] profileNames, String totalProfileName) {
		
		if (oldProfiler != null) {
			this.oldProfiler = oldProfiler;
			this.oldData = new ProfilerData(oldProfiler);
			this.oldGraph = new ProfilerGraph(oldData);
			this.oldGraph.setProfiles(profileNames);
		}
		
		if (newProfiler != null) {
			this.newProfiler = newProfiler;
			this.newData = new ProfilerData(newProfiler);
			this.newGraph = new ProfilerGraph(newData);
			this.newGraph.setProfiles(profileNames);
		}
		
		if (newProfiler != null && oldProfiler != null) {
			this.panelBars = new ProfilerBars(oldData, newData, "OLD", "NEW");
			this.panelBars.setProfiles(profileNames);
			this.panelBars.setAverageProfileName(totalProfileName);
		}
		
		
		setTitle("DBPF Packing Performance Test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 600);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(10, 10));
		
		panelLegend = new JPanel();
		contentPane.add(panelLegend, BorderLayout.EAST);
		panelLegend.setLayout(new BoxLayout(panelLegend, BoxLayout.Y_AXIS));
		
		JLabel lblLegend = new JLabel("Legend:");
		lblLegend.setFont(new Font("Tahoma", Font.PLAIN, 15));
		panelLegend.add(lblLegend);
		
		JPanel panelGraphs = new JPanel();
		contentPane.add(panelGraphs, BorderLayout.CENTER);
		GridBagLayout gbl_panelGraphs = new GridBagLayout();
		gbl_panelGraphs.columnWidths = new int[]{0, 0, 0};
		gbl_panelGraphs.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panelGraphs.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panelGraphs.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		panelGraphs.setLayout(gbl_panelGraphs);
		
		JLabel lblOldMethod = new JLabel("OLD METHOD");
		lblOldMethod.setFont(new Font("Tahoma", Font.BOLD, 15));
		GridBagConstraints gbc_lblOldMethod = new GridBagConstraints();
		gbc_lblOldMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblOldMethod.gridx = 0;
		gbc_lblOldMethod.gridy = 0;
		panelGraphs.add(lblOldMethod, gbc_lblOldMethod);
		
		JLabel lblNewMethod = new JLabel("NEW METHOD");
		lblNewMethod.setFont(new Font("Tahoma", Font.BOLD, 15));
		GridBagConstraints gbc_lblNewMethod = new GridBagConstraints();
		gbc_lblNewMethod.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewMethod.gridx = 1;
		gbc_lblNewMethod.gridy = 0;
		panelGraphs.add(lblNewMethod, gbc_lblNewMethod);
		
		if (oldProfiler != null && totalProfileName != null) {
			lblTotalTime = new JLabel("Average time: " + getTimeString(this.oldProfiler.getTime(totalProfileName)));
			lblTotalTime.setFont(new Font("Tahoma", Font.PLAIN, 15));
			
			GridBagConstraints gbc_lblTotalTime = new GridBagConstraints();
			gbc_lblTotalTime.insets = new Insets(0, 0, 5, 5);
			gbc_lblTotalTime.gridx = 0;
			gbc_lblTotalTime.gridy = 1;
			panelGraphs.add(lblTotalTime, gbc_lblTotalTime);
		}
		
		if (newProfiler != null && totalProfileName != null) {
			lblTotalTime_1 = new JLabel("Average time: " + getTimeString(this.newProfiler.getTime(totalProfileName)));
			lblTotalTime_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
			
			GridBagConstraints gbc_lblTotalTime_1 = new GridBagConstraints();
			gbc_lblTotalTime_1.insets = new Insets(0, 0, 5, 0);
			gbc_lblTotalTime_1.gridx = 1;
			gbc_lblTotalTime_1.gridy = 1;
			panelGraphs.add(lblTotalTime_1, gbc_lblTotalTime_1);
		}
		
		if (oldProfiler != null) {
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 0, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.anchor = GridBagConstraints.CENTER;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 2;
			panelGraphs.add(oldGraph, gbc_panel);
		}
			
		if (newProfiler != null) {
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.anchor = GridBagConstraints.CENTER;
			gbc_panel_1.gridx = 1;
			gbc_panel_1.gridy = 2;
			panelGraphs.add(newGraph, gbc_panel_1);
		}
		
		if (oldGraph != null) {
			addLegend(oldGraph);
		}
		
		if (panelBars != null) {
			contentPane.add(panelBars, BorderLayout.SOUTH);
		}
	}
	
	private String getTimeString(long time) {
		return time + " ms (" + String.format("%.3f", time / 1000f) + " s)";
	}

	private void addLegend(ProfilerGraph graph) {
		
		HashMap<String, Color> legend = graph.getColorLegend();
		
		for (HashMap.Entry<String, Color> entry : legend.entrySet()) {
			
			JPanel entryPanel = new JPanel();
			FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
			layout.setHgap(10);
			entryPanel.setLayout(layout);
			
			JPanel colorPanel = new JPanel();
			colorPanel.setPreferredSize(new Dimension(24, 24));
			colorPanel.setBackground(entry.getValue());
			
			entryPanel.add(colorPanel);
			entryPanel.add(new JLabel(entry.getKey()));
			
			panelLegend.add(entryPanel);
		}
	}
}
