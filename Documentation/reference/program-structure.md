# Program Structure - Complete Structural Hierarchy

## Overview

This document provides a complete structural hierarchy of the AppCat Ruleset Reader codebase, documenting all classes, methods, fields, and their relationships extracted through static code analysis.

**Package:** `com.example.demo`  
**Total Classes:** 4 (2 top-level, 2 inner classes)  
**Total Lines:** 520 lines  
**Source Files:** 2 files  

---

## Class Hierarchy

```
com.example.demo
├── DemoApplication (top-level class)
│   └── CliRunner (inner static class)
└── RulesetToExcel (top-level class)
    └── RuleData (inner static class)
```

---

## DemoApplication Class

**File:** `src/main/java/com/example/demo/DemoApplication.java` (Lines 1-132)  
**Type:** Public class  
**Annotations:** `@SpringBootApplication`  
**Purpose:** Application entry point and command-line argument orchestrator

### Class-Level Annotations

```java
@SpringBootApplication
```
- **Line:** 13
- **Purpose:** Enables Spring Boot auto-configuration, component scanning, and Spring Boot-specific configuration
- **Implications:** 
  - Triggers component scanning in com.example.demo package
  - Enables auto-configuration based on classpath dependencies
  - Registers this class as a configuration source

### Static Fields

All fields are private static, accessible through getters:

| Field Name | Type | Line | Initial Value | Purpose |
|------------|------|------|---------------|---------|
| `rulesetPath` | String | 15 | null | Stores path to ruleset directory from command-line |
| `outputPath` | String | 16 | null | Stores path to output directory from command-line |
| `filters` | List<String> | 17 | null | Stores optional directory filters from command-line |
| `action` | String | 18 | null | Stores action type (extract or analyze-spring) |

### Public Constants

| Constant | Type | Value | Line | Purpose |
|----------|------|-------|------|---------|
| `ACTION_EXTRACT` | String | "extract" | 20 | Defines extract action identifier |
| `ACTION_ANALYZE_SPRING` | String | "analyze-spring" | 21 | Defines analyze-spring action identifier |

### Methods

#### main(String[] args)
- **Signature:** `public static void main(String[] args)`
- **Line:** 23
- **Return Type:** void
- **Purpose:** Application entry point that launches Spring Boot
- **Implementation:** Single line - delegates to `SpringApplication.run(DemoApplication.class, args)`
- **Side Effects:** Starts Spring Boot application context

#### getRulesetPath()
- **Signature:** `public static String getRulesetPath()`
- **Lines:** 126-128
- **Return Type:** String
- **Purpose:** Accessor for rulesetPath static field
- **Returns:** Current value of rulesetPath (may be null)

#### getOutputPath()
- **Signature:** `public static String getOutputPath()`
- **Lines:** 130-132
- **Return Type:** String
- **Purpose:** Accessor for outputPath static field
- **Returns:** Current value of outputPath (may be null)

#### getFilters()
- **Signature:** `public static List<String> getFilters()`
- **Lines:** 134-136
- **Return Type:** List<String>
- **Purpose:** Accessor for filters static field
- **Returns:** Current value of filters list (may be null)

### Inner Class: CliRunner

**Full Name:** `DemoApplication.CliRunner`  
**Modifier:** Static  
**Annotations:** `@Component`  
**Implements:** `CommandLineRunner`  
**Lines:** 26-122  

#### Purpose
Spring-managed component that executes after application context initialization, processing command-line arguments and orchestrating the appropriate action.

#### Implemented Methods

##### run(String... args)
- **Signature:** `public void run(String... args) throws Exception`
- **Annotation:** `@Override`
- **Lines:** 27-121
- **Return Type:** void (declared to throw Exception per interface contract)
- **Purpose:** Main command-line processing logic
- **Parameters:**
  - `args` - Command-line arguments in format `key=value`
- **Complexity:** HIGH (100+ lines, multiple branches, estimated cyclomatic complexity 15+)

**Detailed Logic Flow:**

