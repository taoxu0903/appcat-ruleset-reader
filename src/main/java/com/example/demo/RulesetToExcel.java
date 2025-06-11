package com.example.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RulesetToExcel {

    /**
     * Analyze function for action=analyze.
     * Logic:
     * 1. For each sheet in the Excel file at outputPath:
     *    - Add a new column "spring specific?".
     *    - For each row, check the "When" column. If it contains "spring" or "properties",
     *      mark "spring specific?" as "Yes", otherwise "No".
     * 2. Save the modified Excel file back to outputPath.
     */
    public static void recognizeSpringRules(String outputPath) {
        String excelFile = outputPath + "/appcat-ruleset.xlsx";
        // Use a single try-with-resources block for both input and workbook
        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);

                // Find the "When" column index from the title row (assume it's the second row, index 1)
                Row titleRow = sheet.getRow(1);
                if (titleRow == null) continue;
                int lastCol = titleRow.getLastCellNum();

                // if "spring specific?" column already exists, skip adding it and refer to it by springHeader cell; 
                // otherwise, create it and refer to it by springHeader cell.
                Cell springHeader = null;
                for (int c = 0; c < lastCol; c++) {
                    Cell cell = titleRow.getCell(c);
                    if (cell != null && "spring specific?".equalsIgnoreCase(cell.getStringCellValue().trim())) {
                        springHeader = cell;
                        break;
                    }
                }
                Boolean springHeaderExists = (springHeader != null);
                // If "spring specific?" column does not exist, create it.
                if (springHeader == null) {
                    springHeader = titleRow.createCell(lastCol);
                    springHeader.setCellValue("spring specific?");
                }
                lastCol = springHeader.getColumnIndex();
                int lastRow = sheet.getLastRowNum();
                for (int r = 2; r <= lastRow; r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    String result = isSpringSpecificRule(row) ? "Yes" : "No";
                    Cell springCell = springHeaderExists ? row.getCell(lastCol) : row.createCell(lastCol);
                    springCell.setCellValue(result);
                }
                // Optionally, auto-size the new column
                sheet.autoSizeColumn(lastCol);
            }

            // Save and close workbook and output stream in try-with-resources
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
                fos.flush();
            }
            workbook.close(); // Explicitly close workbook to release file lock
            System.out.println("Analyze completed and Excel updated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isSpringSpecificRule (Row row) {
        if (row == null) return false;

        // Check all columns' text on the row:
        // 1. get the text of each cell in the row
        // 2. if the text contains "spring", return true; especially if for "When" column, if it contains "properties|", return true.
        int whenCol = -1;
        int lastCol = row.getLastCellNum();
        for (int c = 0; c < lastCol; c++) {
            Cell cell = row.getCell(c);
            String cellText = (cell != null) ? cell.toString().toLowerCase() : "";
            if (cellText.contains("spring")) {
                return true;
            }
            if (whenCol == -1 && cell != null && "when".equalsIgnoreCase(cell.getSheet().getRow(1).getCell(c).getStringCellValue().trim())) {
                whenCol = c;
            }
        }
        // Now, check "When" column for "properties|"
        if (whenCol != -1) {
            Cell whenCell = row.getCell(whenCol);
            String whenVal = (whenCell != null) ? whenCell.toString().toLowerCase() : "";
            if (whenVal.contains("properties|")) {
                return true;
            }
        }
        return false;
    }   

    public static void execute(String rulesetPath, String outputPath) {
        execute(rulesetPath, outputPath, null);
    }

    public static void execute(String rulesetPath, String outputPath, List<String> filters) {
        String inputDir = rulesetPath; // 更改为您的实际路径
        String outputFile = outputPath + "/appcat-ruleset.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            File rootDir = new File(inputDir);
            if (!rootDir.isDirectory()) {
                System.err.println("指定的路径不是一个目录！");
                return;
            }
            processRulesetFolder(rootDir, workbook, filters);

            // 写入Excel文件
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
                System.out.println("Excel文件生成成功！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processRulesetFolder(File rootDir, Workbook workbook, List<String> filters) {
        for (File subDir : rootDir.listFiles(File::isDirectory)) {
            // Skip if filters are provided and this directory is not in the filter list
            if (filters != null && !filters.isEmpty()) {
                boolean shouldProcess = false;
                String dirName = subDir.getName();
                
                // Check if any filter matches this directory
                for (String filter : filters) {
                    if (dirName.equals(filter) || dirName.contains(filter)) {
                        shouldProcess = true;
                        break;
                    }
                }
                
                if (!shouldProcess) {
                    System.out.println("Skipping directory: " + dirName + " (not in filter list)");
                    continue;
                }
            }
            processSubDirectory(subDir, workbook);
        }
    }

    private static void processSubDirectory(File subDir, Workbook workbook) {
        File rulesetFile = new File(subDir, "ruleset.yaml");
        if (!rulesetFile.exists()) {
            return;
        }

        // 解析ruleset.yaml
        String name = "";
        String description = "";
        try (InputStream input = new FileInputStream(rulesetFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);
            name = (String) data.getOrDefault("name", "");
            description = (String) data.getOrDefault("description", "");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 收集规则数据
        List<RuleData> rules = new ArrayList<>();
        File[] yamlFiles = subDir.listFiles(file ->
                file.isFile() &&
                file.getName().endsWith(".yaml") &&
                !file.getName().equals("ruleset.yaml")
        );

        if (yamlFiles != null) {
            for (File yamlFile : yamlFiles) {
                processYamlFile(yamlFile, rules);
            }
        }

        // 创建Sheet并写入数据
        if (name.isEmpty()) {
            name = "Sheet" + (workbook.getNumberOfSheets() + 1);
        }
        String safeName = WorkbookUtil.createSafeSheetName(name);
        Sheet sheet = workbook.createSheet(safeName);
        int rowNum = 0;

        // 写入首行：name和description
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Description");
        headerRow.createCell(1).setCellValue("name:" + name + " Description:" + description);

        // 写入数据标题行
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("RuleID");
        titleRow.createCell(1).setCellValue("When");
        titleRow.createCell(2).setCellValue("Description & Message");

        // 写入规则数据
        for (RuleData rule : rules) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(rule.ruleId);
            dataRow.createCell(1).setCellValue(rule.when);
            dataRow.createCell(2).setCellValue(rule.mergedDescription);
        }

        // 自动调整列宽
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Enable word wrap for all cells
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        
        // Apply word wrap to all cells in the sheet
        for (int i = 0; i <= rowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < 3; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        cell.setCellStyle(wrapStyle);
                    }
                }
            }
        }
    }

    private static void processYamlFile(File yamlFile, List<RuleData> rules) {
        try (InputStream input = new FileInputStream(yamlFile)) {
            Yaml yaml = new Yaml();
            Object data = yaml.load(input);
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (item instanceof Map) {
                        extractRuleData((Map<String, Object>) item, rules);
                    }
                }
            } else if (data instanceof Map) {
                extractRuleData((Map<String, Object>) data, rules);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractRuleData(Map<String, Object> ruleData, List<RuleData> rules) {
        String ruleId = (String) ruleData.get("ruleID");
        Object whenObj = ruleData.get("when");
        String when = serializeWhen(whenObj);
        String desc = (String) ruleData.get("description");
        String message = (String) ruleData.get("message");
        String merged = mergeDescriptionAndMessage(desc, message);

        rules.add(new RuleData(ruleId, when, merged));
    }

    private static String serializeWhen(Object whenObj) {
        if (whenObj == null) return "";
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        return new Yaml(options).dump(whenObj).trim();
    }

    private static String mergeDescriptionAndMessage(String desc, String msg) {
        desc = (desc == null) ? "" : desc.trim();
        msg = (msg == null) ? "" : msg.trim();
        if (!desc.isEmpty() && !msg.isEmpty()) {
            return desc + "\n" + msg;
        } else {
            return desc + msg;
        }
    }

    static class RuleData {
        String ruleId;
        String when;
        String mergedDescription;

        RuleData(String ruleId, String when, String mergedDescription) {
            this.ruleId = ruleId;
            this.when = when;
            this.mergedDescription = mergedDescription;
        }
    }
}