package org.example;

import io.javalin.Javalin;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.example.Parser.RSSParser;
import org.example.domain.Conversion;
import org.example.repositories.ConversionRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

@Log
public class Main {
    @SneakyThrows({SQLException.class, IOException.class})
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Properties properties = new Properties();

        FileInputStream ip = new FileInputStream("src/main/resources/application.properties");
        properties.load(ip);

        Connection connection = DriverManager.getConnection(properties.getProperty("DB_URL"), properties.getProperty("DB_USERNAME"), properties.getProperty("DB_PASSWORD"));
        List<Conversion> currencies;
        log.info("Connected to database");
        ConversionRepository conversionRepository = new ConversionRepository(connection);
        log.info("Enter \"start\" to start the endpoints or \"data\" to add data to the database");
        String input = scanner.nextLine();

        switch (input) {
            case "start" -> {
                Javalin app = Javalin.create().start(Integer.parseInt(properties.getProperty("PORT")));
                app.get("/", ctx -> {
                    ctx.redirect("/currencies");
                });
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
                log.info("Data added to database");
            }
        }
    }
}