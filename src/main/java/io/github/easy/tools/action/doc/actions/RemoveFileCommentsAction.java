package io.github.easy.tools.action.doc.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.psi.PsiFile;
import io.github.easy.tools.processor.doc.CommentProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 删除当前文件所有注释的动作类
 * <p>
 * 该动作类负责删除整个Java文件中的所有文档注释，
 * 包括类、方法和字段的注释。
 * </p>
 */
public class RemoveFileCommentsAction extends AbstractEasyDocAction {

    /**
     * 执行动作事件，删除整个文件的注释
     *
     * @param e 动作事件对象，包含执行上下文信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        CommentProcessor processor = this.getProcessor(file);
        if (processor != null && file != null) {
            processor.removeFileComment(file);
        }
    }
}