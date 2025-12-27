package com.honghu.ut.test.ai.assistant.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * UT 助手配置面板
 * 为 UT AI 助手插件提供配置界面
 * 
 * 功能：
 * - 显示插件配置选项
 * - 允许用户设置 JaCoCo 参数
 * - 保存和重置配置设置
 */
public class UtAssistantConfigurable implements Configurable {
    private UtAssistantConfigurationPanel configPanel;
    private final Project project;

    public UtAssistantConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public String getDisplayName() {
        return "UT AI 助手";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        configPanel = new UtAssistantConfigurationPanel();
        return configPanel.getRootPanel();
    }

    @Override
    public boolean isModified() {
        if (configPanel == null) return false;
        return configPanel.isModified();
    }

    @Override
    public void apply() {
        if (configPanel != null) {
            configPanel.applySettings();
        }
    }

    @Override
    public void reset() {
        if (configPanel != null) {
            configPanel.resetSettings();
        }
    }

    @Override
    public void disposeUIResources() {
        configPanel = null;
    }
}