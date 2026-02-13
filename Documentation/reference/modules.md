# Modules - Package Organization and Structure

## Overview

This document describes the module and package organization of the AppCat Ruleset Reader codebase.

**Module Type:** Single-module Maven project  
**Package Structure:** Simple flat structure with single package  
**Total Packages:** 1  
**Organization:** All classes in `com.example.demo` package

---

## Module Structure

### Maven Module

**Artifact Coordinates:**
- **GroupId:** `com.example`
- **ArtifactId:** `demo`
- **Version:** `0.0.1-SNAPSHOT`
- **Packaging:** `jar`

**Parent POM:**
- **GroupId:** `org.springframework.boot`
- **ArtifactId:** `spring-boot-starter-parent`
- **Version:** `3.2.3`

**Build Output:**
- **JAR File:** `target/demo-0.0.1-SNAPSHOT.jar`
- **Type:** Executable Spring Boot JAR (includes dependencies)

---

## Package Structure

### com.example.demo

**Purpose:** Main application package containing all application code  
**Location:** `src/main/java/com/example/demo/`  
**Classes:** 4 total (2 top-level, 2 inner)

#### Package Contents

```
com.example.demo
├── DemoApplication.java
│   ├── DemoApplication (class) - Application entry point
│   └── CliRunner (inner class) - Command-line runner
└── RulesetToExcel.java
    ├── RulesetToExcel (class) - YAML to Excel utility
    └── RuleData (inner class) - Rule data transfer object
```

#### Class Distribution

| File | Top-Level Classes | Inner Classes | Total Classes |
|------|-------------------|---------------|---------------|
| DemoApplication.java | 1 | 1 (CliRunner) | 2 |
| RulesetToExcel.java | 1 | 1 (RuleData) | 2 |
| **Total** | **2** | **2** | **4** |

#### Package Responsibilities

**DemoApplication.java:**
- Application bootstrapping
- Command-line argument processing
- Spring Boot integration
- Action orchestration

**RulesetToExcel.java:**
- YAML parsing and processing
- Excel generation and manipulation
- Business logic implementation
- Data extraction and transformation

---

## Source Directory Structure

### Complete Directory Tree

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── demo/
│   │               ├── DemoApplication.java (132 lines)
│   │               └── RulesetToExcel.java (388 lines)
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── (no test files present)
```

### Source Statistics

| Directory | Files | Lines of Code | Purpose |
|-----------|-------|---------------|---------|
| src/main/java/com/example/demo | 2 | 520 | Application source code |
| src/main/resources | 1 | ~15 | Configuration files |
| src/test/java | 0 | 0 | Unit tests (none present) |

---

## Package Dependencies

### Internal Dependencies

**Within com.example.demo:**
```
DemoApplication → RulesetToExcel
  - CliRunner.run() calls RulesetToExcel.execute()
  - CliRunner.run() calls RulesetToExcel.recognizeSpringRules()

RulesetToExcel → RuleData
  - Creates RuleData instances
  - Stores in List<RuleData>
