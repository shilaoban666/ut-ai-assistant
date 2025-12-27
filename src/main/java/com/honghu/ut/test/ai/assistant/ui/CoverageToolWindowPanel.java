package com.honghu.ut.test.ai.assistant.ui;

import com.honghu.ut.test.ai.assistant.jacoco.CoverageData;
import com.honghu.ut.test.ai.assistant.jacoco.JacocoCoverageService;
import com.honghu.ut.test.ai.assistant.jacoco.JacocoUtils;
import com.honghu.ut.test.ai.assistant.plugin.UtAssistantPlugin;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 覆盖率工具窗口面板
 * 显示覆盖率信息的用户界面组件
 * 
 * 功能：
 * - 显示覆盖率摘要树形结构（类名，分支级别%，类级别%，方法级别%，行级别%）
 * - 提供详细报告视图
 * - 包含生成报告和AI修复按钮
 * - 支持报告下载功能
 */
public class CoverageToolWindowPanel extends SimpleToolWindowPanel {
    private final Project project;
    private final JacocoCoverageService coverageService;
    private JTabbedPane mainTabbedPane;        // 主标签页组件（报告/修补）
    private JEditorPane reportArea;         // 报告文本区域（使用JEditorPane支持HTML）
    private JEditorPane analysisResultArea; // 分析结果区域
    private JTree coverageTree;             // 覆盖率树形结构
    private JLabel statusLabel;             // 状态标签
    private JPanel coverageSummaryPanel;    // 覆盖率摘要面板
    private JPanel detailedReportPanel;     // 详细报告面板
    private JPanel contentPanel;            // 内容面板，用于切换显示
    private JComboBox<String> aiClassComboBox;    // AI分析类选择器
    private JComboBox<String> aiModelComboBox;    // AI分析模型选择器

    public CoverageToolWindowPanel(Project project) {
        super(true, true);
        this.project = project;
        this.coverageService = UtAssistantPlugin.getInstance(project).getJacocoCoverageService();
        if (this.coverageService == null) {
            throw new RuntimeException("JaCoCo覆盖率服务未正确初始化");
        }
        setupUI();
    }

    private void setupUI() {
        setToolbar(createToolbar());
        setContent(createContent());
    }

    private JComponent createContent() {
        // 创建主标签页（报告/修补）
        mainTabbedPane = new JTabbedPane();
        
        // 报告标签页
        mainTabbedPane.addTab("报告", createReportTab());
        
        // 修补标签页
        mainTabbedPane.addTab("修补", createFixTab());

        return mainTabbedPane;
    }

    // 创建报告标签页内容
    private JComponent createReportTab() {
        // 创建主面板
        JPanel reportPanel = new JPanel(new BorderLayout());
        
        // 顶部按钮区域 - AI分析和生成测试报告作为标签页式按钮
        JTabbedPane topTabbedPane = new JTabbedPane();
        
        // AI分析面板 - 专门用于AI分析
        JPanel aiAnalysisPanel = new JPanel(new BorderLayout());
        
        // 创建AI分析控制面板
        JPanel aiControlsPanel = new JPanel(new BorderLayout());
        aiControlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建顶部按钮和选择器面板
        JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton aiAnalysisButton = new JButton("AI分析");
        aiAnalysisButton.addActionListener(e -> performAIAnalysis());
        
        JLabel classLabel = new JLabel("选择类:");
        aiClassComboBox = new JComboBox<>();
        aiClassComboBox.setPreferredSize(new Dimension(200, 25));
        // 填充类列表
        populateClassComboBox(aiClassComboBox);
        
        JLabel modelLabel = new JLabel("选择模型:");
        String[] models = {"GPT-4", "GPT-5", "DeepSeek", "Claude-3", "Gemini Pro"};
        aiModelComboBox = new JComboBox<>(models);
        aiModelComboBox.setPreferredSize(new Dimension(120, 25));
        
        topControlsPanel.add(aiAnalysisButton);
        topControlsPanel.add(Box.createHorizontalStrut(15));
        topControlsPanel.add(classLabel);
        topControlsPanel.add(aiClassComboBox);
        topControlsPanel.add(Box.createHorizontalStrut(15));
        topControlsPanel.add(modelLabel);
        topControlsPanel.add(aiModelComboBox);
        
        // 将顶部控制面板添加到控制面板中
        aiControlsPanel.add(topControlsPanel, BorderLayout.NORTH);
        
        // 分析结果区域
        analysisResultArea = new JEditorPane();
        analysisResultArea.setEditable(false);
        analysisResultArea.setContentType("text/html;charset=utf-8");
        analysisResultArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        
        // 创建带滚动条的分析结果面板
        JScrollPane analysisScrollPane = new JScrollPane(analysisResultArea);
        analysisScrollPane.setBorder(BorderFactory.createTitledBorder("分析结果"));
        
        aiAnalysisPanel.add(aiControlsPanel, BorderLayout.NORTH);
        aiAnalysisPanel.add(analysisScrollPane, BorderLayout.CENTER);
        
        // 生成测试报告面板 - 专门用于生成测试报告
        JPanel generateReportPanel = new JPanel(new BorderLayout());
        
        // "生成全局测试报告"按钮
        JPanel generateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        generateButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JButton generateGlobalReportButton = new JButton("生成全局测试报告");
        generateGlobalReportButton.addActionListener(e -> generateCoverageReport());
        generateButtonPanel.add(generateGlobalReportButton);
        
        // 覆盖率摘要按钮区域 - 3个按钮水平对齐
        JPanel summaryButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        summaryButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton coverageSummaryButton = new JButton("覆盖率摘要");
        JButton detailedReportButton = new JButton("详细报告");
        JButton downloadReportButton = new JButton("下载报告");
        
        // 添加按钮事件
        coverageSummaryButton.addActionListener(e -> showCoverageSummary());
        detailedReportButton.addActionListener(e -> showDetailedReport());
        downloadReportButton.addActionListener(e -> downloadCoverageReport());
        
        summaryButtonsPanel.add(coverageSummaryButton);
        summaryButtonsPanel.add(Box.createHorizontalStrut(15));
        summaryButtonsPanel.add(detailedReportButton);
        summaryButtonsPanel.add(Box.createHorizontalStrut(15));
        summaryButtonsPanel.add(downloadReportButton);
        
        // 创建覆盖率摘要面板和详细报告面板
        coverageSummaryPanel = new JPanel(new BorderLayout());
        coverageSummaryPanel.setBorder(BorderFactory.createTitledBorder("覆盖率摘要"));
        
        // 覆盖率树形结构
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("项目: " + project.getName());
        coverageTree = new JTree(new DefaultTreeModel(rootNode));
        coverageTree.setCellRenderer(new CoverageTreeCellRenderer());
        coverageTree.setShowsRootHandles(true);
        coverageTree.expandRow(0); // 默认展开根节点
        
        coverageSummaryPanel.add(new JBScrollPane(coverageTree), BorderLayout.CENTER);
        
        // 详细报告面板
        detailedReportPanel = new JPanel(new BorderLayout());
        detailedReportPanel.setBorder(BorderFactory.createTitledBorder("详细报告"));
        
        reportArea = new JEditorPane();
        reportArea.setEditable(false);
        reportArea.setContentType("text/html;charset=utf-8");
        reportArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        
        detailedReportPanel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        
        // 创建内容面板，用于切换显示覆盖率摘要和详细报告
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(coverageSummaryPanel, BorderLayout.CENTER);
        
        // 创建一个垂直布局的面板来包含按钮和内容
        JPanel buttonAndContentPanel = new JPanel();
        buttonAndContentPanel.setLayout(new BoxLayout(buttonAndContentPanel, BoxLayout.Y_AXIS));
        
        // 将按钮面板添加到布局中
        buttonAndContentPanel.add(generateButtonPanel);
        buttonAndContentPanel.add(summaryButtonsPanel);
        
        // 创建一个可滚动的面板来包含内容区域
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(contentPanel, BorderLayout.CENTER);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // 使用JSplitPane来分隔按钮和内容区域，确保内容区域可以拉伸
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(buttonAndContentPanel);
        splitPane.setBottomComponent(contentWrapper);
        splitPane.setDividerLocation(120); // 设置初始分割位置
        splitPane.setResizeWeight(0.15); // 让内容区域占用更多空间
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        
        generateReportPanel.add(splitPane, BorderLayout.CENTER);
        
        // 添加到标签页
        topTabbedPane.addTab("AI分析", aiAnalysisPanel);
        topTabbedPane.addTab("生成测试报告", generateReportPanel);
        
        // 中间内容区域 - 覆盖率树形结构
        reportPanel.add(topTabbedPane, BorderLayout.CENTER);
        
        return reportPanel;
    }

