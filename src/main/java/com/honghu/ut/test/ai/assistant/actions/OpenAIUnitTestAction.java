package com.honghu.ut.test.ai.assistant.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * AI测试选中代码动作
 */
public class OpenAIUnitTestAction extends AnAction {
    
    public OpenAIUnitTestAction() {
        super("AI测试选中代码");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Messages.showMessageDialog(e.getProject(), 
            "AI测试选中代码功能将在后续版本中实现", 
            "功能提示", 
            Messages.getInformationIcon());
    }
}