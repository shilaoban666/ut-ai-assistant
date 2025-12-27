package com.honghu.ut.test.ai.assistant.services;

import com.honghu.ut.test.ai.assistant.jacoco.JacocoCoverageService;
import com.honghu.ut.test.ai.assistant.testgen.UnitTestGenerationService;
import com.intellij.openapi.project.Project;

/**
 * 服务管理器
 * 协调不同功能的中央服务管理器
 * 
 * 功能：
 * - 初始化和管理覆盖率服务
 * - 初始化和管理测试生成服务
 * - 协调完整的分析工作流程
 */
public class ServiceManager {
    private final Project project;
    private JacocoCoverageService coverageService;        // 覆盖率服务
    private UnitTestGenerationService testGenerationService; // 测试生成服务

    public ServiceManager(Project project) {
        this.project = project;
        initializeServices();
    }

    private void initializeServices() {
        coverageService = new JacocoCoverageService(project);
        testGenerationService = new UnitTestGenerationService(project);
    }

    public JacocoCoverageService getCoverageService() {
        return coverageService;
    }

    public UnitTestGenerationService getTestGenerationService() {
        return testGenerationService;
    }

    /**
     * 运行完整分析：执行测试，收集覆盖率，生成报告
     */
    public void runFullAnalysis() {
        // 这将协调完整的工作流程：
        // 1. 运行测试
        // 2. 收集覆盖率数据
        // 3. 生成报告
        // 4. 更新 UI
    }

    /**
     * 生成测试并运行覆盖率分析
     */
    public void generateTestsAndAnalyze() {
        // 这将：
        // 1. 生成测试
        // 2. 运行生成的测试
        // 3. 收集覆盖率
        // 4. 生成报告
    }
}