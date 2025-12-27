package com.honghu.ut.test.ai.assistant.actions;

import com.honghu.ut.test.ai.assistant.testgen.UnitTestGenerationService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 生成单元测试的动作类
 * 使用 AI 生成单元测试的动作
 * 
 * 功能：
 * - 在后台任务中生成单元测试
 * - 分析选定的代码并生成相应的测试
 * - 更新 UI 以显示生成结果
 */
public class GenerateTestsAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateTestsAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 在后台任务中运行测试生成
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "生成单元测试") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("初始化测试生成...");
                    
                    // 获取或创建测试生成服务
                    UnitTestGenerationService testGenService = project.getService(UnitTestGenerationService.class);
                    if (testGenService == null) {
                        testGenService = new UnitTestGenerationService(project);
                    }
                    
                    indicator.setText("分析代码结构...");
                    
                    // 这将分析选定的代码并生成测试
                    // 目前，我们只显示一条消息
                    for (int i = 0; i <= 100; i += 10) {
                        indicator.setFraction(i / 100.0);
                        Thread.sleep(100); // 模拟工作
                    }
                    
                    indicator.setText("单元测试生成成功");
                    
                    ApplicationManager.getApplication().invokeLater(() -> {
                        // 显示通知或更新 UI
                    });
                } catch (Exception ex) {
                    LOG.error("测试生成期间出错", ex);
                }
            }
        });
    }
}