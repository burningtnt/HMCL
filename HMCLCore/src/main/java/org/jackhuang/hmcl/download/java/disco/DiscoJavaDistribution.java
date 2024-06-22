/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2024 huangyuhui <huanghongxun2008@126.com> and contributors
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
package org.jackhuang.hmcl.download.java.disco;

public enum DiscoJavaDistribution {
    ADOPTIUM("Adoptium", true, false),
    LIBERICA("Liberica", true, true),
    GRAALVM("Oracle GraalVM", false, false);

    private final String displayString;
    private final boolean jreSupported;
    private final boolean bundleJavaFXSupported;

    DiscoJavaDistribution(String displayString, boolean jreSupported, boolean bundleJavaFXSupported) {
        this.displayString = displayString;
        this.jreSupported = jreSupported;
        this.bundleJavaFXSupported = bundleJavaFXSupported;
    }

    public String getDisplayString() {
        return displayString;
    }


    public boolean isJreSupported() {
        return jreSupported;
    }

    public boolean isBundleJavaFXSupported() {
        return bundleJavaFXSupported;
    }
}
