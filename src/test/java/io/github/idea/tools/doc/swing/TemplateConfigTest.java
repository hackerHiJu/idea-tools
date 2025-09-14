package io.github.idea.tools.doc.swing;

import org.junit.Test;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class TemplateConfigTest extends JPanel { // 确保继承 JPanel

    private JCheckBox enableAI;
    private JComboBox<String> modelType;
    private JTextField modelAddress;
    private JTextField modelName;
    private JTextArea classConfig;
    private JTextArea methodTemplate;
    private JTextArea fieldTemplate;
    private JTextArea internalVariables;
    private JTextArea customVariables;

    public TemplateConfigTest() {
        setLayout(new BorderLayout()); // 设置布局管理器

        // AI Configuration Section
        JPanel aiConfigPanel = new JPanel(new GridLayout(4, 2));
        enableAI = new JCheckBox("开启AI");
        modelType = new JComboBox<>(new String[]{"openai"});
        modelAddress = new JTextField();
        modelName = new JTextField();

        aiConfigPanel.add(enableAI);
        aiConfigPanel.add(new JLabel("模型类型"));
        aiConfigPanel.add(modelType);
        aiConfigPanel.add(new JLabel("模型地址"));
        aiConfigPanel.add(modelAddress);
        aiConfigPanel.add(new JLabel("模型名称"));
        aiConfigPanel.add(modelName);

        // Annotation Template Configuration Section
        JPanel annotationConfigPanel = new JPanel(new BorderLayout());
        JPanel annotationLabels = new JPanel(new GridLayout(3, 1));
        annotationLabels.add(new JLabel("类配置"));
        annotationLabels.add(new JLabel("方法模板"));
        annotationLabels.add(new JLabel("字段模板"));

        JPanel annotationAreas = new JPanel(new GridLayout(3, 1));
        classConfig = new JTextArea(5, 20);
        methodTemplate = new JTextArea(5, 20);
        fieldTemplate = new JTextArea(5, 20);
        annotationAreas.add(classConfig);
        annotationAreas.add(methodTemplate);
        annotationAreas.add(fieldTemplate);

        annotationConfigPanel.add(annotationLabels, BorderLayout.WEST);
        annotationConfigPanel.add(annotationAreas, BorderLayout.CENTER);

        // Internal Variables Section
        JPanel internalVariablesPanel = new JPanel(new BorderLayout());
        internalVariablesPanel.add(new JLabel("内置变量"), BorderLayout.NORTH);
        internalVariables = new JTextArea(5, 20);
        internalVariablesPanel.add(internalVariables, BorderLayout.CENTER);

        // Custom Variables Section
        JPanel customVariablesPanel = new JPanel(new BorderLayout());
        customVariablesPanel.add(new JLabel("自定义变量 (k=v形式使用分号分割)"), BorderLayout.NORTH);
        customVariables = new JTextArea(5, 20);
        customVariablesPanel.add(customVariables, BorderLayout.CENTER);

        // Add all sections to the main panel
        add(aiConfigPanel, BorderLayout.NORTH);
        add(annotationConfigPanel, BorderLayout.CENTER);

        // 合并底部两个面板为一个整体
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.add(internalVariablesPanel);
        bottomPanel.add(customVariablesPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        // 设置所有 JTextArea 的边框
        classConfig.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));
        methodTemplate.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));
        fieldTemplate.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));
        internalVariables.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));
        customVariables.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));
    }

    @Test
    public void testConfigUI() throws InterruptedException {
        JFrame frame = new JFrame("AI Configuration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new TemplateConfigTest());
        frame.pack();
        frame.setSize(600, 800);
        frame.setVisible(true);
        Thread.currentThread().join();
    }
}
