package org.example.repositories;

import lombok.SneakyThrows;
import org.example.domain.Conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConversionRepository {
    private final Connection connection;

    public ConversionRepository(Connection connection) {
        this.connection = connection;
    }

    @SneakyThrows(SQLException.class)
    public List<Conversion> getLatestDataFromDatabase() {

        List<Conversion> conversions = new ArrayList<>();
        String sql = "SELECT * FROM currencies WHERE date = (SELECT MAX(date) FROM currencies)";
        PreparedStatement statement = this.connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String date = resultSet.getString("date");
            String currency = resultSet.getString("currency");
            String conversionRate = resultSet.getString("conversionRate");
            conversions.add(new Conversion(date, currency, conversionRate));
        }
        return conversions;
    }

    @SneakyThrows(SQLException.class)
    public void saveToDatabase(List<Conversion> currencies) {

        for (Conversion conversion : currencies) {
            String sql = "SELECT * FROM currencies WHERE date = ? AND currency = ? AND conversionRate = ?";
            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setString(1, conversion.getDate());
            statement.setString(2, conversion.getCurrency());
            statement.setString(3, conversion.getConversionRate());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                String sql2 = "INSERT INTO currencies (date, currency, conversionRate) VALUES (?, ?, ?)";
                PreparedStatement statement2 = this.connection.prepareStatement(sql2);
                statement2.setString(1, conversion.getDate());
                statement2.setString(2, conversion.getCurrency());
                statement2.setString(3, conversion.getConversionRate());
                statement2.executeUpdate();
            }
        }
    }


    @SneakyThrows(SQLException.class)
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS currencies (date VARCHAR(255), currency VARCHAR(255), conversionRate VARCHAR(255))";
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.executeUpdate();
    }

    @SneakyThrows(SQLException.class)
    public List<Conversion> getSpecificCurrencyData(String currency) {
        List<Conversion> conversions = new ArrayList<>();
        String sql = "SELECT * FROM currencies WHERE currency = ?";
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.setString(1, currency);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String date = resultSet.getString("date");
            String conversionRate = resultSet.getString("conversionRate");
            conversions.add(new Conversion(date, currency, conversionRate));
        }
        return conversions;
    }
}