1. **Empty Arguments Check** (Lines 28-36)
   - If no arguments, print error and usage to stderr
   - Exit with code 1

2. **Argument Parsing Loop** (Lines 37-56)
   - Iterate through all arguments
   - Parse key=value format:
     - `rulesetpath=` → sets rulesetPath
     - `outputpath=` → sets outputPath
     - `filters=` → splits comma-separated values, trims, sets filters list
     - `action=` → sets action
   - Print each parsed value to stdout

3. **Extract Action Validation** (Lines 60-67)
   - If action is "extract":
     - Verify rulesetpath and outputpath are not null
     - If missing, print error and exit with code 1

4. **Analyze-Spring Action Validation** (Lines 68-73)
   - If action is "analyze-spring":
     - Verify outputpath is not null
     - If missing, print error and exit with code 1

5. **Extract Action Execution** (Lines 75-102)
   - Create File objects for ruleset and output directories
   - Validate ruleset directory exists and is a directory
   - If output directory doesn't exist, attempt to create it
   - Validate output path is a directory
   - Print configuration to stdout
   - Invoke `RulesetToExcel.execute(rulesetPath, outputPath, filters)`

6. **Analyze-Spring Action Execution** (Lines 103-115)
   - Create File object for output directory
   - Validate output directory exists and is a directory
   - Print configuration to stdout
   - Invoke `RulesetToExcel.recognizeSpringRules(outputPath)`

7. **Unknown Action Handling** (Lines 116-119)
   - If action doesn't match known actions, print error and exit with code 1

**Error Handling:**
- All validation failures result in System.err messages and System.exit(1)
- No exceptions thrown (terminates process instead)

**Side Effects:**
- Modifies static fields in DemoApplication
- Writes to stdout (System.out) for logging
- Writes to stderr (System.err) for errors
- Calls System.exit(1) on validation failures
- Creates directories on filesystem
- Invokes RulesetToExcel static methods

---

## RulesetToExcel Class

**File:** `src/main/java/com/example/demo/RulesetToExcel.java` (Lines 1-388)  
**Type:** Public class  
**Purpose:** Utility class providing static methods for YAML-to-Excel conversion and Spring rule analysis

### Design Pattern
**Utility Class Pattern** - All methods are static, no instance state

### Public Methods

#### recognizeSpringRules(String outputPath)
- **Signature:** `public static void recognizeSpringRules(String outputPath)`
- **Lines:** 25-78
- **Return Type:** void
- **Purpose:** Analyze Excel file to identify and mark Spring-specific rules
- **Parameters:**
  - `outputPath` - Directory containing appcat-ruleset.xlsx file
- **Algorithm:**
  1. Open Excel file at `outputPath/appcat-ruleset.xlsx`
  2. For each sheet in workbook:
     - Locate title row (row index 1)
     - Check if "spring specific?" column exists
     - If not, create it at end of columns
     - For each data row (starting at row index 2):
       - Call `isSpringSpecificRule(row)` to determine result
       - Write "Yes" or "No" to spring specific column
     - Auto-size the new column
  3. Write workbook back to file
  4. Explicitly close workbook
  5. Print success message
- **Exception Handling:** Catches IOException and prints stack trace
- **Resource Management:** Uses try-with-resources for FileInputStream and inner try-with-resources for FileOutputStream

#### execute(String rulesetPath, String outputPath)
- **Signature:** `public static void execute(String rulesetPath, String outputPath)`
- **Lines:** 111-113
- **Return Type:** void
- **Purpose:** Convenience overload that calls main execute method with null filters
- **Delegates to:** `execute(rulesetPath, outputPath, null)`

#### execute(String rulesetPath, String outputPath, List<String> filters)
- **Signature:** `public static void execute(String rulesetPath, String outputPath, List<String> filters)`
- **Lines:** 115-147
- **Return Type:** void
- **Purpose:** Main processing method that converts YAML rulesets to Excel
- **Parameters:**
  - `rulesetPath` - Root directory containing ruleset subdirectories
  - `outputPath` - Directory where Excel file will be created
  - `filters` - Optional list of directory name filters (null = process all)
