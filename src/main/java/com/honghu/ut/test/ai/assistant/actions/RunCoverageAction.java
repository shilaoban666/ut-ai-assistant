package com.honghu.ut.test.ai.assistant.actions;

import com.honghu.ut.test.ai.assistant.jacoco.JacocoCoverageService;
import com.honghu.ut.test.ai.assistant.plugin.UtAssistantPlugin;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 运行覆盖率分析的动作类
 * 触发 JaCoCo 覆盖率分析的动作
 * 
 * 功能：
 * - 在后台任务中运行 JaCoCo 覆盖率分析
 * - 更新 UI 以显示分析结果
 * - 支持对选中的目录或整个项目进行分析
 */
public class RunCoverageAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(RunCoverageAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取当前选中的文件或目录
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        
        // 在后台任务中运行覆盖率分析
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "运行 JaCoCo 覆盖率分析") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("初始化 JaCoCo 覆盖率分析...");
                    
                    // 获取或创建覆盖率服务
                    JacocoCoverageService coverageService = project.getService(JacocoCoverageService.class);
                    
                    // 确定要分析的目录
                    String targetPath;
                    if (selectedFile != null && selectedFile.isDirectory()) {
                        targetPath = selectedFile.getPath();
                        indicator.setText("分析目录: " + targetPath);
                    } else {
                        targetPath = project.getBasePath();
                        indicator.setText("分析整个项目: " + project.getName());
                    }
                    
                    // 查找并分析类文件
                    List<String> classFilePaths = findClassFiles(targetPath, indicator);
                    
                    if (classFilePaths.isEmpty()) {
                        indicator.setText("未找到类文件进行分析");
                        ApplicationManager.getApplication().invokeLater(() -> {
                            Messages.showMessageDialog(project, 
                                "在目标目录中未找到类文件", 
                                "信息", 
                                Messages.getInformationIcon());
                        });
                        return;
                    }
                    
                    indicator.setText("分析 " + classFilePaths.size() + " 个类文件...");
                    
                    // 加载执行数据 (假设存在jacoco.exec文件)
                    String execFilePath = findExecFile(project.getBasePath());
                    if (execFilePath != null) {
                        coverageService.loadExecutionData(execFilePath);
                        indicator.setText("加载执行数据: " + execFilePath);
                    }
                    
                    // 分析类文件
                    coverageService.analyzeClassFiles(classFilePaths);
                    
                    indicator.setText("覆盖率分析完成");
                    
                    ApplicationManager.getApplication().invokeLater(() -> {
                        // 更新UI显示结果
                        Messages.showMessageDialog(project, 
                            "覆盖率分析完成！共分析了 " + classFilePaths.size() + " 个类文件", 
                            "分析完成", 
                            Messages.getInformationIcon());
                    });
                } catch (Exception ex) {
                    LOG.error("覆盖率分析期间出错", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showMessageDialog(project, 
                            "覆盖率分析出错: " + ex.getMessage(), 
                            "错误", 
                            Messages.getErrorIcon());
                    });
                }
            }
        });
    }
    
    /**
     * 查找目录中的类文件
     */
    private List<String> findClassFiles(String basePath, ProgressIndicator indicator) {
        List<String> classFiles = new ArrayList<>();
        File baseDir = new File(basePath);
        
        // 递归查找类文件
        findClassFilesRecursive(baseDir, classFiles, indicator);
        
        return classFiles;
    }
    
    private void findClassFilesRecursive(File dir, List<String> classFiles, ProgressIndicator indicator) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (indicator.isCanceled()) {
                return;
            }
            
            if (file.isDirectory()) {
                // 跳过某些不需要的目录
                if (!file.getName().equals(".gradle") && 
                    !file.getName().equals("build") && 
                    !file.getName().equals(".git") &&
                    !file.getName().startsWith(".")) {
                    findClassFilesRecursive(file, classFiles, indicator);
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
        // 在常见位置查找jacoco.exec文件
        String[] possiblePaths = {
            basePath + "/build/jacoco.exec",
            basePath + "/target/jacoco.exec",
            basePath + "/build/jacoco/test.exec",
            basePath + "/jacoco.exec"
        };
        
        for (String path : possiblePaths) {
            File execFile = new File(path);
            if (execFile.exists()) {
                return path;
            }
        }
        
        return null; // 未找到
    }
}