package space;

public record LaunchDay(double temperature, double wind, double humidity, double precipitation,
                        String lightning, String clouds) {
    private static final int TEMP_INDEX = 0;
    private static final int WIND_INDEX = 1;
    private static final int HUMIDITY_INDEX = 2;
    private static final int PRECIPITATION_INDEX = 3;
    private static final int LIGHTNING_INDEX = 4;
    private static final int CLOUDS_INDEX = 5;

    private static final String DELIMITER = ",";

    public static LaunchDay of(String line) {
        String[] tokens = line.split(DELIMITER);
        double temp = Double.parseDouble(tokens[TEMP_INDEX]);
        double wind = Double.parseDouble(tokens[WIND_INDEX]);
        double humidity = Double.parseDouble(tokens[HUMIDITY_INDEX]);
        double precipitation = Double.parseDouble(tokens[PRECIPITATION_INDEX]);
        String lightning = tokens[LIGHTNING_INDEX];
        String clouds = tokens[CLOUDS_INDEX];

        return new LaunchDay(temp, wind, humidity, precipitation, lightning, clouds);

    }
}
