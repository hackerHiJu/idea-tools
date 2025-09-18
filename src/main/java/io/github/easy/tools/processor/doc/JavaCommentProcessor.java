package io.github.easy.tools.processor.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.easy.tools.factory.doc.CommentGenerationStrategyFactory;
import io.github.easy.tools.service.doc.CommentGenerationStrategy;
import io.github.easy.tools.service.doc.JavaCommentGenerationStrategy;

/**
 * Java注释处理器，实现删除与生成逻辑
 * <p>
 * 该类是Java文件注释处理的具体实现，负责协调工厂类获取合适的策略，
 * 并执行相应的注释生成或删除操作。
 * </p>
 *
 * @author zhonghaijun
 * @version 1.0.0
 * @email "mailto:zhonghaijun@zhxx.com"
 * @date 2025.09.17 14:44
 * @since y.y.y
 */
public class JavaCommentProcessor implements CommentProcessor {

    /** Comment generation strategy factory */
    private final CommentGenerationStrategyFactory commentGenerationStrategyFactory;

    /**
     * Java comment processor
     *
     * @since y.y.y
     */
    public JavaCommentProcessor() {
        this.commentGenerationStrategyFactory = new CommentGenerationStrategyFactory();
    }

    /**
     * 删除整个文件的注释
     * <p>
     * 通过策略工厂获取Java注释生成策略，并调用其删除方法来删除整个文件的注释。
     * </p>
     *
     * @param file 需要删除注释的文件
     * @since y.y.y
     */
    @Override
    public void removeFileComment(PsiFile file) {
        // 1. 获取合适的生成策略（如Java）
        CommentGenerationStrategy strategy = this.commentGenerationStrategyFactory.getStrategy(file);
        // 2. 执行删除逻辑
        strategy.remove(file);
    }

    /**
     * 删除指定元素的注释
     * <p>
     * 通过策略工厂获取Java注释生成策略，并调用其删除方法来删除指定元素的注释。
     * </p>
     *
     * @param file    需要删除注释的文件
     * @param element 需要删除注释的元素
     * @since y.y.y
     */
    @Override
    public void removeElementComment(PsiFile file, PsiElement element) {
        // 1. 获取合适的生成策略（如Java）
        CommentGenerationStrategy strategy = this.commentGenerationStrategyFactory.getStrategy(file);
        // 2. 执行删除逻辑
        strategy.remove(file, element);
    }

    /**
     * 生成整个文件的注释
     * <p>
     * 通过策略工厂获取Java注释生成策略，并调用其生成方法来为整个文件生成注释。
     * </p>
     *
     * @param file      需要生成注释的文件
     * @param overwrite 是否覆盖已存在的注释
     * @since y.y.y
     */
    @Override
    public void generateFileComment(PsiFile file, boolean overwrite) {
        // 1. 获取合适的生成策略（如Java）
        CommentGenerationStrategy strategy = this.commentGenerationStrategyFactory.getStrategy(file);
        // 2. 执行生成逻辑
        if (strategy instanceof JavaCommentGenerationStrategy javaStrategy) {
            javaStrategy.generate(file, overwrite);
        } else {
            strategy.generate(file);
        }
    }

    /**
     * 生成指定元素的注释
     * <p>
     * 通过策略工厂获取Java注释生成策略，并调用其生成方法来为指定元素生成注释。
     * </p>
     *
     * @param file      需要生成注释的文件
     * @param element   需要生成注释的元素
     * @param overwrite 是否覆盖已存在的注释
     * @since y.y.y
     */
    @Override
    public void generateElementComment(PsiFile file, PsiElement element, boolean overwrite) {
        // 1. 获取合适的生成策略（如Java）
        CommentGenerationStrategy strategy = this.commentGenerationStrategyFactory.getStrategy(file);
        // 2. 执行生成逻辑
        if (strategy instanceof JavaCommentGenerationStrategy javaStrategy) {
            javaStrategy.generate(file, element, overwrite);
        } else {
            strategy.generate(file, element);
        }
    }

    /**
     * 生成整个文件的注释
     * <p>
     * 通过策略工厂获取Java注释生成策略，并调用其生成方法来为整个文件生成注释。
     * </p>
     *
     * @param file 需要生成注释的文件
     * @since y.y.y
     */
    @Override
    public void generateFileComment(PsiFile file) {
        this.generateFileComment(file, true);
    }

    /**
     * 生成指定元素的注释
     * <p>
     * 通过策略工厂获取Java注释生成策略，并调用其生成方法来为指定元素生成注释。
     * </p>
     *
     * @param file    需要生成注释的文件
     * @param element 需要生成注释的元素
     * @since y.y.y
     */
    @Override
    public void generateElementComment(PsiFile file, PsiElement element) {
        this.generateElementComment(file, element, true);
    }
}
