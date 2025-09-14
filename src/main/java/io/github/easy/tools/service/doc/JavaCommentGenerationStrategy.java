package io.github.easy.tools.service.doc;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaDocumentedElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.easy.tools.entity.doc.TemplateParameter;
import io.github.easy.tools.ui.config.DocConfigService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java 注释生成策略，使用 Velocity 模板引擎生成注释。
 * <p>
 * 该类实现了CommentGenerationStrategy接口，提供了Java文件注释生成和删除的具体实现。
 * 支持类、方法和字段三种元素类型的注释处理，使用策略模式和工厂模式来管理不同元素类型的处理器。
 * </p>
 */
public class JavaCommentGenerationStrategy implements CommentGenerationStrategy {

    /**
     * 文档处理器映射表，用于根据元素类型获取对应的处理器
     */
    private static final Map<String, DocHandler> docHandlerMap = new HashMap<>();

    /**
     * 静态初始化块，初始化文档处理器映射表
     */
    static {
        docHandlerMap.put("class", new ClassDocHandler());
        docHandlerMap.put("method", new MethodDocHandler());
        docHandlerMap.put("field", new FieldDocHandler());
    }

    /**
     * 为文件生成注释
     * <p>
     * 遍历文件中的所有元素并为可注释的元素生成注释
     * </p>
     *
     * @param file 需要生成注释的文件
     */
    @Override
    public void generate(PsiFile file) {
        // 遍历文件中的所有元素并生成注释
        this.generateCommentsRecursively(file, file);
    }

    /**
     * 递归遍历元素并生成注释
     *
     * @param file    当前文件
     * @param element 当前元素
     */
    private void generateCommentsRecursively(PsiFile file, PsiElement element) {
        // 为当前元素生成注释（如果是可注释的元素）
        if (element instanceof PsiClass ||
                element instanceof PsiMethod ||
                element instanceof PsiField) {
            this.generate(file, element);
        }

        // 递归处理所有子元素
        for (PsiElement child : element.getChildren()) {
            this.generateCommentsRecursively(file, child);
        }
    }

    /**
     * 为元素生成注释
     * <p>
     * 根据元素类型选择合适的处理器生成注释内容，并写入到文件中
     * </p>
     *
     * @param file    需要生成注释的文件
     * @param element 需要生成注释的元素
     */
    @Override
    public void generate(PsiFile file, PsiElement element) {
        String doc = "";
        DocHandler handler = null;
        if (element instanceof PsiClass) {
            handler = docHandlerMap.get("class");
        } else if (element instanceof PsiMethod) {
            handler = docHandlerMap.get("method");
        } else if (element instanceof PsiField) {
            handler = docHandlerMap.get("field");
        }

        if (handler != null) {
            doc = handler.generateDoc(file, element);
        }
        Project project = file.getProject();
        this.writeDoc(project, element, doc);
    }

