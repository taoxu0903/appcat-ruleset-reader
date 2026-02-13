# Workflows - Process Flows and Execution Sequences

## Overview

This document provides detailed process flows for both primary actions supported by the AppCat Ruleset Reader: Extract Action and Analyze-Spring Action. Each workflow is documented with step-by-step sequences, data transformations, and decision points.

---

## Extract Action Workflow

### Purpose
Convert YAML-based migration rulesets into a structured Excel workbook with one sheet per ruleset.

### Workflow Overview

```
[CLI Input] → [Argument Parsing] → [Validation] → [YAML Processing] → [Excel Generation] → [File Output]
```

### Detailed Process Flow

#### Phase 1: Initialization and Argument Parsing

**Entry Point:** `CliRunner.run(String... args)`  
**Location:** DemoApplication.java, Lines 27-121

**Steps:**
1. **Check for Arguments** (Lines 28-36)
   - If args.length == 0:
     - Print error message to stderr
     - Print usage information
     - Call System.exit(1)

2. **Parse Arguments Loop** (Lines 37-56)
   - For each argument in args:
     - Print "Processing argument: {arg}"
     - Parse key=value format:
       ```
       rulesetpath=X → DemoApplication.rulesetPath = X
       outputpath=Y → DemoApplication.outputPath = Y
       filters=A,B,C → DemoApplication.filters = [A, B, C] (trimmed)
       action=Z → DemoApplication.action = Z
       ```
     - Filters split by comma, each value trimmed

3. **Print Parsed Configuration** (Lines 60-62)
   - Output to stdout:
     ```
     Parsed rulesetPath: {value}
     Parsed outputPath: {value}
     Parsed filters: {list}
     Parsed action: {value}
     ```

#### Phase 2: Validation

**Location:** CliRunner.run(), Lines 63-93

**Steps:**
4. **Validate Extract Action Requirements** (Lines 63-67)
   - If action equals "extract" (case-insensitive):
     - Check rulesetpath not null
     - Check outputpath not null
     - If either null:
       - Print error to stderr
       - Print usage
       - Call System.exit(1)

5. **Validate Directory Paths** (Lines 75-93)
   - Create File object for rulesetPath
   - Check rulesetPath exists and is directory:
     - If not: Error message, System.exit(1)
   - Create File object for outputPath
   - Check if outputPath exists:
     - If not exists: Call mkdirs() to create
       - If creation fails: Error message, System.exit(1)
     - If exists but not directory: Error message, System.exit(1)

6. **Print Configuration** (Lines 94-100)
   - Output to stdout:
     ```
     Ruleset folder path: {rulesetPath}
     Output folder path: {outputPath}
     ```
   - If filters not null:
     - Output: "Filters applied: {filters}"
   - Else:
     - Output: "No filters applied - processing all subdirectories"

#### Phase 3: Excel File Preparation

**Entry Point:** `RulesetToExcel.execute(rulesetPath, outputPath, filters)`  
**Location:** RulesetToExcel.java, Lines 115-147

**Steps:**
7. **Delete Existing Excel File** (Lines 117-122)
   - Construct path: `outputPath + "/appcat-ruleset.xlsx"`
   - Create File object for Excel file
   - If file exists:
     - Call excelFile.delete()
     - If deletion fails:
       - Print error (Chinese): "无法删除已存在的Excel文件: {path}"
       - Return early

8. **Create New Workbook** (Line 123)
   - Initialize: `workbook = new XSSFWorkbook()`

9. **Validate Ruleset Directory** (Lines 125-129)
   - Check if rulesetPath is directory
   - If not directory:
     - Print error (Chinese): "指定的路径不是一个目录！"
     - Return early

#### Phase 4: Directory Processing

**Method:** `RulesetToExcel.processRulesetFolder(rootDir, workbook, filters)`  
**Location:** RulesetToExcel.java, Lines 149-170

**Steps:**
10. **Iterate Through Subdirectories** (Lines 150)
    - List all subdirectories in rootDir
    - For each subdirectory:

11. **Apply Filters** (Lines 153-168)
    - If filters provided and not empty:
      - Initialize shouldProcess = false
      - Get directory name
      - For each filter in filters list:
        - Check if dirName equals filter OR dirName contains filter
        - If match: Set shouldProcess = true, break loop
      - If shouldProcess is false:
        - Print: "Skipping directory: {dirName} (not in filter list)"
        - Continue to next directory

