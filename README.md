# AppCat Ruleset Reader

A Spring Boot Java application that processes AppCat (Application Modernization and Migration Assessment Tool) rulesets and exports them to Excel format for easy analysis and review.

**Note**: To leverage AI for quickly summarizing rulesets, tools like ChatGPT or DeepSeek are recommended.

## Features

- **Extract Action**: Processes AppCat ruleset directories and exports rule data to Excel
  - Reads YAML ruleset files from subdirectories
  - Extracts rule metadata including RuleID, conditions, descriptions, labels
  - Supports filtering rulesets by directory name
  - Generates structured Excel workbook with separate sheets per ruleset
  - Formats output with column sizing and text wrapping for readability

- **Analyze-Spring Action**: Identifies Spring-specific migration rules
  - Analyzes existing Excel output to detect Spring framework patterns
  - Adds "spring specific?" column to identify rules related to Spring or properties files
  - Helps prioritize Spring migration efforts

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Dependencies

- Spring Boot 3.2.3
- Apache POI 5.2.5 (Excel manipulation)
- SnakeYAML 2.2 (YAML parsing)

## Building the Project

```bash
./mvnw clean install
```

This will create an executable JAR file at `target/demo-0.0.1-SNAPSHOT.jar`.

## Running the Application

The application supports two primary actions: **extract** and **analyze-spring**.

### Action: Extract

Extracts ruleset data from YAML files and generates an Excel workbook.

**Syntax:**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar rulesetpath=<path> outputpath=<path> action=extract [filters=<filter1,filter2,...>]
```

**Arguments:**
- `rulesetpath` (required): Path to the directory containing AppCat ruleset subdirectories
- `outputpath` (required): Path where the Excel file (`appcat-ruleset.xlsx`) will be saved
- `action` (required): Must be set to `extract`
- `filters` (optional): Comma-separated list of subdirectory names to process. If omitted, all subdirectories are processed.

**Example:**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar rulesetpath=/path/to/rulesets outputpath=./output action=extract filters=azure,cloud-readiness,openjdk11,openjdk17,openjdk21
```

### Action: Analyze-Spring

Analyzes an existing Excel file to identify Spring-specific rules.

**Syntax:**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar outputpath=<path> action=analyze-spring
```

**Arguments:**
- `outputpath` (required): Path to the directory containing the existing `appcat-ruleset.xlsx` file
- `action` (required): Must be set to `analyze-spring`

**Example:**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar outputpath=./output action=analyze-spring
```

This will add a "spring specific?" column to each sheet, marking rules that contain Spring-related patterns.

## Output Format

The generated Excel file (`appcat-ruleset.xlsx`) contains:

- **One sheet per ruleset subdirectory** (e.g., "azure", "cloud-readiness")
- **Header row**: Contains the ruleset name and description
- **Column headers**:
  - RuleID: Unique identifier for the rule
  - When: Conditional logic (YAML format)
  - Description & Message: Combined rule description and message
  - Source: Source technology (from labels)
  - Target: Target technology (from labels)
  - Domain: Domain classification (from labels)
  - Category: Category classification (from labels)
  - spring specific? (added by analyze-spring action)

## Filters

Filters allow you to selectively process specific ruleset subdirectories:

- Filter names should match the subdirectory names in your ruleset path
- Multiple filters are comma-separated
- If filters are not specified, all subdirectories are processed
- Filtering supports both exact name matching and partial substring matching (e.g., filter "openjdk" will match directories "openjdk11", "openjdk17", etc.)

**Common filter examples:**
- `azure` - Azure migration rules
- `cloud-readiness` - Cloud readiness assessment rules
- `openjdk11`, `openjdk17`, `openjdk21` - OpenJDK version migration rules
- `spring-boot` - Spring Boot migration rules

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
