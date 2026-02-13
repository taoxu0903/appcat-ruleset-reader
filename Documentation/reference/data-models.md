# Data Models - Type Definitions and Data Structures

## Overview

This document provides comprehensive documentation of all data structures, type definitions, and data transfer objects used in the AppCat Ruleset Reader codebase.

**Primary Data Model:** `RulesetToExcel.RuleData`  
**Purpose:** Transfer object for rule information extracted from YAML files  
**Total Fields:** 7  
**Immutability:** Mutable (no defensive copying, direct field access)

---

## RuleData Class

### Class Definition

**Full Qualified Name:** `com.example.demo.RulesetToExcel.RuleData`  
**File:** `src/main/java/com/example/demo/RulesetToExcel.java` (Lines 336-360)  
**Modifier:** Static inner class  
**Access:** Package-private (no access modifier on class)  
**Purpose:** Data Transfer Object (DTO) holding extracted rule information for Excel export

### Design Pattern

**Pattern:** Data Transfer Object (DTO)  
**Characteristics:**
- Simple data container with no business logic
- Package-private fields (direct access within package)
- Multiple constructors for convenience
- No validation logic
- No getters/setters (fields accessed directly)
- Mutable structure

---

## Field Specifications

### Complete Field List

| Field Name | Type | Access | Nullable | Purpose | Excel Column |
|------------|------|--------|----------|---------|--------------|
| `ruleId` | String | package-private | Yes | Unique rule identifier | Column 0: RuleID |
| `when` | String | package-private | Yes | Serialized conditional logic (YAML) | Column 1: When |
| `mergedDescription` | String | package-private | Yes | Combined description and message | Column 2: Description & Message |
| `source` | String | package-private | Yes | Source platform(s) from labels | Column 3: Source |
| `target` | String | package-private | Yes | Target platform(s) from labels | Column 4: Target |
| `domain` | String | package-private | Yes | Domain classification | Column 5: Domain |
| `category` | String | package-private | Yes | Category classification | Column 6: Category |

---

## Field Details

### ruleId

**Declaration:**
```java
String ruleId;
```

**Location:** Line 337  
**Type:** String  
**Access:** Package-private  
**Nullability:** Nullable  
**Source:** Extracted from YAML field `ruleID`

**Purpose:**
Unique identifier for the migration rule. Used as the primary key to reference specific rules.

**Expected Format:**
- Alphanumeric string with optional hyphens/underscores
- Example: `java-001`, `spring-boot-config-001`

**Populated By:**
`RulesetToExcel.extractRuleData()` - Direct assignment from YAML Map

**Usage:**
Written to Excel Column 0 (RuleID)

---

### when

**Declaration:**
```java
String when;
```

**Location:** Line 338  
**Type:** String  
**Access:** Package-private  
**Nullability:** Nullable  
**Source:** Extracted from YAML field `when`, serialized to string

**Purpose:**
Stores the conditional logic that determines when this rule applies. Contains YAML-serialized representation of complex conditional expressions.

**Format:**
YAML block style with 2-space indentation, may contain:
- Property patterns (e.g., `properties|spring.application.name`)
- File patterns
- Code patterns
- Logical operators (and, or)
- Nested structures

**Example Values:**
```yaml
builtin.file:
  pattern: ".*\\.xml$"
```

**Processing:**
- Input: Object from YAML (could be Map, List, or primitive)
- Transformation: `RulesetToExcel.serializeWhen(whenObj)` converts to YAML string
- Configuration: BLOCK flow style, 2-space indent, pretty flow enabled

**Populated By:**
`RulesetToExcel.extractRuleData()` via `serializeWhen()` helper method

**Usage:**
- Written to Excel Column 1 (When)
- Analyzed by `isSpringSpecificRule()` to detect "properties|" pattern
- Used for Spring-specific rule detection

---

### mergedDescription

**Declaration:**
```java
String mergedDescription;
```

**Location:** Line 339  
**Type:** String  
**Access:** Package-private  
**Nullability:** Nullable  
**Source:** Combination of YAML fields `description` and `message`

**Purpose:**
Human-readable explanation of what the rule detects and what action should be taken. Combines separate description and message fields into single text block.

**Format:**
- Plain text, potentially multi-line
- If both description and message exist: `description + "\n" + message`
- If only one exists: that field's value
- If neither exists: empty string

**Processing:**
1. Extract `description` field from YAML (may be null)
2. Extract `message` field from YAML (may be null)
3. Trim both values
4. Merge using `RulesetToExcel.mergeDescriptionAndMessage(desc, msg)`

**Merge Logic:**
```java
if (!desc.isEmpty() && !msg.isEmpty()) {
    return desc + "\n" + msg;
} else {
    return desc + msg;
}
```

**Example Values:**
```
Description: Spring Boot configuration file detected
Message: Migrate spring.application.name to application.properties
```

**Populated By:**
`RulesetToExcel.extractRuleData()` via `mergeDescriptionAndMessage()` helper method