```

**Dependency Direction:** One-way  
**Cyclic Dependencies:** None  
**Coupling:** Tight (direct static method calls)

### External Dependencies

**Spring Boot Framework:**
```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
```

**Apache POI (Excel):**
```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
```

**SnakeYAML:**
```java
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
```

**Java Standard Library:**
```java
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
```

---

## Module Characteristics

### Cohesion

**Package Cohesion:** High  
- All classes work together toward single goal (ruleset processing)
- Clear separation of concerns (entry point vs processing logic)
- Focused on command-line ruleset processing

**Class Cohesion:** Mixed
- DemoApplication: High cohesion (focused on CLI orchestration)
- RulesetToExcel: Moderate cohesion (multiple related responsibilities)

### Coupling

**Inter-class Coupling:** Tight
- Static method calls between classes
- Direct dependencies (no interfaces)
- No dependency injection for core logic

**External Coupling:** Moderate
- Depends on specific versions of libraries
- Spring Boot framework tightly coupled
- Apache POI and SnakeYAML APIs directly used

### Modularity

**Current State:** Monolithic single-module design

**Potential Modularization:**
```
Possible future structure:
- appcat-core: RuleData, interfaces
- appcat-yaml: YAML processing logic
- appcat-excel: Excel generation logic
- appcat-cli: Command-line interface
```

---

## Package Naming Conventions

### Current Naming

**Package:** `com.example.demo`  
**Assessment:** Non-production naming convention

**Issues:**
- "example" suggests sample/demo code
- "demo" suggests temporary/non-production
- Not descriptive of actual functionality
- Inappropriate for production deployment

### Recommended Naming

**Better alternatives:**
- `com.appcat.ruleset.reader`
- `com.redhat.appcat.tools`
- `com.migration.ruleset.processor`

**Benefits:**
- Professional naming
- Descriptive of functionality
- Follows Java naming conventions
- Suitable for production use

---

## Resource Files

### application.properties

**Location:** `src/main/resources/application.properties`  
**Purpose:** Spring Boot configuration  
**Size:** ~15 lines  

**Contents:**
- Server configuration (unused for CLI)
- Application name
- Logging configuration
- Actuator configuration (unnecessary for CLI)
- DevTools configuration (should be disabled in production)

**Issues:** See [Technical Debt Report](../technical-debt-report.md) for configuration concerns

---

## Build Configuration

### Maven Dependencies

**Declared in pom.xml:**

| Dependency | Group ID | Artifact ID | Version | Scope |
|------------|----------|-------------|---------|-------|
| Spring Boot Starter | org.springframework.boot | spring-boot-starter | (inherited) | compile |
| SnakeYAML | org.yaml | snakeyaml | 2.2 | compile |
| Apache POI | org.apache.poi | poi | 5.2.5 | compile |
| Apache POI OOXML | org.apache.poi | poi-ooxml | 5.2.5 | compile |

**Transitive Dependencies:** ~50+ from Spring Boot parent

### Build Plugins

**Spring Boot Maven Plugin:**
- Packages executable JAR with dependencies
- Enables `java -jar` execution
- Configures Spring Boot application

**Compiler Plugin:**
- Java version: 17
- Source: 17
- Target: 17

---

## Module Deployment

### JAR Structure

**Produced JAR:** `demo-0.0.1-SNAPSHOT.jar`

**Contents:**
```
demo-0.0.1-SNAPSHOT.jar
├── BOOT-INF/
│   ├── classes/
│   │   ├── com/example/demo/
│   │   │   ├── DemoApplication.class
│   │   │   ├── DemoApplication$CliRunner.class
│   │   │   ├── RulesetToExcel.class
│   │   │   └── RulesetToExcel$RuleData.class
│   │   └── application.properties
│   └── lib/
│       └── (all dependency JARs)
├── META-INF/
│   └── MANIFEST.MF
└── org/springframework/boot/loader/
    └── (Spring Boot loader classes)
```

### Execution

**Command:**
```bash
java -jar demo-0.0.1-SNAPSHOT.jar [arguments]
```

**Classpath:** Embedded (all dependencies included in JAR)

---

## Module Evolution Recommendations

### Short-Term

1. **Rename package** from `com.example.demo` to descriptive name
2. **Add test module** with `src/test/java` structure
3. **Separate resources** for different profiles (dev, prod)

### Medium-Term

1. **Extract interfaces** for testability
2. **Add service layer** separating CLI from business logic
3. **Create separate configuration class**

### Long-Term

1. **Multi-module structure:**
   - Core module (domain, interfaces)
   - YAML module (parsing logic)
   - Excel module (generation logic)
   - CLI module (command-line interface)
2. **Shared library** for reuse in other tools
3. **Plugin architecture** for extensibility

---

## Related Documentation

- **[Program Structure](program-structure.md)** - Detailed class structure
- **[Components](../architecture/components.md)** - Component responsibilities
- **[Dependencies](../architecture/dependencies.md)** - Detailed dependency analysis
- **[Build Configuration](../specialized/maven/build-configuration.md)** - Maven build details
- **[Technical Debt](../technical-debt/maintenance-burden.md)** - Package naming issues

---

*This document describes the current single-module, single-package structure of the application. For more details on Maven configuration and build process, see the specialized Maven documentation.*