- **Algorithm:**
  1. Delete existing Excel file if present
  2. Create new XSSFWorkbook
  3. Validate rulesetPath is a directory
  4. Call `processRulesetFolder(rootDir, workbook, filters)`
  5. Write workbook to `outputPath/appcat-ruleset.xlsx`
  6. Flush output stream
  7. Print success message (in Chinese)
- **Exception Handling:** 
  - Catches IOException and prints stack trace
  - Finally block ensures streams and workbook are closed
- **Error Messages:** Chinese language ("无法删除已存在的Excel文件", "指定的路径不是一个目录！", "Excel文件生成成功！")
- **Resource Management:** Manual try-finally with explicit close calls

### Private Helper Methods

#### isSpringSpecificRule(Row row)
- **Signature:** `private static boolean isSpringSpecificRule(Row row)`
- **Lines:** 80-109
- **Return Type:** boolean
- **Purpose:** Determine if a rule is Spring-specific based on content analysis
- **Algorithm:**
  1. Return false if row is null
  2. Iterate through all cells in row
  3. Check if any cell text contains "spring" (case-insensitive) → return true
  4. Identify "When" column by checking title row
  5. If "When" column found, check if it contains "properties|" → return true
  6. Return false if no Spring indicators found
- **Complexity:** Moderate (nested loops, multiple conditional checks)

#### processRulesetFolder(File rootDir, Workbook workbook, List<String> filters)
- **Signature:** `private static void processRulesetFolder(File rootDir, Workbook workbook, List<String> filters)`
- **Lines:** 149-170
- **Return Type:** void
- **Purpose:** Iterate through subdirectories applying filters and processing each ruleset
- **Algorithm:**
  1. List all subdirectories in rootDir
  2. For each subdirectory:
     - If filters are provided and not empty:
       - Check if directory name matches any filter (exact match or substring)
       - Skip directory if no match
     - Call `processSubDirectory(subDir, workbook)`
- **Filter Logic:** 
  - No filters (null or empty) → process all directories
  - Filters present → directory name must equal or contain at least one filter string
  - Case-sensitive matching

#### processSubDirectory(File subDir, Workbook workbook)
- **Signature:** `private static void processSubDirectory(File subDir, Workbook workbook)`
- **Lines:** 172-245
- **Return Type:** void
- **Purpose:** Process a single ruleset directory, creating an Excel sheet
- **Algorithm:**
  1. Check for ruleset.yaml file, return if not present
  2. Parse ruleset.yaml to extract name and description
  3. Collect all .yaml files (excluding ruleset.yaml)
  4. Process each YAML file to extract rules
  5. Create sheet with safe name (from ruleset name or default)
  6. Write header row with description
  7. Write title row with column headers
  8. Write data rows for each rule
  9. Set column widths (20, 50, 100, 20, 20, 20, 20 character units)
  10. Apply word wrap style to all cells
- **Exception Handling:** Catches IOException on ruleset.yaml parsing, prints stack trace and returns
- **Sheet Structure:**
  - Row 0: Header with "Description" and combined name/description
  - Row 1: Title row with column names
  - Row 2+: Data rows

#### processYamlFile(File yamlFile, List<RuleData> rules)
- **Signature:** `private static void processYamlFile(File yamlFile, List<RuleData> rules)`
- **Lines:** 247-265
- **Return Type:** void
- **Purpose:** Parse a single YAML rule file and extract rule data
- **Algorithm:**
  1. Load YAML file using SnakeYAML
  2. Check if data is List or Map
  3. If List: iterate and call extractRuleData for each Map item
  4. If Map: call extractRuleData directly
- **Exception Handling:** Catches IOException, prints stack trace
- **Type Safety:** Uses @SuppressWarnings("unchecked") for Map casting

#### extractRuleData(Map<String, Object> ruleData, List<RuleData> rules)
- **Signature:** `private static void extractRuleData(Map<String, Object> ruleData, List<RuleData> rules)`
- **Lines:** 267-315
- **Return Type:** void
- **Purpose:** Extract rule fields from YAML Map and create RuleData object
- **Extracted Fields:**
  - `ruleID` - Direct string field
  - `when` - Serialized using serializeWhen()
  - `description` + `message` - Merged using mergeDescriptionAndMessage()
  - `labels` - Parsed array for source/target/domain/category
