package com.college.studentportal.tests.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV reader built only on the JDK file APIs ({@link Files}, {@link BufferedReader}).
 *
 * <p>Supported syntax: comma separated values, quoted values and doubled quotes inside a quoted
 * value (RFC 4180 style). Lines that are empty or start with {@code #} are ignored.</p>
 */
public final class CsvDataReader {

    /** Column order of the test data file. */
    public static final List<String> EXPECTED_HEADER = List.of(
            "testCaseId", "scenario", "description", "name", "email", "password", "confirmPassword",
            "department", "gender", "interests", "dob", "expectSuccess", "expectedMessage");

    private CsvDataReader() {
    }

    public static List<TestRecord> readTestRecords(Path csvFile) throws IOException {
        List<TestRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            List<String> header = null;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                List<String> values = parseLine(line);
                if (header == null) {
                    header = values;
                    if (!EXPECTED_HEADER.equals(header)) {
                        throw new IOException("Unexpected CSV header on line " + lineNumber + ": " + header
                                + System.lineSeparator() + "Expected: " + EXPECTED_HEADER);
                    }
                    continue;
                }
                if (values.size() != EXPECTED_HEADER.size()) {
                    throw new IOException("Line " + lineNumber + " has " + values.size()
                            + " columns but " + EXPECTED_HEADER.size() + " were expected: " + line);
                }
                records.add(toRecord(values, lineNumber));
            }
        }
        return records;
    }

    private static TestRecord toRecord(List<String> values, int lineNumber) throws IOException {
        String scenarioName = values.get(1).trim().toUpperCase();
        Scenario scenario;
        try {
            scenario = Scenario.valueOf(scenarioName);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Line " + lineNumber + " uses the unknown scenario '" + scenarioName + "'");
        }
        String expectSuccess = values.get(11).trim();
        if (!"true".equalsIgnoreCase(expectSuccess) && !"false".equalsIgnoreCase(expectSuccess)) {
            throw new IOException("Line " + lineNumber + " must use true/false for expectSuccess but has '"
                    + expectSuccess + "'");
        }
        return new TestRecord(
                values.get(0).trim(),
                scenario,
                values.get(2).trim(),
                values.get(3).trim(),
                values.get(4).trim(),
                values.get(5),
                values.get(6),
                values.get(7).trim(),
                values.get(8).trim(),
                values.get(9).trim(),
                values.get(10).trim(),
                Boolean.parseBoolean(expectSuccess),
                values.get(12).trim());
    }

    /** Splits one CSV line, honouring double quotes. */
    static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (insideQuotes) {
                if (character == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        insideQuotes = false;
                    }
                } else {
                    current.append(character);
                }
            } else if (character == '"') {
                insideQuotes = true;
            } else if (character == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString());
        return values;
    }
}
