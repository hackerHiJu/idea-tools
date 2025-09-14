package io.github.easy.tools.factory.doc;

import com.intellij.psi.PsiFile;
import io.github.easy.tools.service.doc.CommentGenerationStrategy;
import io.github.easy.tools.service.doc.JavaCommentGenerationStrategy;

/**
 * 注释生成策略工厂，用于创建合适的处理策略
 * <p>
 * 该工厂类根据文件类型创建相应的注释生成策略实例。
 * 目前主要支持Java文件的注释生成和删除操作。
 * 使用单例模式确保全局只有一个工厂实例。
 * </p>
 */
public class CommentGenerationStrategyFactory {

    /**
     * 单例实例
     */
    private static final CommentGenerationStrategyFactory INSTANCE = new CommentGenerationStrategyFactory();

    /**
     * 私有构造函数
     * <p>
     * 防止外部直接实例化该类，确保单例模式的正确实现。
     * </p>
     */
    private CommentGenerationStrategyFactory() {
        // 私有构造函数防止外部实例化
    }

    /**
     * 获取工厂实例
     *
     * @return 注释生成策略工厂的单例实例
     */
    public static CommentGenerationStrategyFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 根据文件类型获取合适的注释生成策略
     *
     * @param file 需要处理的文件
     * @return 注释生成策略实例
     */
    public CommentGenerationStrategy getStrategy(PsiFile file) {
        // 目前只支持Java文件
        if (file.getFileType().getName().equals("JAVA")) {
            return new JavaCommentGenerationStrategy();
        }

        // 默认返回Java注释生成策略
        return new JavaCommentGenerationStrategy();
    }
}