**Usage:**
Written to Excel Column 2 (Description & Message) with word wrap enabled for readability

---

### source

**Declaration:**
```java
String source;
```

**Location:** Line 340  
**Type:** String  
**Access:** Package-private  
**Nullability:** Nullable  
**Source:** Extracted from YAML `labels` array, prefix `konveyor.io/source=`

**Purpose:**
Identifies the source platform(s) or technology stack that this rule applies to (e.g., what you're migrating FROM).

**Format:**
- Single value: `"java"`
- Multiple values: `"java, spring-boot, hibernate"` (comma-space separated)

**Extraction Logic:**
1. Iterate through `labels` array in YAML
2. Find labels starting with `konveyor.io/source=`
3. Extract value after prefix
4. If multiple source labels exist, concatenate with ", " separator

**Example YAML:**
```yaml
labels:
  - konveyor.io/source=java
  - konveyor.io/source=spring-boot
```

**Resulting value:** `"java, spring-boot"`

**Populated By:**
`RulesetToExcel.extractRuleData()` - Label parsing loop

**Usage:**
Written to Excel Column 3 (Source)

---

### target

**Declaration:**
```java
String target;
```

**Location:** Line 341  
**Type:** String  
**Access:** Package-private  
**Nullability:** Nullable  
**Source:** Extracted from YAML `labels` array, prefix `konveyor.io/target=`

**Purpose:**
Identifies the target platform(s) or technology stack for migration (e.g., what you're migrating TO).

**Format:**
- Single value: `"quarkus"`
- Multiple values: `"quarkus, jakarta-ee"` (comma-space separated)

**Extraction Logic:**
Same as source field, but looks for `konveyor.io/target=` prefix

**Example YAML:**
```yaml
labels:
  - konveyor.io/target=quarkus
  - konveyor.io/target=jakarta-ee
```

**Resulting value:** `"quarkus, jakarta-ee"`

**Populated By:**
`RulesetToExcel.extractRuleData()` - Label parsing loop

**Usage:**
Written to Excel Column 4 (Target)

---

### domain

**Declaration:**
```java
String domain;
```

**Location:** Line 342  
**Type:** String  
**Access:** Package-private  
**Nullability:** Nullable  
**Source:** Extracted from YAML `labels` array, prefix `domain=`

**Purpose:**
Classifies the rule by technical domain or functional area (e.g., security, configuration, persistence).

**Format:**
- Single value: `"configuration"`
- Multiple values: `"configuration, security"` (comma-space separated)

**Extraction Logic:**
1. Iterate through `labels` array in YAML
2. Find labels starting with `domain=`
3. Extract value after prefix
4. Concatenate multiple values with ", "

**Example Values:**
- `"configuration"`
- `"persistence"`
- `"security"`
- `"web"`

**Populated By:**
`RulesetToExcel.extractRuleData()` - Label parsing loop

**Usage:**
Written to Excel Column 5 (Domain)

---

### category

**Declaration:**
```java
String category;
```

**Location:** Line 343  
**Type:** String  
**Access:** Package-private  
**Nullability:** Nullable  
**Source:** Extracted from YAML `labels` array, prefix `category=`

**Purpose:**
Further categorizes the rule within its domain (sub-classification for organizational purposes).

**Format:**
- Single value: `"mandatory"`
- Multiple values: `"mandatory, high-priority"` (comma-space separated)

**Extraction Logic:**
Same as domain field, but looks for `category=` prefix

**Example Values:**
- `"mandatory"`
- `"optional"`
- `"potential"`

**Populated By:**
`RulesetToExcel.extractRuleData()` - Label parsing loop

**Usage:**
Written to Excel Column 6 (Category)

---

## Constructors

### Three-Parameter Constructor

**Signature:**
```java
RuleData(String ruleId, String when, String mergedDescription)
```

**Location:** Lines 345-347  
**Access:** Package-private  
**Purpose:** Convenience constructor for core fields only

**Parameters:**
- `ruleId` - Rule identifier
- `when` - Conditional logic
- `mergedDescription` - Combined description and message

**Behavior:**
Delegates to full constructor with empty strings for label fields:
```java
this(ruleId, when, mergedDescription, "", "", "", "");
```

**Usage Example:**
```java
RuleData rule = new RuleData("rule-001", "when condition", "description");
// source, target, domain, category all initialized to ""
```

---

### Seven-Parameter Constructor

**Signature:**
```java
RuleData(String ruleId, String when, String mergedDescription, 
         String source, String target, String domain, String category)
```

**Location:** Lines 348-356  
**Access:** Package-private  
**Purpose:** Full constructor initializing all fields

**Parameters:**
1. `ruleId` - Rule identifier
2. `when` - Conditional logic (YAML string)
3. `mergedDescription` - Combined description and message
4. `source` - Source platform(s)
5. `target` - Target platform(s)
6. `domain` - Domain classification
7. `category` - Category classification

**Behavior:**
Direct assignment of all parameters to corresponding fields:
```java
this.ruleId = ruleId;
this.when = when;
this.mergedDescription = mergedDescription;
this.source = source;
this.target = target;
this.domain = domain;
this.category = category;
```

**Validation:** None (accepts null or empty strings for any parameter)

**Usage Example:**
```java
RuleData rule = new RuleData(
    "spring-001",
    "properties|spring.application.name",
    "Spring application name property detected",
    "spring-boot",
    "quarkus",
    "configuration",
    "mandatory"
);
```

---

## Data Flow

### Creation Flow

```
YAML File
   ↓ (SnakeYAML parsing)
Map<String, Object>
   ↓ (RulesetToExcel.extractRuleData())
RuleData Constructor
   ↓
RuleData Instance
   ↓ (added to List<RuleData>)
Excel Row
   ↓ (Apache POI)
Excel File
```

### Field Population Sequence

```
1. YAML Parsing:
   - ruleID → ruleId field
   - when (Object) → serializeWhen() → when field
   - description + message → mergeDescriptionAndMessage() → mergedDescription field
   - labels array → label parsing loop → source, target, domain, category fields

2. Object Creation:
   - All 7 fields populated
   - new RuleData(...) called
   - Instance added to rules List

3. Excel Writing:
   - Each field written to corresponding column
   - Word wrap applied to all cells
   - Column widths configured
```

---

## Usage Patterns

### Creation Pattern

```java
// In RulesetToExcel.extractRuleData():
String ruleId = (String) ruleData.get("ruleID");
Object whenObj = ruleData.get("when");
String when = serializeWhen(whenObj);
String desc = (String) ruleData.get("description");
String message = (String) ruleData.get("message");
String merged = mergeDescriptionAndMessage(desc, message);

// ... label parsing ...

rules.add(new RuleData(ruleId, when, merged, source, target, domain, category));
```

### Usage Pattern

```java
// In RulesetToExcel.processSubDirectory():
for (RuleData rule : rules) {
    Row dataRow = sheet.createRow(rowNum++);
    dataRow.createCell(0).setCellValue(rule.ruleId);
    dataRow.createCell(1).setCellValue(rule.when);
    dataRow.createCell(2).setCellValue(rule.mergedDescription);
    dataRow.createCell(3).setCellValue(rule.source);
    dataRow.createCell(4).setCellValue(rule.target);
    dataRow.createCell(5).setCellValue(rule.domain);
    dataRow.createCell(6).setCellValue(rule.category);
}
```

---

## Data Validation

### Current State

**No validation is performed:**
- ✗ No null checks on constructor parameters
- ✗ No empty string validation
- ✗ No format validation for ruleId
- ✗ No YAML syntax validation for when field
- ✗ No length limits enforced

### Implications

**Possible Data Issues:**
1. Null values can be passed and stored
2. Empty strings treated same as meaningful data
3. Invalid YAML in when field not detected
4. Excel cells may contain null or empty values
5. No data integrity guarantees

### Recommendations

For production use, consider adding:
- Constructor validation (null checks)
- Field length limits
- Format validation for structured fields
- Builder pattern for safer construction
- Immutability (final fields, defensive copying)

---

## Memory Characteristics

### Object Size

**Estimated heap usage per RuleData instance:**
- Object header: ~16 bytes
- 7 String references: 7 × 8 = 56 bytes (64-bit JVM)
- String content: Variable (depends on actual string lengths)

**Typical instance:** ~200-500 bytes including string content

**Memory Scaling:**
- 1,000 rules ≈ 0.2-0.5 MB
- 10,000 rules ≈ 2-5 MB
- 100,000 rules ≈ 20-50 MB

### Lifecycle

1. **Created:** During YAML parsing in extractRuleData()
2. **Stored:** Added to ArrayList in processYamlFile()
3. **Used:** Iterated during Excel writing in processSubDirectory()
4. **Destroyed:** Garbage collected after processSubDirectory() completes (List goes out of scope)

**Lifetime:** Short-lived, exists only during single ruleset processing

---

## Thread Safety

**Status:** Not thread-safe

**Concerns:**
- Mutable fields (can be modified after construction)
- No synchronization
- Package-private access allows external modification
- Direct field access pattern

**Recommendation:** Use only from single thread (current usage pattern is safe)

---

## Related Documentation

- **[Program Structure](program-structure.md)** - RuleData in context of RulesetToExcel
- **[Interfaces](interfaces.md)** - How RuleData is used in public APIs
- **[Business Logic](../behavior/business-logic.md)** - Data transformation logic
- **[Excel Processing](../specialized/excel-processing/excel-schema.md)** - Excel mapping details
- **[YAML Processing](../specialized/yaml-processing/ruleset-schema.md)** - Source YAML structure

---

*This document provides complete specification of the RuleData data model based on static code analysis. All field information, constructors, and usage patterns documented from source code at src/main/java/com/example/demo/RulesetToExcel.java lines 336-360.*
