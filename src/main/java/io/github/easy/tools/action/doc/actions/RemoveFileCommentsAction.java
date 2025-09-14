package io.github.easy.tools.action.doc.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import io.github.easy.tools.processor.doc.JavaCommentProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 删除当前文件所有注释的动作类
 * <p>
 * 该动作类负责删除整个Java文件中的所有文档注释，
 * 包括类、方法和字段的注释。
 * </p>
 */
public class RemoveFileCommentsAction extends AnAction {

    /**
     * Java注释处理器实例，用于执行实际的注释删除操作
     */
    private final JavaCommentProcessor processor = new JavaCommentProcessor();

    /**
     * 执行动作事件，删除整个文件的注释
     *
     * @param e 动作事件对象，包含执行上下文信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.processor.removeFileComment(e.getData(PlatformDataKeys.PSI_FILE));
    }
}