12. **Process Matching Directory**
    - Call `processSubDirectory(subDir, workbook)`

#### Phase 5: Ruleset Processing

**Method:** `RulesetToExcel.processSubDirectory(subDir, workbook)`  
**Location:** RulesetToExcel.java, Lines 172-245

**Steps:**
13. **Check for ruleset.yaml** (Lines 173-176)
    - Create File object for "ruleset.yaml" in subdirectory
    - If file doesn't exist: Return early (skip this directory)

14. **Parse ruleset.yaml** (Lines 179-204)
    - Open FileInputStream for ruleset.yaml
    - Create SnakeYAML Yaml parser
    - Load YAML into Map<String, Object>
    - Extract fields:
      ```java
      name = data.getOrDefault("name", "")
      description = data.getOrDefault("description", "")
      ```
    - Close input stream
    - If IOException occurs: printStackTrace(), return early

15. **Collect Rule Files** (Lines 206-212)
    - Initialize empty List<RuleData> rules
    - List all files in subdirectory matching criteria:
      - Is file (not directory)
      - Name ends with ".yaml"
      - Name NOT equals "ruleset.yaml"

16. **Process Each Rule File** (Lines 214-216)
    - For each YAML file:
      - Call `processYamlFile(yamlFile, rules)`
      - Rules list populated by reference

#### Phase 6: YAML File Processing

**Method:** `RulesetToExcel.processYamlFile(File yamlFile, List<RuleData> rules)`  
**Location:** RulesetToExcel.java, Lines 247-265

**Steps:**
17. **Load YAML Data** (Lines 249-252)
    - Open FileInputStream for rule file
    - Create SnakeYAML Yaml parser
    - Load YAML: `Object data = yaml.load(input)`

18. **Handle Data Structure** (Lines 253-262)
    - If data is instanceof List:
      - For each item in List:
        - If item is Map:
          - Call `extractRuleData((Map) item, rules)`
    - Else if data is instanceof Map:
      - Call `extractRuleData((Map) data, rules)`

19. **Error Handling** (Line 263-265)
    - Catch IOException: printStackTrace()

#### Phase 7: Rule Data Extraction

**Method:** `RulesetToExcel.extractRuleData(Map<String,Object> ruleData, List<RuleData> rules)`  
**Location:** RulesetToExcel.java, Lines 267-315

**Steps:**
20. **Extract Core Fields** (Lines 268-273)
    - ruleId = ruleData.get("ruleID") as String
    - whenObj = ruleData.get("when") as Object
    - when = serializeWhen(whenObj) → YAML string
    - desc = ruleData.get("description") as String
    - message = ruleData.get("message") as String
    - merged = mergeDescriptionAndMessage(desc, message)

21. **Initialize Label Fields** (Lines 276-279)
    - source = ""
    - target = ""
    - domain = ""
    - category = ""

22. **Parse Labels Array** (Lines 280-308)
    - Get labelsObj = ruleData.get("labels")
    - If labelsObj is instanceof List:
      - For each labelObj in List:
        - If labelObj is String:
          - If starts with "konveyor.io/source=":
            - Extract value after prefix
            - If source not empty: append ", "
            - Append value to source
          - If starts with "konveyor.io/target=":
            - Extract value, append to target (same pattern)
          - If starts with "domain=":
            - Extract value, append to domain
          - If starts with "category=":
            - Extract value, append to category

23. **Create RuleData Object** (Line 310)
    - Construct: `new RuleData(ruleId, when, merged, source, target, domain, category)`
    - Add to rules list

#### Phase 8: Excel Sheet Creation

**Continuing in:** `processSubDirectory()`, Lines 218-244

**Steps:**
24. **Determine Sheet Name** (Lines 219-221)
    - If name is empty:
      - name = "Sheet" + (workbook.getNumberOfSheets() + 1)
    - Sanitize name: `safeName = WorkbookUtil.createSafeSheetName(name)`

25. **Create Sheet** (Line 222)
    - sheet = workbook.createSheet(safeName)
    - Initialize rowNum = 0

26. **Write Header Row** (Lines 225-227, rowNum=0)
    - Cell 0: "Description"
    - Cell 1: "name:{name} Description:{description}"
    - Increment rowNum