    /**
     * 将生成的注释写入到元素中
     *
     * @param project    项目实例
     * @param element    目标元素
     * @param docContent 注释内容
     */
    private void writeDoc(Project project, PsiElement element, String docContent) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                if (element instanceof PsiJavaDocumentedElement psiJavaDocumentedElement) {
                    PsiDocComment docCommentFromText = elementFactory.createDocCommentFromText(docContent);
                    PsiDocComment docComment = psiJavaDocumentedElement.getDocComment();
                    if (docComment != null) {
                        docComment.replace(docCommentFromText);
                    } else {
                        psiJavaDocumentedElement.addBefore(docCommentFromText, psiJavaDocumentedElement.getFirstChild());
                    }
                }
            } catch (Exception e) {
                // 使用消息进行提示
                e.printStackTrace();
            }
        });
    }

    /**
     * 删除文件中的所有注释
     * <p>
     * 删除指定文件中所有可注释元素的文档注释
     * </p>
     *
     * @param file 需要删除注释的文件
     */
    @Override
    public void remove(PsiFile file) {
        // 实现删除文件中所有注释的逻辑
        WriteCommandAction.runWriteCommandAction(file.getProject(), () -> {
            // 递归遍历所有元素并删除注释
            this.removeCommentsRecursively(file);
        });
    }

    /**
     * 递归删除元素及其子元素的注释
     *
     * @param element 要删除注释的元素
     */
    private void removeCommentsRecursively(PsiElement element) {
        if (element instanceof PsiJavaDocumentedElement psiJavaDocumentedElement) {
            PsiDocComment docComment = psiJavaDocumentedElement.getDocComment();
            if (docComment != null) {
                docComment.delete();
            }
        }

        // 递归处理所有子元素
        for (PsiElement child : element.getChildren()) {
            this.removeCommentsRecursively(child);
        }
    }

    /**
     * 删除元素的注释
     * <p>
     * 删除指定元素的文档注释
     * </p>
     *
     * @param file    需要删除注释的文件
     * @param element 需要删除注释的元素
     */
    @Override
    public void remove(PsiFile file, PsiElement element) {
        // 实现删除特定元素注释的逻辑
        WriteCommandAction.runWriteCommandAction(file.getProject(), () -> {
            if (element instanceof PsiJavaDocumentedElement psiJavaDocumentedElement) {
                PsiDocComment docComment = psiJavaDocumentedElement.getDocComment();
                if (docComment != null) {
                    docComment.delete();
                }
            } else {
                element.delete();
            }
        });
    }

    // 内部接口：文档处理器

    /**
     * 文档处理器接口，定义了文档生成的方法
     *
     * @param <P> 处理的元素类型
     */
    private interface DocHandler<P extends PsiElement> {
        /**
         * 生成元素的文档
         *
         * @param file    文件
         * @param element 元素
         * @return 生成的文档内容
         */
        String generateDoc(PsiFile file, P element);
    }

    /**
     * 抽象文档处理器，提供了文档生成的通用实现
     *
     * @param <P> 处理的元素类型
     */
    private static abstract class AbstractDocHandler<P extends PsiElement> implements DocHandler<P> {

        /**
         * 模板渲染服务实例，用于渲染模板
         */
        protected final TemplateRenderer templateRenderer = TemplateRendererFactory.getTemplateRenderer();

        /**
         * 生成元素的文档
         * <p>
         * 获取模板参数并创建上下文，然后调用具体实现生成文档内容
         * </p>
         *
         * @param file    文件
         * @param element 元素
         * @return 生成的文档内容
         */
        @Override
        public String generateDoc(PsiFile file, P element) {
            // 1. 获取模板参数（基础 + 自定义 + 特定元素参数）
            Context context = this.createContext(file, element);
            return this.templateRenderer.render(this.doGenerateDoc(file, element, context), context, element);
        }

        /**
         * 执行具体的文档生成
         *
         * @param file    文件
         * @param element 元素
         * @param context 上下文
         * @return 模板内容
         */
        protected abstract String doGenerateDoc(PsiFile file, P element, Context context);

        /**
         * 构建 Velocity 上下文
         *
         * @param file    文件
         * @param element 元素
         * @return Velocity上下文
         */
        private Context createContext(PsiFile file, P element) {
            VelocityContext context = new VelocityContext();

            // 添加基础参数到上下文
            this.addBaseParameters(context, file);

            // 添加自定义参数到上下文
            this.addCustomParameters(context);

            // 添加特定元素参数到上下文
            this.addElementSpecificParameters(context, element);

            return context;
        }

        /**
         * 添加基础参数到上下文
         *
         * @param context Velocity上下文
         * @param file    当前文件
         */
        private void addBaseParameters(VelocityContext context, PsiFile file) {
            List<TemplateParameter> baseParameters = this.getBaseParameters(file);
            for (TemplateParameter param : baseParameters) {
                context.put(param.getName(), param.getValue());
            }
        }

        /**
         * 添加自定义参数到上下文
         *
         * @param context Velocity上下文
         */
        private void addCustomParameters(VelocityContext context) {
            List<TemplateParameter> customParameters = this.getCustomParameters();
            for (TemplateParameter param : customParameters) {
                context.put(param.getName(), param.getValue());
            }
        }

        /**
         * 添加特定元素参数到上下文
         *
         * @param context Velocity上下文
         * @param element 当前处理的元素
         */
        protected abstract void addElementSpecificParameters(VelocityContext context, P element);

        /**
         * 获取基础参数列表
         *
         * @param file 文件
         * @return 基础参数列表
         */
        private List<TemplateParameter> getBaseParameters(PsiFile file) {
            List<TemplateParameter> baseParameters = DocConfigService.getInstance().getBaseParameters();
            // 替换version
            baseParameters.stream()
                    .filter(param -> param.getName().equals("version"))
                    .findFirst()
                    .ifPresent(param -> {
                        String version = this.getProjectVersion(file);
                        param.setValue(version);
                    });
            return baseParameters;
        }

        /**
         * 获取项目版本号
         *
         * @param file 当前文件
         * @return 项目版本号
         */
        private String getProjectVersion(PsiFile file) {
            String version = "1.0.0";
            try {
                // 获取项目根目录
                Project project = file.getProject();
                // 修改为通过文件路径向上查找项目根目录
                VirtualFile projectDir = file.getVirtualFile().getParent();
                while (projectDir != null && projectDir.findChild("pom.xml") == null) {
                    projectDir = projectDir.getParent();
                }
                if (projectDir != null) {
                    // 查找 pom.xml 文件
                    VirtualFile pomFile = projectDir.findChild("pom.xml");
                    if (pomFile != null && pomFile.exists()) {
                        // 解析 pom.xml 文件获取版本号
                        String pomContent = new String(pomFile.contentsToByteArray());
                        version = this.extractVersionFromPom(pomContent);
                    }
                }
            } catch (Exception e) {
                // 如果出现异常，使用默认版本号
            }
            return version;
        }

        /**
         * 从 pom.xml 内容中提取版本号
         *
         * @param pomContent pom.xml 文件内容
         * @return 版本号
         */
        private String extractVersionFromPom(String pomContent) {
            String version = "1.0.0";
            try {
                // 简单的 XML 解析，提取 <version> 标签内容
                int versionStart = pomContent.indexOf("<version>");
                if (versionStart != -1) {
                    int versionEnd = pomContent.indexOf("</version>", versionStart);
                    if (versionEnd != -1) {
                        version = pomContent.substring(
                                versionStart + "<version>".length(),
                                versionEnd
                        ).trim();
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，使用默认版本号
            }
            return version;
        }

        /**
         * 获取用户自定义参数列表
         *
         * @return 自定义参数列表
         */
        private List<TemplateParameter> getCustomParameters() {
            return DocConfigService.getInstance().customParameters;
        }
    }


    /**
     * 类文档处理器，处理类元素的文档生成
     */
    private static class ClassDocHandler extends AbstractDocHandler<PsiClass> {

        /**
         * 执行类文档生成
         *
         * @param file    文件
         * @param element 类元素
         * @param context 上下文
         * @return 类模板内容
         */
        @Override
        protected String doGenerateDoc(PsiFile file, PsiClass element, Context context) {
            DocConfigService cfg = DocConfigService.getInstance();
            return this.templateRenderer.render(cfg.classTemplate, context, element);
        }

        /**
         * 添加类元素特定参数到上下文
         *
         * @param context Velocity上下文
         * @param element 类元素
         */
        @Override
        protected void addElementSpecificParameters(VelocityContext context, PsiClass element) {
            context.put("description", element.getName());
            context.put("since", "1.0.0");
        }
    }

    /**
     * 方法文档处理器，处理方法元素的文档生成
     */
    private static class MethodDocHandler extends AbstractDocHandler<PsiMethod> {

        /**
         * 执行方法文档生成
         *
         * @param file    文件
         * @param element 方法元素
         * @param context 上下文
         * @return 方法模板内容
         */
        @Override
        protected String doGenerateDoc(PsiFile file, PsiMethod element, Context context) {
            DocConfigService cfg = DocConfigService.getInstance();
            return this.templateRenderer.render(cfg.methodTemplate, context, element);
        }

        /**
         * 添加方法元素特定参数到上下文
         *
         * @param context Velocity上下文
         * @param element 方法元素
         */
        @Override
        protected void addElementSpecificParameters(VelocityContext context, PsiMethod element) {
            context.put("description", element.getName() + " method");
            // 添加方法返回值类型
            if (element.getReturnType() != null) {
                context.put("returnType", element.getReturnType().getPresentableText());
            }
            // 添加方法参数信息
            List<Map<String, String>> parameters = new ArrayList<>();
            for (PsiParameter parameter : element.getParameterList().getParameters()) {
                Map<String, String> param = new HashMap<>();
                param.put("name", parameter.getName());
                param.put("description", parameter.getType().getPresentableText());
                parameters.add(param);
            }
            context.put("parameters", parameters);

            // 添加方法抛出的异常信息
            List<String> exceptions = new ArrayList<>();
            for (com.intellij.psi.PsiClassType exceptionType : element.getThrowsList().getReferencedTypes()) {
                exceptions.add(exceptionType.getPresentableText());
            }
            context.put("exceptions", exceptions);
        }
    }

    /**
     * 字段文档处理器，处理字段元素的文档生成
     */
    private static class FieldDocHandler extends AbstractDocHandler<PsiField> {

        /**
         * 执行字段文档生成
         *
         * @param file    文件
         * @param element 字段元素
         * @param context 上下文
         * @return 字段模板内容
         */
        @Override
        protected String doGenerateDoc(PsiFile file, PsiField element, Context context) {
            DocConfigService cfg = DocConfigService.getInstance();
            return this.templateRenderer.render(cfg.fieldTemplate, context, element);
        }

        /**
         * 添加字段元素特定参数到上下文
         *
         * @param context Velocity上下文
         * @param element 字段元素
         */
        @Override
        protected void addElementSpecificParameters(VelocityContext context, PsiField element) {
            context.put("fieldName", element.getName());
        }
    }
}
