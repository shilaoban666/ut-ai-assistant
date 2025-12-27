package com.honghu.ut.test.ai.assistant.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * 覆盖率工具窗口工厂
 * 创建覆盖率工具窗口的工厂类
 * 
 * 功能：
 * - 创建覆盖率工具窗口内容
 * - 将覆盖率面板添加到工具窗口中
 */
public class CoverageToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        CoverageToolWindowPanel windowPanel = new CoverageToolWindowPanel(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(windowPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}