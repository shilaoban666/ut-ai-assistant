package com.honghu.ut.test.ai.assistant.plugin;

import com.honghu.ut.test.ai.assistant.jacoco.JacocoCoverageService;
import com.honghu.ut.test.ai.assistant.services.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * UT AI 助手插件服务
 * 提供 JaCoCo 覆盖率检查和单元测试生成功能
 * 
 * 功能：
 * - 项目打开/关闭时初始化和清理服务
 * - 管理插件的服务管理器
 */
@Service(Service.Level.PROJECT)
public final class UtAssistantPlugin {
    public static final String PLUGIN_ID = "com.honghu.ut.test.ai.assigment.ut-ai-assistant";
    private static final Logger LOG = Logger.getInstance(UtAssistantPlugin.class);
    
    private ServiceManager serviceManager;
    private final Project project;
    private JacocoCoverageService jacocoCoverageService;

    public UtAssistantPlugin(Project project) {
        this.project = project;
        LOG.info("UT AI 助手插件为项目初始化: " + project.getName());
        serviceManager = new ServiceManager(project);
        jacocoCoverageService = new JacocoCoverageService(project);
    }

    public static UtAssistantPlugin getInstance(Project project) {
        return project.getService(UtAssistantPlugin.class);
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public JacocoCoverageService getJacocoCoverageService() {
        return jacocoCoverageService;
    }

    public void init() {
        LOG.info("UT AI 助手插件初始化");
    }

    public void dispose() {
        LOG.info("UT AI 助手插件释放");
        serviceManager = null;
        jacocoCoverageService = null;
    }
}