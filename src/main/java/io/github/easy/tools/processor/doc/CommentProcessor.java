package io.github.easy.tools.processor.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * 注释处理器接口，定义注释处理契约
 * <p>
 * 该接口定义了注释处理的基本操作，包括生成和删除文件或元素的注释。
 * 所有具体的注释处理器实现类都必须实现该接口。
 * </p>
 */
public interface CommentProcessor {

    /**
     * 生成整个文件的注释
     *
     * @param file 需要生成注释的文件
     */
    void generateFileComment(PsiFile file);

    /**
     * 生成指定元素的注释
     *
     * @param file    需要生成注释的文件
     * @param element 需要生成注释的元素
     */
    void generateElementComment(PsiFile file, PsiElement element);

    /**
     * 删除整个文件的注释
     *
     * @param file 需要删除注释的文件
     */
    void removeFileComment(PsiFile file);

    /**
     * 删除指定元素的注释
     *
     * @param file    需要删除注释的文件
     * @param element 需要删除注释的元素
     */
    void removeElementComment(PsiFile file, PsiElement element);

}