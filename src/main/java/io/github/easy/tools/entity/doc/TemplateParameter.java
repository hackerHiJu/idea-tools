package io.github.easy.tools.entity.doc;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 模板参数模型类，用于承载Velocity模板渲染所需的参数
 * <p>
 * 该类表示一个键值对形式的模板参数，用于在Velocity模板渲染时提供变量替换的值。
 * 使用Lombok注解简化getter/setter和构造函数的编写。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateParameter<T> {
    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数值
     */
    private T value;

    /**
     * 参数描述
     */
    private String description;
}