- **Label Parsing Logic:**
  - `konveyor.io/source=` prefix → source field
  - `konveyor.io/target=` prefix → target field
  - `domain=` prefix → domain field
  - `category=` prefix → category field
  - Multiple values concatenated with ", " separator
- **Output:** Adds new RuleData object to rules list

#### serializeWhen(Object whenObj)
- **Signature:** `private static String serializeWhen(Object whenObj)`
- **Lines:** 317-324
- **Return Type:** String
- **Purpose:** Convert when clause object to YAML string representation
- **Configuration:**
  - Flow style: BLOCK
  - Indent: 2 spaces
  - Pretty flow: enabled
- **Returns:** Trimmed YAML string, or empty string if null

#### mergeDescriptionAndMessage(String desc, String msg)
- **Signature:** `private static String mergeDescriptionAndMessage(String desc, String msg)`
- **Lines:** 326-334
- **Return Type:** String
- **Purpose:** Combine description and message fields intelligently
- **Logic:**
  - Both non-empty: concatenate with newline separator
  - Only one non-empty: return that value
  - Both empty: return empty string
- **Preprocessing:** Trims both inputs, treats null as empty

### Inner Class: RuleData

**Full Name:** `RulesetToExcel.RuleData`  
**Modifier:** Static  
**Lines:** 336-360  
**Purpose:** Data Transfer Object holding extracted rule information

#### Fields

All fields are package-private (no access modifier):

| Field | Type | Purpose |
|-------|------|---------|
| `ruleId` | String | Unique identifier for the rule |
| `when` | String | Serialized conditional logic (YAML format) |
| `mergedDescription` | String | Combined description and message |
| `source` | String | Source platform(s) from labels |
| `target` | String | Target platform(s) from labels |
| `domain` | String | Domain classification from labels |
| `category` | String | Category classification from labels |

#### Constructors

##### RuleData(String ruleId, String when, String mergedDescription)
- **Signature:** `RuleData(String ruleId, String when, String mergedDescription)`
- **Lines:** 345-347
- **Purpose:** Convenience constructor with 3 core fields
- **Delegates to:** Full constructor with empty strings for source/target/domain/category

##### RuleData(String ruleId, String when, String mergedDescription, String source, String target, String domain, String category)
- **Signature:** `RuleData(String ruleId, String when, String mergedDescription, String source, String target, String domain, String category)`
- **Lines:** 348-356
- **Purpose:** Full constructor initializing all 7 fields
- **Parameters:** Direct assignment to corresponding fields

---

## Import Dependencies

### DemoApplication.java Imports

```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
```

**External Dependencies:**
- Spring Boot framework (boot, autoconfigure)
- Spring core (stereotype)
- Java standard library (io, util)

### RulesetToExcel.java Imports

```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
```

**External Dependencies:**
- Apache POI (poi, poi-ooxml) - Excel manipulation
- SnakeYAML - YAML parsing
- Java standard library (io, util)

---

## Class Relationships

### Containment Relationships

```
DemoApplication contains CliRunner (inner static class)
RulesetToExcel contains RuleData (inner static class)
```

### Usage Relationships

```
DemoApplication.main() → SpringApplication (framework)
CliRunner.run() → RulesetToExcel.execute()
CliRunner.run() → RulesetToExcel.recognizeSpringRules()
RulesetToExcel.execute() → RulesetToExcel.processRulesetFolder()
RulesetToExcel.processRulesetFolder() → RulesetToExcel.processSubDirectory()
RulesetToExcel.processSubDirectory() → RulesetToExcel.processYamlFile()
RulesetToExcel.processYamlFile() → RulesetToExcel.extractRuleData()
RulesetToExcel.extractRuleData() → RulesetToExcel.serializeWhen()
RulesetToExcel.extractRuleData() → RulesetToExcel.mergeDescriptionAndMessage()
RulesetToExcel.extractRuleData() → new RuleData()
RulesetToExcel.recognizeSpringRules() → RulesetToExcel.isSpringSpecificRule()
```

