package org.example.domain;

import lombok.Data;
import lombok.NonNull;

@Data
public class Conversion {
    private @NonNull String date;
    private @NonNull String currency;
    private @NonNull String conversionRate;
}
