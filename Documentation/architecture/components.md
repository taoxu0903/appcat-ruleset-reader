# Components - Major Components and Their Responsibilities

## Overview

This document describes the major components of the AppCat Ruleset Reader application, their responsibilities, interactions, and architectural roles.

**Total Components:** 3 major functional components  
**Architecture Style:** Layered utility architecture  
**Component Pattern:** Static utility with Spring Boot integration

---

## Component Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    User / Shell                         │
└──────────────────────┬──────────────────────────────────┘
                       │ command-line arguments
                       ↓
┌─────────────────────────────────────────────────────────┐
│              Component 1: DemoApplication               │
│  ┌────────────────────────────────────────────────────┐ │
│  │  - SpringBootApplication Entry Point               │ │
│  │  - Static configuration storage                    │ │
│  │  - Public accessors                                │ │
│  │  ┌──────────────────────────────────────────────┐  │ │
│  │  │  Inner: CliRunner (CommandLineRunner)        │  │ │
│  │  │  - Argument parsing                           │  │ │
│  │  │  - Validation logic                           │  │ │
│  │  │  - Action orchestration                       │  │ │
│  │  └──────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────┘
                       │ invokes static methods
                       ↓
┌─────────────────────────────────────────────────────────┐
│            Component 2: RulesetToExcel                  │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Public API:                                       │ │
│  │  - execute() - Extract action                     │ │
│  │  - recognizeSpringRules() - Analyze action        │ │
│  │                                                    │ │
│  │  Private Processing:                              │ │
│  │  - processRulesetFolder()                         │ │
│  │  - processSubDirectory()                          │ │
│  │  - processYamlFile()                              │ │
│  │  - extractRuleData()                              │ │
│  │  - serializeWhen()                                │ │
│  │  - mergeDescriptionAndMessage()                   │ │
│  │  - isSpringSpecificRule()                         │ │
│  │  ┌──────────────────────────────────────────────┐  │ │
│  │  │  Inner: RuleData (DTO)                       │  │ │
│  │  │  - 7 fields for rule information             │  │ │
│  │  └──────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────┘ │
└──────────┬──────────────────────┬──────────────────────┘
           │ reads                │ writes
           ↓                      ↓
    ┌─────────────┐      ┌──────────────┐
    │  File       │      │  File        │
    │  System     │      │  System      │
    │  (YAML)     │      │  (Excel)     │
    └─────────────┘      └──────────────┘
           ↑                      ↑
           │ uses                 │ uses
    ┌─────────────┐      ┌──────────────┐
    │ SnakeYAML   │      │ Apache POI   │
    │ Library     │      │ Library      │
    └─────────────┘      └──────────────┘
