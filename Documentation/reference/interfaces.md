# Interfaces and Public APIs

## Overview

This document provides comprehensive documentation of all public interfaces, API contracts, and public method specifications in the AppCat Ruleset Reader codebase.

**Key Interfaces:**
- Spring Boot CommandLineRunner implementation
- Public static utility methods in RulesetToExcel
- Public accessor methods in DemoApplication

---

## Spring Boot Interface Implementation

### CommandLineRunner Interface

**Implemented By:** `DemoApplication.CliRunner`  
**Package:** org.springframework.boot  
**Purpose:** Spring Boot callback interface for executing code after application context initialization

#### Interface Contract

```java
public interface CommandLineRunner {
    void run(String... args) throws Exception;
}
```

#### Implementation: CliRunner.run()

**Location:** `src/main/java/com/example/demo/DemoApplication.java` (Lines 27-121)

**Signature:**
```java
@Override
public void run(String... args) throws Exception
```

**Contract Obligations:**
- ✅ **Implemented:** Single method `run()` with varargs String parameter
- ✅ **Exception Declaration:** Declares `throws Exception` per interface contract
- ⚠️ **Actual Behavior:** Never throws exceptions; calls System.exit(1) on errors instead

**Input Specification:**
- **Parameter:** `args` - Variable-length array of command-line arguments
- **Expected Format:** `key=value` pairs
- **Supported Keys:**
  - `rulesetpath=<directory>` - Source directory for extract action
  - `outputpath=<directory>` - Output directory for both actions  
  - `filters=<name1,name2,...>` - Optional comma-separated directory filters
  - `action=<extract|analyze-spring>` - Required action type

**Output Specification:**
- **Standard Output:** Progress messages and parsed argument values
- **Standard Error:** Validation error messages and usage information
- **File System:** Creates Excel file at `{outputpath}/appcat-ruleset.xlsx`
- **Exit Codes:**
  - Normal completion: 0 (Spring Boot default)
  - Validation failure: 1 (System.exit)

**Side Effects:**
1. Modifies static fields in DemoApplication (rulesetPath, outputPath, filters, action)
2. Writes to stdout and stderr
3. Creates directories if needed
4. Invokes RulesetToExcel static methods
5. May call System.exit(1) terminating the JVM

**Thread Safety:** Not thread-safe (modifies static state)

**Usage Example:**
```bash
java -jar demo.jar rulesetpath=/data/rules outputpath=/data/out action=extract
```

**Spring Boot Integration:**
- Annotated with `@Component` for automatic Spring detection
- Registered as Spring bean during component scanning
- Automatically invoked by Spring Boot after ApplicationContext is ready
- Execution happens on main thread before server start (if server is configured)

---

## Public API: DemoApplication

### Static Field Accessors

These methods provide read-only access to command-line argument values stored in static fields.

#### getRulesetPath()

**Location:** `src/main/java/com/example/demo/DemoApplication.java` (Lines 126-128)

**Signature:**
```java
public static String getRulesetPath()
```

**Contract:**
- **Returns:** Current value of rulesetPath static field
- **Return Type:** String (nullable)
- **Possible Values:**
  - Parsed directory path from command line
  - null if not set via command line
- **Side Effects:** None (pure accessor)
- **Thread Safety:** Not thread-safe (reads mutable static state)

**Usage Example:**
```java
String path = DemoApplication.getRulesetPath();
if (path != null) {
    // Use path
}
```

#### getOutputPath()

**Location:** `src/main/java/com/example/demo/DemoApplication.java` (Lines 130-132)

**Signature:**
```java
public static String getOutputPath()
```

**Contract:**
- **Returns:** Current value of outputPath static field
- **Return Type:** String (nullable)
- **Possible Values:**
  - Parsed directory path from command line
  - null if not set via command line
- **Side Effects:** None (pure accessor)
- **Thread Safety:** Not thread-safe (reads mutable static state)

#### getFilters()

**Location:** `src/main/java/com/example/demo/DemoApplication.java` (Lines 134-136)

**Signature:**
```java
public static List<String> getFilters()
```

**Contract:**
- **Returns:** Current value of filters static field
- **Return Type:** List<String> (nullable)
- **Possible Values:**
  - List of trimmed filter strings from command line
  - null if no filters argument provided
- **Side Effects:** None (pure accessor)
- **Thread Safety:** Not thread-safe (reads mutable static state)
- **Mutability:** Returned list is mutable (Arrays.asList result)

