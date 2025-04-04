package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class RulesetToExcel {

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