package org.example.Parser;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import lombok.SneakyThrows;
import org.example.domain.Conversion;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSParser {
    private final String url;

    public RSSParser(String url) {
        this.url = url;
    }

    @SneakyThrows({IOException.class, FeedException.class})

    @SuppressWarnings("unchecked")
    // Suppressing the warning because the RSS feed is not a generic type
    public List<Conversion> parseRSS() {

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(this.url)));
        List<Conversion> currencies = new ArrayList<>();
        List<SyndEntry> entries = feed.getEntries();
        entries.forEach(entry -> {
            String description = entry.getDescription().getValue();
            String[] split = description.split(" ");
            String date = String.format("%tF", entry.getPublishedDate());
            for (int i = 0; i < split.length; i++) {
                if (i % 2 == 0) {
                    String currency = split[i];
                    String conversionRate = split[i + 1];
                    Conversion conversion = new Conversion(date, currency, conversionRate);
                    currencies.add(conversion);
                }
            }
        });

        return currencies;
    }
}