    // 显示覆盖率摘要
    private void showCoverageSummary() {
        contentPanel.removeAll();
        contentPanel.add(coverageSummaryPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    // 显示详细报告
    private void showDetailedReport() {
        contentPanel.removeAll();
        contentPanel.add(detailedReportPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // 创建修补标签页内容
    private JComponent createFixTab() {
        // 创建主面板
        JPanel fixPanel = new JPanel(new BorderLayout());
        
        // 修补按钮区域 - 作为标签页式按钮
        JTabbedPane fixTabbedPane = new JTabbedPane();
        fixTabbedPane.setPreferredSize(new Dimension(800, 400));
        
        // 全局快速修补面板
        JPanel globalFixPanel = new JPanel(new BorderLayout());
        globalFixPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel globalButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton globalQuickFixButton = new JButton("全局快速修补");
        globalQuickFixButton.addActionListener(e -> performGlobalQuickFix());
        
        JProgressBar globalFixProgressBar = new JProgressBar();
        globalFixProgressBar.setStringPainted(true);
        globalFixProgressBar.setString("全局修补进度: 0%");
        
        globalButtonPanel.add(globalQuickFixButton);
        globalButtonPanel.add(Box.createHorizontalStrut(15));
        globalButtonPanel.add(new JLabel("全局修补进度:"));
        globalButtonPanel.add(globalFixProgressBar);
        
        globalFixPanel.add(globalButtonPanel, BorderLayout.NORTH);
        globalFixPanel.add(new JLabel("全局修补结果将显示在此处"), BorderLayout.CENTER);
        
        // 详细修补面板
        JPanel detailedFixPanel = new JPanel(new BorderLayout());
        detailedFixPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel detailedButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton detailedFixButton = new JButton("详细修补");
        detailedFixButton.addActionListener(e -> performDetailedFix());
        
        JProgressBar detailedFixProgressBar = new JProgressBar();
        detailedFixProgressBar.setStringPainted(true);
        detailedFixProgressBar.setString("详细修补进度: 0%");
        
        detailedButtonPanel.add(detailedFixButton);
        detailedButtonPanel.add(Box.createHorizontalStrut(15));
        detailedButtonPanel.add(new JLabel("详细修补进度:"));
        detailedButtonPanel.add(detailedFixProgressBar);
        
        // 详细修补配置区域 - 使用网格布局以获得更好的视觉效果
        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 添加组件间距
        
        // 输入文件夹选择
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel inputFolderLabel = new JLabel("输入文件夹:");
        configPanel.add(inputFolderLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel inputFolderContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField inputFolderField = new JTextField(20);
        inputFolderField.setPreferredSize(new Dimension(250, 25));
        JButton inputFolderButton = new JButton("浏览...");
        inputFolderButton.addActionListener(e -> browseFolder(inputFolderField));
        inputFolderContentPanel.add(inputFolderField);
        inputFolderContentPanel.add(inputFolderButton);
        configPanel.add(inputFolderContentPanel, gbc);
        
        // 模型选择
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel modelLabel = new JLabel("选择模型:");
        configPanel.add(modelLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        String[] models = {"GPT-4", "GPT-5", "DeepSeek", "Claude-3", "Gemini Pro"};
        JComboBox<String> modelComboBox = new JComboBox<>(models);
        modelComboBox.setPreferredSize(new Dimension(150, 25));
        configPanel.add(modelComboBox, gbc);
        
        // 输出文件夹选择
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel outputFolderLabel = new JLabel("输出文件夹:");
        configPanel.add(outputFolderLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel outputFolderContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField outputFolderField = new JTextField(20);
        outputFolderField.setPreferredSize(new Dimension(250, 25));
        JButton outputFolderButton = new JButton("浏览...");
        outputFolderButton.addActionListener(e -> browseFolder(outputFolderField));
        outputFolderContentPanel.add(outputFolderField);
        outputFolderContentPanel.add(outputFolderButton);
        configPanel.add(outputFolderContentPanel, gbc);
        
        detailedFixPanel.add(detailedButtonPanel, BorderLayout.NORTH);
        detailedFixPanel.add(configPanel, BorderLayout.CENTER);
        
        // 添加滚动面板以处理空间不足
        JScrollPane detailedScrollPane = new JScrollPane(detailedFixPanel);
        detailedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // 添加到标签页
        fixTabbedPane.addTab("全局快速修补", globalFixPanel);
        fixTabbedPane.addTab("详细修补", detailedScrollPane);
        
        // 组装修补面板
        fixPanel.add(fixTabbedPane, BorderLayout.CENTER);
        
        return fixPanel;
    }

    private JComponent createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 创建设置按钮
        JButton settingsButton = new JButton("⚙️"); // 设置按钮
        settingsButton.setPreferredSize(new Dimension(30, 30));
        settingsButton.addActionListener(e -> showSettingsMenu(settingsButton));

        statusLabel = new JLabel("就绪");
        statusLabel.setPreferredSize(new Dimension(200, 20));

        // 添加填充以保持布局
        toolbar.add(settingsButton);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(statusLabel);

        return toolbar;
    }

    private void showSettingsMenu(Component component) {
        JPopupMenu settingsMenu = new JPopupMenu();
        
        JMenuItem modelSettingsItem = new JMenuItem("模型设置");
        modelSettingsItem.addActionListener(e -> openModelSettings());
        settingsMenu.add(modelSettingsItem);
        
        JMenuItem reportSettingsItem = new JMenuItem("测试报告设置");
        reportSettingsItem.addActionListener(e -> openReportSettings());
        settingsMenu.add(reportSettingsItem);
        
        JMenuItem ruleSettingsItem = new JMenuItem("规则设置");
        ruleSettingsItem.addActionListener(e -> openRuleSettings());
        settingsMenu.add(ruleSettingsItem);
        
        JMenuItem agentSettingsItem = new JMenuItem("Agent设置");
        agentSettingsItem.addActionListener(e -> openAgentSettings());
        settingsMenu.add(agentSettingsItem);
        
        settingsMenu.show(component, 0, component.getHeight());
    }
    
    private void openModelSettings() {
        // 打开模型设置
        JOptionPane.showMessageDialog(this, "模型设置功能将在后续版本中实现");
    }
    
    private void openReportSettings() {
        // 打开测试报告设置
        JOptionPane.showMessageDialog(this, "测试报告设置功能将在后续版本中实现");
    }
    
    private void openRuleSettings() {
        // 打开规则设置
        JOptionPane.showMessageDialog(this, "规则设置功能将在后续版本中实现");
    }
    
    private void openAgentSettings() {
        // 打开Agent设置
        JOptionPane.showMessageDialog(this, "Agent设置功能将在后续版本中实现");
    }

    private void performAIAnalysis() {
        // 获取当前选择的类和模型
        if (aiClassComboBox == null || aiModelComboBox == null) {
            String analysisResult = "<html><head><meta charset=\"utf-8\"></head><body>" +
                    "<div style=\"padding: 20px; font-family: Consolas, 'Courier New', monospace; background-color: #272822; color: #f8f8f2;\">" +
                    "<h2 style=\"color: #f92672;\">错误：无法获取选择器</h2>" +
                    "<p>无法找到类选择器或模型选择器，请刷新插件界面。</p>" +
                    "</div></body></html>";
            analysisResultArea.setText(analysisResult);
            return;
        }
        
        String selectedClass = (String) aiClassComboBox.getSelectedItem();
        String selectedModel = (String) aiModelComboBox.getSelectedItem();
        
        if (selectedClass == null || selectedClass.isEmpty()) {
            String analysisResult = "<html><head><meta charset=\"utf-8\"></head><body>" +
                    "<div style=\"padding: 20px; font-family: Consolas, 'Courier New', monospace; background-color: #272822; color: #f8f8f2;\">" +
                    "<h2 style=\"color: #f92672;\">错误：未选择类</h2>" +
                    "<p>请从下拉列表中选择一个要分析的类。</p>" +
                    "</div></body></html>";
            analysisResultArea.setText(analysisResult);
            return;
        }
        
        // 模拟AI分析
        String analysisResult = generateAIAnalysisReport(selectedClass, selectedModel);
        analysisResultArea.setText(analysisResult);
    }
    
    // 生成AI分析报告
    private String generateAIAnalysisReport(String className, String model) {
        // 模拟分析结果
        String methodName = "processUserInput"; // 假设检测到的方法
        String potentialIssue = "Null pointer exception可能在未验证输入参数时发生";
        String coverageIssue = "该方法的分支覆盖率仅为45%，存在未覆盖的边界条件";
        String suggestion = "建议添加针对null值、空字符串和边界条件的测试用例";
        
        return "<html><head><meta charset=\"utf-8\"><style>" +
               "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background-color: #f5f5f5; color: #333333; }" +
               ".report-container { background-color: #ffffff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; border-radius: 6px; margin-bottom: 20px; }" +
               ".section { margin: 15px 0; padding: 15px; border-left: 4px solid #3498db; background-color: #f8f9fa; border-radius: 0 4px 4px 0; }" +
               ".warning { border-left-color: #f39c12; background-color: #fef9e7; }" +
               ".error { border-left-color: #e74c3c; background-color: #fadbd8; }" +
               ".info { border-left-color: #3498db; background-color: #d6eaf8; }" +
               ".suggestion { border-left-color: #27ae60; background-color: #d5f4e6; }" +
               ".code-block { background-color: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 4px; font-family: 'Consolas', 'Courier New', monospace; overflow-x: auto; margin: 10px 0; }" +
               ".highlight { background-color: #f1c40f; color: #2c3e50; padding: 2px 4px; border-radius: 3px; }" +
               ".stats { display: flex; justify-content: space-around; flex-wrap: wrap; margin: 20px 0; }" +
               ".stat-card { background: linear-gradient(135deg, #74ebd5 0%, #9face6 100%); padding: 15px; border-radius: 8px; text-align: center; min-width: 120px; margin: 5px; }" +
               ".stat-value { font-size: 24px; font-weight: bold; color: #2c3e50; }" +
               ".stat-label { font-size: 14px; color: #7f8c8d; }" +
               "h2 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 5px; }" +
               "h3 { color: #34495e; margin-top: 15px; }" +
               "ul { padding-left: 20px; }" +
               "li { margin: 8px 0; }" +
               "</style></head><body>" +
               "<div class=\"report-container\">" +
               "<div class=\"header\">" +
               "<h1>AI分析报告</h1>" +
               "<p><strong>分析类:</strong> " + className + "</p>" +
               "<p><strong>使用模型:</strong> " + model + "</p>" +
               "<p><strong>分析时间:</strong> " + new java.util.Date() + "</p>" +
               "</div>" +
               
               "<div class=\"stats\">" +
               "<div class=\"stat-card\">" +
               "<div class=\"stat-value\">67%</div>" +
               "<div class=\"stat-label\">方法覆盖率</div>" +
               "</div>" +
               "<div class=\"stat-card\">" +
               "<div class=\"stat-value\">45%</div>" +
               "<div class=\"stat-label\">分支覆盖率</div>" +
               "</div>" +
               "<div class=\"stat-card\">" +
               "<div class=\"stat-value\">3</div>" +
               "<div class=\"stat-label\">高危问题</div>" +
               "</div>" +
               "<div class=\"stat-card\">" +
               "<div class=\"stat-value\">5</div>" +
               "<div class=\"stat-label\">改进建议</div>" +
               "</div>" +
               "</div>" +
               
               "<div class=\"section error\">" +
               "<h3>⚠️ 检测到高危问题</h3>" +
               "<p><strong>方法:</strong> <span class=\"highlight\">" + methodName + "</span></p>" +
               "<p><strong>问题描述:</strong> " + potentialIssue + "</p>" +
               "<div class=\"code-block\">" +
               "// 问题代码示例:\\n" +
               "public String " + methodName + "(String input) {\\n" +
               "  return input.trim().toUpperCase(); // 当input为null时会抛出NPE\\n" +
               "}" +
               "</div>" +
               "</div>" +
               
               "<div class=\"section warning\">" +
               "<h3>⚠️ 覆盖率不足</h3>" +
               "<p>" + coverageIssue + "</p>" +
               "<ul>" +
               "<li>缺少对空值输入的测试</li>" +
               "<li>缺少对边界值的测试（如最大/最小值）</li>" +
               "<li>缺少对异常路径的测试</li>" +
               "</ul>" +
               "</div>" +
               
               "<div class=\"section suggestion\">" +
               "<h3>💡 单元测试建议</h3>" +
               "<p>" + suggestion + "</p>" +
               "<div class=\"code-block\">" +
               "@Test\\n" +
               "public void test" + methodName + "_WithNullInput() {\\n" +
               "  // 预期抛出NullPointerException或返回默认值\\n" +
               "  assertThrows(NullPointerException.class, () -> {\\n" +
               "    obj." + methodName + "(null);\\n" +
               "  });\\n" +
               "}\\n\\n" +
               "@Test\\n" +
               "public void test" + methodName + "_WithEmptyString() {\\n" +
               "  String result = obj." + methodName + "(\"\"));\\n" +
               "  assertEquals(\"\", result); // 或其他期望值\\n" +
               "}" +
               "</div>" +
               "</div>" +
               
               "<div class=\"section info\">" +
               "<h3>📋 分析摘要</h3>" +
               "<ul>" +
               "<li>检测到 <span class=\"highlight\">1</span> 个高风险方法</li>" +
               "<li>发现 <span class=\"highlight\">3</span> 个潜在的空指针问题</li>" +
               "<li>建议增加 <span class=\"highlight\">7</span> 个新的测试用例</li>" +
               "<li>覆盖率可提升 <span class=\"highlight\">28%</span> 通过新增测试</li>" +
               "</ul>" +
               "</div>" +
               "</div>" +
               "</body></html>";
    }

    private void generateCoverageReport() {
        statusLabel.setText("正在生成全局覆盖率报告...");
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // 自动执行测试并生成覆盖率数据
                String projectPath = project.getBasePath();
                
                try {
                    statusLabel.setText("正在分析项目类文件...");
                    
                    // 获取覆盖率服务并执行分析
                    coverageService.executeTestsAndGenerateCoverage(projectPath);
                    
                    // 获取分析后的覆盖率数据
                    List<CoverageData> coverageDataList = coverageService.getAllCoverageData();
                    
                    // 生成HTML格式的报告
                    StringBuilder htmlReport = new StringBuilder();
                    htmlReport.append("<!DOCTYPE html>");
                    htmlReport.append("<html><head><meta charset=\"utf-8\"><style>");
                    htmlReport.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background-color: #ffffff; color: #333333; }");
                    htmlReport.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; font-size: 24px; }");
                    htmlReport.append("h2 { color: #34495e; margin-top: 25px; font-size: 20px; }");
                    htmlReport.append("h3 { color: #34495e; margin-top: 20px; font-size: 18px; }");
                    htmlReport.append("p { line-height: 1.6; margin: 8px 0; }");
                    htmlReport.append("table { border-collapse: collapse; width: 100%; margin: 15px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }");
                    htmlReport.append("th, td { border: 1px solid #e0e0e0; padding: 12px 15px; text-align: left; }");
                    htmlReport.append("th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-weight: bold; }");
                    htmlReport.append("tr:nth-child(even) { background-color: #f8f9fa; }");
                    htmlReport.append("tr:hover { background-color: #f1f7fd; }");
                    htmlReport.append(".coverage-high { background-color: #e8f5e8; color: #2e7d32; font-weight: bold; }"); // 浅绿色背景
                    htmlReport.append(".coverage-medium { background-color: #e3f2fd; color: #1976d2; font-weight: bold; }"); // 天蓝色背景
                    htmlReport.append(".coverage-low { background-color: #fff8e1; color: #f57f17; font-weight: bold; }"); // 浅黄色背景
                    htmlReport.append(".coverage-very-low { background-color: #ffebee; color: #d32f2f; font-weight: bold; }"); // 橙色背景
                    htmlReport.append(".coverage-critical { background-color: #ffcdd2; color: #c62828; font-weight: bold; }"); // 浅红色背景
                    htmlReport.append(".folder-header { background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%); color: white; font-weight: bold; padding: 12px; margin: 15px 0 10px 0; border-radius: 6px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
                    htmlReport.append(".stats-container { background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #3498db; }");
                    htmlReport.append(".suggestion { background-color: #e8f4fd; padding: 12px; border-radius: 6px; margin: 10px 0; border-left: 4px solid #55acee; }");
                    htmlReport.append(".suggestion-title { font-weight: bold; color: #1976d2; margin-bottom: 5px; }");
                    htmlReport.append(".report-header { background: linear-gradient(135deg, #74b9ff 0%, #a29bfe 100%); padding: 20px; border-radius: 10px; margin-bottom: 20px; color: white; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }");
                    htmlReport.append(".report-info { margin: 5px 0; }");
                    htmlReport.append("ul { padding-left: 20px; }");
                    htmlReport.append("li { margin: 5px 0; }");
                    htmlReport.append("hr { border: 0; height: 1px; background: #e0e0e0; margin: 20px 0; }");
                    htmlReport.append("</style></head><body>");
                    
                    htmlReport.append("<div class='report-header'>");
                    htmlReport.append("<h1>").append(project.getName()).append(" 项目测试覆盖率报告</h1>");
                    htmlReport.append("<p class='report-info'><strong>项目:</strong> ").append(project.getName()).append("</p>");
                    htmlReport.append("<p class='report-info'><strong>时间:</strong> ").append(new java.util.Date()).append("</p>");
                    
                    // 计算总体统计信息
                    if (!coverageDataList.isEmpty()) {
                        double totalLineCoverage = 0;
                        double totalBranchCoverage = 0;
                        double totalMethodCoverage = 0;
                        double totalClassCoverage = 0;
                        
                        for (CoverageData data : coverageDataList) {
                            totalLineCoverage += data.getLineCoverage();
                            totalBranchCoverage += data.getBranchCoverage();
                            totalMethodCoverage += data.getMethodCoverage();
                            totalClassCoverage += data.getClassCoverage();
                        }
                        
                        double avgLineCoverage = totalLineCoverage / coverageDataList.size();
                        double avgBranchCoverage = totalBranchCoverage / coverageDataList.size();
                        double avgMethodCoverage = totalMethodCoverage / coverageDataList.size();
                        double avgClassCoverage = totalClassCoverage / coverageDataList.size();
                        
                        htmlReport.append("<p class='report-info'><strong>覆盖率统计:</strong> 行覆盖率: ").append(String.format("%.2f%%", avgLineCoverage))
                                 .append(" | 分支覆盖率: ").append(String.format("%.2f%%", avgBranchCoverage))
                                 .append(" | 方法覆盖率: ").append(String.format("%.2f%%", avgMethodCoverage))
                                 .append(" | 类覆盖率: ").append(String.format("%.2f%%", avgClassCoverage)).append("</p>");
                    } else {
                        htmlReport.append("<p class='report-info'><strong>覆盖率统计:</strong> 未找到可分析的类文件或项目尚未编译</p>");
                    }
                    
                    htmlReport.append("</div>");
                    
                    if (!coverageDataList.isEmpty()) {
                        htmlReport.append("<h2>覆盖率详情</h2>");
                        htmlReport.append("<table>");
                        htmlReport.append("<tr><th>类名</th><th>行覆盖率</th><th>分支覆盖率</th><th>方法覆盖率</th><th>类覆盖率</th></tr>");
                        
                        // 添加真实覆盖率数据
                        for (CoverageData data : coverageDataList) {
                            addCoverageRowWithClass(htmlReport, data.getName(), 
                                                  data.getFormattedLineCoverage(), 
                                                  data.getFormattedBranchCoverage(),
                                                  data.getFormattedMethodCoverage(),
                                                  data.getFormattedClassCoverage());
                        }
                        
                        htmlReport.append("</table>");
                        
                        // 按包名分组显示
                        htmlReport.append("<h2>按包分组的覆盖率</h2>");
                        
                        // 按包名分组
                        java.util.Map<String, java.util.List<CoverageData>> groupedByPackage = new java.util.HashMap<>();
                        for (CoverageData data : coverageDataList) {
                            String className = data.getName();
                            String packageName = className.contains("/") ? 
                                className.substring(0, className.lastIndexOf("/")) : 
                                className.contains(".") ? 
                                    className.substring(0, className.lastIndexOf(".")) : 
                                    "default";
                                    
                            groupedByPackage.computeIfAbsent(packageName, k -> new java.util.ArrayList<>()).add(data);
                        }
                        
                        // 为每个包生成报告
                        for (java.util.Map.Entry<String, java.util.List<CoverageData>> entry : groupedByPackage.entrySet()) {
                            String packageName = entry.getKey();
                            java.util.List<CoverageData> packageDataList = entry.getValue();
                            
                            htmlReport.append("<div class='folder-header'>").append(packageName).append("</div>");
                            htmlReport.append("<table>");
                            htmlReport.append("<tr><th>类名</th><th>行覆盖率</th><th>分支覆盖率</th><th>方法覆盖率</th><th>类覆盖率</th></tr>");
                            
                            for (CoverageData data : packageDataList) {
                                addCoverageRowWithClass(htmlReport, 
                                                      data.getName().substring(data.getName().lastIndexOf('.') + 1), 
                                                      data.getFormattedLineCoverage(), 
                                                      data.getFormattedBranchCoverage(),
                                                      data.getFormattedMethodCoverage(),
                                                      data.getFormattedClassCoverage());
                            }
                            
                            htmlReport.append("</table>");
                        }
                    } else {
                        htmlReport.append("<h2>覆盖率详情</h2>");
                        htmlReport.append("<p>未找到可分析的类文件。请确保项目已编译且包含.class文件。</p>");
                        htmlReport.append("<p>如果项目尚未编译，请运行构建命令（如 gradle build 或 mvn compile）。</p>");
                    }
                    
                    htmlReport.append("<hr/>");
                    
                    htmlReport.append("<div class='stats-container'>");
                    htmlReport.append("<h2>统计摘要</h2>");
                    htmlReport.append("<ul>");
                    htmlReport.append("<li><strong>总类数:</strong> ").append(coverageDataList.size()).append("</li>");
                    
                    if (!coverageDataList.isEmpty()) {
                        // 计算覆盖不足的类
                        long lowCoverageClasses = coverageDataList.stream()
                            .filter(data -> data.getLineCoverage() < 70)
                            .count();
                        htmlReport.append("<li><strong>覆盖率低于70%的类:</strong> ").append(lowCoverageClasses).append("</li>");
                    } else {
                        htmlReport.append("<li><strong>覆盖率低于70%的类:</strong> N/A (无数据)</li>");
                    }
                    
                    htmlReport.append("</ul>");
                    htmlReport.append("</div>");
                    
                    if (!coverageDataList.isEmpty()) {
                        long lowCoverageClasses = coverageDataList.stream()
                            .filter(data -> data.getLineCoverage() < 70)
                            .count();
                        
                        if (lowCoverageClasses > 0) {
                            htmlReport.append("<h2>覆盖率建议</h2>");
                            htmlReport.append("<div class='suggestion'>");
                            htmlReport.append("<div class='suggestion-title'>需要改进的区域:</div>");
                            htmlReport.append("<ul>");
                            
                            for (CoverageData data : coverageDataList) {
                                if (data.getLineCoverage() < 70) {
                                    htmlReport.append("<li>").append(data.getName()).append(" 行覆盖率: ")
                                             .append(data.getFormattedLineCoverage()).append(" (需要改进)</li>");
                                }
                            }
                            
                            htmlReport.append("</ul>");
                            htmlReport.append("</div>");
                        }
                    }
                    
                    htmlReport.append("</body></html>");
                    
                    return htmlReport.toString();
                } catch (Exception e) {
                    statusLabel.setText("执行测试或分析覆盖率时出错: " + e.getMessage());
                    return "执行测试或分析覆盖率时出错: " + e.getMessage();
                }
            }

            private void addCoverageRowWithClass(StringBuilder html, String className, String lineCoverage, 
                                               String branchCoverage, String methodCoverage, String classCoverage) {
                html.append("<tr>");
                html.append("<td>").append(className).append("</td>");
                html.append("<td class='").append(getCoverageClass(lineCoverage)).append("'>").append(lineCoverage).append("</td>");
                html.append("<td class='").append(getCoverageClass(branchCoverage)).append("'>").append(branchCoverage).append("</td>");
                html.append("<td class='").append(getCoverageClass(methodCoverage)).append("'>").append(methodCoverage).append("</td>");
                html.append("<td class='").append(getCoverageClass(classCoverage)).append("'>").append(classCoverage).append("</td>");
                html.append("</tr>");
            }
            
            private String getCoverageClass(String coverageStr) {
                if (coverageStr.endsWith("%")) {
                    try {
                        double coverage = Double.parseDouble(coverageStr.substring(0, coverageStr.length() - 1));
                        if (coverage == 100.0) {
                            return "coverage-high";
                        } else if (coverage >= 75.0) {
                            return "coverage-medium";
                        } else if (coverage >= 50.0) {
                            return "coverage-low";
                        } else if (coverage >= 25.0) {
                            return "coverage-very-low";
                        } else {
                            return "coverage-critical";
                        }
                    } catch (NumberFormatException e) {
                        return "";
                    }
                }
                return "";
            }

            @Override
            protected void done() {
                try {
                    String report = get();
                    updateReportText(report);
                    updateCoverageSummary(); // 更新覆盖率摘要
                    if (report.contains("执行测试或分析覆盖率时出错")) {
                        statusLabel.setText("生成报告失败: " + report.substring(report.indexOf("执行测试或分析覆盖率时出错:") + 12));
                    } else {
                        statusLabel.setText("全局报告已生成");
                    }
                } catch (Exception e) {
                    statusLabel.setText("生成报告失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void performGlobalQuickFix() {
        // 模拟全局快速修补
        JOptionPane.showMessageDialog(this, "全局快速修补功能将在此处开始");
    }

    private void performDetailedFix() {
        // 模拟详细修补
        JOptionPane.showMessageDialog(this, "详细修补功能将在此处开始");
    }

    private void browseFolder(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void downloadCoverageReport() {
        // 检查是否有报告内容
        if (reportArea == null || reportArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可下载的报告内容，请先生成报告", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存覆盖率报告");
        fileChooser.setSelectedFile(new File(project.getName() + "_coverage_report.html"));
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave, StandardCharsets.UTF_8)) {
                // 获取当前报告内容并写入文件
                String reportContent = reportArea.getText();
                writer.write(reportContent);
                
                // 获取文件的父目录
                Path parentDir = fileToSave.toPath().getParent();
                if (parentDir != null) {
                    // 在新进程中打开包含文件的文件夹
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        Runtime.getRuntime().exec(new String[]{"explorer", "/select,", fileToSave.getAbsolutePath()});
                    } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                        Runtime.getRuntime().exec(new String[]{"open", parentDir.toString()});
                    } else {
                        Runtime.getRuntime().exec(new String[]{"xdg-open", parentDir.toString()});
                    }
                }
                
                JOptionPane.showMessageDialog(this, "报告已保存到: " + fileToSave.getAbsolutePath() + "\n文件夹已自动打开");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "保存报告时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateCoverageData(Object[][] data) {
        // 创建树形结构
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("项目: " + project.getName());
        
        // 按包名组织数据
        java.util.Map<String, java.util.List<Object[]>> groupedByPackage = new java.util.HashMap<>();
        for (Object[] row : data) {
            String fullClassName = (String) row[0];
            String packageName = fullClassName.contains(".") ? 
                fullClassName.substring(0, fullClassName.lastIndexOf(".")) : 
                "default";
                
            groupedByPackage.computeIfAbsent(packageName, k -> new java.util.ArrayList<>()).add(row);
        }
        
        // 为每个包创建节点
        for (java.util.Map.Entry<String, java.util.List<Object[]>> entry : groupedByPackage.entrySet()) {
            String packageName = entry.getKey();
            java.util.List<Object[]> packageDataList = entry.getValue();
            
            DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode(new CoverageTreeNode(packageName, null));
            
            // 为每个类创建子节点
            for (Object[] classData : packageDataList) {
                DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(
                    new CoverageTreeNode((String) classData[0], new double[]{
                        Double.parseDouble(((String) classData[1]).replace("%", "")),
                        Double.parseDouble(((String) classData[2]).replace("%", "")),
                        Double.parseDouble(((String) classData[3]).replace("%", "")),
                        Double.parseDouble(((String) classData[4]).replace("%", ""))
                    })
                );
                packageNode.add(classNode);
            }
            
            root.add(packageNode);
        }
        
        // 更新树模型
        DefaultTreeModel model = new DefaultTreeModel(root);
        coverageTree.setModel(model);
        
        // 展开所有节点
        for (int i = 0; i < coverageTree.getRowCount(); i++) {
            coverageTree.expandRow(i);
        }
    }

    public void updateReportText(String report) {
        if (reportArea != null) {
            reportArea.setText(report);
            reportArea.setCaretPosition(0); // 滚动到顶部
        }
    }
    
    // 更新覆盖率摘要的方法
    public void updateCoverageSummary() {
        // 获取当前覆盖率数据并更新树形结构
        List<CoverageData> coverageDataList = coverageService.getAllCoverageData();
        
        // 创建树形结构
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("项目: " + project.getName());
        
        // 按包名组织数据
        java.util.Map<String, java.util.List<CoverageData>> groupedByPackage = new java.util.HashMap<>();
        for (CoverageData data : coverageDataList) {
            String fullClassName = data.getName();
            String packageName = fullClassName.contains("/") ? 
                fullClassName.substring(0, fullClassName.lastIndexOf("/")) : 
                fullClassName.contains(".") ? 
                    fullClassName.substring(0, fullClassName.lastIndexOf(".")) : 
                    "default";
                    
            groupedByPackage.computeIfAbsent(packageName, k -> new java.util.ArrayList<>()).add(data);
        }
        
        // 为每个包创建节点
        for (java.util.Map.Entry<String, java.util.List<CoverageData>> entry : groupedByPackage.entrySet()) {
            String packageName = entry.getKey();
            java.util.List<CoverageData> packageDataList = entry.getValue();
            
            DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode(new CoverageTreeNode(packageName, null));
            
            // 为每个类创建子节点
            for (CoverageData data : packageDataList) {
                DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(
                    new CoverageTreeNode(data.getName(), new double[]{
                        data.getLineCoverage(), // 行覆盖率
                        data.getBranchCoverage(), // 分支覆盖率
                        data.getMethodCoverage(), // 方法覆盖率
                        data.getClassCoverage() // 类覆盖率
                    })
                );
                packageNode.add(classNode);
            }
            
            root.add(packageNode);
        }
        
        // 更新树模型
        DefaultTreeModel model = new DefaultTreeModel(root);
        coverageTree.setModel(model);
        
        // 展开所有节点
        for (int i = 0; i < coverageTree.getRowCount(); i++) {
            coverageTree.expandRow(i);
        }
    }
    
    // 覆盖率树节点数据类
    static class CoverageTreeNode {
        String name;
        double[] coverageData; // [line, branch, method, class]
        
        public CoverageTreeNode(String name, double[] coverageData) {
            this.name = name;
            this.coverageData = coverageData;
        }
        
        public String toString() {
            if (coverageData != null) {
                return String.format("%s (%.2f%%, %.2f%%, %.2f%%, %.2f%%)", 
                    name, coverageData[0], coverageData[1], coverageData[2], coverageData[3]);
            } else {
                return name;
            }
        }
        
        public String getName() {
            return name;
        }
        
        public double[] getCoverageData() {
            return coverageData;
        }
    }
    
    // 自定义树单元格渲染器
    class CoverageTreeCellRenderer extends JPanel implements TreeCellRenderer {
        private final JLabel iconLabel = new JLabel();
        private final JLabel textLabel = new JLabel();
        private final JPanel coveragePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        public CoverageTreeCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            
            // 设置覆盖率面板布局
            coveragePanel.setOpaque(false);
            
            add(iconLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
            add(coveragePanel, BorderLayout.EAST);
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();
            
            // 重置组件
            iconLabel.setIcon(null);
            coveragePanel.removeAll();
            
            if (obj instanceof CoverageTreeNode) {
                CoverageTreeNode coverageNode = (CoverageTreeNode) obj;
                
                // 设置节点文本
                textLabel.setText(coverageNode.getName());
                
                // 根据覆盖率设置背景色
                if (coverageNode.getCoverageData() != null) {
                    // 类节点，显示覆盖率信息
                    double[] coverageData = coverageNode.getCoverageData();
                    double lineCoverage = coverageData[0];
                    
                    if (selected) {
                        setBackground(UIManager.getColor("Tree.selectionBackground"));
                        textLabel.setForeground(UIManager.getColor("Tree.selectionForeground"));
                    } else {
                        // 根据行覆盖率设置背景色
                        if (lineCoverage >= 90) {
                            setBackground(new Color(0xe8f5e8)); // 浅绿色
                            textLabel.setForeground(new Color(0x2e7d32));
                        } else if (lineCoverage >= 75) {
                            setBackground(new Color(0xe3f2fd)); // 天蓝色
                            textLabel.setForeground(new Color(0x1976d2));
                        } else if (lineCoverage >= 50) {
                            setBackground(new Color(0xfff8e1)); // 浅黄色
                            textLabel.setForeground(new Color(0xf57f17));
                        } else if (lineCoverage >= 25) {
                            setBackground(new Color(0xffebee)); // 淡红色
                            textLabel.setForeground(new Color(0xd32f2f));
                        } else {
                            setBackground(new Color(0xffcdd2)); // 浅红色
                            textLabel.setForeground(new Color(0xc62828));
                        }
                    }
                    
                    // 添加覆盖率信息到面板
                    String coverageText = String.format("行:%.1f%% | 分支:%.1f%% | 方法:%.1f%% | 类:%.1f%%", 
                        coverageData[0], coverageData[1], coverageData[2], coverageData[3]);
                    JLabel coverageLabel = new JLabel(coverageText);
                    coverageLabel.setFont(tree.getFont().deriveFont(10f));
                    coveragePanel.add(coverageLabel);
                    
                    // 设置图标
                    iconLabel.setText("  📄 "); // 文件图标
                } else {
                    // 包节点
                    if (selected) {
                        setBackground(UIManager.getColor("Tree.selectionBackground"));
                        textLabel.setForeground(UIManager.getColor("Tree.selectionForeground"));
                    } else {
                        setBackground(tree.getBackground());
                        textLabel.setForeground(tree.getForeground());
                    }
                    
                    // 设置包图标
                    iconLabel.setText("  📁 "); // 文件夹图标
                }
            } else {
                textLabel.setText(value.toString());
                if (selected) {
                    setBackground(UIManager.getColor("Tree.selectionBackground"));
                    textLabel.setForeground(UIManager.getColor("Tree.selectionForeground"));
                } else {
                    setBackground(tree.getBackground());
                    textLabel.setForeground(tree.getForeground());
                }
                
                // 设置项目图标
                iconLabel.setText("  📚 "); // 项目图标
            }
            
            textLabel.setFont(tree.getFont());
            
            return this;
        }
    }
    
    // 填充类选择器的方法
    private void populateClassComboBox(JComboBox<String> classComboBox) {
        // 清空现有的项
        classComboBox.removeAllItems();
        
        // 获取项目中的所有类文件
        List<String> classNames = findClassNamesInProject();
        
        // 添加到选择器中
        for (String className : classNames) {
            classComboBox.addItem(className);
        }
    }
    
    // 查找项目中的所有类名
    private List<String> findClassNamesInProject() {
        List<String> classNames = new java.util.ArrayList<>();
        
        // 获取项目根路径
        String projectPath = project.getBasePath();
        if (projectPath != null) {
            // 查找源代码目录
            String[] sourceDirs = {
                projectPath + "/src/main/java",
                projectPath + "/src/test/java",
                projectPath + "/src/main/kotlin",
                projectPath + "/src/test/kotlin"
            };
            
            for (String sourceDir : sourceDirs) {
                findJavaFilesRecursively(new File(sourceDir), classNames);
            }
        }
        
        return classNames;
    }
    
    // 递归查找Java文件
    private void findJavaFilesRecursively(File dir, List<String> classNames) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                findJavaFilesRecursively(file, classNames);
            } else if (file.getName().endsWith(".java")) {
                // 获取相对于源代码目录的路径，并转换为类名
                String className = file.getAbsolutePath();
                
                // 移除项目路径前缀
                String projectPath = project.getBasePath();
                if (className.startsWith(projectPath + "/src/main/java/")) {
                    className = className.substring((projectPath + "/src/main/java/").length());
                } else if (className.startsWith(projectPath + "/src/test/java/")) {
                    className = className.substring((projectPath + "/src/test/java/").length());
                } else if (className.startsWith(projectPath + "/src/main/kotlin/")) {
                    className = className.substring((projectPath + "/src/main/kotlin/").length());
                } else if (className.startsWith(projectPath + "/src/test/kotlin/")) {
                    className = className.substring((projectPath + "/src/test/kotlin/").length());
                }
                
                // 将路径转换为类名
                className = className.replace(File.separatorChar, '.').replace(".java", "");
                
                // 确保只添加有效的类名
                if (!className.isEmpty() && !className.contains(" ")) {
                    classNames.add(className);
                }
            }
        }
    }
}
