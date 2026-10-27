package com.college.studentportal.tests.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Loads the CSV once per JVM and exposes the rows in file order.
 *
 * <p>The suite relies on this order: {@code TC01} must run before {@code TC02} (duplicate email)
 * and before the profile edit, so the data can never be shuffled or run in parallel.</p>
 */
public final class TestDataSet {

    /** Number of test cases the suite is required to execute. */
    public static final int EXPECTED_TEST_CASE_COUNT = 20;

    private static final String DEFAULT_CSV_PATH = "src/test/resources/testdata/student_portal_testdata.csv";

    private static List<TestRecord> cache;

    private TestDataSet() {
    }

    public static synchronized List<TestRecord> all() {
        if (cache == null) {
            cache = load();
        }
        return cache;
    }

    public static List<TestRecord> withScenario(Scenario... scenarios) {
        List<Scenario> wanted = Arrays.asList(scenarios);
        List<TestRecord> selected = new ArrayList<>();
        for (TestRecord record : all()) {
            if (wanted.contains(record.getScenario())) {
                selected.add(record);
            }
        }
        return selected;
    }

    public static TestRecord onlyOne(Scenario scenario) {
        List<TestRecord> selected = withScenario(scenario);
        if (selected.size() != 1) {
            throw new IllegalStateException("Expected exactly one CSV row for " + scenario + " but found "
                    + selected.size());
        }
        return selected.get(0);
    }

    /** The first row of a scenario, used for cross row checks such as the profile edit test. */
    public static TestRecord first(Scenario scenario) {
        List<TestRecord> selected = withScenario(scenario);
        if (selected.isEmpty()) {
            throw new IllegalStateException("No CSV row found for " + scenario);
        }
        return selected.get(0);
    }

    private static List<TestRecord> load() {
        String configured = System.getProperty("testdata.csv", DEFAULT_CSV_PATH);
        Path csvFile = Paths.get(configured);
        if (!Files.exists(csvFile)) {
            throw new IllegalStateException("CSV test data not found at " + csvFile.toAbsolutePath()
                    + " (start Maven from the project root or pass -Dtestdata.csv=<path>)");
        }
        List<TestRecord> records;
        try {
            records = CsvDataReader.readTestRecords(csvFile);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (records.size() != EXPECTED_TEST_CASE_COUNT) {
            throw new IllegalStateException("The suite must execute exactly " + EXPECTED_TEST_CASE_COUNT
                    + " CSV driven test cases but the file contains " + records.size());
        }
        for (int index = 0; index < records.size(); index++) {
            String expectedId = String.format(Locale.ROOT, "TC%02d", index + 1);
            String actualId = records.get(index).getTestCaseId();
            if (!expectedId.equals(actualId)) {
                throw new IllegalStateException("CSV rows must be ordered " + expectedId + " but row "
                        + (index + 1) + " is " + actualId);
            }
        }
        return records;
    }
}
