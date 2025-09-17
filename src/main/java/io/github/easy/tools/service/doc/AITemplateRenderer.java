package io.github.easy.tools.service.doc;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.easy.tools.ui.config.DocConfigService;
import org.apache.velocity.context.Context;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * AI模板渲染器
 * <p>
 * 当启用AI功能时使用的模板渲染器，集成OpenAI API进行智能注释生成。
 * 使用Hutool的HTTP工具和Azure OpenAI SDK实现API调用。
 * </p>
 */
public class AITemplateRenderer implements TemplateRenderer {

    /** ERROR_DOC */
    private static final String ERROR_DOC = """
            /**
             * 注释生成异常:{error}
             */
            """;

    /**
     * 默认提示词模板
     */
    private static final String DEFAULT_PROMPT_TEMPLATE = """
            请根据以下代码模板和上下文信息生成符合JavaDoc标准的注释:
            
            模板内容:
            {template}
            
            参数信息：
            {context}
            
            要求:
            1. 严格按照模板内容格式，以标准JavaDoc格式输出
            2. 确保注释内容准确反映代码意图，如果是类则需要在描述部分对当前类的功能进行归纳，如果是方法则需要对方法内容按照顺序进行解释归纳
            3. 如果是方法则需要包含适当的@param, @return, @throws等标签
            4. 使用简洁明了的中文描述
            5. 不要修改代码结构，只生成注释部分
            """;

    /**
     * Velocity模板服务实例，用于备用渲染
     */
    private final VelocityTemplateService velocityTemplateService;

    /**
     * 构造函数，初始化Velocity模板服务
     */
    public AITemplateRenderer() {
        this.velocityTemplateService = new VelocityTemplateService();
    }

    /**
     * 使用AI辅助渲染模板内容
     * <p>
     * 调用OpenAI API生成智能注释内容
     * </p>
     *
     * @param templateContent 模板内容
     * @param context         渲染上下文
     * @param element         相关的Psi元素
     * @return 渲染后的内容
     */
    @Override
    public String render(String templateContent, Context context, PsiElement element) {
        // 获取配置服务
        DocConfigService config = DocConfigService.getInstance();

        // 检查是否启用AI功能
        if (config.enableAi && config.baseUrl != null && !config.baseUrl.isEmpty()) {
            try {
                // 使用AI生成注释（异步方式）
                return this.generateAICommentAsync(templateContent, context, element, config);
            } catch (Exception e) {
                return ERROR_DOC.replace("{error}", e.getMessage());
            }
        }

        // 默认使用Velocity模板服务进行渲染
        return """
                /**
                 * 请配置大模型相关信息
                 */
                """;
    }

