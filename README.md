# AppCat Ruleset Reader

A Spring Boot CLI application that processes AppCat (Application Containerization/Modernization Tool) rulesets and exports them to Excel format for analysis. The tool helps developers and architects understand migration rules by converting YAML-based rulesets into structured Excel spreadsheets, with additional capabilities to identify Spring-specific migration patterns.

**Note**: To leverage AI for quick ruleset summarization, ChatGPT or DeepSeek are recommended.

## Features

### Core Capabilities
- **YAML to Excel Conversion**: Processes AppCat ruleset directories and converts YAML files to structured Excel workbooks
- **Selective Processing**: Filters rulesets based on specified criteria (directory name matching)
- **Spring Framework Detection**: Automatically identifies Spring-specific migration rules using keyword analysis
- **Metadata Extraction**: Parses `konveyor.io` labels to extract source/target technologies, domains, and categories
- **Formatted Output**: Generates Excel files with:
  - Auto-sized columns with text wrapping
  - Multiple sheets (one per ruleset)
  - Comprehensive rule details (RuleID, When conditions, Description, Source, Target, Domain, Category)

### Actions

#### 1. Extract (`action=extract`)
Converts YAML rulesets into an Excel workbook with one sheet per ruleset.

**Excel Output Structure:**
- **Sheet Name**: Ruleset name
- **Header Row**: Ruleset name and description
- **Columns**: RuleID | When | Description & Message | Source | Target | Domain | Category

#### 2. Analyze Spring (`action=analyze-spring`)
Analyzes an existing Excel workbook to identify Spring-specific rules, adding a "spring specific?" column.

**Detection Logic:**
- Marks rules as "Yes" if any cell contains "spring" (case-insensitive)
- Marks rules as "Yes" if "When" column contains "properties|" pattern
- Otherwise marks as "No"

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Dependencies

- **Spring Boot**: 3.2.3 (CLI framework)
- **Apache POI**: 5.2.5 (Excel manipulation)
- **SnakeYAML**: 2.2 (YAML parsing)

## Building the Project

```bash
./mvnw clean install
```

## Running the Application

### General Syntax

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar action=<action> rulesetpath=<path> outputpath=<path> filters=<filter1,filter2,...>
```

### Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `action` | No | Action to perform: `extract` or `analyze-spring` (default: extract if not specified) |
| `rulesetpath` | Yes (extract only) | Path to the directory containing AppCat rulesets |
| `outputpath` | Yes | Path where the Excel file will be saved/read |
| `filters` | No | Comma-separated list of directory name filters (supports partial matching) |

### Usage Examples

#### Example 1: Extract All Rulesets

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar \
  action=extract \
  rulesetpath=/path/to/rulesets \
  outputpath=./output
```

**Result**: Creates `./output/appcat-ruleset.xlsx` with all rulesets

#### Example 2: Extract with Filters

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar \
  action=extract \
  rulesetpath=/path/to/rulesets \
  outputpath=./output \
  filters=azure,cloud-readiness,openjdk11,openjdk17,openjdk21,os
```

**Result**: Only processes directories matching the filter names (exact or partial match)

#### Example 3: Analyze Spring-Specific Rules

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar \
  action=analyze-spring \
  outputpath=./output
```

**Result**: Updates `./output/appcat-ruleset.xlsx` by adding/updating the "spring specific?" column

## Input Structure

The application expects the following directory structure for rulesets:

```
rulesetpath/
├── ruleset1/
│   ├── ruleset.yaml      # Contains name and description
│   ├── rule1.yaml        # Individual rule definitions
│   ├── rule2.yaml
│   └── ...
├── ruleset2/
│   ├── ruleset.yaml
│   └── ...
└── ...
```

### YAML Schema

**ruleset.yaml:**
```yaml
name: "Ruleset Name"
description: "Ruleset description"
```

**rule.yaml:**
```yaml
ruleID: "unique-rule-id"
when:
  # Conditional logic (can be complex nested structure)
description: "Rule description"
message: "Additional message"
labels:
  - "konveyor.io/source=java"
  - "konveyor.io/target=kubernetes"
  - "domain=containerization"
  - "category=mandatory"
```

## Output Format

**Excel File**: `appcat-ruleset.xlsx` (located in `outputpath`)

**Structure:**
- One sheet per processed ruleset
- Each sheet contains:
  - Row 1: Description header and ruleset metadata
  - Row 2: Column headers (RuleID, When, Description & Message, Source, Target, Domain, Category)
  - Row 3+: Rule data

**Column Widths:**
- RuleID: 20 characters
- When: 50 characters
- Description & Message: 100 characters
- Source/Target/Domain/Category: 20 characters each

All cells have text wrapping enabled for readability.

## Architecture

For detailed architecture and class diagrams, see [class-diagram.md](class-diagram.md).

**Main Components:**
- `DemoApplication`: Spring Boot entry point and CLI argument parser
- `CliRunner`: CommandLineRunner that validates arguments and routes to appropriate action
- `RulesetToExcel`: Core logic for YAML processing and Excel generation
- `RuleData`: Data model representing a single migration rule

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