27. **Write Title Row** (Lines 230-236, rowNum=1)
    - Cell 0: "RuleID"
    - Cell 1: "When"
    - Cell 2: "Description & Message"
    - Cell 3: "Source"
    - Cell 4: "Target"
    - Cell 5: "Domain"
    - Cell 6: "Category"
    - Increment rowNum

28. **Write Data Rows** (Lines 239-246, rowNum=2+)
    - For each RuleData rule in rules:
      - Create row at rowNum
      - Cell 0: rule.ruleId
      - Cell 1: rule.when
      - Cell 2: rule.mergedDescription
      - Cell 3: rule.source
      - Cell 4: rule.target
      - Cell 5: rule.domain
      - Cell 6: rule.category
      - Increment rowNum

29. **Apply Formatting** (Lines 249-265)
    - Set column widths (in character units × 256):
      - Column 0 (RuleID): 20 chars
      - Column 1 (When): 50 chars
      - Column 2 (Description): 100 chars
      - Columns 3-6: 20 chars each
    - Create CellStyle with word wrap enabled
    - Apply word wrap style to all cells in all rows (0 to rowNum)

#### Phase 9: File Writing

**Returning to:** `execute()`, Lines 133-145

**Steps:**
30. **Write Workbook to File** (Lines 133-136)
    - Create FileOutputStream: `outputPath + "/appcat-ruleset.xlsx"`
    - Call workbook.write(fos)
    - Call fos.flush()
    - Print success message (Chinese): "Excel文件生成成功！"

31. **Resource Cleanup** (Lines 137-145)
    - Finally block:
      - Close FileOutputStream if not null
      - Close Workbook if not null
    - Catch IOException on close operations: printStackTrace()

32. **Return to CLI**
    - Execution returns to CliRunner.run()
    - CliRunner completes
    - Spring Boot application exits normally

### Data Transformation Flow

```
YAML File (ruleset.yaml)
  ↓ name, description
Excel Sheet Header Row

YAML File (rule.yaml)
  ↓ ruleID
RuleData.ruleId
  ↓
Excel Cell [row, 0]

YAML File (rule.yaml)
  ↓ when (Object)
  ↓ serializeWhen() (YAML serialization)
RuleData.when (String)
  ↓
Excel Cell [row, 1]

YAML File (rule.yaml)
  ↓ description + message
  ↓ mergeDescriptionAndMessage()
RuleData.mergedDescription (String)
  ↓
Excel Cell [row, 2]

YAML File (rule.yaml)
  ↓ labels array
  ↓ parse "konveyor.io/source=" prefix
RuleData.source (comma-separated String)
  ↓
Excel Cell [row, 3]

(Similar for target, domain, category → cells 4, 5, 6)
```

### Error Scenarios and Recovery

| Error Condition | Detection Point | Action Taken | Recovery |
|-----------------|-----------------|--------------|----------|
| No arguments | CliRunner start | Error message, usage, exit(1) | None - terminates |
| Missing required arguments | Validation phase | Error message, usage, exit(1) | None - terminates |
| Invalid ruleset directory | Validation phase | Error message, exit(1) | None - terminates |
| Cannot create output directory | Validation phase | Error message, exit(1) | None - terminates |
| Excel file deletion fails | File preparation | Error message, return | None - terminates action |
| ruleset.yaml missing | Per-directory | Skip directory | Continue with next directory |
| ruleset.yaml parse error | YAML parsing | printStackTrace, skip directory | Continue with next directory |
| Rule file parse error | YAML parsing | printStackTrace | Continue with next file |
| Excel write error | File writing | printStackTrace | Try to close resources, terminate |

---

## Analyze-Spring Action Workflow

### Purpose
Analyze existing Excel file to identify and mark Spring Framework-specific rules.

### Workflow Overview

```
[CLI Input] → [Argument Parsing] → [Validation] → [Excel Reading] → [Pattern Analysis] → [Excel Writing]
```

### Detailed Process Flow

#### Phase 1: Initialization and Argument Parsing

**Entry Point:** `CliRunner.run(String... args)`  
**Location:** DemoApplication.java, Lines 27-121

**Steps:**
1-3. **Same as Extract Action** (argument parsing)

