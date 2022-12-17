package org.example;

import io.javalin.Javalin;
import org.example.Parser.RSSParser;
import org.example.domain.Conversion;
import org.example.repositories.ConversionRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        Properties properties = new Properties();
        Logger logger = LoggerFactory.getLogger(Main.class);

        try(FileInputStream ip = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Connection connection = DriverManager.getConnection(properties.getProperty("DB_URL"), properties.getProperty("DB_USERNAME"), properties.getProperty("DB_PASSWORD"));
            List <Conversion> currencies;
            logger.info("Connected to database");
            ConversionRepository conversionRepository = new ConversionRepository(connection);
            logger.info("Enter \"start\" to start the endpoints or \"data\" to add data to the database");
            String input = scanner.nextLine();
            switch (input) {
                case "start" -> {
                    Javalin app = Javalin.create().start(Integer.parseInt(properties.getProperty("PORT")));
                    app.get("/currencies", ctx -> {
                        ctx.json(conversionRepository.getLatestDataFromDatabase());
                    });
                    app.get("/currencies/{currency}", ctx -> {
                        String currency = ctx.pathParam("currency");
                        ctx.json(conversionRepository.getSpecificCurrencyData(currency));
                    });
                }
                case "data" -> {
                    conversionRepository.createTable();
                    RSSParser rssParser = new RSSParser("https://www.bank.lv/vk/ecb_rss.xml");
                    currencies = rssParser.parseRSS();
                    conversionRepository.saveToDatabase(currencies);
                    logger.info("Data added to database");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}