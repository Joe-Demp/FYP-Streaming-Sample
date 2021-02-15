package ie.ucd.dempsey.streamingsample.controller;

import ie.ucd.dempsey.streamingsample.model.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
@Controller
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

    private static List<Pair<Instant, Double>> parseStreamData(String streamData) {
        Scanner scanner = new Scanner(streamData);
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
        return dataList;
    }

    private static void writeStreamDataToFile(List<Pair<Instant, Double>> streamDataList, Path filePath)
            throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<Instant, Double> dataEntry : streamDataList) {
            stringBuilder.append(dataEntry.first.toString())
                    .append(' ')
                    .append(dataEntry.second)
                    .append('\n')
            ;
            Files.writeString(filePath, stringBuilder, StandardOpenOption.APPEND);
            stringBuilder.setLength(0); // clears the builder
        }
    }

    // todo remove duplication between this and parseStreamData
    private static List<Pair<Instant, Double>> readDataFromFile(Path filePath) throws IOException {
        InputStream inputStream = Files.newInputStream(filePath, StandardOpenOption.READ);
        Scanner scanner = new Scanner(inputStream);
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
        inputStream.close();
        return dataList;
    }

    @PostMapping("/submit")
    @ResponseStatus(value = HttpStatus.OK)
    public void acceptStreamAsString(@RequestBody String streamData) throws IOException {
        List<Pair<Instant, Double>> dataPairs = parseStreamData(streamData);
        writeStreamDataToFile(dataPairs, dataFilePath);
    }
}
