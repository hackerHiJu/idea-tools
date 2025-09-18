package io.github.easy.tools.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * 通知工具类
 * <p>
 * 用于统一处理IDEA中的通知消息，包括错误、警告和信息提示
 * </p>
 */
public class NotificationUtil {

    /**
     * 通知组名称
     */
    private static final String NOTIFICATION_GROUP = "Easy Docs Notification Group";

    /**
     * 显示错误通知
     *
     * @param project 项目实例
     * @param message 错误消息
     */
    public static void showError(Project project, String message) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)
                .createNotification(message, NotificationType.ERROR);
        notification.notify(project);
    }

    /**
     * 显示警告通知
     *
     * @param project 项目实例
     * @param message 警告消息
     */
    public static void showWarning(Project project, String message) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)
                .createNotification(message, NotificationType.WARNING);
        notification.notify(project);
    }

    /**
     * 显示信息通知
     *
     * @param project 项目实例
     * @param message 信息消息
     */
    public static void showInfo(Project project, String message) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)
                .createNotification(message, NotificationType.INFORMATION);
        notification.notify(project);
    }
}