#### Phase 2: Validation

**Location:** CliRunner.run(), Lines 68-73, 103-114

**Steps:**
4. **Validate Analyze-Spring Requirements** (Lines 68-73)
   - If action equals "analyze-spring" (case-insensitive):
     - Check outputpath not null
     - If null:
       - Print error to stderr
       - Print usage
       - Call System.exit(1)

5. **Validate Output Directory** (Lines 103-114)
   - Create File object for outputPath
   - Check if outputPath exists:
     - If not exists: Error message, System.exit(1)
   - Check if outputPath is directory:
     - If not directory: Error message, System.exit(1)

6. **Print Configuration** (Line 110)
   - Output to stdout: "Output folder path: {outputPath}"

#### Phase 3: Excel File Reading

**Entry Point:** `RulesetToExcel.recognizeSpringRules(outputPath)`  
**Location:** RulesetToExcel.java, Lines 25-78

**Steps:**
7. **Open Excel File** (Lines 26-29)
   - Construct path: `outputPath + "/appcat-ruleset.xlsx"`
   - Create FileInputStream for Excel file
   - Create XSSFWorkbook from FileInputStream
   - Use try-with-resources (auto-closes stream and workbook)

#### Phase 4: Sheet Processing

**Location:** Lines 30-70

**Steps:**
8. **Iterate Through Sheets** (Line 30)
   - For i = 0 to workbook.getNumberOfSheets()-1:
     - Get sheet at index i

9. **Locate Title Row** (Lines 33-34)
   - titleRow = sheet.getRow(1) (row index 1)
   - If titleRow is null: Continue to next sheet

10. **Check for Existing Column** (Lines 35-45)
    - Get lastCol = titleRow.getLastCellNum()
    - Initialize springHeader = null
    - For c = 0 to lastCol-1:
      - Get cell at column c
      - If cell not null and cell value equals "spring specific?" (case-insensitive, trimmed):
        - Set springHeader = cell
        - Break loop
    - Set springHeaderExists = (springHeader != null)

11. **Create Column if Needed** (Lines 47-50)
    - If springHeader is null:
      - springHeader = titleRow.createCell(lastCol)
      - springHeader.setCellValue("spring specific?")

12. **Get Column Index** (Line 51)
    - lastCol = springHeader.getColumnIndex()

#### Phase 5: Row Analysis

**Location:** Lines 52-62

**Steps:**
13. **Iterate Through Data Rows** (Lines 52-53)
    - lastRow = sheet.getLastRowNum()
    - For r = 2 to lastRow:

14. **Get Row and Check Null** (Lines 54-55)
    - row = sheet.getRow(r)
    - If row is null: Continue to next row

15. **Analyze Row for Spring Patterns** (Line 56)
    - result = isSpringSpecificRule(row) ? "Yes" : "No"

16. **Write Result** (Lines 57-58)
    - If springHeaderExists:
      - springCell = row.getCell(lastCol)
    - Else:
      - springCell = row.createCell(lastCol)
    - springCell.setCellValue(result)

17. **Auto-size Column** (Line 61)
    - sheet.autoSizeColumn(lastCol)

#### Phase 6: Spring Pattern Detection

**Method:** `RulesetToExcel.isSpringSpecificRule(Row row)`  
**Location:** RulesetToExcel.java, Lines 80-109

**Steps:**
18. **Null Check** (Line 81)
    - If row is null: Return false

19. **Scan All Cells** (Lines 85-95)
    - Initialize whenCol = -1
    - Get lastCol = row.getLastCellNum()
    - For c = 0 to lastCol-1:
      - Get cell at column c
      - Get cellText = cell.toString().toLowerCase() (or "" if null)
      - If cellText contains "spring":
        - Return true immediately
      - If whenCol == -1:
        - Check if current cell is "When" column by checking title row
        - If title cell equals "when" (case-insensitive):
          - Set whenCol = c

20. **Check "When" Column for Pattern** (Lines 97-103)
    - If whenCol != -1:
      - Get whenCell = row.getCell(whenCol)
      - Get whenVal = whenCell.toString().toLowerCase() (or "" if null)
      - If whenVal contains "properties|":
        - Return true

21. **Return Default** (Line 104)
    - Return false (not Spring-specific)

#### Phase 7: File Writing

