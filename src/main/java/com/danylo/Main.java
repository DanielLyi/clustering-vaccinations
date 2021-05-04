package com.danylo;

import com.danylo.logic.Centroid;
import com.danylo.logic.Clustering;
import com.danylo.logic.Country;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException {
        List<Country> countries = readCountriesData();
        Map<Centroid, List<Country>> clusters = Clustering.getClusters(countries, 10);
        System.out.println(clusters);
        for (Map.Entry<Centroid, List<Country>> cluster : clusters.entrySet()) {
            if(cluster.getValue().isEmpty()) {
                System.out.println("Empty cluster..");
                System.out.println();
                continue;
            }
            System.out.println("Cluster: " + cluster.getKey());
            for (Country country : cluster.getValue()) {
                System.out.printf("%s, ", country.name());
            }
            System.out.println();
            System.out.println();
        }
        JSONArray array = new JSONArray();
        for (Map.Entry<Centroid, List<Country>> cluster : clusters.entrySet()) {
            JSONArray countriesArr = new JSONArray();
            for (Country country : cluster.getValue()) {
                countriesArr.put(country.name());
            }
            JSONObject clusterObj = new JSONObject();
            clusterObj.put("centroid", cluster.getKey().toString());
            clusterObj.put("countries", countriesArr);
            array.put(clusterObj);
        }
        System.out.println(array);
    }

    private static List<Country> readCountriesData() throws URISyntaxException, IOException {
        JSONArray countriesArray = new JSONArray(Files.readString(Path.of(Objects.requireNonNull(
                Main.class.getResource("/vaccinations.json")).toURI())));
        List<Country> countries = new ArrayList<>();
        for (int i = 0; i < countriesArray.length(); i++) {
            JSONObject countryObj = countriesArray.getJSONObject(i);
            String name = countryObj.getString("country");
            JSONArray data = countryObj.getJSONArray("data");
            LocalDate mostRecentDate = LocalDate.parse(data.getJSONObject(0).getString("date"));

            Double latestAtLeastOneDosePerHundred = null;
            try {
                latestAtLeastOneDosePerHundred = data.getJSONObject(0).getDouble("people_vaccinated_per_hundred");
            } catch (JSONException e) {
            }

            Double latestFullyVaccinatedPerHundred = null;
            try {
                latestFullyVaccinatedPerHundred = data.getJSONObject(0).getDouble("people_fully_vaccinated_per_hundred");
            } catch (JSONException e) {
            }

            for (int j = 1; j < data.length(); j++) {
                JSONObject dailyStats = data.getJSONObject(j);
                LocalDate date = LocalDate.parse(dailyStats.getString("date"));
                if (date.isAfter(mostRecentDate)) {

                    Double atLeastOneDosePerHundred = null;
                    try {
                        atLeastOneDosePerHundred = dailyStats.getDouble("people_vaccinated_per_hundred");
                    } catch (JSONException e) {
                    }

                    Double fullyVaccinatedPerHundred = null;
                    try {
                        fullyVaccinatedPerHundred = dailyStats.getDouble("people_fully_vaccinated_per_hundred");
                    } catch (JSONException e) {
                    }


                    if (atLeastOneDosePerHundred != null && fullyVaccinatedPerHundred != null) {
                        mostRecentDate = date;
                        latestAtLeastOneDosePerHundred = atLeastOneDosePerHundred;
                        latestFullyVaccinatedPerHundred = fullyVaccinatedPerHundred;
                    }
                }
            }

            if (latestAtLeastOneDosePerHundred != null && latestFullyVaccinatedPerHundred != null) {
                countries.add(new Country(name, latestAtLeastOneDosePerHundred, latestFullyVaccinatedPerHundred));
            }

        }

        return countries;
    }
}
