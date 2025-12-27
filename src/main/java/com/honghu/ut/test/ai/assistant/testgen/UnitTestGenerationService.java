package com.honghu.ut.test.ai.assistant.testgen;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.openapi.diagnostic.Logger;

/**
 * 单元测试生成服务类
 * 使用 AI 技术生成单元测试
 * 
 * 功能：
 * - 为特定方法生成单元测试
 * - 为整个类生成单元测试
 * - 生成项目范围的测试
 */
public class UnitTestGenerationService {
    private static final Logger LOG = Logger.getInstance(UnitTestGenerationService.class);
    private final Project project;

    public UnitTestGenerationService(Project project) {
        this.project = project;
    }

    /**
     * 为特定方法生成单元测试
     */
    public String generateMethodTest(PsiMethod method) {
        StringBuilder testCode = new StringBuilder();

        // 获取测试类的名称
        String className = method.getContainingClass().getName();
        String methodName = method.getName();
        String testMethodName = generateTestMethodName(methodName);

        // 生成基本测试结构
        testCode.append("  @Test\n");
        testCode.append("  public void ").append(testMethodName).append("() {\n");
        testCode.append("    // 初始化被测试的类\n");
        testCode.append("    ").append(className).append(" classUnderTest = new ").append(className).append("();\n\n");

        // 生成带参数的方法调用
        testCode.append("    // TODO: 初始化 ").append(methodName).append(" 的参数\n");
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < method.getParameterList().getParametersCount(); i++) {
            PsiParameter param = method.getParameterList().getParameters()[i];
            PsiType paramType = param.getType();
            String paramName = param.getName();
            
            testCode.append("    ").append(paramType.getCanonicalText()).append(" ").append(paramName)
                    .append(" = /* TODO: 初始化参数 */ ;\n");
            
            if (i > 0) parameters.append(", ");
            parameters.append(paramName);
        }

        // 生成方法调用
        if (!"void".equals(method.getReturnType().getCanonicalText())) {
            testCode.append("\n    ").append(method.getReturnType().getCanonicalText()).append(" result = ");
        } else {
            testCode.append("\n    ");
        }
        
        testCode.append("classUnderTest.").append(methodName).append("(")
                .append(parameters.toString()).append(");\n\n");

        // 添加断言占位符
        if (!"void".equals(method.getReturnType().getCanonicalText())) {
            testCode.append("    // TODO: 为结果添加断言\n");
            testCode.append("    // Assertions.assertThat(result).isNotNull(); // 示例断言\n");
        } else {
            testCode.append("    // TODO: 添加断言以验证方法行为\n");
        }

        testCode.append("  }\n");

        return testCode.toString();
    }

    /**
     * 为整个类生成单元测试
     */
    public String generateClassTest(PsiClass psiClass) {
        StringBuilder testClass = new StringBuilder();

        // 生成类注解和导入
        testClass.append("import org.junit.jupiter.api.Test;\n");
        testClass.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        
        testClass.append("class ").append(psiClass.getName()).append("Test {\n\n");

        // 为每个公共方法生成测试
        for (PsiMethod method : psiClass.getMethods()) {
            // 仅为公共方法（非构造函数）生成测试 - 修正：使用 hasModifierProperty("public")
            if (method.getModifierList().hasModifierProperty("public") && !method.isConstructor()) {
                testClass.append(generateMethodTest(method));
                testClass.append("\n");
            }
        }

        testClass.append("}");

        return testClass.toString();
    }

    /**
     * 根据原始方法名称生成测试方法名称
     */
    private String generateTestMethodName(String originalMethodName) {
        // 转换方法名称为测试名称（例如，calculateSum -> shouldCalculateSum）
        if (originalMethodName.startsWith("get") || originalMethodName.startsWith("set") ||
            originalMethodName.startsWith("is")) {
            // 对于 getter/setter 方法，使用更具描述性的测试名称
            String baseName = originalMethodName.substring(3); // 移除 "get" 或 "set"
            if (Character.isLowerCase(baseName.charAt(0))) {
                baseName = Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1);
            }
            return "should" + capitalizeFirst(originalMethodName) + "Correctly";
        } else {
            // 对于其他方法，使用通用前缀
            return "should" + capitalizeFirst(originalMethodName) + "Correctly";
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 为整个项目生成单元测试
     */
    public void generateProjectTests() {
        LOG.info("开始项目范围的测试生成");
        // 这将遍历项目中的所有类并生成测试
        // 实现将取决于项目结构和特定要求
    }
}