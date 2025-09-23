package com.example.weather;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

public class WeatherApp {

    public static void main(String[] args) {
        String apiKey = System.getenv("OWM_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Set OPENWEATHER_API_KEY environment variable and rerun.");
            return;
        }

        List<String> cities = List.of("St. Louis", "Chicago", "Kansas City");

        Scanner in = new Scanner(System.in);
        RestTemplate rest = new RestTemplate();
        String units = "imperial";

        while (true) {
            System.out.println("\n=== Weather Dashboard ===");
            for (int i = 0; i < cities.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, cities.get(i));
            }
            System.out.println("C) Check another city");
            System.out.println("Q) Quit");
            System.out.print("Choose: ");
            String choice = in.nextLine().trim();

            if (choice.equalsIgnoreCase("q")) break;

            String city;
            if (choice.equalsIgnoreCase("c")) {
                System.out.print("Enter city name (e.g., \"Paris,FR\"): ");
                city = in.nextLine().trim();
            } else {
                int idx;
                try {
                    idx = Integer.parseInt(choice) - 1;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid choice.");
                    continue;
                }
                if (idx < 0 || idx >= cities.size()) {
                    System.out.println("Invalid choice.");
                    continue;
                }
                city = cities.get(idx);
            }

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.openweathermap.org/data/2.5/weather")
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", units)
                    .toUriString();

            Map<String, Object> json = rest.getForObject(url, Map.class);

            String name = (String) json.getOrDefault("name", city);

            Map<String, Object> main = (Map<String, Object>) json.get("main");
            Double temp = main != null ? toDouble(main.get("temp")) : null;
            Integer humidity = main != null ? toInt(main.get("humidity")) : null;

            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) json.get("weather");
            String description = (weatherList != null && !weatherList.isEmpty())
                    ? (String) weatherList.get(0).get("description")
                    : null;

            Map<String, Object> wind = (Map<String, Object>) json.get("wind");
            Double windSpeed = wind != null ? toDouble(wind.get("speed")) : null;

            String unitSymbol = units.equals("imperial") ? "°F" : units.equals("metric") ? "°C" : "K";
            String windUnit = units.equals("imperial") ? "mph" : "m/s";

            System.out.println("\n--- Current Weather ---");
            System.out.println("City:        " + name);
            System.out.println("Temperature: " + fmt(temp) + " " + unitSymbol);
            System.out.println("Humidity:    " + (humidity != null ? humidity + "%" : "N/A"));
            System.out.println("Conditions:  " + (description != null ? cap(description) : "N/A"));
            System.out.println("Wind:        " + (windSpeed != null ? fmt(windSpeed) + " " + windUnit : "N/A"));
            System.out.println("-----------------------");
        }

        System.out.println("Goodbye!");
    }

    private static Double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return o != null ? Double.parseDouble(o.toString()) : null; } catch (Exception e) { return null; }
    }
    private static Integer toInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        try { return o != null ? Integer.parseInt(o.toString()) : null; } catch (Exception e) { return null; }
    }

    private static String fmt(Double d) {
        return d == null ? "N/A" : String.format(Locale.US, "%.1f", d);
    }

    private static String cap(String s) {
        return (s == null || s.isBlank()) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}