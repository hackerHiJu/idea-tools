package io.github.easy.tools.action.doc.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import io.github.easy.tools.processor.doc.CommentProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 为当前文件生成注释的动作类
 * <p>
 * 该动作类负责为整个Java文件生成注释，会遍历文件中的所有元素（类、方法、字段）
 * 并为每个元素生成相应的文档注释。
 * </p>
 */
public class GenerateFileCommentAction extends AbstractEasyDocAction {

    /**
     * 执行动作事件，为整个文件生成注释
     *
     * @param e 动作事件对象，包含执行上下文信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        CommentProcessor processor = this.getProcessor(file);
        // 1. 使用 processor 生成整个文件的注释
        if (processor != null && file != null) {
            processor.generateFileComment(file);
        }
    }
}