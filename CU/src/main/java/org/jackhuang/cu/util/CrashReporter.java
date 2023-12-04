/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.CU.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.jackhuang.CU.Metadata;
import org.jackhuang.CU.countly.CrashReport;
import org.jackhuang.CU.ui.CrashWindow;
import org.jackhuang.CU.upgrade.IntegrityChecker;
import org.jackhuang.CU.upgrade.UpdateChecker;
import org.jackhuang.CU.util.io.NetworkUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import static org.jackhuang.CU.util.Logging.LOG;
import static org.jackhuang.CU.util.Pair.pair;
import static org.jackhuang.CU.util.i18n.I18n.i18n;

/**
 * @author huangyuhui
 */
public class CrashReporter implements Thread.UncaughtExceptionHandler {

    // Lazy initialization resources
    private static final class Hole {
        @SuppressWarnings("unchecked")
        static final Pair<String, String>[] SOURCE = (Pair<String, String>[]) new Pair<?, ?>[]{
                pair("Location is not set", i18n("crash.NoClassDefFound")),
                pair("UnsatisfiedLinkError", i18n("crash.user_fault")),
                pair("java.time.zone.ZoneRulesException: Unable to load TZDB time-zone rules", i18n("crash.user_fault")),
                pair("java.lang.NoClassDefFoundError", i18n("crash.NoClassDefFound")),
                pair("org.jackhuang.CU.util.ResourceNotFoundError", i18n("crash.NoClassDefFound")),
                pair("java.lang.VerifyError", i18n("crash.NoClassDefFound")),
                pair("java.lang.NoSuchMethodError", i18n("crash.NoClassDefFound")),
                pair("java.lang.NoSuchFieldError", i18n("crash.NoClassDefFound")),
                pair("javax.imageio.IIOException", i18n("crash.NoClassDefFound")),
                pair("netscape.javascript.JSException", i18n("crash.NoClassDefFound")),
                pair("java.lang.IncompatibleClassChangeError", i18n("crash.NoClassDefFound")),
                pair("java.lang.ClassFormatError", i18n("crash.NoClassDefFound")),
                pair("com.sun.javafx.css.StyleManager.findMatchingStyles", i18n("launcher.update_java")),
                pair("NoSuchAlgorithmException", "Has your operating system been installed completely or is a ghost system?")
        };
    }

    private boolean checkThrowable(Throwable e) {
        String s = StringUtils.getStackTrace(e);
        for (Pair<String, String> entry : Hole.SOURCE)
            if (s.contains(entry.getKey())) {
                if (StringUtils.isNotBlank(entry.getValue())) {
                    String info = entry.getValue();
                    LOG.severe(info);
                    try {
                        Alert alert = new Alert(AlertType.INFORMATION, info);
                        alert.setTitle(i18n("message.info"));
                        alert.setHeaderText(i18n("message.info"));
                        alert.showAndWait();
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, "Unable to show message", t);
                    }
                }
                return false;
            }
        return true;
    }

    private final boolean showCrashWindow;

    public CrashReporter(boolean showCrashWindow) {
        this.showCrashWindow = showCrashWindow;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.log(Level.SEVERE, "Uncaught exception in thread " + t.getName(), e);

        try {
            CrashReport report = new CrashReport(t, e);
            if (!report.shouldBeReport())
                return;

            String text = report.getDisplayText();

            LOG.log(Level.SEVERE, text);
            Platform.runLater(() -> {
                if (checkThrowable(e)) {
                    if (showCrashWindow) {
                        new CrashWindow(text).show();
                    }
                    if (!UpdateChecker.isOutdated() && IntegrityChecker.isSelfVerified()) {
                        reportToServer(report);
                    }
                }
            });
        } catch (Throwable handlingException) {
            LOG.log(Level.SEVERE, "Unable to handle uncaught exception", handlingException);
        }
    }

    private void reportToServer(CrashReport crashReport) {
        Thread t = new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("crash_report", crashReport.getDisplayText());
            map.put("version", Metadata.VERSION);
            map.put("log", Logging.getLogs());
            try {
                String response = NetworkUtils.doPost(NetworkUtils.toURL("https://CU.huangyuhui.net/CU/crash.php"), map);
                if (StringUtils.isNotBlank(response))
                    LOG.log(Level.SEVERE, "Crash server response: " + response);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Unable to post CU server.", ex);
            }
        });
        t.setDaemon(true);
        t.start();
    }
}