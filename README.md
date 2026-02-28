# AppCat Ruleset Collector

A Java application that collects and processes AppCat rulesets, exporting rule data to Excel or CSV format for review and analysis.

Note: to leverage AI to quickly summarize the ruleset, ChatGPT or DeepSeek are recommended.

## Features

- Scans AppCat ruleset directories and parses YAML rule files
- Filters rulesets by subdirectory name
- **`extract`**: Exports rules to an Excel workbook (`appcat-ruleset.xlsx`), one sheet per ruleset folder
- **`extract-v2`**: Exports rules to a flat CSV file (`appcat-ruleset.csv`) with richer field extraction
- **`analyze-spring`**: Post-processes an existing Excel file to tag Spring-specific rules

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

**Linux / macOS:**
```bash
./mvnw clean package -DskipTests
```

**Windows:**
```powershell
# Using the Maven wrapper script (Unix shell via Git Bash / WSL), or directly:
mvn clean package -DskipTests
```

## Running the Application

```bash
java -jar target/appcat-ruleset-reader-1.0.0.jar [rulesetpath=<path>] [outputpath=<path>] [filters=<f1,f2,...>] [action=<action>]
```

All four arguments have built-in defaults and can be omitted:

| Argument | Default value |
|---|---|
| `rulesetpath` | `C:\Users\taoxu\Downloads\appcat\rulesets` |
| `outputpath` | `C:\Users\taoxu\Downloads\appcat\rulesets\extraction-v2` |
| `filters` | `azure,cloud-readiness,openjdk8,openjdk11,openjdk17,openjdk21,os,jakarta-ee` |
| `action` | `extract-v2` |

---

## Actions

### `extract-v2` *(default)*

Reads every non-`ruleset.yaml` YAML file under each matched subdirectory and writes **`appcat-ruleset.csv`** to the output path.

**CSV columns:**

| Column | Source |
|---|---|
| `ruleID` | `ruleID` field |
| `description` | `description` field |
| `message` | `message` field |
| `category (criticality)` | top-level `category` (e.g. `mandatory` / `optional`) |
| `effort` | `effort` field (integer) |
| `domain` | label `domain=` |
| `label_category` | label `category=` |
| `target` | label `target=` or `konveyor.io/target=` (multi-value, `, ` separated) |
| `capability` | label `capability=` (multi-value, `, ` separated) |
| `os` | label `os=` (multi-value, `, ` separated) |
| `when` | `when` condition serialized as YAML block |

The file is UTF-8 with BOM so it opens correctly in Excel.

**Example (no arguments — uses all defaults):**
```bash
java -jar target/appcat-ruleset-reader-1.0.0.jar
```

**Example (override paths only):**
```bash
java -jar target/appcat-ruleset-reader-1.0.0.jar rulesetpath=/path/to/rulesets outputpath=./output
```

---

### `extract`

Writes **`appcat-ruleset.xlsx`** — one Excel sheet per matched subdirectory.  
Each sheet contains: `RuleID`, `When`, `Description & Message`, `Source`, `Target`, `Domain`, `Category`.

```bash
java -jar target/appcat-ruleset-reader-1.0.0.jar rulesetpath=<path> outputpath=<path> action=extract
```

---

### `analyze-spring`

Post-processes an existing **`appcat-ruleset.xlsx`** in the output folder.  
Adds a `spring specific?` column (`Yes` / `No`) to each sheet, based on whether any cell contains `"spring"` or the `When` column contains `"properties|"`.

```bash
java -jar target/appcat-ruleset-reader-1.0.0.jar outputpath=<path> action=analyze-spring
```

---

## Ruleset Directory Structure

```
<rulesetpath>/
  <subdirectory>/          ← one sheet / section per subdirectory
    ruleset.yaml           ← metadata: name, description
    rule-001.yaml          ← individual rule files (parsed for extract / extract-v2)
    rule-002.yaml
    ...
```

Valid subdirectory names processed by default:
`azure`, `cloud-readiness`, `openjdk8`, `openjdk11`, `openjdk17`, `openjdk21`, `os`, `jakarta-ee`

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Thank you
