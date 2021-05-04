package com.danylo.visual;

import com.danylo.logic.Centroid;
import com.danylo.logic.Country;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Visualizer extends JComponent {
    private static final List<Color> CLUSTER_COLORS =
            List.of(Color.BLUE, Color.ORANGE.darker(), Color.CYAN.darker(), Color.PINK.darker(),
                    Color.GREEN.darker(), Color.MAGENTA.darker(), Color.RED,
                    Color.LIGHT_GRAY.darker(), Color.DARK_GRAY, Color.BLACK,
                    new Color(24, 31, 95), new Color(99, 11, 73),
                    new Color(60, 97, 37), new Color(22, 95, 84),
                    new Color(66, 13, 118), new Color(109, 105, 24),
                    new Color(106, 19, 22), new Color(50, 50, 50),
                    new Color(80, 13, 87), new Color(47, 5, 5));
    private Map<Country, Point2D> countryToPoint;
    private Map<Country, Color> countryToColor;
    private boolean pointsSet = false;
    private double minAtLeastOneDosePerHundred;
    private double maxAtLeastOneDosePerHundred;
    private double minFullyVaccinatedPerHundred;
    private double maxFullyVaccinatedPerHundred;

    private Country hoveredOnCountry = null;

    public Visualizer(List<Country> countries) {
        this.countryToPoint = new HashMap<>();
        this.countryToColor = new HashMap<>();
        for (Country country : countries) {
            this.countryToPoint.put(country, null);
            this.countryToColor.put(country, Color.BLACK);
        }
        minAtLeastOneDosePerHundred = countries.stream()
                .mapToDouble(Country::atLeastOneDosePerHundred).min().orElse(Double.MAX_VALUE);
        maxAtLeastOneDosePerHundred = countries.stream()
                .mapToDouble(Country::atLeastOneDosePerHundred).max().orElse(Double.MIN_VALUE);
        minFullyVaccinatedPerHundred = countries.stream()
                .mapToDouble(Country::fullyVaccinatedPerHundred).min().orElse(Double.MAX_VALUE);
        maxFullyVaccinatedPerHundred = countries.stream()
                .mapToDouble(Country::fullyVaccinatedPerHundred).max().orElse(Double.MIN_VALUE);

        setSize(800, 800);
        setPreferredSize(new Dimension(800, 800));

        repaint();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (pointsSet) {
                    Country closestCountry = null;
                    double closestDistance = Double.MAX_VALUE;
                    for (Map.Entry<Country, Point2D> countryPoint2DEntry : Visualizer.this.countryToPoint.entrySet()) {
                        Point2D countryPoint = countryPoint2DEntry.getValue();
                        double distance = Math.sqrt(Math.pow(e.getX() - countryPoint.getX(), 2) +
                                Math.pow(e.getY() - countryPoint.getY(), 2));
                        if (distance < 10) {
                            if (distance < closestDistance) {
                                closestCountry = countryPoint2DEntry.getKey();
                                closestDistance = distance;
                            }
                        }
                    }
                    hoveredOnCountry = closestCountry;
                    repaint();

                }
            }
        });
    }

    public void feedUpClusteredData(Map<Centroid, List<Country>> clusters) {
        if (clusters == null) {
            for (Country country : countryToColor.keySet()) {
                this.countryToColor.put(country, Color.BLACK);
            }
            repaint();
            return;
        }
        int colorNum = 0;
        for (Map.Entry<Centroid, List<Country>> cluster : clusters.entrySet()) {
            Color color = CLUSTER_COLORS.get(colorNum);
            for (Country country : cluster.getValue()) {
                countryToColor.put(country, color);
            }
            colorNum++;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.GRAY);
        g2.draw(new Rectangle2D.Double(100, 100, 600, 600));
        double stepX = (maxAtLeastOneDosePerHundred - minAtLeastOneDosePerHundred) / 10;
        double stepY = (maxFullyVaccinatedPerHundred - minFullyVaccinatedPerHundred) / 10;

        g2.setPaint(Color.BLACK);
        for (int i = 0; i <= 10; i++) {
            g2.draw(new Line2D.Double(100 + 60 * i, 90, 100 + 60 * i, 110));
            g2.drawString(String.format("%.2f", stepX * i), 100 + 60 * i, 80);
        }
        g2.drawString("At least one dose (%)", 650, 60);
        for (int i = 0; i <= 10; i++) {
            g2.draw(new Line2D.Double(90, 100 + 60 * i, 110, 100 + 60 * i));
            g2.drawString(String.format("%.2f", stepY * i), 60, 100 + 60 * i);
        }
        g2.drawString("Fully vaccinated (%)", 60, 720);

        if (!pointsSet) {
            for (Map.Entry<Country, Point2D> countryPoint2DEntry : countryToPoint.entrySet()) {
                Country country = countryPoint2DEntry.getKey();
                g2.setPaint(countryToColor.get(country));
                countryToPoint.put(country, new Point2D.Double(100 + 60 * country.atLeastOneDosePerHundred() / stepX,
                        100 + 60 * country.fullyVaccinatedPerHundred() / stepY));
                g2.fill(new Ellipse2D.Double(100 + 60 * country.atLeastOneDosePerHundred() / stepX - 4,
                        100 + 60 * country.fullyVaccinatedPerHundred() / stepY - 4, 8, 8));
            }
            pointsSet = true;
        } else {
            for (Map.Entry<Country, Point2D> countryPoint2DEntry : countryToPoint.entrySet()) {
                g2.setPaint(countryToColor.get(countryPoint2DEntry.getKey()));
                g2.fill(new Ellipse2D.Double(countryPoint2DEntry.getValue().getX() - 4,
                        countryPoint2DEntry.getValue().getY() - 4, 8, 8));
            }
        }

        if (hoveredOnCountry != null) {
            g2.setPaint(countryToColor.get(hoveredOnCountry));
            String atLeastOneDose = String.format("%.2f", hoveredOnCountry.atLeastOneDosePerHundred());
            String fullyVaccinated = String.format("%.2f", hoveredOnCountry.fullyVaccinatedPerHundred());
            g2.drawString(hoveredOnCountry.name() + "(" + atLeastOneDose
                    + ", " + fullyVaccinated + ")", 400, 50);
            g2.fill(new Ellipse2D.Double(countryToPoint.get(hoveredOnCountry).getX() - 10,
                    countryToPoint.get(hoveredOnCountry).getY() - 10, 20, 20));
        }
    }
}
