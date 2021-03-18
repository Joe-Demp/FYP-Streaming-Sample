package ie.ucd.dempsey.streamingsample.controller;

import ie.ucd.dempsey.streamingsample.model.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Resources used by this controller are not thread safe.
 */
@RestController
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final Path dataFilePath = Paths.get("data", "streamData.dat");

    public MainController() throws IOException {
        createDataFile();
    }

    private static void createDataFile() throws IOException {
        Path directoryPath = Paths.get("data");
        if (!Files.exists(directoryPath)) {
            Files.createDirectory(directoryPath);
        } else if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("Cannot create data file directory. Quitting");
        }

        // todo remove this when preserving state
        boolean fileExisted = Files.deleteIfExists(dataFilePath);
        logger.info("Data file existed? {}", fileExisted);
        Files.createFile(dataFilePath);
    }

    private static List<Pair<Instant, Double>> parseStreamData(String streamData) throws IOException {
        InputStream stream = new ByteArrayInputStream(streamData.getBytes());
        return parseStreamToList(stream);
    }

    private static void writeStreamDataToFile(List<Pair<Instant, Double>> streamDataList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<Instant, Double> dataEntry : streamDataList) {
            stringBuilder.append(dataEntry.first.toString())
                    .append(' ')
                    .append(dataEntry.second)
                    .append('\n')
            ;
            Files.writeString(dataFilePath, stringBuilder, StandardOpenOption.APPEND);
            stringBuilder.setLength(0); // clears the builder
        }
    }

    private static List<Pair<Instant, Double>> readDataFromFile() throws IOException {
        InputStream inputStream = Files.newInputStream(dataFilePath, StandardOpenOption.READ);
        return parseStreamToList(inputStream);
    }

    private static List<Pair<Instant, Double>> parseStreamToList(InputStream input) throws IOException {
        Scanner scanner = new Scanner(input);
        List<Pair<Instant, Double>> dataList = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] dataParts = line.split(" ");

            Pair<Instant, Double> pair = new Pair<>();
            pair.first = Instant.parse(dataParts[0].trim());
            pair.second = Double.parseDouble(dataParts[1].trim());

            dataList.add(pair);
        }

        scanner.close();
        input.close();
        return dataList;
    }

    private static String listToString(List<Pair<Instant, Double>> listOfPairs) {
        return listOfPairs.stream()
                .map(pair -> new StringBuilder().append(pair.first).append(' ').append(pair.second).append('\n'))
                .reduce(new StringBuilder(), StringBuilder::append)
                .toString();
    }

    @PostMapping("/submit")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void acceptStreamAsString(@RequestBody String streamData) throws IOException {
        List<Pair<Instant, Double>> dataPairs = parseStreamData(streamData);
        writeStreamDataToFile(dataPairs);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public String getSubmittedContent() throws IOException {
        List<Pair<Instant, Double>> dataList = readDataFromFile();
        return listToString(dataList);
    }
}
