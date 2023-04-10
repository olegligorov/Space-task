package space;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpaceShuttleLauncher {
    private final String senderEmail;
    private final String senderPassword;
    private String filepath;
    private final String recipientEmail;

    private List<LaunchDay> launchDays;

    public SpaceShuttleLauncher(String filepath, String senderEmail, String senderPassword, String recipientEmail) {
        this.filepath = filepath;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        this.recipientEmail = recipientEmail;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    private void readFromFile2() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            launchDays = reader.lines().skip(1).map(LaunchDay::of).toList();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from this file", e);
        }
    }

    private void readFromFile() {
        this.launchDays = new ArrayList<>();
        String DELIMITER = ",";

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line = reader.readLine();
            try {
                String[] temp = reader.readLine().split(DELIMITER);
                String[] wind = reader.readLine().split(DELIMITER);
                String[] humidity = reader.readLine().split(DELIMITER);
                String[] precipitation = reader.readLine().split(DELIMITER);
                String[] lightning = reader.readLine().split(DELIMITER);
                String[] clouds = reader.readLine().split(DELIMITER);

                int size = temp.length;
                for (int i = 1; i < size; ++i) {
                    launchDays.add(new LaunchDay(
                            Double.parseDouble(temp[i]), Double.parseDouble(wind[i]), Double.parseDouble(humidity[i]),
                            Double.parseDouble(precipitation[i]), lightning[i], clouds[i]
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Invalid file content");

            }

        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from this file", e);
        }
    }

    public void launch() {
        readFromFile();
//        readFromFile()2;

        var possibleDays = launchDays.stream()
                .filter(launchDay -> launchDay.temperature() >= 2
                        && launchDay.temperature() <= 31
                        && launchDay.wind() <= 10
                        && launchDay.humidity() < 60
                        && launchDay.precipitation() == 0
                        && Objects.equals(launchDay.lightning(), "No")
                        && !Objects.equals(launchDay.clouds(), "Cumulus")
                        && !Objects.equals(launchDay.clouds(), "Nimbus"))
                // if there are multiple possible days sort them by wind, if the wind is equal then sort by humidity
                .sorted((LaunchDay a, LaunchDay b) -> {
                    if (a.wind() == b.wind()) {
                        return (int) (a.humidity() - b.humidity());
                    }
                    return (int) (a.wind() - b.wind());
                }).
                toList();

        //result are the best days to launch
        List<LaunchDay> result = new ArrayList<>();
        LaunchDay first = possibleDays.get(0);
        result.add(first);

        for (int i = 1; i < possibleDays.size(); ++i) {
            if (possibleDays.get(i).wind() != first.wind()) {
                break;
            }
            if (possibleDays.get(i).humidity() == first.humidity()) {
                result.add(possibleDays.get(i));
            }
        }

        for (var day : result) {
            System.out.println(day.toString());
        }

        calculateData(result);
        EmailSender emailSender = new EmailSender();
        emailSender.sendMail(senderEmail, senderPassword, recipientEmail);
    }

    public void calculateData(List<LaunchDay> possibleDays) {
        double averageTemp = 0;
        double averageWind = 0;
        double averageHumidity = 0;
        double averagePrecipitation = 0;

        double maxTemp = Double.MIN_VALUE;
        double minTemp = Double.MAX_VALUE;
        double maxWind = Double.MIN_VALUE;
        double minWind = Double.MAX_VALUE;
        double maxHumidity = Double.MIN_VALUE;
        double minHumidity = Double.MAX_VALUE;
        double maxPrecipitation = Double.MIN_VALUE;
        double minPrecipitation = Double.MAX_VALUE;

        for (var launchDay : launchDays) {
            averageTemp += launchDay.temperature();
            averageWind += launchDay.wind();
            averageHumidity += launchDay.humidity();
            averagePrecipitation += launchDay.precipitation();

            maxTemp = Math.max(maxTemp, launchDay.temperature());
            minTemp = Math.min(minTemp, launchDay.temperature());

            maxWind = Math.max(maxWind, launchDay.wind());
            minWind = Math.min(minWind, launchDay.wind());

            maxHumidity = Math.max(maxHumidity, launchDay.humidity());
            minHumidity = Math.min(minHumidity, launchDay.humidity());

            maxPrecipitation = Math.max(maxPrecipitation, launchDay.precipitation());
            minPrecipitation = Math.min(minPrecipitation, launchDay.precipitation());
        }

        averageTemp /= launchDays.size();
        averageWind /= launchDays.size();
        averageHumidity /= launchDays.size();
        averagePrecipitation /= launchDays.size();

        double medianTemp = launchDays.stream().map(LaunchDay::temperature)
                .sorted().collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        temperatures -> {
                            int count = temperatures.size();
                            if (count % 2 == 0) {
                                return (temperatures.get(count / 2 - 1) + temperatures.get(count / 2)) / 2;
                            } else {
                                return temperatures.get(count / 2);
                            }
                        }));

        double medianWind = launchDays.stream().map(LaunchDay::wind)
                .sorted().collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        winds -> {
                            int count = winds.size();
                            if (count % 2 == 0) {
                                return (winds.get(count / 2 - 1) + winds.get(count / 2)) / 2;
                            } else {
                                return winds.get(count / 2);
                            }
                        }));

        double medianHumidity = launchDays.stream().map(LaunchDay::humidity)
                .sorted().collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        humidityList -> {
                            int count = humidityList.size();
                            if (count % 2 == 0) {
                                return (humidityList.get(count / 2 - 1) + humidityList.get(count / 2)) / 2;
                            } else {
                                return humidityList.get(count / 2);
                            }
                        }));

        double medianPrecipitation = launchDays.stream().map(LaunchDay::precipitation)
                .sorted().collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        precipitationList -> {
                            int count = precipitationList.size();
                            if (count % 2 == 0) {
                                return (precipitationList.get(count / 2 - 1) + precipitationList.get(count / 2)) / 2;
                            } else {
                                return precipitationList.get(count / 2);
                            }
                        }));
        generateCSV(
                minTemp, maxTemp, averageTemp, medianTemp,
                minWind, maxWind, averageWind, medianWind,
                minHumidity, maxHumidity, averageHumidity, medianHumidity,
                minPrecipitation, maxPrecipitation, averagePrecipitation, medianPrecipitation, possibleDays);
    }

    private void generateCSV(double minTemp, double maxTemp, double averageTemp, double medianTemp,
                             double minWind, double maxWind, double averageWind, double medianWind,
                             double minHumidity, double maxHumidity, double averageHumidity, double medianHumidity,
                             double minPrecipitation, double maxPrecipitation, double averagePrecipitation,
                             double medianPrecipitation, List<LaunchDay> bestDaysToLaunch) {
        List<String> dataLines = new ArrayList<>();

        StringBuilder resultTemp = new StringBuilder();
        StringBuilder resultWind = new StringBuilder();
        StringBuilder resultHumidity = new StringBuilder();
        StringBuilder resultPrecipitation = new StringBuilder();
        StringBuilder resultLightning = new StringBuilder();
        StringBuilder resultClouds = new StringBuilder();

        for (int i = 0; i < bestDaysToLaunch.size() - 1; ++i) {
            var day = bestDaysToLaunch.get(i);
            resultTemp.append(day.temperature()).append(",");
            resultWind.append(day.wind()).append(",");
            resultHumidity.append(day.humidity()).append(",");
            resultPrecipitation.append(day.precipitation()).append(",");
            resultLightning.append(day.lightning()).append(",");
            resultClouds.append(day.clouds()).append(",");
        }
        if (bestDaysToLaunch.size() > 0) {
            LaunchDay day = bestDaysToLaunch.get(bestDaysToLaunch.size() - 1);
            resultTemp.append(day.temperature());
            resultWind.append(day.wind());
            resultHumidity.append(day.humidity());
            resultPrecipitation.append(day.precipitation());
            resultLightning.append(day.lightning());
            resultClouds.append(day.clouds());
        }


        dataLines.add("Values/Parameter,max,min,average,median,most appropriate day(s)");
        dataLines.add(String.format("Temperature (C),%.1f,%.1f,%.1f,%.1f,%s",
                maxTemp, minTemp, averageTemp, medianTemp, resultTemp));
        dataLines.add(String.format("Wind (m/s),%.1f,%.1f,%.1f,%.1f,%s",
                maxWind, minWind, averageWind, medianWind, resultWind));
        dataLines.add(String.format("Humidity (%%),%.1f,%.1f,%.1f,%.1f,%s",
                maxHumidity, minHumidity, averageHumidity, medianHumidity, resultHumidity));
        dataLines.add(String.format("Precipitation (%%),%.1f,%.1f,%.1f,%.1f,%s",
                maxPrecipitation, minPrecipitation, averagePrecipitation, medianPrecipitation, resultPrecipitation));
        dataLines.add(String.format("Lightning,,,,,%s", resultLightning));
        dataLines.add(String.format("Clouds,,,,,%s", resultClouds));

        try (PrintWriter pw = new PrintWriter("WeatherReport.csv")) {
            dataLines.forEach(pw::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
