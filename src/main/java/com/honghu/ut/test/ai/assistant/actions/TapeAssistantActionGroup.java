package com.honghu.ut.test.ai.assistant.actions;

import com.honghu.ut.test.ai.assistant.ui.CoverageToolWindowPanel;
import com.honghu.ut.test.ai.assistant.jacoco.JacocoCoverageService;
import com.honghu.ut.test.ai.assistant.plugin.UtAssistantPlugin;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tape -- AI UT assistant 动作组
 * 提供右键菜单中的主入口和子菜单
 */
public class TapeAssistantActionGroup extends ActionGroup {
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 根据选中的文件类型启用/禁用动作组
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean enabled = file != null && file.isDirectory();
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (e == null) {
            return new AnAction[0];
        }
        
        Project project = e.getProject();
        if (project == null) {
            return new AnAction[0];
        }

        // 创建子菜单动作
        return new AnAction[]{
            new OpenAIUnitTestAction(),
            new GenerateTestReportAction(),
            new AIFixReportAction()
        };
    }
}