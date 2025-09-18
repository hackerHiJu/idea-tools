package io.github.easy.tools.ui.config;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import io.github.easy.tools.action.doc.listener.FileSaveListenerManager;
import io.github.easy.tools.entity.doc.TemplateParameter;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * <p> 配置界面类，实现Configurable接口，用于在IDEA设置中展示和管理配置 </p>
 *
 * @author haijun
 * @version x.x.x
 * @email "mailto:zhonghaijun@zhxx.com"
 * @date 2025.09.12 09:24
 * @since x.x.x
 */
public class DocConfig implements Configurable {
    /**
     * 主面板组件
     */
    private JPanel mainPanel;

    /**
     * AI配置面板
     */
    private JPanel aiContent;

    /**
     * 类模板文本框
     */
    private JTextArea classTemplate;

    /**
     * 方法模板文本框
     */
    private JTextArea methodTemplate;

    /**
     * 字段模板文本框
     */
    private JTextArea fieldTemplate;

    /**
     * 内置变量面板
     */
    private JPanel varContent;

    /**
     * 模型基础URL输入框
     */
    private JTextField baseUrl;

    /**
     * 模型名称输入框
     */
    private JTextField modelName;

    /**
     * 模型地址标签
     */
    private JLabel baseUrlTitle;

    /**
     * 模型名称标签
     */
    private JLabel modelNameTitle;

    /**
     * 启用AI复选框
     */
    private JCheckBox enableAi;

    /**
     * 模型类型下拉框
     */
    private JComboBox modelType;

    /**
     * 内置变量描述文本框
     */
    private JTextPane varDesc;

    /**
     * 自定义变量面板
     */
    private JPanel customContent;

    /**
     * 自定义变量文本区域
     */
    private JTextArea customVar;

    /**
     * 类配置面板
     */
    private JScrollPane classContent;

    /**
     * 方法配置面板
     */
    private JScrollPane methodContent;

    /**
     * 字段配置面板
     */
    private JScrollPane fieldContent;

    /**
     * 模型类型标签
     */
    private JLabel modelTypeTitle;
    /** Api key */
    private JTextField apiKey;
    /** Api key title */
    private JLabel apiKeyTitle;
    /** Advanced features content */
    private JPanel advancedFeaturesContent;
    /** Save listener */
    private JCheckBox saveListener;

    /**
     * 配置是否被修改的标志
     */
    private boolean isModified = false;


    /**
     * 获取显示名称
     *
     * @return 配置面板的显示名称 display name
     * @since y.y.y
     */
    @Override
    public String getDisplayName() {
        return "Easy Tools";
    }

    /**
     * 创建配置组件
     *
     * @return 配置界面的主面板组件 j component
     * @since y.y.y
     */
    @Override
    public @Nullable JComponent createComponent() {
        this.initTemplateText();
        this.initEventListeners();
        // 初始化时调整文本面板大小
        return this.mainPanel;
    }

    /**
     * 强制重新计算布局和绘制
     *
     * @since y.y.y
     */
    private void repaint() {
        SwingUtilities.invokeLater(() -> {
            this.classContent.revalidate();
            this.classContent.repaint();
            this.methodContent.revalidate();
            this.methodContent.repaint();
            this.fieldContent.revalidate();
            this.fieldContent.repaint();

            // 触发主面板的重新布局
            this.mainPanel.revalidate();
            this.mainPanel.repaint();
        });
    }

    /**
     * 检查配置是否被修改
     *
     * @return 如果配置被修改返回true ，否则返回false
     * @since y.y.y
     */
    @Override
    public boolean isModified() {
        DocConfigService config = DocConfigService.getInstance();
        return this.isModified
                || !Objects.equals(this.enableAi.isSelected(), config.enableAi)
                || !Objects.equals(this.apiKey.getText(), config.apiKey)
                || !Objects.equals(this.baseUrl.getText(), config.baseUrl)
                || !Objects.equals(this.modelName.getText(), config.modelName)
                || !Objects.equals(this.modelType.getSelectedItem(), config.modelType)
                || !Objects.equals(this.classTemplate.getText(), config.classTemplate)
                || !Objects.equals(this.methodTemplate.getText(), config.methodTemplate)
                || !Objects.equals(this.fieldTemplate.getText(), config.fieldTemplate)
                || !Objects.equals(this.customVar.getText(), config.customVar)
                || !Objects.equals(this.saveListener.isSelected(), config.saveListener);
    }

