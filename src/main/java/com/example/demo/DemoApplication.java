package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@SpringBootApplication
@Configuration
public class DemoApplication {

    private static String rulesetPath;
    private static String outputPath;
    private static List<String> filters;

    public static void main(String[] args) {
        // Process arguments before starting Spring Boot
        if (args.length == 0) {
            System.err.println("Error: Arguments are required");
            System.err.println("Usage: java -jar demo.jar rulesetpath=<path> outputpath=<path> filters=<filter1,filter2,...>");
            System.exit(1);
        }

        // Process each argument
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
            }
        }

        System.out.println("Parsed rulesetPath: " + rulesetPath);
        System.out.println("Parsed outputPath: " + outputPath);
        System.out.println("Parsed filters: " + filters);

        if (rulesetPath == null || outputPath == null) {
            System.err.println("Error: Both rulesetpath and outputpath are required");
            System.err.println("Usage: java -jar demo.jar rulesetpath=<path> outputpath=<path> filters=<filter1,filter2,...>");
            System.exit(1);
        }

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
        
        // Execute the conversion
        RulesetToExcel.execute(rulesetPath, outputPath, filters);
        
        // Start Spring Boot application
        SpringApplication app = new SpringApplication(DemoApplication.class);
        app.run(args);
    }

    @Bean
    public String processArguments(ApplicationArguments args) {
        return "Arguments processed successfully";
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