### Interface Implementations

```
CliRunner implements CommandLineRunner (Spring Boot interface)
```

---

## Access Modifiers Summary

### DemoApplication
- **Class:** public
- **Fields:** private static (4 fields)
- **Constants:** public static final (2 constants)
- **Methods:** public static (4 methods)
- **Inner Class:** public static (CliRunner)

### RulesetToExcel
- **Class:** public
- **Public Methods:** 3 (recognizeSpringRules, execute x2)
- **Private Methods:** 6 (isSpringSpecificRule, processRulesetFolder, processSubDirectory, processYamlFile, extractRuleData, serializeWhen, mergeDescriptionAndMessage)
- **Inner Class:** static (RuleData)

### CliRunner
- **Class:** public static
- **Methods:** public (1 - run method)

### RuleData
- **Class:** static
- **Fields:** package-private (7 fields)
- **Constructors:** package-private (2 constructors)

---

## Method Signature Reference

### Quick Reference Table

| Class | Method | Parameters | Return Type | Access | Lines |
|-------|--------|------------|-------------|--------|-------|
| DemoApplication | main | String[] | void | public static | 23 |
| DemoApplication | getRulesetPath | none | String | public static | 126-128 |
| DemoApplication | getOutputPath | none | String | public static | 130-132 |
| DemoApplication | getFilters | none | List<String> | public static | 134-136 |
| CliRunner | run | String... | void | public | 27-121 |
| RulesetToExcel | recognizeSpringRules | String | void | public static | 25-78 |
| RulesetToExcel | execute | String, String | void | public static | 111-113 |
| RulesetToExcel | execute | String, String, List<String> | void | public static | 115-147 |
| RulesetToExcel | isSpringSpecificRule | Row | boolean | private static | 80-109 |
| RulesetToExcel | processRulesetFolder | File, Workbook, List<String> | void | private static | 149-170 |
| RulesetToExcel | processSubDirectory | File, Workbook | void | private static | 172-245 |
| RulesetToExcel | processYamlFile | File, List<RuleData> | void | private static | 247-265 |
| RulesetToExcel | extractRuleData | Map<String,Object>, List<RuleData> | void | private static | 267-315 |
| RulesetToExcel | serializeWhen | Object | String | private static | 317-324 |
| RulesetToExcel | mergeDescriptionAndMessage | String, String | String | private static | 326-334 |

---

## Package Structure

```
com.example.demo
├── DemoApplication.java
│   ├── DemoApplication (class)
│   └── DemoApplication.CliRunner (inner class)
└── RulesetToExcel.java
    ├── RulesetToExcel (class)
    └── RulesetToExcel.RuleData (inner class)
```

**Total Package Members:**
- **Top-level classes:** 2
- **Inner classes:** 2
- **Total classes:** 4

---

## Source Code Traceability

All information in this document is derived from static analysis of:
- **[DemoApplication.java](../../src/main/java/com/example/demo/DemoApplication.java)** - Lines 1-136
- **[RulesetToExcel.java](../../src/main/java/com/example/demo/RulesetToExcel.java)** - Lines 1-388

Line number references throughout this document link to specific locations in the source code for verification and deeper investigation.

---

## Related Documentation

- **[Interfaces Documentation](interfaces.md)** - Detailed CommandLineRunner implementation
- **[Data Models](data-models.md)** - Complete RuleData structure
- **[Components](../architecture/components.md)** - Component responsibilities and interactions
- **[Dependencies](../architecture/dependencies.md)** - Internal and external dependency mapping
- **[Business Logic](../behavior/business-logic.md)** - How these structures implement business rules
- **[Code Metrics](../analysis/code-metrics.md)** - Quantitative analysis of this structure

---

*Generated through comprehensive static code analysis. All class, method, and field information extracted from source code without compilation or execution.*
