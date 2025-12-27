package com.honghu.ut.test.ai.assistant.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * AI补全报告动作
 */
public class AIFixReportAction extends AnAction {
    
    public AIFixReportAction() {
        super("AI补全报告");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Messages.showMessageDialog(e.getProject(), 
            "AI补全报告功能将在后续版本中实现", 
            "功能提示", 
            Messages.getInformationIcon());
    }
}