---

## Public API: RulesetToExcel

### Static Utility Methods

All methods in RulesetToExcel are public static, following the Utility Class pattern.

#### execute(String rulesetPath, String outputPath)

**Location:** `src/main/java/com/example/demo/RulesetToExcel.java` (Lines 111-113)

**Signature:**
```java
public static void execute(String rulesetPath, String outputPath)
```

**Purpose:** Convenience method for extract action without filters

**Contract:**
- **Parameters:**
  - `rulesetPath` - Path to root directory containing ruleset subdirectories (required, non-null)
  - `outputPath` - Path to output directory where Excel will be created (required, non-null)
- **Returns:** void
- **Throws:** No declared exceptions (IOException caught internally)
- **Delegates To:** `execute(rulesetPath, outputPath, null)`

**Preconditions:**
- rulesetPath must point to existing directory
- rulesetPath must contain subdirectories with ruleset.yaml files
- outputPath must be valid writable directory path

**Postconditions:**
- Excel file created at `{outputPath}/appcat-ruleset.xlsx`
- All ruleset subdirectories processed (no filtering)
- Success message printed to stdout

**Side Effects:**
- Creates/overwrites Excel file
- Deletes existing Excel file if present
- Prints messages to stdout
- Prints stack traces to stderr on errors

**Usage Example:**
```java
RulesetToExcel.execute("/data/rulesets", "/data/output");
```

#### execute(String rulesetPath, String outputPath, List<String> filters)

**Location:** `src/main/java/com/example/demo/RulesetToExcel.java` (Lines 115-147)

**Signature:**
```java
public static void execute(String rulesetPath, String outputPath, List<String> filters)
```

**Purpose:** Main extract action method with optional directory filtering

**Contract:**
- **Parameters:**
  - `rulesetPath` - Path to root directory containing ruleset subdirectories (required, non-null)
  - `outputPath` - Path to output directory where Excel will be created (required, non-null)
  - `filters` - Optional list of directory name filters (nullable, empty list = no filtering)
- **Returns:** void
- **Throws:** No declared exceptions (IOException caught internally)

**Processing Algorithm:**
1. Check for existing Excel file and delete if present
2. Create new XSSFWorkbook
3. Validate rulesetPath is directory
4. Process each subdirectory (with filtering if filters provided)
5. Write workbook to `{outputPath}/appcat-ruleset.xlsx`
6. Close resources

**Preconditions:**
- rulesetPath must point to existing directory
- outputPath must be valid directory path
- filters, if provided, should contain non-empty strings

**Postconditions:**
- Excel file created at `{outputPath}/appcat-ruleset.xlsx`
- Only filtered subdirectories processed if filters provided
- All subdirectories processed if filters null or empty
- Success message printed: "Excel文件生成成功！"

**Side Effects:**
- Creates/overwrites Excel file without confirmation
- Deletes existing Excel file without backup
- Creates XSSFWorkbook in memory (heap usage)
- Prints messages to stdout (Chinese)
- Prints stack traces to stderr on IOException
- Modifies file system

**Error Handling:**
- IOException during processing: Prints stack trace, continues execution
- Invalid rulesetPath: Prints error message "指定的路径不是一个目录！", returns early
- File deletion failure: Prints error message, returns early

**Filter Behavior:**
- **null filters:** Process all subdirectories
- **Empty list:** Process all subdirectories
- **Non-empty list:** Only process directories where name equals or contains any filter string
- **Matching:** Case-sensitive substring or exact match

**Performance Characteristics:**
- **I/O Bound:** Performance depends on file system and YAML parsing
- **Memory Usage:** Scales with number of rules (entire workbook in memory)
- **Processing Time:** ~1000 rules/second (estimated)

**Thread Safety:** Not thread-safe (file system operations, shared workbook object)

**Usage Examples:**

Without filters:
```java
RulesetToExcel.execute("/data/rulesets", "/data/output", null);
```

With filters:
```java
List<String> filters = Arrays.asList("java", "spring", "jee");
RulesetToExcel.execute("/data/rulesets", "/data/output", filters);
```

**Excel Output Schema:**
- **File:** `{outputPath}/appcat-ruleset.xlsx`
- **Format:** XLSX (Excel 2007+)
- **Structure:**
  - One sheet per processed ruleset
  - Row 0: Description row
  - Row 1: Column headers
  - Row 2+: Rule data