    /**
     * 应用配置修改
     * <p>
     * 将界面中的配置保存到配置服务中
     * </p>
     *
     * @since y.y.y
     */
    @Override
    public void apply() {
        DocConfigService config = DocConfigService.getInstance();
        config.enableAi = this.enableAi.isSelected();
        config.apiKey = this.apiKey.getText();
        config.baseUrl = this.baseUrl.getText();
        config.modelName = this.modelName.getText();
        config.modelType = (String) this.modelType.getSelectedItem();
        config.classTemplate = this.classTemplate.getText();
        config.methodTemplate = this.methodTemplate.getText();
        config.fieldTemplate = this.fieldTemplate.getText();
        config.customVar = this.customVar.getText();
        if (StrUtil.isNotBlank(config.customVar)) {
            List<String> split = StrUtil.split(config.customVar, ";");
            config.customParameters = split.stream().map(s -> {
                String[] properties = s.split("=");
                String property = properties[0];
                // 截取key后面的()号里面的数据
                String desc = StrUtil.subBetween(property, "(", ")");
                return new TemplateParameter(properties[0], properties[1], desc);
            }).toList();
        }
        config.saveListener = this.saveListener.isSelected();
        // 更新监听器状态
        FileSaveListenerManager.getInstance().updateListenerState();

        this.isModified = false;
        this.repaint();
    }

    /**
     * 初始化模板文本
     * <p>
     * 从配置服务中获取模板文本并设置到界面组件中
     * </p>
     *
     * @since y.y.y
     */
    private void initTemplateText() {
        DocConfigService config = DocConfigService.getInstance();
        this.modelName.setText(config.modelName);
        this.modelType.setSelectedItem(config.modelType);
        this.baseUrl.setText(config.baseUrl);
        this.enableAi.setSelected(config.enableAi);
        this.apiKey.setText(config.apiKey);
        this.classTemplate.setText(config.classTemplate);
        this.methodTemplate.setText(config.methodTemplate);
        this.fieldTemplate.setText(config.fieldTemplate);
        List<TemplateParameter> baseParameters = config.getBaseParameters();
        StringJoiner joiner = new StringJoiner("\n");
        for (TemplateParameter parameter : baseParameters) {
            String description = parameter.getDescription();
            String format = String.format("%s(%s)", parameter.getName(), description);
            joiner.add(format + "=" + parameter.getValue());
        }
        this.varDesc.setText(joiner.toString());
        this.customVar.setText(config.customVar);
        this.saveListener.setSelected(config.saveListener);
        this.repaint();
    }

