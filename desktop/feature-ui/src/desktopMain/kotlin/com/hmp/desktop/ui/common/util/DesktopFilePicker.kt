package com.hmp.desktop.ui.common.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * 桌面端文件选择器工具
 * 使用 AWT/Swing 原生文件对话框
 */
object DesktopFilePicker {

    /**
     * 选择图片文件（用于头像选择）
     * @return 选中的文件路径，取消时返回 null
     */
    fun pickImageFile(): String? {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.webp)",
                "jpg", "jpeg", "png", "webp"
            )
            isMultiSelectionEnabled = false
        }

        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
    }

    /**
     * 选择备份文件（用于恢复备份）
     * @return 选中的文件路径，取消时返回 null
     */
    fun pickBackupFile(): String? {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter(
                "Backup Files (*.json, *.zip, *.hmp)",
                "json", "zip", "hmp"
            )
            isMultiSelectionEnabled = false
        }

        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
    }

    /**
     * 选择保存路径（用于导出备份）
     * @param defaultName 默认文件名
     * @return 选中的文件路径，取消时返回 null
     */
    fun pickSaveLocation(defaultName: String = "backup.hmp"): String? {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            selectedFile = File(defaultName)
        }

        val result = chooser.showSaveDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
    }
}
