package org.example;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.XmlReader;
import io.javalin.Javalin;
import org.example.domain.Conversion;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/conversions";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "toor";

    public static void main(String[] args) throws SQLException, FeedException, IOException {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;
        List < Conversion > currencies = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Enter \"start\" to start the endpoints or \"data\" add data to the database");
        String input = scanner.nextLine();
        switch (input) {
            case "start" -> {
                assert connection != null;
                Javalin app = Javalin.create().start(7000);
                Connection finalConnection1 = connection;
                app.get("/currencies", ctx -> {
                    ctx.json(getLatestDataFromDatabase(finalConnection1));
                });
                Connection finalConnection = connection;
                app.get("/currencies/{currency}", ctx -> {
                    String currency = ctx.pathParam("currency");
                    ctx.json(getSpecificCurrencyData(currency, finalConnection));
                });
            }
            case "data" -> {
                assert connection != null;
                String sql = "CREATE TABLE IF NOT EXISTS currencies (date VARCHAR(255), currency VARCHAR(255), conversionRate VARCHAR(255))";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.executeUpdate();
                currencies = parseRSS("https://www.bank.lv/vk/ecb_rss.xml");
                saveToDatabase(currencies, connection);
                System.out.println("Data added to database");
            }
        }
    }

    public static List < Conversion > parseRSS(String url) throws IOException, FeedException {
        List < Conversion > currencies = new ArrayList < > ();
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(url)));
        SyndEntry entry;
        for (Object o: feed.getEntries()) {
            entry = (SyndEntry) o;
            Date date = entry.getPublishedDate();
            String formattedDate = String.format("%tF", date);
            String description = entry.getDescription().getValue();
            String[] split = description.split(" ");

            for (int i = 0; i < split.length; i++) {
                if (i % 2 == 0) {
                    String currency = split[i];
                    String conversionRate = split[i + 1];
                    currencies.add(new Conversion(formattedDate, currency, conversionRate));
                }
            }
        }
        return currencies;
    }
    public static List < Conversion > getSpecificCurrencyData(String currency, Connection connection) throws SQLException {
        String sql = "SELECT * FROM currencies WHERE currency = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, currency);
        ResultSet resultSet = statement.executeQuery();
        List < Conversion > conversions = new ArrayList < > ();
        while (resultSet.next()) {
            String date = resultSet.getString("date");
            String conversionRate = resultSet.getString("conversionRate");
            conversions.add(new Conversion(date, currency, conversionRate));
        }
        return conversions;
    }

    public static List < Conversion > getLatestDataFromDatabase(Connection connection) throws SQLException {
        List < Conversion > conversions = new ArrayList < > ();
        String sql = "SELECT * FROM currencies WHERE date = (SELECT MAX(date) FROM currencies)";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String date = resultSet.getString("date");
            String currency = resultSet.getString("currency");
            String conversionRate = resultSet.getString("conversionRate");
            conversions.add(new Conversion(date, currency, conversionRate));
        }
        return conversions;
    }
    public static void saveToDatabase(List < Conversion > currencies, Connection connection) throws SQLException {
        for (Conversion conversion: currencies) {
            String sql = "SELECT * FROM currencies WHERE date = ? AND currency = ? AND conversionRate = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, conversion.getDate());
            statement.setString(2, conversion.getCurrency());
            statement.setString(3, conversion.getConversionRate());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                String sql2 = "INSERT INTO currencies (date, currency, conversionRate) VALUES (?, ?, ?)";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, conversion.getDate());
                statement2.setString(2, conversion.getCurrency());
                statement2.setString(3, conversion.getConversionRate());
                statement2.executeUpdate();
            }
        }
    }
}