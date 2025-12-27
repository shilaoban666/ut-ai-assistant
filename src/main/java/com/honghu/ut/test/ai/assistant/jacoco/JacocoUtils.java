package com.honghu.ut.test.ai.assistant.jacoco;

import com.intellij.openapi.project.Project;

import java.io.File;

/**
 * JaCoCo工具类
 * 提供JaCoCo相关的实用方法
 */
public class JacocoUtils {
    
    /**
     * 查找jacoco.exec文件
     */
    public static String findExecFile(String projectPath) {
        // 在常见位置查找jacoco.exec文件
        String[] possiblePaths = {
            projectPath + "/build/jacoco.exec",
            projectPath + "/target/jacoco.exec",
            projectPath + "/build/jacoco/test.exec",
            projectPath + "/jacoco.exec",
            projectPath + "/build/reports/jacoco/test/jacocoTestReport.xml",
            projectPath + "/target/site/jacoco/jacoco.xml"
        };
        
        for (String path : possiblePaths) {
            File execFile = new File(path);
            if (execFile.exists()) {
                // 如果是XML报告文件，返回对应的.exec文件
                if (path.endsWith(".xml")) {
                    String execPath = path.replace(".xml", ".exec").replace("jacocoTestReport.xml", "exec");
                    if (execPath.contains("jacoco.xml")) {
                        execPath = execPath.replace("jacoco.xml", "jacoco.exec");
                    }
                    File execFileAlt = new File(execPath);
                    if (execFileAlt.exists()) {
                        return execPath;
                    }
                } else {
                    return path;
                }
            }
        }
        
        return null; // 未找到
    }
    
    /**
     * 检查项目是否配置了JaCoCo
     */
    public static boolean isJacocoConfigured(Project project) {
        String projectPath = project.getBasePath();
        if (projectPath == null) {
            return false;
        }
        
        // 检查是否有构建文件包含JaCoCo配置
        File gradleFile = new File(projectPath + "/build.gradle");
        File gradleKtsFile = new File(projectPath + "/build.gradle.kts");
        File pomFile = new File(projectPath + "/pom.xml");
        
        // 检查Gradle文件
        if (gradleFile.exists()) {
            return checkGradleForJacoco(gradleFile);
        } else if (gradleKtsFile.exists()) {
            return checkGradleForJacoco(gradleKtsFile);
        }
        
        // 检查Maven文件
        if (pomFile.exists()) {
            return checkMavenForJacoco(pomFile);
        }
        
        return false;
    }
    
    private static boolean checkGradleForJacoco(File buildFile) {
        try {
            String content = java.nio.file.Files.readString(buildFile.toPath());
            return content.toLowerCase().contains("jacoco");
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean checkMavenForJacoco(File pomFile) {
        try {
            String content = java.nio.file.Files.readString(pomFile.toPath());
            return content.toLowerCase().contains("jacoco");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取项目的类文件目录
     */
    public static String[] getClassDirectories(String projectPath) {
        return new String[] {
            projectPath + "/build/classes/java/main",  // Gradle
            projectPath + "/build/classes/kotlin/main", // Gradle with Kotlin
            projectPath + "/target/classes",            // Maven
            projectPath + "/out/production",            // IntelliJ IDEA
            projectPath + "/classes"                   // Other
        };
    }
}