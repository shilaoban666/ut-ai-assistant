package com.honghu.ut.test.ai.assistant.actions;

import com.honghu.ut.test.ai.assistant.jacoco.JacocoCoverageService;
import com.honghu.ut.test.ai.assistant.jacoco.CoverageData;
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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.honghu.ut.test.ai.assistant.ui.CoverageToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 生成测试报告动作
 * 与之前的"生成单元测试覆盖率报告"功能一致
 */
public class GenerateTestReportAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateTestReportAction.class);

    public GenerateTestReportAction() {
        super("生成测试报告");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取当前选中的文件或目录
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        
        if (selectedFile == null || !selectedFile.isDirectory()) {
            Messages.showMessageDialog(project, 
                "请右键点击一个目录来生成测试报告", 
                "错误", 
                Messages.getErrorIcon());
            return;
        }

        // 在后台任务中运行覆盖率分析
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "生成 " + selectedFile.getName() + " 的测试报告") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("初始化 JaCoCo 覆盖率分析...");
                    
                    // 获取覆盖率服务
                    JacocoCoverageService coverageService = UtAssistantPlugin.getInstance(project).getJacocoCoverageService();
                    
                    if (coverageService == null) {
                        throw new Exception("JaCoCo覆盖率服务未正确初始化");
                    }
                    
                    // 确定要分析的目录
                    String targetPath = selectedFile.getPath();
                    indicator.setText("分析目录: " + targetPath);
                    
                    // 自动执行测试并生成覆盖率数据
                    indicator.setText("正在分析类文件并生成覆盖率数据...");
                    coverageService.executeTestsAndGenerateCoverage(targetPath);
                    
                    indicator.setText("覆盖率分析完成");
                    
                    ApplicationManager.getApplication().invokeLater(() -> {
                        // 更新UI显示真实覆盖率数据
                        updateUIWithCoverageData(project, coverageService);
                        
                        // 获取覆盖率数据以确定显示的消息
                        List<CoverageData> coverageDataList = coverageService.getAllCoverageData();
                        String message;
                        if (coverageDataList.isEmpty()) {
                            message = "测试报告生成完成！未找到可分析的类文件。\n请确保项目已编译且包含.class文件。";
                        } else {
                            message = "测试报告生成完成！已分析 " + targetPath + " 目录的 " + coverageDataList.size() + " 个类文件。";
                        }
                        
                        Messages.showMessageDialog(project, 
                            message, 
                            "报告生成完成", 
                            Messages.getInformationIcon());
                    });
                } catch (Exception ex) {
                    LOG.error("测试报告生成期间出错", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showMessageDialog(project, 
                            "测试报告生成出错: " + ex.getMessage(), 
                            "错误", 
                            Messages.getErrorIcon());
                    });
                }
            }
        });
    }
    
    /**
     * 更新UI以显示覆盖率数据
     */
    private void updateUIWithCoverageData(Project project, JacocoCoverageService coverageService) {
        try {
            // 获取覆盖率数据
            List<CoverageData> coverageDataList = coverageService.getAllCoverageData();
            
            // 获取工具窗口并更新内容
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Tape AI UT assistant");
            if (toolWindow != null) {
                ContentManager contentManager = toolWindow.getContentManager();
                Content content = contentManager.getContent(0);
                if (content != null && content.getComponent() instanceof CoverageToolWindowPanel) {
                    CoverageToolWindowPanel panel = (CoverageToolWindowPanel) content.getComponent();
                    
                    // 清除之前的数据
                    Object[][] tableData = new Object[coverageDataList.size()][5];
                    for (int i = 0; i < coverageDataList.size(); i++) {
                        CoverageData data = coverageDataList.get(i);
                        tableData[i][0] = data.getName(); // 类名
                        tableData[i][1] = data.getFormattedLineCoverage(); // 行覆盖率
                        tableData[i][2] = data.getFormattedBranchCoverage(); // 分支覆盖率
                        tableData[i][3] = data.getFormattedMethodCoverage(); // 方法覆盖率
                        tableData[i][4] = data.getFormattedClassCoverage(); // 类覆盖率
                    }
                    
                    // 更新UI
                    panel.updateCoverageData(tableData);
                    panel.updateReportText(coverageService.generateCoverageReport());
                    panel.updateCoverageSummary(); // 更新覆盖率摘要
                }
            }
        } catch (Exception e) {
            LOG.error("更新UI时出错", e);
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 根据选中的文件类型启用/禁用动作
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean enabled = file != null && file.isDirectory();
        e.getPresentation().setEnabledAndVisible(enabled);
    }
}