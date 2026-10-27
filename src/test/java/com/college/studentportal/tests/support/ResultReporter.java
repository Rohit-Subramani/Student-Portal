package com.college.studentportal.tests.support;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes an individual result line per CSV test case and a summary when the suite finishes.
 *
 * <p>Output goes to {@code target/automation-results/individual-results.csv} and
 * {@code target/automation-results/summary.txt}. Registered in {@code testng.xml}.</p>
 */
public class ResultReporter implements ITestListener, ISuiteListener {

    private static final Path OUTPUT_DIR = Paths.get("target", "automation-results");

    private final List<String> rows = new ArrayList<>();
    private int passed;
    private int failed;
    private int skipped;
    private long suiteStartMillis;

    @Override
    public void onStart(ISuite suite) {
        suiteStartMillis = System.currentTimeMillis();
        System.out.println(">>> Student Portal automation suite started: " + suite.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        passed++;
        record(result, "PASSED", "");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failed++;
        Throwable throwable = result.getThrowable();
        record(result, "FAILED", throwable == null ? "no details" : oneLine(throwable.getMessage()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skipped++;
        Throwable throwable = result.getThrowable();
        record(result, "SKIPPED", throwable == null ? "skipped" : oneLine(throwable.getMessage()));
    }

    @Override
    public void onFinish(ISuite suite) {
        long durationMillis = System.currentTimeMillis() - suiteStartMillis;
        try {
            Files.createDirectories(OUTPUT_DIR);
            writeIndividualResults();
            writeSummary(suite, durationMillis);
        } catch (IOException ex) {
            System.out.println("!!! Could not write the automation result files: " + ex.getMessage());
        }
    }

    private void record(ITestResult result, String status, String message) {
        String testCaseId = testCaseId(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        rows.add(String.join(",",
                csv(testCaseId),
                status,
                Long.toString(duration),
                csv(message)));
        System.out.println(status + " | " + testCaseId
                + (message.isEmpty() ? "" : " | " + message));
    }

    private void writeIndividualResults() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("testCase,status,durationMillis,message");
        lines.addAll(rows);
        Files.write(OUTPUT_DIR.resolve("individual-results.csv"), lines, StandardCharsets.UTF_8);
    }

    private void writeSummary(ISuite suite, long durationMillis) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("Student Portal - Selenium WebDriver + TestNG automation summary");
        lines.add("Suite: " + suite.getName());
        lines.add("Total CSV driven test cases: " + (passed + failed + skipped));
        lines.add("Passed: " + passed);
        lines.add("Failed: " + failed);
        lines.add("Skipped: " + skipped);
        lines.add("Duration (ms): " + durationMillis);
        lines.add("Result: " + (failed == 0 ? "SUCCESS" : "FAILURE"));
        Files.write(OUTPUT_DIR.resolve("summary.txt"), lines, StandardCharsets.UTF_8);
        System.out.println(">>> Summary: " + passed + " passed, " + failed + " failed, " + skipped + " skipped");
        System.out.println(">>> Reports written to " + OUTPUT_DIR.toAbsolutePath());
    }

    private static String testCaseId(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length > 0 && parameters[0] instanceof TestRecord record) {
            return record.getTestCaseId() + " " + record.getScenario();
        }
        return result.getName();
    }

    private static String oneLine(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private static String csv(String value) {
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
