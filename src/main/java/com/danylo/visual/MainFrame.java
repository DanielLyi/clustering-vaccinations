package com.danylo.visual;

import com.danylo.logic.Centroid;
import com.danylo.logic.Clustering;
import com.danylo.logic.Country;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    boolean hasClustered = false;

    public MainFrame(List<Country> countries) {
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        Visualizer visualizer = new Visualizer(countries);
        add(visualizer, new GBC(0, 0, 1, 2)
                .setAnchor(GBC.WEST).setWeights(2, 1));
        JButton clusterButton = new JButton("Cluster");
        JComboBox<Integer> numOfClustersCombo = new JComboBox<>();
        for (int i = 2; i < 21; i++) {
            numOfClustersCombo.addItem(i);
        }
        add(numOfClustersCombo, new GBC(1, 0, 1, 1)
                        .setAnchor(GBC.SOUTHWEST).setWeights(1, 1).setInsets(25));
        numOfClustersCombo.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<Centroid, List<Country>> clusters =
                        Clustering.getClusters(countries,
                                numOfClustersCombo.getItemAt(numOfClustersCombo.getSelectedIndex()));
                visualizer.feedUpClusteredData(clusters);
                clusterButton.setText("Uncluster");
                hasClustered = true;
            }
        });


        clusterButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!hasClustered) {
                    Map<Centroid, List<Country>> clusters =
                            Clustering.getClusters(countries,
                                    numOfClustersCombo.getItemAt(numOfClustersCombo.getSelectedIndex()));
                    visualizer.feedUpClusteredData(clusters);
                    clusterButton.setText("Uncluster");
                    hasClustered = true;
                } else {
                    visualizer.feedUpClusteredData(null);
                    clusterButton.setText("Cluster");
                    hasClustered = false;
                }

            }
        });
        clusterButton.setMaximumSize(clusterButton.getPreferredSize());
        add(clusterButton, new GBC(1, 1, 1, 1)
                .setAnchor(GBC.NORTHWEST).setWeights(1, 1).setInsets(25));
    }
}
