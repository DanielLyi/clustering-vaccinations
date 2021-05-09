package com.danylo;

import com.danylo.logic.Country;
import com.danylo.visual.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.install();
        Locale.setDefault(Locale.ENGLISH);
        EventQueue.invokeLater(() -> {
            List<Country> countries = null;
            try {
                countries = readCountriesData();
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
            MainFrame mainFrame = new MainFrame(countries);
            mainFrame.setVisible(true);
        });

    }

    private static List<Country> readCountriesData() throws URISyntaxException, IOException {
        InputStream in = Main.class.getResourceAsStream("/vaccinations.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder jsonText = new StringBuilder();
        reader.lines().forEach(jsonText::append);
        JSONArray countriesArray = new JSONArray(jsonText.toString());
        List<Country> countries = new ArrayList<>();
        for (int i = 0; i < countriesArray.length(); i++) {
            JSONObject countryObj = countriesArray.getJSONObject(i);
            String name = countryObj.getString("country");
            JSONArray data = countryObj.getJSONArray("data");
            LocalDate mostRecentDate = LocalDate.parse(data.getJSONObject(0).getString("date"));

            Double latestAtLeastOneDosePerHundred = null;
            try {
                latestAtLeastOneDosePerHundred = data.getJSONObject(0).getDouble("people_vaccinated_per_hundred");
            } catch (JSONException ignored) {
            }

            Double latestFullyVaccinatedPerHundred = null;
            try {
                latestFullyVaccinatedPerHundred = data.getJSONObject(0).getDouble("people_fully_vaccinated_per_hundred");
            } catch (JSONException ignored) {
            }

            for (int j = 1; j < data.length(); j++) {
                JSONObject dailyStats = data.getJSONObject(j);
                LocalDate date = LocalDate.parse(dailyStats.getString("date"));
                if (date.isAfter(mostRecentDate)) {

                    Double atLeastOneDosePerHundred = null;
                    try {
                        atLeastOneDosePerHundred = dailyStats.getDouble("people_vaccinated_per_hundred");
                    } catch (JSONException ignored) {
                    }

                    Double fullyVaccinatedPerHundred = null;
                    try {
                        fullyVaccinatedPerHundred = dailyStats.getDouble("people_fully_vaccinated_per_hundred");
                    } catch (JSONException ignored) {
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
