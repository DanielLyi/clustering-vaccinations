package com.danylo.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Clustering {
    public static Map<Centroid, List<Country>> getClusters(List<Country> countries, int numOfClusters) {
        List<Centroid> centroids = generateCentroids(countries, numOfClusters);
        Map<Centroid, List<Country>> clusters = null;
        Map<Centroid, List<Country>> oldClusters;
        do {
            oldClusters = clusters;
            clusters = recalculateClusters(countries, centroids);
            centroids = recalculateCentroids(clusters);
        } while (!clusters.equals(oldClusters));
        return clusters;
    }

    private static List<Centroid> generateCentroids(List<Country> countries, int numOfClusters) {
        double minAtLeastOneDosePerHundred = countries.stream()
                .mapToDouble(Country::atLeastOneDosePerHundred).min().orElse(Double.MAX_VALUE);
        double maxAtLeastOneDosePerHundred = countries.stream()
                .mapToDouble(Country::atLeastOneDosePerHundred).max().orElse(Double.MIN_VALUE);
        double minFullyVaccinatedPerHundred = countries.stream()
                .mapToDouble(Country::fullyVaccinatedPerHundred).min().orElse(Double.MAX_VALUE);
        double maxFullyVaccinatedPerHundred = countries.stream()
                .mapToDouble(Country::fullyVaccinatedPerHundred).max().orElse(Double.MIN_VALUE);

        List<Centroid> centroids = new ArrayList<>(numOfClusters);

        for (int i = 0; i < numOfClusters; i++) {
            double atLeastOneDosePerHundred = minAtLeastOneDosePerHundred
                    + (maxAtLeastOneDosePerHundred - minAtLeastOneDosePerHundred) * Math.random();
            double fullyVaccinatedPerHundred = minFullyVaccinatedPerHundred
                    + (maxFullyVaccinatedPerHundred - minFullyVaccinatedPerHundred) * Math.random();
            centroids.add(new Centroid(atLeastOneDosePerHundred, fullyVaccinatedPerHundred));
        }

        return centroids;
    }

    private static List<Centroid> recalculateCentroids(Map<Centroid, List<Country>> clusters) {
        List<Centroid> centroids = new ArrayList<>(clusters.size());

        clusters.entrySet().forEach(entry -> {
            List<Country> cluster = entry.getValue();
            double atLeastOneDosePerHundred = cluster.stream()
                    .mapToDouble(Country::atLeastOneDosePerHundred).sum() / cluster.size();
            double fullyVaccinatedPerHundredSum = cluster.stream()
                    .mapToDouble(Country::fullyVaccinatedPerHundred).sum() / cluster.size();
            if (cluster.size() == 0) {
                centroids.add(entry.getKey());
            } else {
                centroids.add(new Centroid(atLeastOneDosePerHundred, fullyVaccinatedPerHundredSum));
            }
        });

        return centroids;
    }

    private static Map<Centroid, List<Country>> recalculateClusters(List<Country> countries, List<Centroid> centroids) {
        Map<Centroid, List<Country>> clusters = new HashMap<>(centroids.size());
        countries.forEach(country -> {
            AtomicReference<Centroid> closestCentroid = new AtomicReference<>(centroids.get(0));
            AtomicReference<Double> closestDistance = new AtomicReference<>(getDistanceBetween(country, centroids.get(0)));
            centroids.forEach(centroid -> {
                double distance = getDistanceBetween(country, centroid);
                if (distance < closestDistance.get()) {
                    closestCentroid.set(centroid);
                    closestDistance.set(distance);
                }
            });
            Centroid closestCentroidComputed = closestCentroid.get();
            clusters.computeIfAbsent(closestCentroidComputed, list -> new ArrayList<>());
            clusters.compute(closestCentroidComputed, (centroid, list) -> {
                list.add(country);
                return list;
            });
        });
        centroids.forEach(centroid ->
                clusters.computeIfAbsent(centroid, list -> new ArrayList<>()));
        return clusters;
    }

    private static double getDistanceBetween(Country country, Centroid centroid) {
        return Math.sqrt(Math.pow(country.atLeastOneDosePerHundred() - centroid.atLeastOneDosePerHundred(), 2)
                + Math.pow(country.fullyVaccinatedPerHundred() - centroid.fullyVaccinatedPerHundred(), 2));
    }


}
