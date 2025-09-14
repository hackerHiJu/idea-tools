package io.github.easy.tools.action.doc.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.easy.tools.processor.doc.JavaCommentProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 为当前元素生成注释的动作类
 * <p>
 * 该动作类负责为光标所在的Java元素（类、方法或字段）生成注释。
 * 如果光标位置没有直接对应元素，则会向下查找第一个匹配的Java元素。
 * </p>
 */
public class GenerateElementCommentAction extends AbstractEasyDocAction {

    /**
     * Java注释处理器实例，用于执行实际的注释生成操作
     */
    private final JavaCommentProcessor processor = new JavaCommentProcessor();

    /**
     * 执行动作事件，为当前元素生成注释
     *
     * @param e 动作事件对象，包含执行上下文信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        PsiElement element = e.getData(PlatformDataKeys.PSI_ELEMENT);

        // 如果当前元素为空，则调用AbstractEasyDocAction中向下查找第一个java元素的方法
        if (element == null && file != null) {
            Editor editor = e.getData(PlatformDataKeys.EDITOR);
            if (editor != null) {
                int offset = editor.getCaretModel().getOffset();
                element = this.findFirstElementFromCaret(file, offset);
            }
        }

        if (file != null && element != null) {
            this.processor.generateElementComment(file, element);
        }
    }
}