    /**
     * 使用OpenAI API异步生成注释
     *
     * @param templateContent 模板内容
     * @param context         渲染上下文
     * @param element         相关的Psi元素
     * @param config          配置服务
     * @return AI生成的注释内容或默认内容
     */
    private String generateAICommentAsync(String templateContent, Context context, PsiElement element, DocConfigService config) {
        // 在读操作中获取元素文本内容
        String elementText = ReadAction.compute(() -> element.getText());

        // 构建上下文信息字符串
        StringBuilder contextInfo = new StringBuilder();
        contextInfo.append("代码上下文信息:\n");

        // 获取上下文中的所有键值对（在读操作中执行）
        String[] keys = ReadAction.compute(() -> context.getKeys());
        Stream.of(keys).forEach(key -> {
            Object value = ReadAction.compute(() -> context.get(key));
            contextInfo.append(key).append(": ").append(value).append("\n");
        });

        // 构建完整的提示词
        String prompt = this.buildPrompt(templateContent, contextInfo.toString(), element, elementText);

        // 创建CompletableFuture用于异步处理
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 构建请求体
                String requestBody = this.buildRequestBody(config.modelName, prompt);

                // 发送HTTP请求
                HttpResponse response = HttpRequest.post(config.baseUrl + "/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + config.apiKey)
                        .body(requestBody)
                        .timeout(300000)
                        .execute();

                // 解析响应
                if (response.getStatus() == 200) {
                    String responseBody = response.body();
                    return this.extractCommentFromResponse(responseBody);
                } else {
                    // 出现异常时回退到Velocity渲染
                    return ERROR_DOC.replace("{error}", response.body());
                }
            } catch (Exception e) {
                // 出现异常时回退到Velocity渲染
                Notification notification = NotificationGroupManager.getInstance()
                        .getNotificationGroup("Easy Docs Notification Group")
                        .createNotification("调用AI服务失败: " + e.getMessage(), NotificationType.ERROR);
                notification.notify(element.getProject());
                return ERROR_DOC.replace("{error}", e.getMessage());
            }
        });

        // 异步处理结果并更新注释
        future.thenAccept(result -> {
            if (result != null && !result.isEmpty()) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    this.updateElementComment(element, result);
                });
            }
        }).exceptionally(throwable -> {
            Notification notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Easy Docs Notification Group")
                    .createNotification("处理AI注释结果时发生异常: " + throwable.getMessage(), NotificationType.ERROR);
            notification.notify(element.getProject());
            return null;
        });

        return """
                /**
                 * 正在等待大模型生成注释，生成后将会进行替换
                 */
                """;
    }

    /**
     * 更新元素的注释
     *
     * @param element     目标元素
     * @param commentText 注释文本
     */
    private void updateElementComment(PsiElement element, String commentText) {
        try {
            if (element instanceof PsiClass || element instanceof PsiMethod || element instanceof PsiField) {
                // 获取元素所在的文件和项目
                PsiFile file = element.getContainingFile();
                if (file != null) {
                    Project project = file.getProject();

                    // 使用WriteCommandAction确保在正确的上下文中修改PSI
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        // 创建新的文档注释
                        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                        PsiElement docCommentFromText = elementFactory.createDocCommentFromText(commentText);

                        // 获取现有的文档注释
                        PsiDocComment existingDocComment = null;
                        if (element instanceof PsiClass psiClass) {
                            existingDocComment = psiClass.getDocComment();
                        } else if (element instanceof PsiMethod psiMethod) {
                            existingDocComment = psiMethod.getDocComment();
                        } else if (element instanceof PsiField psiField) {
                            existingDocComment = psiField.getDocComment();
                        }

                        // 更新或添加注释
                        if (existingDocComment != null) {
                            existingDocComment.replace(docCommentFromText);
                        } else {
                            element.addBefore(docCommentFromText, element.getFirstChild());
                        }
                    });
                }
            }
        } catch (Exception e) {
            // 使用IDEA的通知系统显示错误信息
            Notification notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Easy Docs Notification Group")
                    .createNotification("更新注释失败: " + e.getMessage(), NotificationType.ERROR);
            notification.notify(element.getProject());
        }
    }

    /**
     * 构建请求体
     *
     * @param model  模型名称
     * @param prompt 提示词
     * @return 请求体JSON字符串
     */
    private String buildRequestBody(String model, String prompt) {
        // 构建消息数组
        JSON messages = JSONUtil.createArray()
                .put(JSONUtil.createObj()
                        .set("role", "system")
                        .set("content", "你是一名专业的Java开发者，擅长编写高质量的Java代码注释。请根据提供的代码和上下文信息生成符合JavaDoc标准的注释。"))
                .put(JSONUtil.createObj()
                        .set("role", "user")
                        .set("content", prompt));

        // 构建请求体
        return JSONUtil.createObj()
                .set("model", model)
                .set("messages", messages)
                .set("temperature", 0.7)
                .set("stream", false)
                .toString();
    }

    /**
     * 构建提示词
     *
     * @param templateContent 模板内容
     * @param contextInfo     上下文信息
     * @param element         相关的Psi元素
     * @param elementText     元素的文本内容
     * @return 完整的提示词
     */
    private String buildPrompt(String templateContent, String contextInfo, PsiElement element, String elementText) {
        // 默认提示词
        return DEFAULT_PROMPT_TEMPLATE
                .replace("{code}", elementText)
                .replace("{template}", templateContent)
                .replace("{context}", contextInfo);
    }

    /**
     * 从AI响应中提取注释内容
     *
     * @param responseBody 响应体
     * @return 提取的注释
     */
    private String extractCommentFromResponse(String responseBody) {
        try {
            // 解析JSON响应
            JSON responseJson = JSONUtil.parse(responseBody);
            String content = responseJson.getByPath("choices[0].message.content", String.class);

            // 尝试从代码块中提取注释
            if (content != null && content.contains("```")) {
                int start = content.indexOf("```");
                int end = content.lastIndexOf("```");
                if (start >= 0 && end > start) {
                    String codeBlock = content.substring(start + 3, end).trim();
                    // 如果代码块以java开头，则移除
                    if (codeBlock.startsWith("java")) {
                        codeBlock = codeBlock.substring(4).trim();
                    }
                    return codeBlock;
                }
            }

            // 如果没有代码块，直接返回响应内容
            return content != null ? content : "";
        } catch (Exception e) {
            System.err.println("解析响应失败: " + e.getMessage());
            return "";
        }
    }
}
