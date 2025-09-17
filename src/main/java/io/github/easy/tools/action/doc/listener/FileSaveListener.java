package io.github.easy.tools.action.doc.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.easy.tools.processor.doc.CommentProcessor;
import io.github.easy.tools.processor.doc.JavaCommentProcessor;
import io.github.easy.tools.ui.config.DocConfigService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件保存监听器
 * <p>
 * 监听文件保存事件，当配置开启saveListener时，会在文件保存后自动生成文档注释
 * </p>
 */
public class FileSaveListener implements FileDocumentManagerListener {

    /**
     * 注释处理器映射
     */
    private static final Map<String, CommentProcessor> PROCESSOR_MAP = new HashMap<>();

    /**
     * 正在处理的文档集合，用于避免死循环
     */
    private static final Set<Document> PROCESSING_DOCUMENTS = ConcurrentHashMap.newKeySet();

    /**
     * Java元素类型列表，包括文档注释、类、方法和字段
     */
    private static final Class<?>[] JAVA_ELEMENTS = {
            PsiDocComment.class,
            PsiClass.class,
            PsiMethod.class,
            PsiField.class
    };

    static {
        PROCESSOR_MAP.put("JAVA", new JavaCommentProcessor());
    }

    /**
     * 在文件保存前触发的方法
     *
     * @param document 将要保存的文档
     */
    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        // 检查是否开启了保存监听功能
        if (!DocConfigService.getInstance().saveListener) {
            return;
        }

        // 检查文档是否正在处理中，避免死循环
        if (PROCESSING_DOCUMENTS.contains(document)) {
            return;
        }

        // 获取所有打开的项目
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            // 获取文档管理器
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            // 通过文档获取对应的PsiFile
            PsiFile psiFile = psiDocumentManager.getPsiFile(document);

            // 如果是支持的文件类型，则准备处理
            if (psiFile != null && PROCESSOR_MAP.containsKey(psiFile.getFileType().getName())) {
                // 使用invokeLater将PSI修改操作推迟执行，确保在保存操作完成后执行
                ApplicationManager.getApplication().invokeLater(() -> {
                    // 确保项目未被释放
                    if (!project.isDisposed()) {
                        // 提交文档确保PSI同步
                        psiDocumentManager.commitDocument(document);

                        // 获取当前活动的编辑器
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        FileEditor fileEditor = fileEditorManager.getSelectedEditor(psiFile.getVirtualFile());
                        Editor selectedEditor = null;

                        // 如果是文本编辑器，则获取其中的Editor实例
                        if (fileEditor instanceof TextEditor) {
                            selectedEditor = ((TextEditor) fileEditor).getEditor();
                        }

                        // 获取光标位置对应的待注释元素
                        PsiElement elementAtCaret = selectedEditor != null ?
                                this.getElementAtCaret(psiFile, selectedEditor) :
                                psiFile;

                        // 获取对应文件类型的处理器
                        CommentProcessor processor = PROCESSOR_MAP.get(psiFile.getFileType().getName());

                        if (processor != null) {
                            // 标记文档正在处理中
                            PROCESSING_DOCUMENTS.add(document);

                            try {
                                // 在写操作中执行注释生成
                                ApplicationManager.getApplication().runWriteAction(() -> {
                                    // 如果元素是文件本身，则为整个文件生成注释，否则只为当前元素生成注释
                                    if (elementAtCaret == psiFile) {
                                        processor.generateFileComment(psiFile, true);
                                    } else {
                                        processor.generateElementComment(psiFile, elementAtCaret, true);
                                    }
                                });
                            } finally {
                                // 处理完成后移除标记
                                PROCESSING_DOCUMENTS.remove(document);
                            }
                        }
                    }
                });
                break;
            }
        }
    }

    /**
     * 获取光标位置的元素
     *
     * @param file   文件
     * @param editor 编辑器
     * @return 光标位置的元素
     */
    private PsiElement getElementAtCaret(PsiFile file, Editor editor) {
        if (editor == null) {
            return file;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);

        // 跳过空白字符
        while (element instanceof PsiWhiteSpace) {
            element = element.getNextSibling();
        }

        // 如果找不到元素，返回文件本身
        if (element == null) {
            return file;
        }

        // 查找最近的可注释元素
        return this.findElementFromCaret(file, offset);
    }

    /**
     * 从光标位置开始查找最近的可生成注释的元素
     * 改进查找逻辑，支持向前查找和向上查找父级元素
     *
     * @param file   当前文件
     * @param offset 光标位置偏移量
     * @return 找到的元素，如果未找到则返回文件本身
     */
    private PsiElement findElementFromCaret(PsiFile file, int offset) {
        // 首先尝试在光标位置找到元素
        PsiElement element = file.findElementAt(offset);

        // 如果光标位置没有元素，则尝试在光标前一个位置查找
        if (element == null && offset > 0) {
            element = file.findElementAt(offset - 1);
        }

        // 跳过空白字符
        while (element instanceof PsiWhiteSpace && element.getNextSibling() != null) {
            element = element.getNextSibling();
        }

        // 如果仍然找不到元素，尝试在光标前查找
        if (element == null || element instanceof PsiWhiteSpace) {
            element = findElementBeforeCaret(file, offset);
        }

        // 检查当前元素是否可以直接注释
        if (isCommentableElement(element)) {
            return element;
        }

        // 向后查找同级元素
        PsiElement nextElement = element;
        while (nextElement != null) {
            nextElement = nextElement.getNextSibling();
            if (isCommentableElement(nextElement)) {
                return nextElement;
            }
        }

        // 向前查找同级元素
        PsiElement prevElement = element;
        while (prevElement != null) {
            prevElement = prevElement.getPrevSibling();
            if (isCommentableElement(prevElement)) {
                return prevElement;
            }
        }

        // 向上查找父级元素
        PsiElement parentElement = element;
        while (parentElement != null) {
            parentElement = parentElement.getParent();
            if (isCommentableElement(parentElement)) {
                return parentElement;
            }
        }

        return file;
    }

    /**
     * 在光标前查找元素
     *
     * @param file   当前文件
     * @param offset 光标位置偏移量
     * @return 光标前的元素
     */
    private PsiElement findElementBeforeCaret(PsiFile file, int offset) {
        for (int i = offset - 1; i >= 0; i--) {
            PsiElement element = file.findElementAt(i);
            if (element != null && !(element instanceof PsiWhiteSpace)) {
                return element;
            }
        }
        return null;
    }

    /**
     * 检查元素是否为可注释的Java元素类型
     *
     * @param element 待检查的Psi元素
     * @return 如果元素是可注释的Java元素返回true，否则返回false
     */
    private boolean isCommentableElement(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        for (Class<?> javaElement : JAVA_ELEMENTS) {
            if (javaElement.isAssignableFrom(element.getClass())) {
                return true;
            }
        }
        return false;
    }
}
