package org.example.Parser;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.example.domain.Conversion;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RSSParser {
    private final String url;

    public RSSParser(String url) {
        this.url = url;
    }

    public List<Conversion> parseRSS() {
        try {
            List<Conversion> currencies = new ArrayList<>();
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(this.url)));
            for (Object entry : feed.getEntries()) {
                SyndEntry item = (SyndEntry) entry;
                Date date = item.getPublishedDate();
                String formattedDate = String.format("%tF", date);
                String description = item.getDescription().getValue();
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
        } catch (IOException | FeedException e) {
            throw new RuntimeException(e);
        }
    }
}
