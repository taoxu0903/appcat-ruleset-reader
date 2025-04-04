# AppCat Ruleset Collector

A Java application that collects and processes AppCat rulesets and exports them to Excel format.

## Features

- Processes AppCat ruleset directories
- Filters rulesets based on specified criteria
- Exports ruleset data to Excel format
- Supports command-line arguments for configuration

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

```bash
./mvnw clean install
```

## Running the Application

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar rulesetpath=<path> outputpath=<path> filters=<filter1,filter2,...>
```

### Arguments

- `rulesetpath`: Path to the directory containing AppCat rulesets
- `outputpath`: Path where the Excel file will be saved
- `filters`: Comma-separated list of filters to apply (optional)

### Example

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar rulesetpath=/path/to/rulesets outputpath=./output filters=azure,cloud-readiness,openjdk11
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 