/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2021  huangyuhui <huanghongxun2008@126.com> and contributors
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
package org.jackhuang.hmcl;

import org.jackhuang.hmcl.util.StringUtils;
import org.jackhuang.hmcl.util.io.JarUtils;
import org.jackhuang.hmcl.util.platform.OperatingSystem;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stores metadata about this application.
 */
public final class Metadata {
    private Metadata() {}

    public static final String NAME = net.burningtnt.hmclprs.PRCollection.onInitApplicationName("HMCL");
    public static final String FULL_NAME = net.burningtnt.hmclprs.PRCollection.onInitApplicationFullName("Hello Minecraft! Launcher");
    public static final String VERSION = net.burningtnt.hmclprs.PRCollection.onInitApplicationVersion(System.getProperty("hmcl.version.override", JarUtils.getManifestAttribute("Implementation-Version", "@develop@")));

    public static final String TITLE = NAME + " " + VERSION;
    public static final String FULL_TITLE = FULL_NAME + " v" + VERSION;

    public static final String HMCL_UPDATE_URL = System.getProperty("hmcl.update_source.override", net.burningtnt.hmclprs.PRCollection.onInitApplicationDefaultUpdateLink("https://hmcl.huangyuhui.net/api/update_link"));
    public static final String CONTACT_URL = "https://docs.hmcl.net/help.html";
    public static final String HELP_URL = "https://docs.hmcl.net";
    public static final String CHANGELOG_URL = "https://docs.hmcl.net/changelog/";
    public static final String PUBLISH_URL = net.burningtnt.hmclprs.PRCollection.onInitApplicationPublishURL("https://hmcl.huangyuhui.net");
    public static final String EULA_URL = "https://docs.hmcl.net/eula/hmcl.html";

    public static final String BUILD_CHANNEL = JarUtils.getManifestAttribute("Build-Channel", "nightly");
    public static final String GITHUB_SHA = JarUtils.getManifestAttribute("GitHub-SHA", null);

    public static final Path MINECRAFT_DIRECTORY = OperatingSystem.getWorkingDirectory("minecraft");
    public static final Path HMCL_DIRECTORY;

    static {
        String hmclHome = System.getProperty("hmcl.home");
        if (hmclHome == null) {
            if (OperatingSystem.CURRENT_OS.isLinuxOrBSD()) {
                String xdgData = System.getenv("XDG_DATA_HOME");
                if (StringUtils.isNotBlank(xdgData)) {
                    HMCL_DIRECTORY = Paths.get(xdgData, "hmcl").toAbsolutePath();
                } else {
                    HMCL_DIRECTORY = Paths.get(System.getProperty("user.home", "."), ".local", "share", "hmcl").toAbsolutePath();
                }
            } else {
                HMCL_DIRECTORY = OperatingSystem.getWorkingDirectory("hmcl");
            }
        } else {
            HMCL_DIRECTORY = Paths.get(hmclHome).toAbsolutePath().normalize();
        }
    }

    public static boolean isStable() {
        return "stable".equals(BUILD_CHANNEL);
    }

    public static boolean isDev() {
        return "dev".equals(BUILD_CHANNEL);
    }

    public static boolean isNightly() {
        return !isStable() && !isDev();
    }
}
