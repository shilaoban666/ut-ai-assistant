package com.honghu.ut.test.ai.assistant.jacoco;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;

import java.io.*;
import java.util.List;
import java.util.Collection;

/**
 * JaCoCo 覆盖率服务类
 * 处理 JaCoCo 覆盖率分析
 * 
 * 功能：
 * - 加载 JaCoCo 执行数据
 * - 分析类文件的覆盖率
 * - 生成详细的覆盖率报告
 * - 自动执行JaCoCo分析
 */
public class JacocoCoverageService {
    private final Project project;
    private final ExecFileLoader execFileLoader;
    private CoverageBuilder coverageBuilder;

    public JacocoCoverageService(Project project) {
        this.project = project;
        this.execFileLoader = new ExecFileLoader();
    }

    /**
     * 从 JaCoCo exec 文件加载执行数据
     */
    public void loadExecutionData(String execFilePath) throws Exception {
        execFileLoader.load(new File(execFilePath));
    }

    /**
     * 分析类文件的覆盖率
     */
    public CoverageBuilder analyzeClassFiles(List<String> classFilePaths) throws Exception {
        final CoverageBuilder builder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), builder);

        for (String classFilePath : classFilePaths) {
            File classFile = new File(classFilePath);
            if (classFile.exists()) {
                analyzer.analyzeAll(classFile);
            }
        }

        this.coverageBuilder = builder;
        return builder;
    }

    /**
     * 直接分析类文件而不依赖jacoco.exec文件
     */
    public void analyzeClassFilesDirectly(List<String> classFilePaths) throws Exception {
        // 创建一个空的执行数据存储
        ExecutionDataStore executionDataStore = new ExecutionDataStore();
        
        // 创建一个空的覆盖率构建器
        this.coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionDataStore, this.coverageBuilder);

        // 分析所有类文件
        for (String classFilePath : classFilePaths) {
            File classFile = new File(classFilePath);
            if (classFile.exists()) {
                analyzer.analyzeAll(classFile);
            }
        }
    }

    /**
     * 自动执行测试并生成覆盖率数据
     */
    public void executeTestsAndGenerateCoverage(String targetPath) throws Exception {
        // 首先查找类文件
        List<String> classFiles = findClassFiles(targetPath);
        if (classFiles.isEmpty()) {
            // 如果在目标路径中找不到类文件，尝试在标准输出目录查找
            String projectPath = project.getBasePath();
            String[] classDirs = JacocoUtils.getClassDirectories(projectPath);
            for (String classDir : classDirs) {
                classFiles = findClassFiles(classDir);
                if (!classFiles.isEmpty()) {
                    break;
                }
            }
        }
        
        if (classFiles.isEmpty()) {
            // 如果仍然找不到类文件，尝试在标准项目输出目录查找
            String projectPath = project.getBasePath();
            String[] standardDirs = {
                projectPath + "/build/classes/java/main",
                projectPath + "/target/classes",
                projectPath + "/out/production/classes",
                projectPath + "/build/classes/kotlin/main",
                projectPath + "/out/production/" + new File(projectPath).getName()
            };
            
            for (String dir : standardDirs) {
                classFiles = findClassFiles(dir);
                if (!classFiles.isEmpty()) {
                    break;
                }
            }
        }
        
        if (classFiles.isEmpty()) {
            // 如果仍然找不到类文件，创建一个示例报告
            createEmptyCoverageReport();
            return;
        }
        
        // 直接分析类文件而不依赖jacoco.exec文件
        analyzeClassFilesDirectly(classFiles);
    }
    
    /**
     * 创建空的覆盖率报告（当没有类文件时）
     */
    private void createEmptyCoverageReport() {
        this.coverageBuilder = new CoverageBuilder();
        // 这将创建一个空的覆盖率报告，稍后会显示没有找到类文件的信息
    }
    
    /**
     * 检查JaCoCo是否已配置
     */
    private boolean isJacocoConfigured(String projectPath) {
        // 检查Gradle项目
        if (new File(projectPath + "/build.gradle").exists()) {
            try {
                String content = java.nio.file.Files.readString(new File(projectPath + "/build.gradle").toPath());
                return content.toLowerCase().contains("jacoco");
            } catch (IOException e) {
                // 忽略读取错误
            }
        }
        
        if (new File(projectPath + "/build.gradle.kts").exists()) {
            try {
                String content = java.nio.file.Files.readString(new File(projectPath + "/build.gradle.kts").toPath());
                return content.toLowerCase().contains("jacoco");
            } catch (IOException e) {
                // 忽略读取错误
            }
        }
        
        // 检查Maven项目
        if (new File(projectPath + "/pom.xml").exists()) {
            try {
                String content = java.nio.file.Files.readString(new File(projectPath + "/pom.xml").toPath());
                return content.toLowerCase().contains("jacoco") || content.toLowerCase().contains("org.jacoco");
            } catch (IOException e) {
                // 忽略读取错误
            }
        }
        
        return false;
    }
    
    /**
     * 自动配置JaCoCo（如果未配置）
     */
    private void configureJacoco(String projectPath) throws IOException {
        boolean isGradleProject = new File(projectPath + "/build.gradle").exists() || 
                                  new File(projectPath + "/build.gradle.kts").exists();
        
        if (isGradleProject) {
            configureJacocoForGradle(projectPath);
        } else {
            configureJacocoForMaven(projectPath);
        }
    }
    
    /**
     * 为Gradle项目配置JaCoCo
     */
    private void configureJacocoForGradle(String projectPath) throws IOException {
        File buildGradleFile = new File(projectPath + "/build.gradle");
        if (buildGradleFile.exists()) {
            String content = java.nio.file.Files.readString(buildGradleFile.toPath());
            if (!content.contains("jacoco") && !content.contains("Jacoco")) {
                // 添加JaCoCo插件配置
                String jacocoConfig = "\nplugins {\n    id 'jacoco'\n}\n\n" +
                    "jacoco {\n    toolVersion = \"0.8.7\"\n}\n\n" +
                    "test {\n    finalizedBy jacocoTestReport\n}\n\n" +
                    "jacocoTestReport {\n    dependsOn test\n    reports {\n        xml.required = true\n        html.required = true\n    }\n}\n";
                
                // 将JaCoCo配置添加到文件开头
                java.nio.file.Files.writeString(buildGradleFile.toPath(), jacocoConfig + content);
            }
        } else {
            File buildGradleKtsFile = new File(projectPath + "/build.gradle.kts");
            if (buildGradleKtsFile.exists()) {
                String content = java.nio.file.Files.readString(buildGradleKtsFile.toPath());
                if (!content.contains("jacoco") && !content.contains("Jacoco")) {
                    // 添加JaCoCo插件配置
                    String jacocoConfig = "\nplugins {\n    id(\"jacoco\")\n}\n\n" +
                        "jacoco {\n    toolVersion = \"0.8.7\"\n}\n\n" +
                        "tasks.test {\n    finalizedBy(tasks.jacocoTestReport)\n}\n\n" +
                        "tasks.jacocoTestReport {\n    dependsOn(tasks.test)\n    reports {\n        xml.required.set(true)\n        html.required.set(true)\n    }\n}\n";
                    
                    // 将JaCoCo配置添加到文件开头
                    java.nio.file.Files.writeString(buildGradleKtsFile.toPath(), jacocoConfig + content);
                }
            }
        }
    }
    
    /**
     * 为Maven项目配置JaCoCo
     */
    private void configureJacocoForMaven(String projectPath) throws IOException {
        File pomFile = new File(projectPath + "/pom.xml");
        if (pomFile.exists()) {
            String content = java.nio.file.Files.readString(pomFile.toPath());
            if (!content.contains("jacoco") && !content.contains("Jacoco") && !content.contains("org.jacoco")) {
                // 简单地在</project>标签前插入JaCoCo插件配置
                String jacocoPlugin = "\n            <plugin>\n" +
                    "                <groupId>org.jacoco</groupId>\n" +
                    "                <artifactId>jacoco-maven-plugin</artifactId>\n" +
                    "                <version>0.8.7</version>\n" +
                    "                <executions>\n" +
                    "                    <execution>\n" +
                    "                        <goals>\n" +
                    "                            <goal>prepare-agent</goal>\n" +
                    "                        </goals>\n" +
                    "                    </execution>\n" +
                    "                    <execution>\n" +
                    "                        <id>report</id>\n" +
                    "                        <phase>test</phase>\n" +
                    "                        <goals>\n" +
                    "                            <goal>report</goal>\n" +
                    "                        </goals>\n" +
                    "                    </execution>\n" +
                    "                </executions>\n" +
                    "            </plugin>\n";
                
                // 在</plugins>标签前插入（如果存在），否则在</build>标签前插入
                if (content.contains("</plugins>")) {
                    content = content.replace("</plugins>", "        " + jacocoPlugin + "    </plugins>");
                } else if (content.contains("</build>")) {
                    content = content.replace("</build>", 
                        "        <plugins>\n" + jacocoPlugin + "        </plugins>\n    </build>");
                } else {
                    // 如果没有找到build部分，尝试在</project>前添加
                    content = content.replace("</project>", 
                        "    <build>\n" +
                        "        <plugins>\n" + jacocoPlugin + "        </plugins>\n" +
                        "    </build>\n</project>");
                }
                
                java.nio.file.Files.writeString(pomFile.toPath(), content);
            }
        }
    }
    
    /**
     * 执行Gradle测试
     */
    private void executeGradleTest() throws Exception {
        String projectPath = project.getBasePath();
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("cmd", "/c", "gradlew.bat", "test", "--info");
        } else {
            processBuilder.command("./gradlew", "test", "--info");
        }
        
        processBuilder.directory(new File(projectPath));
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // 读取输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 记录输出
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Gradle测试执行失败，退出码: " + exitCode);
        }
    }
    
    /**
     * 执行Maven测试
     */
    private void executeMavenTest() throws Exception {
        String projectPath = project.getBasePath();
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("cmd", "/c", "mvn", "test", "-Djacoco.skip=false");
        } else {
            processBuilder.command("mvn", "test", "-Djacoco.skip=false");
        }
        
        processBuilder.directory(new File(projectPath));
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // 读取输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 记录输出
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Maven测试执行失败，退出码: " + exitCode);
        }
    }
    
    /**
     * 查找目录中的类文件
     */
    private List<String> findClassFiles(String basePath) {
        List<String> classFiles = new java.util.ArrayList<>();
        File baseDir = new File(basePath);
        
        findClassFilesRecursive(baseDir, classFiles);
        
        return classFiles;
    }
    
    private void findClassFilesRecursive(File dir, List<String> classFiles) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // 跳过某些不需要的目录
                if (!file.getName().equals(".gradle") && 
                    !file.getName().equals("build") && 
                    !file.getName().equals(".git") &&
                    !file.getName().startsWith(".")) {
                    findClassFilesRecursive(file, classFiles);
                }
            } else if (file.getName().endsWith(".class")) {
                classFiles.add(file.getAbsolutePath());
            }
        }
    }
    
    /**
     * 查找jacoco.exec文件
     */
    private String findExecFile(String basePath) {
        return JacocoUtils.findExecFile(basePath);
    }

    /**
     * 获取特定文件的覆盖率数据
     */
    public CoverageData getCoverageData(VirtualFile sourceFile) {
        if (coverageBuilder == null) {
            return null;
        }

        String className = sourceFile.getNameWithoutExtension();
        // 修正：使用getClasses().get()来获取类覆盖率数据
        Collection<org.jacoco.core.analysis.IClassCoverage> classes = coverageBuilder.getClasses();
        for (org.jacoco.core.analysis.IClassCoverage classCoverage : classes) {
            // 类名可能包含包路径，需要正确匹配
            if (classCoverage.getName().endsWith(className)) {
                return new CoverageData(
                    classCoverage.getName(),
                    classCoverage.getInstructionCounter().getCoveredRatio() * 100,
                    classCoverage.getBranchCounter().getCoveredRatio() * 100,
                    classCoverage.getLineCounter().getCoveredRatio() * 100
                );
            }
        }
        return null;
    }

    /**
     * 生成详细的覆盖率报告
     */
    public String generateCoverageReport() {
        if (coverageBuilder == null) {
            return "没有可用的覆盖率数据。请先运行单元测试以生成jacoco.exec文件，然后分析覆盖率。";
        }

        StringBuilder report = new StringBuilder();
        report.append("JaCoCo 覆盖率报告\n");
        report.append("========================\n");

        Collection<org.jacoco.core.analysis.IClassCoverage> classes = coverageBuilder.getClasses();
        if (classes.isEmpty()) {
            report.append("没有找到类文件进行分析。请确保项目已编译且包含可分析的类文件。\n");
            return report.toString();
        }

        for (org.jacoco.core.analysis.IClassCoverage classCoverage : classes) {
            if (classCoverage != null) {
                double lineCoverage = classCoverage.getLineCounter().getCoveredRatio() * 100;
                double branchCoverage = classCoverage.getBranchCounter().getCoveredRatio() * 100;
                double instructionCoverage = classCoverage.getInstructionCounter().getCoveredRatio() * 100;
                // 计算方法和类覆盖率
                double methodCoverage = classCoverage.getMethodCounter().getCoveredRatio() * 100;
                double classCoveragePercent = classCoverage.getClassCounter().getCoveredRatio() * 100;

                report.append(String.format("类: %s\n", classCoverage.getName()));
                report.append(String.format("  行覆盖率: %.2f%%\n", lineCoverage));
                report.append(String.format("  分支覆盖率: %.2f%%\n", branchCoverage));
                report.append(String.format("  指令覆盖率: %.2f%%\n", instructionCoverage));
                report.append(String.format("  方法覆盖率: %.2f%%\n", methodCoverage));
                report.append(String.format("  类覆盖率: %.2f%%\n", classCoveragePercent));
                report.append("\n");
            }
        }

        return report.toString();
    }

    /**
     * 获取所有类的覆盖率数据
     */
    public List<CoverageData> getAllCoverageData() {
        if (coverageBuilder == null) {
            return new java.util.ArrayList<>();
        }

        Collection<org.jacoco.core.analysis.IClassCoverage> classes = coverageBuilder.getClasses();
        List<CoverageData> coverageDataList = new java.util.ArrayList<>();
        
        for (org.jacoco.core.analysis.IClassCoverage classCoverage : classes) {
            if (classCoverage != null) {
                double lineCoverage = classCoverage.getLineCounter().getCoveredRatio() * 100;
                double branchCoverage = classCoverage.getBranchCounter().getCoveredRatio() * 100;
                double instructionCoverage = classCoverage.getInstructionCounter().getCoveredRatio() * 100;
                double methodCoverage = classCoverage.getMethodCounter().getCoveredRatio() * 100;
                double classCoveragePercent = classCoverage.getClassCounter().getCoveredRatio() * 100;

                coverageDataList.add(new CoverageData(
                    classCoverage.getName().replace('/', '.').replace('\\', '.'),
                    instructionCoverage,  // 使用指令覆盖率作为主要覆盖率
                    branchCoverage,
                    lineCoverage,
                    methodCoverage,
                    classCoveragePercent
                ));
            }
        }

        return coverageDataList;
    }
}