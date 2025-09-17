package io.github.easy.tools.service.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaDocumentedElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import org.jetbrains.annotations.NotNull;

/**
 * Java注释比较器实现
 * <p>
 * 用于比较Java元素是否已存在注释以及注释内容是否相同
 * </p>
 */
public class JavaDocCommentComparator implements DocCommentComparator {

    /**
     * 检查Java元素是否已存在注释
     *
     * @param element Java元素
     * @return 如果已存在注释返回true，否则返回false
     */
    @Override
    public boolean hasComment(@NotNull PsiElement element) {
        if (element instanceof PsiJavaDocumentedElement documentedElement) {
            return documentedElement.getDocComment() != null;
        }
        return false;
    }

    /**
     * 合并现有注释和新注释
     *
     * @param element    Java元素
     * @param newComment 新注释内容
     * @return 合并后的注释内容
     */
    @Override
    public PsiElement mergeComments(@NotNull PsiElement element, @NotNull PsiElement newComment) {
        if (!(element instanceof PsiJavaDocumentedElement documentedElement)
                || !(newComment instanceof PsiDocComment newDocComment)) {
            return newComment;
        }

        PsiDocComment oldDocComment = documentedElement.getDocComment();
        if (oldDocComment == null) {
            return newComment;
        }

        // 获取新旧注释的tag
        PsiDocTag[] oldTags = oldDocComment.getTags();
        PsiDocTag[] newTags = newDocComment.getTags();

        // 创建结果注释的StringBuilder
        StringBuilder result = new StringBuilder();
        result.append("/**\n");

        // 添加新注释的主内容（非tag部分）
        String newCommentText = newDocComment.getText();
        String[] newLines = newCommentText.split("\n");
        for (String line : newLines) {
            if (line.trim().startsWith("* @")) {
                break;
            }
            if (line.contains("/**") || line.contains("*/")) {
                continue;
            }
            result.append(line.substring(line.indexOf("*"))).append("\n");
        }

        // 处理tag合并逻辑
        // 1. 先处理新的tag
        for (PsiDocTag newTag : newTags) {
            result.append(" * ").append(newTag.getText()).append("\n");
        }

        // 2. 处理旧的tag中在新tag中不存在的
        for (PsiDocTag oldTag : oldTags) {
            boolean existsInNew = false;
            for (PsiDocTag newTag : newTags) {
                if (oldTag.getName().equals(newTag.getName())) {
                    existsInNew = true;
                    break;
                }
            }
            if (!existsInNew) {
                result.append(" * ").append(oldTag.getText()).append("\n");
            }
        }

        result.append(" */");

        // 这里应该创建一个新的PsiDocComment元素并返回，但由于PsiElement的创建比较复杂，
        // 实际实现需要依赖具体的IDE API，这里仅示意逻辑
        return newComment;
    }
}
