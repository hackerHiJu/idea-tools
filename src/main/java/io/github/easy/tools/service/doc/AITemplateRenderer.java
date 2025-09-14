package io.github.easy.tools.service.doc;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import io.github.easy.tools.ui.config.DocConfigService;
import org.apache.velocity.context.Context;

import java.util.stream.Stream;

/**
 * AI模板渲染器
 * <p>
 * 当启用AI功能时使用的模板渲染器，集成OpenAI API进行智能注释生成。
 * 使用Hutool的HTTP工具和Azure OpenAI SDK实现API调用。
 * </p>
 */
public class AITemplateRenderer implements TemplateRenderer {

    /**
     * 类注释提示词模板
     */
    private static final String CLASS_PROMPT_TEMPLATE = """
            请根据以下Java类的代码生成符合JavaDoc标准的类注释:
            
            类代码:
            {code}
            
            要求:
            1. 总结整个类的功能和用途
            2. 简要描述类的主要职责
            3. 严格按照JavaDoc格式输出
            4. 不要包含任何解释性文字，只输出注释代码
            5. 使用简洁明了的中文描述
            
            示例格式:
            /**
             * 类描述
             *
             * @author 作者
             * @version 版本
             * @since 从哪个版本开始可用
             */
            """;

    /**
     * 方法注释提示词模板
     */
    private static final String METHOD_PROMPT_TEMPLATE = """
            请根据以下Java方法的代码生成符合JavaDoc标准的方法注释:
            
            方法代码:
            {code}
            
            要求:
            1. 描述方法的功能和用途
            2. 详细说明每个参数的含义
            3. 说明返回值的含义（如果有返回值）
            4. 列出可能抛出的异常（如果有）
            5. 严格按照JavaDoc格式输出
            6. 不要包含任何解释性文字，只输出注释代码
            7. 使用简洁明了的中文描述
            
            示例格式:
            /**
             * 方法描述
             *
             * @param 参数名 参数描述
             * @return 返回值描述
             * @throws 异常类型 异常描述
             */
            """;

    /**
     * 字段注释提示词模板
     */
    private static final String FIELD_PROMPT_TEMPLATE = """
            请根据以下Java字段的代码生成符合JavaDoc标准的字段注释:
            
            字段代码:
            {code}
            
            要求:
            1. 描述字段的含义和用途
            2. 如果字段有特殊约束或默认值，请说明
            3. 严格按照JavaDoc格式输出
            4. 不要包含任何解释性文字，只输出注释代码
            5. 使用简洁明了的中文描述
            
            示例格式:
            /**
             * 字段描述
             */
            """;

    /**
     * 默认提示词模板
     */
    private static final String DEFAULT_PROMPT_TEMPLATE = """
            请根据以下代码模板和上下文信息生成符合JavaDoc标准的注释:
            
            模板内容:
            {template}
            
            {context}
            
            要求:
            1. 严格按照JavaDoc格式输出
            2. 不要包含任何解释性文字，只输出注释代码
            3. 确保注释内容准确反映代码意图
            4. 包含适当的@param, @return, @throws等标签
            5. 使用简洁明了的中文描述
            6. 不要修改代码结构，只生成注释部分
            
            示例格式:
            /**
             * 方法描述
             *
             * @param 参数名 参数描述
             * @return 返回值描述
             */
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
                // 使用AI生成注释
                return generateAIComment(templateContent, context, element, config);
            } catch (Exception e) {
                System.err.println("AI注释生成失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 默认使用Velocity模板服务进行渲染
        return velocityTemplateService.render(templateContent, context);
    }

    /**
     * 使用OpenAI API生成注释
     *
     * @param templateContent 模板内容
     * @param context         渲染上下文
     * @param element         相关的Psi元素
     * @param config          配置服务
     * @return AI生成的注释内容
     */
    private String generateAIComment(String templateContent, Context context, PsiElement element, DocConfigService config) {
        try {
            // 构建上下文信息字符串
            StringBuilder contextInfo = new StringBuilder();
            contextInfo.append("代码上下文信息:\n");

            // 获取上下文中的所有键值对
            Stream.of(context.getKeys()).forEach(key -> {
                Object value = context.get(key);
                contextInfo.append(key).append(": ").append(value).append("\n");
            });

            // 构建完整的提示词
            String prompt = buildPrompt(templateContent, contextInfo.toString(), element);

            // 构建请求体
            String requestBody = buildRequestBody(config.modelName, prompt);

            // 发送HTTP请求
            HttpResponse response = HttpRequest.post(config.baseUrl + "/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + getApiKey(config))
                    .body(requestBody)
                    .timeout(300000)
                    .execute();

            // 解析响应
            if (response.getStatus() == 200) {
                String responseBody = response.body();
                return extractCommentFromResponse(responseBody);
            } else {
                System.err.println("调用OpenAI API失败，状态码: " + response.getStatus());
                System.err.println("响应内容: " + response.body());
                // 出现异常时回退到Velocity渲染
                return velocityTemplateService.render(templateContent, context);
            }
        } catch (Exception e) {
            System.err.println("调用OpenAI API失败: " + e.getMessage());
            // 出现异常时回退到Velocity渲染
            return velocityTemplateService.render(templateContent, context);
        }
    }

    /**
     * 获取API密钥
     *
     * @param config 配置服务
     * @return API密钥
     */
    private String getApiKey(DocConfigService config) {
        String apiKey = "EMPTY"; // 默认值，对于本地部署的模型通常为空
        if ("openai".equals(config.modelType)) {
            // 如果是OpenAI官方服务，需要提供有效的API密钥
            // 这里应该从安全的地方获取API密钥，例如配置文件或环境变量
            apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null) {
                apiKey = "EMPTY"; // 回退到默认值
            }
        }
        return apiKey;
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
     * @return 完整的提示词
     */
    private String buildPrompt(String templateContent, String contextInfo, PsiElement element) {
        // 根据元素类型构建不同的提示词
        if (element instanceof PsiClass) {
            return CLASS_PROMPT_TEMPLATE.replace("{code}", element.getText());
        } else if (element instanceof PsiMethod) {
            return METHOD_PROMPT_TEMPLATE.replace("{code}", element.getText());
        } else if (element instanceof PsiField) {
            return FIELD_PROMPT_TEMPLATE.replace("{code}", element.getText());
        } else {
            // 默认提示词
            return DEFAULT_PROMPT_TEMPLATE
                    .replace("{template}", templateContent)
                    .replace("{context}", contextInfo);
        }
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
