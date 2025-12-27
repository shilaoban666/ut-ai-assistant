package com.honghu.ut.test.ai.assistant.config;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.awt.*;

/**
 * UT 助手配置面板 UI
 * UT 助手插件的配置面板用户界面
 * 
 * 功能：
 * - 提供 JaCoCo 路径配置
 * - 设置覆盖率阈值
 * - 配置自动运行和生成选项
 */
public class UtAssistantConfigurationPanel {
    private JBPanel<?> rootPanel;              // 根面板
    private JBTextField jacocoExecPath;        // JaCoCo 执行文件路径
    private JBTextField jacocoClassPath;       // JaCoCo 类路径
    private JBTextField minLineCoverage;       // 最小行覆盖率
    private JBTextField minBranchCoverage;     // 最小分支覆盖率
    private JBCheckBox enableAutoRunTests;     // 启用自动运行测试
    private JBCheckBox enableAutoGenerateTests; // 启用自动生成测试
    private JBCheckBox showCoverageInEditor;   // 在编辑器中显示覆盖率

    // 文件浏览按钮
    private TextFieldWithBrowseButton execPathBrowse;
    private TextFieldWithBrowseButton classPathBrowse;

    public UtAssistantConfigurationPanel() {
        createUIComponents();
        setupUI();
    }

    private void createUIComponents() {
        rootPanel = new JBPanel<>(new BorderLayout());
        
        // 初始化组件
        jacocoExecPath = new JBTextField();
        jacocoClassPath = new JBTextField();
        minLineCoverage = new JBTextField("80.0");
        minBranchCoverage = new JBTextField("70.0");
        enableAutoRunTests = new JBCheckBox("生成后自动运行测试");
        enableAutoGenerateTests = new JBCheckBox("为未覆盖的方法自动生成测试");
        showCoverageInEditor = new JBCheckBox("在编辑器中显示覆盖率");

        // 文件路径的浏览按钮
        execPathBrowse = new TextFieldWithBrowseButton(jacocoExecPath);
        execPathBrowse.addBrowseFolderListener(
            "选择 JaCoCo Exec 文件",
            "选择 JaCoCo 执行数据文件 (jacoco.exec)",
            null,
            new FileChooserDescriptor(true, false, false, false, false, false)
        );

        classPathBrowse = new TextFieldWithBrowseButton(jacocoClassPath);
        classPathBrowse.addBrowseFolderListener(
            "选择类目录",
            "选择用于覆盖率分析的类文件目录",
            null,
            new FileChooserDescriptor(false, true, false, false, false, false)
        );
    }

    private void setupUI() {
        // 创建使用网格布局的主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // JaCoCo Exec 路径
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JBLabel("JaCoCo Exec 文件路径:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(execPathBrowse, gbc);

        // JaCoCo 类路径
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JBLabel("类目录路径:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(classPathBrowse, gbc);

        // 最小行覆盖率
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JBLabel("最小行覆盖率 (%):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(minLineCoverage, gbc);

        // 最小分支覆盖率
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JBLabel("最小分支覆盖率 (%):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(minBranchCoverage, gbc);

        // 复选框
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        mainPanel.add(enableAutoRunTests, gbc);
        gbc.gridy = 5;
        mainPanel.add(enableAutoGenerateTests, gbc);
        gbc.gridy = 6;
        mainPanel.add(showCoverageInEditor, gbc);

        rootPanel.add(mainPanel, BorderLayout.CENTER);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isModified() {
        // 在实际实现中，这将比较当前值与保存的值
        return true;
    }

    public void applySettings() {
        // 在实际实现中，这将保存设置
        System.out.println("应用设置...");
        System.out.println("JaCoCo Exec 路径: " + jacocoExecPath.getText());
        System.out.println("类路径: " + jacocoClassPath.getText());
        System.out.println("最小行覆盖率: " + minLineCoverage.getText());
        System.out.println("最小分支覆盖率: " + minBranchCoverage.getText());
    }

    public void resetSettings() {
        // 在实际实现中，这将重置为保存的值
        System.out.println("重置设置...");
    }
}