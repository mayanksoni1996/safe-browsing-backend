package tech.mayanksoni.safebrowsing.models;

public record PhoneticModel(
        String soundexCode,
        String metaphoneCode,
        String doubleMetaphoneCode
) {
}
