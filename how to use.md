to maintain the ruleset visibility for customers, need follow below steps to produce the up-to-date doc content:
## 1. about ISO5055 - tech debts, cwe issue list (so far, security for java only, actually covering all)
 - follow [readme.md](C:\Users\taoxu\Downloads\git\appcat-ruleset-reader\generate-ruleset\readme.md)

## 2. appcat ruleset extraction
steps: 
 1. download latest AppCAT zip and put it into C:\Users\taoxu\Downloads\appcat\
 2. directly run this app: java -jar target/appcat-ruleset-reader-1.0.0.jar
 3. rulset csv file will be generated into C:\Users\taoxu\Downloads\appcat\extraction-v2
 4. go to Notebook LM to pull that csv in and execute prompt in "Azure 云迁移技术审计与规约报告" to generate an up-to-date issue summary table.


## Below are the default values for extracting csv from AppCAT ruleset folder.
1. valid rule folder in AppCAT:
azure,cloud-readiness,openjdk8,openjdk11,openjdk17,openjdk21,os,jakarta-ee

2. the info to extract per rule:
- description
- message
- labels: domain
- labels: category
- category (criticality)
- effort
- labels: target
- labels: capability
- labels: os
- ruleID
- when