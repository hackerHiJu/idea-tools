package io.github.easy.tools.action.conversion;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性名称转换工具类
 * 提供多种格式的属性名称转换功能
 */
public class PropertyNameConverter {

    /**
     * 转换为小写驼峰格式 (lower camel case)
     * 例如: USER_NAME -> userName, user_name -> userName
     *
     * @param name 原始名称
     * @return 小写驼峰格式
     */
    public static String toLowerCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        List<String> words = splitToWords(name);
        if (words.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (i == 0) {
                result.append(word.toLowerCase());
            } else {
                result.append(capitalize(word));
            }
        }

        return result.toString();
    }

    /**
     * 转换为大写驼峰格式 (upper camel case)
     * 例如: user_name -> UserName, USER_NAME -> UserName
     *
     * @param name 原始名称
     * @return 大写驼峰格式
     */
    public static String toUpperCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        List<String> words = splitToWords(name);
        if (words.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(capitalize(word));
        }

        return result.toString();
    }

    /**
     * 转换为小写下划线格式 (lower snake case)
     * 例如: userName -> user_name, UserName -> user_name
     *
     * @param name 原始名称
     * @return 小写下划线格式
     */
    public static String toLowerUnderline(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        return String.join("_", splitToWords(name)).toLowerCase();
    }

    /**
     * 转换为大写下划线格式 (upper snake case)
     * 例如: userName -> USER_NAME, user_name -> USER_NAME
     *
     * @param name 原始名称
     * @return 大写下划线格式
     */
    public static String toUpperUnderline(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        return String.join("_", splitToWords(name)).toUpperCase();
    }

    /**
     * 将属性名分割为单词列表
     *
     * @param name 原始名称
     * @return 单词列表
     */
    private static List<String> splitToWords(String name) {
        List<String> words = new ArrayList<>();

        if (name == null || name.isEmpty()) {
            return words;
        }

        // 处理包含下划线的情况
        if (name.contains("_")) {
            String[] parts = name.split("_");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    words.add(part.toLowerCase());
                }
            }
            return words;
        }

        // 处理驼峰命名
        StringBuilder currentWord = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            // 如果是大写字母且不是第一个字符，则开始一个新单词
            if (Character.isUpperCase(c) && currentWord.length() > 0) {
                words.add(currentWord.toString().toLowerCase());
                currentWord = new StringBuilder();
            }

            currentWord.append(c);
        }

        // 添加最后一个单词
        if (currentWord.length() > 0) {
            words.add(currentWord.toString().toLowerCase());
        }

        return words;
    }

    /**
     * 首字母大写
     *
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
