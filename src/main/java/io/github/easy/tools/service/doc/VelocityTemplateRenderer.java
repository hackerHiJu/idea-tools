package io.github.easy.tools.service.doc;

import com.intellij.psi.PsiElement;
import org.apache.velocity.context.Context;

/**
 * Velocity模板渲染器
 * <p>
 * 使用Velocity模板引擎进行模板渲染的实现类。
 * 这是默认的模板渲染方式。
 * </p>
 */
public class VelocityTemplateRenderer implements TemplateRenderer {
    
    /**
     * Velocity模板服务实例
     */
    private final VelocityTemplateService velocityTemplateService;
    
    /**
     * 构造函数，初始化Velocity模板服务
     */
    public VelocityTemplateRenderer() {
        this.velocityTemplateService = new VelocityTemplateService();
    }
    
    /**
     * 使用Velocity引擎渲染模板内容
     *
     * @param templateContent 模板内容
     * @param context         渲染上下文
     * @param element         相关的Psi元素（Velocity渲染不需要此参数）
     * @return 渲染后的内容
     */
    @Override
    public String render(String templateContent, Context context, PsiElement element) {
        return velocityTemplateService.render(templateContent, context);
    }
}