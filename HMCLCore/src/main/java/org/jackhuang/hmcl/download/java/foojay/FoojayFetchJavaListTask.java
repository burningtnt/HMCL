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
package org.jackhuang.hmcl.download.java.foojay;

import org.jackhuang.hmcl.download.DownloadProvider;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.io.NetworkUtils;
import org.jackhuang.hmcl.util.platform.Architecture;
import org.jackhuang.hmcl.util.platform.OperatingSystem;
import org.jackhuang.hmcl.util.platform.Platform;

import java.util.*;

/**
 * @author Glavo
 */
public final class FoojayFetchJavaListTask extends Task<TreeMap<Integer, FoojayRemoteVersion>> {

    private static String getOperatingSystemName(OperatingSystem os) {
        return os == OperatingSystem.OSX ? "macos" : os.getCheckedName();
    }

    private static String getArchitectureName(Architecture arch) {
        return arch.getCheckedName();
    }

    private final Task<?> dependent;

    public FoojayFetchJavaListTask(DownloadProvider downloadProvider, FoojayJavaDistribution distribution, Platform platform, boolean isJRE) {
        HashMap<String, String> params = new HashMap<>();
        params.put("distribution", distribution.name().toLowerCase(Locale.ROOT));
        params.put("package_type", isJRE ? "jre" : "jdk");
        params.put("javafx_bundle", "false");
        params.put("operating_system", getOperatingSystemName(platform.getOperatingSystem()));
        params.put("architecture", getArchitectureName(platform.getArchitecture()));
        params.put("archive_type", platform.getOperatingSystem() == OperatingSystem.WINDOWS ? "zip" : "tar.gz");
        params.put("directly_downloadable", "true");

        this.dependent = new GetTask(downloadProvider.injectURLWithCandidates(NetworkUtils.withQuery("https://api.foojay.io/disco/v3.0/packages", params)));
    }

    @Override
    public Collection<Task<?>> getDependents() {
        return Collections.singleton(dependent);
    }

    @Override
    public void execute() throws Exception {
        // TODO
    }
}
