package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RoomBooking {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static void main(String[] args) {
        String date = datePicker();
        String buildingId = buildingChooser();
        //roomChooser();

        HttpCookie loginCookie = loginRequest();
        if (loginCookie != null){
            scrapeAvailability(loginCookie, buildingId, date);
        }
    }

    public static String datePicker() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(RED + "Book for today (0) or tomorrow (1)." + RESET);
        int day = scanner.nextInt();
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
        Calendar cal = Calendar.getInstance();

        if (day == 1) {
            cal.add(Calendar.DATE, 1);
        }

        return dateFormat.format(cal.getTime());
    }

    public static String buildingChooser() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose building");
        System.out.println("Niagara (N), Orkanen (O), Library (L)");
        String building;

        do {
            building = scanner.nextLine();
        } while (!Objects.equals(building, "N") && !Objects.equals(building, "O") && !Objects.equals(building, "L"));

        switch (building){
            case "N" -> {
                return "FLIK-0017";
            }
            case "O" -> {
                return "FLIK_0000";
            }
            case "L" -> {
                return "FLIK_0004";
            }
            default -> {
                return null;
            }
       }
    }

    public static String roomChooser() {

        //https://schema.mau.se/ajax/ajax_resursbokning.jsp?op=hamtaBokningar&datum=23-09-24&flik=FLIK_0000
        //Document document =

       /*switch (building){
            case "N" ->
            case "O" ->
            case "L" ->
       }*/
        return null;
    }

    public static void scrapeAvailability(HttpCookie httpCookie, String buildingId, String date) {
        try {
            String URL = "https://schema.mau.se/ajax/ajax_resursbokning.jsp?op=hamtaBokningar&datum=" + date + "&flik=" + buildingId;
            System.out.println(URL);
            Document document = Jsoup.connect(URL)
                    .header("Cookie", httpCookie.toString())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .get();
            int newline = 0;
            int roomRow = 2;

            System.out.println(document);

            for (Element row : document.select("table.grupprum-table td.grupprum-kolumn")) {
                if (row.toString().contains("grupprum-ledig") || row.toString().contains("grupprum-upptagen")) {
                    Element aElement = row.select("a[title]").first();

                    if (aElement != null) {
                        String title = aElement.attr("title");
                        String start = "Klicka för att boka resursen ";
                        System.out.print(GREEN + title.substring(start.length(), title.length() - 1) + " " + RESET);




                    } else {
                        Elements Row = document.select("table tr:nth-child(" + roomRow + ") > td.grupprum-kolumn b");
                        System.out.print(RED + Row.text() + " " + RESET);

                    }
                    newline++;

                    if (newline == 5) {
                        System.out.println();
                        newline = 0;
                        roomRow++;
                    }
                }


            }
        } catch (IOException e) {
            System.out.println("scrapeAvailability Failed");
            throw new RuntimeException(e);

        }

    }

    public static void bookRoom(HttpCookie cookies) {
        try {
            // Create an instance of HttpClient
            HttpClient httpClient = HttpClient.newBuilder().build();

            // Define the URL
            URI uri = URI.create("https://schema.mau.se/ajax/ajax_resursbokning.jsp?op=boka&datum=23-09-24&id=NI%3AA0301&typ=RESURSER_LOKALER&intervall=0&moment=a&flik=FLIK-0017");

            // Create a request.js with the POST data
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", cookies.toString())
                    .GET()
                    .build();

            // Send the request.js and retrieve the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


            // Get the response code
            int statusCode = response.statusCode();
            System.out.println(statusCode);
            System.out.println(response.body());

            // Handle the response body here (response.body())


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static HttpCookie loginRequest() {
        try {
            // Create a CookieManager to store and manage cookies
            CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);


            // Create an instance of HttpClient
            HttpClient httpClient = HttpClient.newBuilder().build();

            // Define the URL
            URI uri = URI.create("https://schema.mau.se/login_do.jsp");

            // Define the POST data as a map of key-value pairs
            Map<String, String> postData = new HashMap<>();

            Dotenv dotenv = Dotenv.configure()
                    .directory("src/main/resources")
                    .filename(".env") // instead of '.env', use 'env'
                    .load();

            postData.put("username", dotenv.get("LOGIN_USER"));
            postData.put("password", dotenv.get("PASSWORD"));


            // Create a request.js with the POST data
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    //.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0")
                    //.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    //.header("Accept-Language", "en-US,en;q=0.5")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    //.header("Upgrade-Insecure-Requests", "1")
                    //.header("Sec-Fetch-Dest", "document")
                    //.header("Sec-Fetch-Mode", "navigate")
                    //.header("Sec-Fetch-Site", "same-origin")
                    //.header("Sec-Fetch-User", "?1")
                    //.header("Sec-GPC", "1")
                    .POST(buildFormDataFromMap(postData))
                    .build();

            // Send the request.js and retrieve the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


            // Get the response code
            int statusCode = response.statusCode();
            System.out.println("Login Response Code: " + statusCode);


            Map<String, List<String>> headers = response.headers().map();

            List<String> cookieHeaders = headers.get("Set-Cookie");

            HttpCookie cookie = null;

            if (cookieHeaders != null) {
                for (String cookieHeader : cookieHeaders) {
                    cookie = HttpCookie.parse(cookieHeader).get(0);

                }
            }

            return cookie;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Helper method to build the request.js body from a map of key-value pairs
    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString(), StandardCharsets.UTF_8);
    }

    // Helper method to build the "Cookie" header from a list of cookies
    private static String buildCookieHeader(List<HttpCookie> cookies) {
        StringBuilder builder = new StringBuilder();
        for (HttpCookie cookie : cookies) {
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(cookie.toString());
        }
        return builder.toString();
    }
}