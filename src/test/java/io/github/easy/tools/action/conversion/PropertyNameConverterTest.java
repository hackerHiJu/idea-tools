package io.github.easy.tools.action.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 属性名称转换器测试类
 */
public class PropertyNameConverterTest {
    
    @Test
    public void testToLowerCamelCase() {
        // 测试下划线转小写驼峰
        assertEquals("userName", PropertyNameConverter.toLowerCamelCase("user_name"));
        assertEquals("xmlHttpRequest", PropertyNameConverter.toLowerCamelCase("xml_http_request"));
        
        // 测试大写驼峰转小写驼峰
        assertEquals("userName", PropertyNameConverter.toLowerCamelCase("UserName"));
        assertEquals("xmlHttpRequest", PropertyNameConverter.toLowerCamelCase("XMLHTTPRequest"));
        
        // 测试大写下划线转小写驼峰
        assertEquals("userAge", PropertyNameConverter.toLowerCamelCase("USER_AGE"));
        assertEquals("xmlHttpRequest", PropertyNameConverter.toLowerCamelCase("XML_HTTP_REQUEST"));
        
        // 边界情况
        assertNull(PropertyNameConverter.toLowerCamelCase(null));
        assertEquals("", PropertyNameConverter.toLowerCamelCase(""));
    }
    
    @Test
    public void testToUpperCamelCase() {
        // 测试下划线转大写驼峰
        assertEquals("UserName", PropertyNameConverter.toUpperCamelCase("user_name"));
        assertEquals("XmlHttpRequest", PropertyNameConverter.toUpperCamelCase("xml_http_request"));
        
        // 测试小写驼峰转大写驼峰
        assertEquals("UserName", PropertyNameConverter.toUpperCamelCase("userName"));
        assertEquals("XmlHttpRequest", PropertyNameConverter.toUpperCamelCase("xmlHttpRequest"));
        
        // 测试大写下划线转大写驼峰
        assertEquals("UserAge", PropertyNameConverter.toUpperCamelCase("USER_AGE"));
        assertEquals("XmlHttpRequest", PropertyNameConverter.toUpperCamelCase("XML_HTTP_REQUEST"));
        
        // 边界情况
        assertNull(PropertyNameConverter.toUpperCamelCase(null));
        assertEquals("", PropertyNameConverter.toUpperCamelCase(""));
    }
    
    @Test
    public void testToLowerUnderline() {
        // 测试驼峰转小写下划线
        assertEquals("user_name", PropertyNameConverter.toLowerUnderline("userName"));
        assertEquals("user_name", PropertyNameConverter.toLowerUnderline("UserName"));
        assertEquals("xml_http_request", PropertyNameConverter.toLowerUnderline("XMLHTTPRequest"));
        assertEquals("xml_http_request", PropertyNameConverter.toLowerUnderline("xmlHttpRequest"));
        
        // 测试大写下划线转小写下划线
        assertEquals("user_name", PropertyNameConverter.toLowerUnderline("USER_NAME"));
        assertEquals("xml_http_request", PropertyNameConverter.toLowerUnderline("XML_HTTP_REQUEST"));
        
        // 边界情况
        assertNull(PropertyNameConverter.toLowerUnderline(null));
        assertEquals("", PropertyNameConverter.toLowerUnderline(""));
    }
    
    @Test
    public void testToUpperUnderline() {
        // 测试驼峰转大写下划线
        assertEquals("USER_NAME", PropertyNameConverter.toUpperUnderline("userName"));
        assertEquals("USER_NAME", PropertyNameConverter.toUpperUnderline("UserName"));
        assertEquals("XML_HTTP_REQUEST", PropertyNameConverter.toUpperUnderline("XMLHTTPRequest"));
        assertEquals("XML_HTTP_REQUEST", PropertyNameConverter.toUpperUnderline("xmlHttpRequest"));
        
        // 测试小写下划线转大写下划线
        assertEquals("USER_NAME", PropertyNameConverter.toUpperUnderline("user_name"));
        assertEquals("XML_HTTP_REQUEST", PropertyNameConverter.toUpperUnderline("xml_http_request"));
        
        // 边界情况
        assertNull(PropertyNameConverter.toUpperUnderline(null));
        assertEquals("", PropertyNameConverter.toUpperUnderline(""));
    }
    
    @Test
    public void testRealWorldScenarios() {
        // 类中的常量 (通常全大写+下划线)
        assertEquals("max_buffer_size", PropertyNameConverter.toLowerUnderline("MAXBUFFERSIZE"));
        assertEquals("max_buffer_size", PropertyNameConverter.toLowerUnderline("MAX_BUFFER_SIZE"));
        
        // 属性字段 (通常驼峰命名)
        assertEquals("user_name", PropertyNameConverter.toLowerUnderline("userName"));
        assertEquals("first_name", PropertyNameConverter.toLowerUnderline("firstName"));
        assertEquals("http_client", PropertyNameConverter.toLowerUnderline("httpClient"));
        assertEquals("xml_parser", PropertyNameConverter.toLowerUnderline("XMLParser"));
        
        // 方法名称 (通常驼峰命名)
        assertEquals("get_user_name", PropertyNameConverter.toLowerUnderline("getUserName"));
        assertEquals("set_first_name", PropertyNameConverter.toLowerUnderline("setFirstName"));
        assertEquals("is_active", PropertyNameConverter.toLowerUnderline("isActive"));
        assertEquals("to_string", PropertyNameConverter.toLowerUnderline("toString"));
        assertEquals("parse_xml_data", PropertyNameConverter.toLowerUnderline("parseXMLData"));
        
        // 转换为各种格式
        String input = "XMLHTTPRequest";
        assertEquals("xmlHttpRequest", PropertyNameConverter.toLowerCamelCase(input));
        assertEquals("XmlHttpRequest", PropertyNameConverter.toUpperCamelCase(input));
        assertEquals("xml_http_request", PropertyNameConverter.toLowerUnderline(input));
        assertEquals("XML_HTTP_REQUEST", PropertyNameConverter.toUpperUnderline(input));
    }
}