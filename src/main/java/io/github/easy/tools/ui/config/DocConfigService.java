package io.github.easy.tools.ui.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.easy.tools.entity.doc.TemplateParameter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * 持久化配置服务类，用于管理插件的各种配置参数
 * <p>
 * 该类负责存储和管理插件的配置信息，包括AI相关配置、模板配置和自定义参数等。
 * 通过IntelliJ Platform的持久化机制，配置信息会在IDE重启后保持不变。
 * </p>
 */
@State(
        name = "EasyToolsConfig",
        storages = @Storage("easy-tools-config.xml")
)
public class DocConfigService implements PersistentStateComponent<DocConfigService> {

    /**
     * 是否启用AI功能
     */
    public boolean enableAi = false;

    /**
     * AI模型的基础URL
     */
    public String baseUrl = "";

    /**
     * AI模型名称
     */
    public String modelName = "";

    /**
     * AI模型类型
     */
    public String modelType = "openai";

    /**
     * AI模型API密钥
     */
    public String apiKey = "";

    /**
     * 类注释模板
     */
    public String classTemplate = """
            /**
             * ${description}
             *
             * @author ${author}
             * @date ${date}
             * @version ${version}
             * @since ${since}
             */
            """;

    /**
     * 方法注释模板
     */
    public String methodTemplate = """
            /**
             * ${description}
             *
             #foreach( $param in $parameters )
             * @param $param.name $param.description
             #end
             #if( $returnType )
             * @return $returnType
             #end
             #foreach( $exception in $exceptions )
             * @throws $exception
             #end
             * @author ${author}
             * @date ${date}
             * @version ${version}
             */
            """;

    /**
     * 字段注释模板
     */
    public String fieldTemplate = """
            /**
             * The ${fieldName}.
             */
            """;

    /**
     * 自定义变量字符串形式
     */
    public String customVar = "";

    /**
     * 自定义参数列表
     */
    public List<TemplateParameter> customParameters = new LinkedList<>();

    /**
     * 是否启用保存监听器
     */
    public boolean saveListener = false;

    /**
     * 获取基础参数列表
     * <p>
     * 基础参数包括作者名、当前日期和空描述，这些参数会在所有模板中使用。
     * </p>
     *
     * @return 基础参数列表
     */
    public List<TemplateParameter> getBaseParameters() {
        String author = System.getProperty("user.name");
        List<TemplateParameter> list = new LinkedList<>();
        TemplateParameter<String> author1 = new TemplateParameter<>("author", author, "作者");
        TemplateParameter<String> date = new TemplateParameter<>("date", DateUtil.now(), "日期");
        TemplateParameter<String> version = new TemplateParameter<>("version", "1.0.0", "版本");
        TemplateParameter<Class<StrUtil>> str = new TemplateParameter<>("str", StrUtil.class, "字符串工具类");

        list.add(author1);
        list.add(date);
        list.add(version);
        list.add(str);
        return list;
    }

    /**
     * 获取配置服务的单例实例
     *
     * @return DocConfigService的单例实例
     */
    public static DocConfigService getInstance() {
        return ApplicationManager.getApplication().getService(DocConfigService.class);
    }

    /**
     * 获取当前状态（用于持久化）
     *
     * @return 当前配置服务实例
     */
    @Override
    public DocConfigService getState() {
        return this;
    }

    /**
     * 加载状态（用于持久化）
     *
     * @param state 要加载的配置状态
     */
    @Override
    public void loadState(@NotNull DocConfigService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
