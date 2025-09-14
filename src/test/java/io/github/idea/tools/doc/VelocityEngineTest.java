package io.github.idea.tools.doc;

import io.github.easy.tools.service.doc.VelocityTemplateService;
import org.apache.velocity.VelocityContext;
import org.junit.Test;

/**
 * <p> </p>
 *
 * @author haijun
 * @version x.x.x
 * @email "mailto:zhonghaijun@zhxx.com"
 * @date 2025.07.22 20:09
 * @since x.x.x
 */
public class VelocityEngineTest {

    @Test
    public void testVelocity() {
        VelocityTemplateService service = new VelocityTemplateService();
        VelocityContext context = new VelocityContext();
        context.put("name", "张三");
        String render = service.render("${name}", context);
        System.out.println(render);
    }

}
