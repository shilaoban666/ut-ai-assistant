package com.honghu.ut.test.ai.assistant.actions;

import com.honghu.ut.test.ai.assistant.config.UtAssistantConfigurable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 设置按钮动作
 * 显示设置下拉菜单，包含模型设置、测试报告设置、规则设置、Agent设置等
 */
public class SettingsAction extends AnAction {
    
    public SettingsAction() {
        super("设置");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 打开插件设置页面
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), "UT AI Assistant");
    }
}