- **Columns:** RuleID, When, Description & Message, Source, Target, Domain, Category
- **Formatting:** Word wrap enabled, specific column widths applied

#### recognizeSpringRules(String outputPath)

**Location:** `src/main/java/com/example/demo/RulesetToExcel.java` (Lines 25-78)

**Signature:**
```java
public static void recognizeSpringRules(String outputPath)
```

**Purpose:** Analyze existing Excel file to identify and mark Spring-specific rules

**Contract:**
- **Parameters:**
  - `outputPath` - Directory containing appcat-ruleset.xlsx file (required, non-null)
- **Returns:** void
- **Throws:** No declared exceptions (IOException caught internally)

**Processing Algorithm:**
1. Open Excel file at `{outputPath}/appcat-ruleset.xlsx` for reading
2. For each sheet in workbook:
   - Locate title row (row index 1)
   - Check if "spring specific?" column exists
   - Create column if doesn't exist
   - For each data row (index 2 onwards):
     - Call isSpringSpecificRule() to analyze row
     - Write "Yes" or "No" to spring specific column
   - Auto-size the column
3. Write modified workbook back to same file
4. Close workbook explicitly
5. Print success message: "Analyze completed and Excel updated."

**Preconditions:**
- outputPath must contain appcat-ruleset.xlsx file
- Excel file must be valid XLSX format
- Excel file must have proper structure (title row at index 1)

**Postconditions:**
- Excel file modified in-place
- "spring specific?" column added to all sheets (if not present)
- All data rows analyzed and marked Yes/No
- Column auto-sized for readability
- Success message printed to stdout

**Side Effects:**
- Modifies Excel file in-place (destructive operation)
- No backup created before modification
- Loads entire workbook into memory
- Prints message to stdout
- Prints stack trace to stderr on IOException

**Spring Detection Logic:**
A rule is marked as Spring-specific ("Yes") if:
1. Any cell in the row contains "spring" (case-insensitive), OR
2. The "When" column contains "properties|" pattern

**Error Handling:**
- IOException during read/write: Prints stack trace, execution terminates
- Missing file: IOException → stack trace
- Invalid Excel format: POI exception → stack trace
- Null row: Skipped (isSpringSpecificRule returns false)

**Column Handling:**
- **Existing column:** Reuses existing "spring specific?" column, updates values
- **New column:** Creates at end of columns in title row
- **Data rows:** Creates new cells if column was just added, updates existing cells if column existed

**Resource Management:**
- Uses try-with-resources for FileInputStream and XSSFWorkbook
- Nested try-with-resources for FileOutputStream
- Explicit workbook.close() call to release file locks

**Performance Characteristics:**
- **I/O Bound:** Performance depends on Excel file size
- **Memory Usage:** Entire workbook loaded into memory
- **Processing Time:** Linear in number of rows × columns

**Thread Safety:** Not thread-safe (file system operations, workbook modification)

**Usage Example:**
```java
RulesetToExcel.recognizeSpringRules("/data/output");
```

**Typical Workflow:**
```java
// Step 1: Generate Excel from YAML
RulesetToExcel.execute("/data/rulesets", "/data/output", null);

// Step 2: Analyze for Spring-specific rules
RulesetToExcel.recognizeSpringRules("/data/output");
```

**Expected Output File:**
- Same file location: `{outputPath}/appcat-ruleset.xlsx`
- Additional column in each sheet: "spring specific?"
- Values: "Yes" or "No" for each rule row

---

## Public Constants

### Action Type Constants

**Location:** `src/main/java/com/example/demo/DemoApplication.java` (Lines 20-21)

#### ACTION_EXTRACT

**Declaration:**
```java
public static final String ACTION_EXTRACT = "extract";
```

**Purpose:** Defines the identifier for the extract action  
**Usage:** Command-line argument validation and comparison  
**Value:** `"extract"`  
**Immutability:** final (cannot be modified)

#### ACTION_ANALYZE_SPRING

**Declaration:**
```java
public static final String ACTION_ANALYZE_SPRING = "analyze-spring";
```

**Purpose:** Defines the identifier for the analyze-spring action  
**Usage:** Command-line argument validation and comparison  
**Value:** `"analyze-spring"`  
**Immutability:** final (cannot be modified)