```

---

## Component 1: DemoApplication

### Identity

**Class:** `com.example.demo.DemoApplication`  
**File:** `src/main/java/com/example/demo/DemoApplication.java`  
**Lines:** 132 lines  
**Type:** Spring Boot Application Entry Point  
**Pattern:** Main class with static configuration holder

### Primary Responsibilities

1. **Application Bootstrapping**
   - Provides main() method as JVM entry point
   - Launches Spring Boot ApplicationContext
   - Triggers Spring Boot auto-configuration

2. **Configuration Management**
   - Stores parsed command-line arguments in static fields
   - Provides public accessors for configuration values
   - Maintains action constants (ACTION_EXTRACT, ACTION_ANALYZE_SPRING)

3. **Component Hosting**
   - Contains CliRunner as inner static class
   - Enables Spring component scanning for CliRunner

### Key Features

**Annotations:**
- `@SpringBootApplication` - Enables Spring Boot features

**Static Fields:**
- `rulesetPath: String` - Ruleset directory path
- `outputPath: String` - Output directory path
- `filters: List<String>` - Optional directory filters
- `action: String` - Action type (extract or analyze-spring)

**Public Constants:**
- `ACTION_EXTRACT = "extract"`
- `ACTION_ANALYZE_SPRING = "analyze-spring"`

**Public Methods:**
- `main(String[] args)` - Application entry point
- `getRulesetPath()` - Accessor for rulesetPath
- `getOutputPath()` - Accessor for outputPath
- `getFilters()` - Accessor for filters

### Inner Component: CliRunner

**Type:** Inner static class implementing CommandLineRunner  
**Annotations:** `@Component`  
**Lines:** ~95 lines (Lines 26-121)

**Responsibilities:**
1. Parse command-line arguments (key=value format)
2. Validate required arguments for each action
3. Validate file system paths (existence, accessibility)
4. Orchestrate action execution (call RulesetToExcel methods)
5. Handle errors (validation failures, missing arguments)

**Key Method:** `run(String... args)` - Main processing logic

### Interactions

**Outbound:**
- Calls `SpringApplication.run()` - Spring Boot framework
- Calls `RulesetToExcel.execute()` - For extract action
- Calls `RulesetToExcel.recognizeSpringRules()` - For analyze-spring action
- Writes to `System.out` and `System.err`
- Calls `System.exit(1)` on errors

**Inbound:**
- Invoked by JVM (main method)
- Invoked by Spring Boot (CliRunner.run)
- Accessed by other classes via public getters (rare usage)

### Technical Characteristics

**Statefulness:** Stateful (maintains static configuration)  
**Lifecycle:** Singleton (single instance per JVM)  
**Thread Safety:** Not thread-safe (mutable static state)  
**Testability:** Poor (static methods, System.exit calls)

---

## Component 2: RulesetToExcel

### Identity

**Class:** `com.example.demo.RulesetToExcel`  
**File:** `src/main/java/com/example/demo/RulesetToExcel.java`  
**Lines:** 388 lines  
**Type:** Static utility class  
**Pattern:** Utility class with static methods

### Primary Responsibilities

1. **YAML Processing**
   - Parse ruleset.yaml files for metadata
   - Parse rule YAML files for rule definitions
   - Serialize complex when clauses to YAML strings
   - Extract and parse labels arrays

2. **Data Extraction**
   - Extract rule fields (ruleID, when, description, message)
   - Parse labels for source/target/domain/category
   - Merge description and message fields
   - Create RuleData objects

3. **Excel Generation (Extract Action)**
   - Create XLSX workbooks with multiple sheets
   - Populate sheets with ruleset data
   - Apply formatting (column widths, word wrap)
   - Write files to specified output directory

4. **Excel Analysis (Analyze-Spring Action)**
   - Read existing XLSX files
   - Analyze rows for Spring-specific patterns
   - Add/update "spring specific?" column
   - Write modified files back

5. **Business Logic**
   - Implement directory filtering logic
   - Implement Spring pattern detection
   - Manage Excel file deletion and recreation
   - Handle file system operations

### Key Features

**Public API (3 methods):**
- `execute(String, String)` - Extract without filters
- `execute(String, String, List<String>)` - Extract with filters
- `recognizeSpringRules(String)` - Analyze Spring patterns

**Private Processing Methods (6 methods):**
- `processRulesetFolder()` - Directory iteration with filtering
- `processSubDirectory()` - Single ruleset processing
- `processYamlFile()` - YAML file parsing
- `extractRuleData()` - Rule field extraction
- `serializeWhen()` - YAML serialization
- `mergeDescriptionAndMessage()` - Text merging
- `isSpringSpecificRule()` - Pattern detection

**Data Model:**
- Inner class `RuleData` - Rule information container

### Sub-Component: RuleData

**Type:** Inner static class (DTO)  
**Lines:** ~25 lines (Lines 336-360)  
**Fields:** 7 (ruleId, when, mergedDescription, source, target, domain, category)  
**Constructors:** 2 (3-param convenience, 7-param full)

**Responsibility:** Hold extracted rule data for Excel export

### Interactions

**Outbound:**
- Uses `Yaml` (SnakeYAML) - YAML parsing
- Uses `XSSFWorkbook`, `Sheet`, `Row`, `Cell` (Apache POI) - Excel manipulation
- Uses `FileInputStream`, `FileOutputStream` - File I/O
- Uses `File` - File system operations
- Writes to `System.out` and `System.err`

**Inbound:**
- Called by `CliRunner.run()` - Action execution
- May be called directly by other code (public static API)

### Processing Patterns

**Extract Action Flow:**
```
execute()
  → Delete existing Excel
  → Create workbook
  → processRulesetFolder()
      → For each subdirectory (filtered):
          → processSubDirectory()
              → Parse ruleset.yaml
              → processYamlFile() for each rule file
                  → extractRuleData()
                      → Create RuleData objects
              → Create Excel sheet
              → Write rows from RuleData list
  → Write workbook to file
```

**Analyze-Spring Action Flow:**
```
recognizeSpringRules()
  → Open Excel file
  → For each sheet:
      → Find/create "spring specific?" column
      → For each data row:
          → isSpringSpecificRule()
              → Check for "spring" keyword
              → Check for "properties|" pattern
          → Write "Yes" or "No"
      → Auto-size column
  → Write workbook back to file
