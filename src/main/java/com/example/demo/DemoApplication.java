package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@SpringBootApplication
public class DemoApplication {

    private static String rulesetPath;
    private static String outputPath;
    private static List<String> filters;
    private static String action;
    // Define supported actions as static strings
    public static final String ACTION_EXTRACT = "extract";
    public static final String ACTION_ANALYZE_SPRING = "analyze-spring";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Component
    public static class CliRunner implements CommandLineRunner {
        @Override
        public void run(String... args) {
            if (args.length == 0) {
                System.err.println("Error: Arguments are required");
                System.err.println(
                        "Usage: java -jar demo.jar rulesetpath=<path> outputpath=<path> filters=<filter1,filter2,...> action=<"
                                + ACTION_EXTRACT + "|" + ACTION_ANALYZE_SPRING + ">");
                System.exit(1);
            }
            for (String arg : args) {
                System.out.println("Processing argument: " + arg);
                if (arg.startsWith("rulesetpath=")) {
                    rulesetPath = arg.substring("rulesetpath=".length());
                } else if (arg.startsWith("outputpath=")) {
                    outputPath = arg.substring("outputpath=".length());
                } else if (arg.startsWith("filters=")) {
                    String filtersStr = arg.substring("filters=".length());
                    if (filtersStr != null && !filtersStr.isEmpty()) {
                        String[] filterArray = filtersStr.split(",");
                        for (int i = 0; i < filterArray.length; i++) {
                            filterArray[i] = filterArray[i].trim();
                        }
                        filters = Arrays.asList(filterArray);
                    }
                } else if (arg.startsWith("action=")) {
                    action = arg.substring("action=".length());
                }
            }
            System.out.println("Parsed rulesetPath: " + rulesetPath);
            System.out.println("Parsed outputPath: " + outputPath);
            System.out.println("Parsed filters: " + filters);
            System.out.println("Parsed action: " + action);
            if (ACTION_EXTRACT.equalsIgnoreCase(action) && (rulesetPath == null || outputPath == null)) {
                System.err.println("Error: rulesetpath, outputpath are required");
                System.err.println(
                        "Usage: java -jar demo.jar rulesetpath=<path> outputpath=<path> filters=<filter1,filter2,...> action=<"
                                + ACTION_EXTRACT + ">");
                System.exit(1);
            }
            if (ACTION_ANALYZE_SPRING.equalsIgnoreCase(action) && outputPath == null) {
                System.err.println("Error: outputpath are required");
                System.err.println("Usage: java -jar demo.jar outputpath=<path> filters=<filter1,filter2,...> action=<"
                        + ACTION_ANALYZE_SPRING + ">");
                System.exit(1);
            }

            if (ACTION_EXTRACT.equalsIgnoreCase(action)) {
                File rulesetFolder = new File(rulesetPath);
                File outputFolder = new File(outputPath);
                if (!rulesetFolder.exists() || !rulesetFolder.isDirectory()) {
                    System.err.println("Error: The specified ruleset path is not a valid directory: " + rulesetPath);
                    System.exit(1);
                }
                if (!outputFolder.exists()) {
                    boolean created = outputFolder.mkdirs();
                    if (!created) {
                        System.err.println("Error: Could not create output directory: " + outputPath);
                        System.exit(1);
                    }
                } else if (!outputFolder.isDirectory()) {
                    System.err.println("Error: The specified output path is not a directory: " + outputPath);
                    System.exit(1);
                }
                System.out.println("Ruleset folder path: " + rulesetPath);
                System.out.println("Output folder path: " + outputPath);
                if (filters != null) {
                    System.out.println("Filters applied: " + filters);
                } else {
                    System.out.println("No filters applied - processing all subdirectories");
                }
                RulesetToExcel.execute(rulesetPath, outputPath, filters);
            } else if (ACTION_ANALYZE_SPRING.equalsIgnoreCase(action)) {
                File outputFolder = new File(outputPath);
                if (!outputFolder.exists()) {
                        System.err.println("Error: the output directory: " + outputPath + " does not exist.");
                        System.exit(1);
                } else if (!outputFolder.isDirectory()) {
                    System.err.println("Error: The specified output path is not a directory: " + outputPath);
                    System.exit(1);
                }

                System.out.println("Output folder path: " + outputPath);

                RulesetToExcel.recognizeSpringRules(outputPath);
            } else {
                System.err.println("Error: Unknown action '" + action + "'. Supported actions: " + ACTION_EXTRACT + ", "
                        + ACTION_ANALYZE_SPRING);
                System.exit(1);
            }
        }
    }

    public static String getRulesetPath() {
        return rulesetPath;
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static List<String> getFilters() {
        return filters;
    }
}