**Usage Example:**
```java
if (ACTION_EXTRACT.equalsIgnoreCase(action)) {
    // Execute extract action
} else if (ACTION_ANALYZE_SPRING.equalsIgnoreCase(action)) {
    // Execute analyze-spring action
}
```

---

## API Usage Patterns

### Extract Action Pattern

```java
// Pattern 1: Simple extract (no filters)
RulesetToExcel.execute(rulesetPath, outputPath);

// Pattern 2: Extract with filtering
List<String> filters = Arrays.asList("java", "spring");
RulesetToExcel.execute(rulesetPath, outputPath, filters);

// Pattern 3: Via command line
// Command: java -jar demo.jar rulesetpath=/data/rules outputpath=/data/out action=extract
// Results in CliRunner calling: RulesetToExcel.execute(rulesetPath, outputPath, filters);
```

### Analyze-Spring Action Pattern

```java
// Pattern 1: Direct invocation
RulesetToExcel.recognizeSpringRules(outputPath);

// Pattern 2: Via command line
// Command: java -jar demo.jar outputpath=/data/out action=analyze-spring
// Results in CliRunner calling: RulesetToExcel.recognizeSpringRules(outputPath);
```

### Complete Workflow Pattern

```java
// Step 1: Extract rulesets to Excel
String rulesetPath = "/data/konveyor-rulesets";
String outputPath = "/data/analysis";
List<String> filters = Arrays.asList("spring", "java");
RulesetToExcel.execute(rulesetPath, outputPath, filters);

// Step 2: Analyze for Spring-specific rules
RulesetToExcel.recognizeSpringRules(outputPath);

// Result: Excel file with Spring analysis column
```

---

## API Limitations and Constraints

### Current Limitations

1. **No Return Values:** All methods are void, no success/failure indication
2. **No Exceptions:** Errors handled internally with stack traces, no propagation
3. **Static Methods:** Cannot be mocked or dependency-injected
4. **File Path Coupling:** Hardcoded file name "appcat-ruleset.xlsx"
5. **No Progress Callbacks:** Silent processing, no progress reporting
6. **Single Output Format:** Only Excel, no JSON/CSV alternatives
7. **In-Memory Processing:** Entire workbook held in memory
8. **Destructive Operations:** File deletion and overwriting without confirmation
9. **Chinese Messages:** Some output messages in Chinese only
10. **No Validation API:** Callers must validate inputs before calling

### Thread Safety Considerations

**None of the public APIs are thread-safe:**
- Static field modifications in DemoApplication
- File system operations without locking
- Shared workbook objects during processing
- No synchronization mechanisms

**Recommendation:** Use from single-threaded context only (e.g., main thread, CommandLineRunner)

### Error Handling Contract

**All public methods follow this error handling pattern:**
1. Catch IOException internally
2. Print stack trace to stderr
3. Return normally (no exception thrown)
4. No indication of success/failure to caller

**Implications:**
- Caller cannot distinguish success from failure
- Must check file system for output file existence
- No structured error reporting

---

## API Compatibility Notes

### Public API Surface

**Stable Public APIs (should not change without major version bump):**
- `DemoApplication.main(String[] args)` - Required by Java
- `CliRunner.run(String... args)` - Required by CommandLineRunner interface
- `RulesetToExcel.execute()` methods - Core functionality
- `RulesetToExcel.recognizeSpringRules()` - Core functionality
- Public constants ACTION_EXTRACT, ACTION_ANALYZE_SPRING

**Internal APIs (can change freely):**
- All private methods in RulesetToExcel
- Static fields in DemoApplication (private)
- RuleData class (package-private)

### Breaking Changes to Avoid

To maintain backward compatibility:
- ✅ Can add new methods
- ✅ Can add new public constants
- ✅ Can enhance internal implementations
- ❌ Cannot change public method signatures
- ❌ Cannot remove public methods
- ❌ Cannot change constant values
- ❌ Cannot change CommandLineRunner contract

---

## Related Documentation

- **[Program Structure](program-structure.md)** - Complete class and method structure
- **[Data Models](data-models.md)** - RuleData structure details
- **[Business Logic](../behavior/business-logic.md)** - Implementation details and algorithms
- **[Workflows](../behavior/workflows.md)** - Process flows using these APIs
- **[Test Specifications](../migration/test-specifications.md)** - API testing requirements

---

*This document provides complete specification of all public APIs based on static code analysis. For implementation details, see Program Structure documentation. For usage examples, see Workflows documentation.*
