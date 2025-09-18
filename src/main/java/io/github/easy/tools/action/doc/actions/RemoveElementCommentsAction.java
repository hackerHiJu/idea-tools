package io.github.easy.tools.action.doc.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.easy.tools.processor.doc.CommentProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 删除当前元素注释的动作类
 * <p>
 * 该动作类负责删除光标所在Java元素（类、方法或字段）的文档注释。
 * 如果光标位置没有直接对应元素，则会向下查找第一个匹配的Java元素。
 * </p>
 */
public class RemoveElementCommentsAction extends AbstractEasyDocAction {

    /**
     * 执行动作事件，删除当前元素的注释
     *
     * @param e 动作事件对象，包含执行上下文信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        // 如果当前元素为空，则调用AbstractEasyDocAction中向下查找第一个java元素的方法
        if (element == null && file != null) {
            Editor editor = e.getData(PlatformDataKeys.EDITOR);
            if (editor != null) {
                int offset = editor.getCaretModel().getOffset();
                element = this.findFirstElementFromCaret(file, offset);
            }
        }

        CommentProcessor processor = this.getProcessor(file);
        if (processor != null && file != null && element != null) {
            processor.removeElementComment(file, element);
        }
    }
}