    /**
     * 初始化事件监听器
     * <p>
     * 为界面组件添加事件监听器，用于检测配置修改
     * </p>
     *
     * @since y.y.y
     */
    private void initEventListeners() {
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                DocConfig.this.isModified = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                DocConfig.this.isModified = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                DocConfig.this.isModified = true;
            }
        };
        this.enableAi.addActionListener(e -> this.isModified = true);
        this.modelType.addActionListener(e -> this.isModified = true);
        this.baseUrl.getDocument().addDocumentListener(documentListener);
        this.modelName.getDocument().addDocumentListener(documentListener);
        this.classTemplate.getDocument().addDocumentListener(documentListener);
        this.methodTemplate.getDocument().addDocumentListener(documentListener);
        this.fieldTemplate.getDocument().addDocumentListener(documentListener);
    }

    /**
     * 重置配置
     * <p>
     * 将界面中的配置重置为配置服务中保存的值
     * </p>
     *
     * @since y.y.y
     */
    @Override
    public void reset() {
        DocConfigService config = DocConfigService.getInstance();
        this.enableAi.setSelected(config.enableAi);
        this.baseUrl.setText(config.baseUrl);
        this.modelName.setText(config.modelName);
        this.apiKey.setText(config.apiKey);
        this.modelType.setSelectedItem(config.modelType);
        this.classTemplate.setText(config.classTemplate);
        this.methodTemplate.setText(config.methodTemplate);
        this.fieldTemplate.setText(config.fieldTemplate);
        this.customVar.setText(config.customVar);
        this.saveListener.setSelected(config.saveListener);
        this.isModified = false;
        // 重置后调整文本面板大小
        this.repaint();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        this.$$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(13, 1, new Insets(0, 0, 0, 0), -1, -1));
        aiContent = new JPanel();
        aiContent.setLayout(new GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(aiContent, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        aiContent.setBorder(BorderFactory.createTitledBorder(null, "AI配置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        baseUrlTitle = new JLabel();
        baseUrlTitle.setText("模型地址");
        aiContent.add(baseUrlTitle, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        baseUrl = new JTextField();
        aiContent.add(baseUrl, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        aiContent.add(spacer1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        modelName = new JTextField();
        aiContent.add(modelName, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        modelNameTitle = new JLabel();
        modelNameTitle.setText("模型名称");
        aiContent.add(modelNameTitle, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        aiContent.add(spacer2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        enableAi = new JCheckBox();
        enableAi.setText("开启AI");
        aiContent.add(enableAi, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        aiContent.add(spacer3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        modelTypeTitle = new JLabel();
        modelTypeTitle.setText("模型类型");
        aiContent.add(modelTypeTitle, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modelType = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("openai");
        modelType.setModel(defaultComboBoxModel1);
        aiContent.add(modelType, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        aiContent.add(spacer4, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        apiKeyTitle = new JLabel();
        apiKeyTitle.setText("APIKEY");
        aiContent.add(apiKeyTitle, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        apiKey = new JTextField();
        aiContent.add(apiKey, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        varContent = new JPanel();
        varContent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(varContent, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        varContent.setBorder(BorderFactory.createTitledBorder(null, "内置变量", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        varDesc = new JTextPane();
        varDesc.setEditable(false);
        varDesc.setEnabled(true);
        varContent.add(varDesc, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        customContent = new JPanel();
        customContent.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(customContent, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        customContent.setBorder(BorderFactory.createTitledBorder(null, "自定义变量（k(描述)=v形式使用分号分割）", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        customVar = new JTextArea();
        customContent.add(customVar, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final Spacer spacer5 = new Spacer();
        mainPanel.add(spacer5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        mainPanel.add(spacer6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        mainPanel.add(spacer7, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        mainPanel.add(spacer8, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        mainPanel.add(spacer9, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        advancedFeaturesContent = new JPanel();
        advancedFeaturesContent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(advancedFeaturesContent, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        advancedFeaturesContent.setBorder(BorderFactory.createTitledBorder(null, "高级特性", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        saveListener = new JCheckBox();
        saveListener.setText("开启保存监听");
        advancedFeaturesContent.add(saveListener, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        mainPanel.add(spacer10, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        classContent = new JScrollPane();
        mainPanel.add(classContent, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        classContent.setBorder(BorderFactory.createTitledBorder(null, "类模板配置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        classTemplate = new JTextArea();
        classContent.setViewportView(classTemplate);
        methodContent = new JScrollPane();
        mainPanel.add(methodContent, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        methodContent.setBorder(BorderFactory.createTitledBorder(null, "方法模板配置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        methodTemplate = new JTextArea();
        methodContent.setViewportView(methodTemplate);
        fieldContent = new JScrollPane();
        mainPanel.add(fieldContent, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fieldContent.setBorder(BorderFactory.createTitledBorder(null, "字段模板配置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        fieldTemplate = new JTextArea();
        fieldContent.setViewportView(fieldTemplate);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
