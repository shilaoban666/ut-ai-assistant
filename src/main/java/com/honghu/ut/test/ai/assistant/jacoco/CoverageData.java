package com.honghu.ut.test.ai.assistant.jacoco;

/**
 * 覆盖率数据类
 * 存储类或方法的覆盖率信息
 * 
 * 功能：
 * - 保存覆盖率数据（指令、分支、行、方法、类覆盖率）
 * - 提供格式化的覆盖率显示方法
 */
public class CoverageData {
    private final String name;           // 类或方法名称
    private final double instructionCoverage;  // 指令覆盖率
    private final double branchCoverage;       // 分支覆盖率
    private final double lineCoverage;         // 行覆盖率
    private final double methodCoverage;       // 方法覆盖率
    private final double classCoverage;        // 类覆盖率

    public CoverageData(String name, double instructionCoverage, double branchCoverage, double lineCoverage) {
        this.name = name;
        this.instructionCoverage = instructionCoverage;
        this.branchCoverage = branchCoverage;
        this.lineCoverage = lineCoverage;
        // 为向后兼容，设置默认值
        this.methodCoverage = 0.0;
        this.classCoverage = 0.0;
    }

    public CoverageData(String name, double instructionCoverage, double branchCoverage, double lineCoverage, 
                       double methodCoverage, double classCoverage) {
        this.name = name;
        this.instructionCoverage = instructionCoverage;
        this.branchCoverage = branchCoverage;
        this.lineCoverage = lineCoverage;
        this.methodCoverage = methodCoverage;
        this.classCoverage = classCoverage;
    }

    public String getName() {
        return name;
    }

    public double getInstructionCoverage() {
        return instructionCoverage;
    }

    public double getBranchCoverage() {
        return branchCoverage;
    }

    public double getLineCoverage() {
        return lineCoverage;
    }

    public double getMethodCoverage() {
        return methodCoverage;
    }

    public double getClassCoverage() {
        return classCoverage;
    }

    public String getFormattedLineCoverage() {
        return String.format("%.2f%%", lineCoverage);
    }

    public String getFormattedBranchCoverage() {
        return String.format("%.2f%%", branchCoverage);
    }

    public String getFormattedInstructionCoverage() {
        return String.format("%.2f%%", instructionCoverage);
    }

    public String getFormattedMethodCoverage() {
        return String.format("%.2f%%", methodCoverage);
    }

    public String getFormattedClassCoverage() {
        return String.format("%.2f%%", classCoverage);
    }
}