```

### Technical Characteristics

**Statefulness:** Stateless (all static methods, no instance fields)  
**Lifecycle:** N/A (never instantiated)  
**Thread Safety:** Not thread-safe (file system operations)  
**Testability:** Poor (static methods, file system coupling)  
**Complexity:** High (388 lines, multiple responsibilities)

---

## Component 3: External Libraries (Virtual Component)

### Spring Boot Framework

**Version:** 3.2.3  
**Role:** Application infrastructure  
**Responsibilities:**
- Application context management
- Component scanning and instantiation
- CommandLineRunner execution
- Auto-configuration
- Logging infrastructure (unused - direct System.out used instead)

**Used By:** DemoApplication

### Apache POI

**Version:** 5.2.5  
**Role:** Excel document manipulation  
**Responsibilities:**
- XLSX file creation (XSSFWorkbook)
- Sheet, row, cell manipulation
- Style application (word wrap, column widths)
- File reading and writing

**Used By:** RulesetToExcel

### SnakeYAML

**Version:** 2.2  
**Role:** YAML parsing and serialization  
**Responsibilities:**
- Parse YAML files to Java objects (Map, List)
- Serialize Java objects to YAML strings
- Configure dump options (flow style, indentation)

**Used By:** RulesetToExcel

---

## Component Interactions

### Call Sequence: Extract Action

```
User → CLI
CLI → main()
main() → SpringApplication.run()
SpringApplication → CliRunner.run()
CliRunner.run() → Parse arguments
CliRunner.run() → Validate inputs
CliRunner.run() → RulesetToExcel.execute()
RulesetToExcel.execute() → processRulesetFolder()
processRulesetFolder() → processSubDirectory() [per directory]
processSubDirectory() → SnakeYAML.load() [ruleset.yaml]
processSubDirectory() → processYamlFile() [per rule file]
processYamlFile() → SnakeYAML.load() [rule.yaml]
processYamlFile() → extractRuleData()
extractRuleData() → Create RuleData instances
processSubDirectory() → Apache POI create sheet
processSubDirectory() → Apache POI write rows
RulesetToExcel.execute() → Apache POI write file
CliRunner.run() → Print success
CliRunner.run() → Return to Spring Boot
Spring Boot → Exit
```

### Call Sequence: Analyze-Spring Action

```
User → CLI
CLI → main()
main() → SpringApplication.run()
SpringApplication → CliRunner.run()
CliRunner.run() → Parse arguments
CliRunner.run() → Validate inputs
CliRunner.run() → RulesetToExcel.recognizeSpringRules()
recognizeSpringRules() → Apache POI read Excel
recognizeSpringRules() → For each sheet:
recognizeSpringRules() → For each row:
recognizeSpringRules() → isSpringSpecificRule()
isSpringSpecificRule() → Check patterns
recognizeSpringRules() → Write result
recognizeSpringRules() → Apache POI write Excel
CliRunner.run() → Print success
CliRunner.run() → Return to Spring Boot
Spring Boot → Exit
```

---

## Component Dependencies

### Dependency Matrix

| Component | Depends On | Dependency Type |
|-----------|------------|-----------------|
| DemoApplication | Spring Boot Framework | Framework (required) |
| CliRunner | RulesetToExcel | Direct static calls |
| CliRunner | Java I/O (File) | Standard library |
| RulesetToExcel | Apache POI | Library (required) |
| RulesetToExcel | SnakeYAML | Library (required) |
| RulesetToExcel | Java I/O | Standard library |
| RuleData | None | Standalone DTO |

### Architectural Layers

```
┌──────────────────────────────────┐
│   Presentation Layer             │
│   - DemoApplication              │
│   - CliRunner                    │
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│   Business Logic Layer           │
│   - RulesetToExcel               │
│   - Processing methods           │
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│   Data Access Layer              │
│   - File System I/O              │
│   - Apache POI (Excel)           │
│   - SnakeYAML (YAML)             │
└──────────────────────────────────┘
```

**Note:** Layers are logical, not enforced by architecture. Direct dependencies exist across layers.

---

## Component Quality Metrics

### DemoApplication

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 132 | Good - concise |
| Public Methods | 4 | Good - minimal API |
| Complexity | Low | Good - simple orchestration |
| Cohesion | High | Good - single purpose |
| Coupling | Medium | Acceptable - depends on RulesetToExcel |
| Testability | Poor | Bad - static state, System.exit |

### RulesetToExcel

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 388 | Fair - large but manageable |
| Public Methods | 3 | Good - minimal API |
| Private Methods | 7 | Fair - good decomposition |
| Complexity | High | Concerning - long methods |
| Cohesion | Medium | Fair - multiple related responsibilities |
| Coupling | High | Concerning - tight library coupling |
| Testability | Poor | Bad - static methods, file I/O |

### RuleData

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 25 | Excellent - tiny |
| Fields | 7 | Good - complete data model |
| Complexity | Very Low | Excellent - simple DTO |
| Cohesion | Very High | Excellent - pure data |
| Coupling | None | Excellent - no dependencies |
| Testability | Good | Good - simple construction |

---

## Component Evolution Recommendations

### Short-Term Improvements

1. **Extract Interfaces**
   - `RulesetProcessor` interface for RulesetToExcel
   - Enable mocking and testing

2. **Dependency Injection**
   - Make RulesetToExcel injectable service
   - Remove static methods

3. **Separate Configuration**
   - Extract configuration to separate class
   - Remove static state from DemoApplication

### Long-Term Refactoring

1. **Service Layer**
   - Extract business logic to service classes
   - Separate Excel, YAML, and filtering concerns

2. **Repository Pattern**
   - Abstract file system access
   - Enable testing with mock repositories

3. **Command Pattern**
   - Extract actions to separate command classes
   - Enable extensibility for new actions

---

## Related Documentation

- **[Program Structure](../reference/program-structure.md)** - Detailed class structure
- **[Dependencies](dependencies.md)** - External dependency details
- **[System Overview](system-overview.md)** - High-level architecture
- **[Design Patterns](patterns.md)** - Pattern analysis
- **[Business Logic](../behavior/business-logic.md)** - Component behaviors

---

*This document describes the major functional components and their interactions based on static code analysis. Component boundaries and responsibilities extracted from source code structure and method analysis.*