**Location:** Lines 71-76

**Steps:**
22. **Write Modified Workbook** (Lines 71-74)
    - Create FileOutputStream for same Excel file
    - Call workbook.write(fos)
    - Call fos.flush()
    - Use try-with-resources (auto-closes stream)

23. **Close Workbook** (Line 75)
    - Explicit workbook.close() to release file lock

24. **Print Success** (Line 76)
    - Output to stdout: "Analyze completed and Excel updated."

25. **Return to CLI**
    - Execution returns to CliRunner.run()
    - CliRunner completes
    - Spring Boot application exits normally

### Pattern Detection Logic

**Spring-Specific Criteria:**

1. **Keyword Matching:**
   - Scan ALL cells in row
   - Convert cell text to lowercase
   - Check if contains "spring"
   - Case-insensitive match

2. **Pattern Matching:**
   - Identify "When" column
   - Check if "When" cell contains "properties|"
   - Indicates Spring property file patterns

**Truth Table:**

| Condition | Result |
|-----------|--------|
| Any cell contains "spring" | Yes (Spring-specific) |
| "When" column contains "properties\|" | Yes (Spring-specific) |
| Neither condition met | No (Not Spring-specific) |
| Row is null | No (skip row) |

### Error Scenarios and Recovery

| Error Condition | Detection Point | Action Taken | Recovery |
|-----------------|-----------------|--------------|----------|
| No arguments | CliRunner start | Error message, usage, exit(1) | None - terminates |
| Missing outputpath | Validation phase | Error message, usage, exit(1) | None - terminates |
| Output directory doesn't exist | Validation phase | Error message, exit(1) | None - terminates |
| Excel file not found | File reading | IOException, printStackTrace | None - terminates |
| Excel file corrupted | File reading | POI exception, printStackTrace | None - terminates |
| Null title row | Sheet processing | Skip sheet | Continue with next sheet |
| Null data row | Row analysis | Skip row | Continue with next row |
| Excel write error | File writing | IOException, printStackTrace | None - terminates |

---

## Workflow Comparison

| Aspect | Extract Action | Analyze-Spring Action |
|--------|----------------|----------------------|
| Input | YAML files in directories | Existing Excel file |
| Output | New Excel file | Modified Excel file |
| Processing | Parse YAML, create rows | Read rows, analyze, update |
| Filtering | Optional directory filters | No filtering |
| File Operations | Read YAML, Write Excel | Read Excel, Write Excel |
| Destructiveness | Deletes existing Excel | Modifies in-place |
| Idempotency | No (overwrites file) | Yes (can run multiple times) |
| Typical Duration | Depends on ruleset size | Depends on rule count |
| Memory Usage | Scales with total rules | Scales with Excel size |

---

## Common Workflow Patterns

### Sequential Execution Pattern

Typical usage combines both actions:

```
Step 1: Extract Action
  Input: /data/rulesets (YAML)
  Output: /data/output/appcat-ruleset.xlsx

Step 2: Analyze-Spring Action
  Input: /data/output/appcat-ruleset.xlsx
  Output: /data/output/appcat-ruleset.xlsx (modified)
```

**Command Sequence:**
```bash
# Step 1: Generate Excel from YAML
java -jar demo.jar rulesetpath=/data/rulesets outputpath=/data/output action=extract

# Step 2: Analyze for Spring rules
java -jar demo.jar outputpath=/data/output action=analyze-spring
```

### Filtered Processing Pattern

Process only specific ruleset categories:

```
Input: /data/rulesets/
  ├── java-rulesets/
  ├── spring-rulesets/
  ├── jee-rulesets/
  └── quarkus-rulesets/

Command: filters=java,spring
Result: Only java-rulesets and spring-rulesets processed
```

---

## Related Documentation

- **[Business Logic](business-logic.md)** - Detailed business rules behind these workflows
- **[Decision Logic](decision-logic.md)** - Decision trees and branching logic
- **[Error Handling](error-handling.md)** - Exception handling patterns
- **[Interfaces](../reference/interfaces.md)** - API contracts for workflow methods
- **[Components](../architecture/components.md)** - Component interactions during workflows

---

*Workflow documentation extracted through static code analysis of control flow, method call sequences, and data transformations. All step numbers reference actual source code locations.*
