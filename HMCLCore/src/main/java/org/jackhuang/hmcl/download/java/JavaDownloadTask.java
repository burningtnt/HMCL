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
package org.jackhuang.hmcl.download.java;

import org.jackhuang.hmcl.download.ArtifactMalformedException;
import org.jackhuang.hmcl.download.DownloadProvider;
import org.jackhuang.hmcl.game.DownloadInfo;
import org.jackhuang.hmcl.game.GameJavaVersion;
import org.jackhuang.hmcl.java.*;
import org.jackhuang.hmcl.task.FileDownloadTask;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.gson.JsonUtils;
import org.jackhuang.hmcl.util.io.ChecksumMismatchException;
import org.tukaani.xz.LZMAInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

public final class JavaDownloadTask extends Task<JavaDownloadTask.Result> {

    private final DownloadProvider downloadProvider;
    private final Path target;
    private final Task<RemoteFiles> javaDownloadsTask;
    private final List<Task<?>> dependencies = new ArrayList<>();

    private volatile JavaDownloads.JavaDownload download;

    public JavaDownloadTask(DownloadProvider downloadProvider, Path target, GameJavaVersion javaVersion, String platform) {
        this.target = target;
        this.downloadProvider = downloadProvider;
        this.javaDownloadsTask = new GetTask(downloadProvider.injectURLWithCandidates(
                "https://piston-meta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json"))
        .thenComposeAsync(javaDownloadsJson -> {
            JavaDownloads allDownloads = JsonUtils.fromNonNullJson(javaDownloadsJson, JavaDownloads.class);

            Map<String, List<JavaDownloads.JavaDownload>> osDownloads = allDownloads.getDownloads().get(platform);
            if (osDownloads == null || !osDownloads.containsKey(javaVersion.getComponent()))
                throw new UnsupportedPlatformException("Unsupported platform: " + platform);
            List<JavaDownloads.JavaDownload> candidates = osDownloads.get(javaVersion.getComponent());
            for (JavaDownloads.JavaDownload download : candidates) {
                if (JavaInfo.parseVersion(download.getVersion().getName()) >= javaVersion.getMajorVersion()) {
                    this.download = download;
                    return new GetTask(downloadProvider.injectURLWithCandidates(download.getManifest().getUrl()));
                }
            }
            throw new UnsupportedPlatformException("Candidates: " + JsonUtils.GSON.toJson(candidates));
        })
        .thenApplyAsync(javaDownloadJson -> JsonUtils.fromNonNullJson(javaDownloadJson, RemoteFiles.class));
    }

    @Override
    public Collection<Task<?>> getDependents() {
        return Collections.singleton(javaDownloadsTask);
    }

    @Override
    public void execute() throws Exception {
        for (Map.Entry<String, RemoteFiles.Remote> entry : javaDownloadsTask.getResult().getFiles().entrySet()) {
            Path dest = target.resolve(entry.getKey());
            if (entry.getValue() instanceof RemoteFiles.RemoteFile) {
                RemoteFiles.RemoteFile file = ((RemoteFiles.RemoteFile) entry.getValue());

                // Use local file if it already exists
                try {
                    BasicFileAttributes localFileAttributes = Files.readAttributes(dest, BasicFileAttributes.class);
                    if (localFileAttributes.isRegularFile() && file.getDownloads().containsKey("raw")) {
                        DownloadInfo downloadInfo = file.getDownloads().get("raw");
                        if (localFileAttributes.size() == downloadInfo.getSize()) {
                            ChecksumMismatchException.verifyChecksum(dest, "SHA-1", downloadInfo.getSha1());
                            LOG.info("Skip existing file: " + dest);
                            continue;
                        }
                    }
                } catch (IOException ignored) {
                }

                if (file.getDownloads().containsKey("lzma")) {
                    DownloadInfo download = file.getDownloads().get("lzma");
                    File tempFile = target.resolve(entry.getKey() + ".lzma").toFile();
                    FileDownloadTask task = new FileDownloadTask(downloadProvider.injectURLWithCandidates(download.getUrl()), tempFile, new FileDownloadTask.IntegrityCheck("SHA-1", download.getSha1()));
                    task.setName(entry.getKey());
                    dependencies.add(task.thenRunAsync(() -> {
                        Path decompressed = target.resolve(entry.getKey() + ".tmp");
                        try (LZMAInputStream input = new LZMAInputStream(new FileInputStream(tempFile))) {
                            Files.copy(input, decompressed, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new ArtifactMalformedException("File " + entry.getKey() + " is malformed", e);
                        }
                        tempFile.delete();

                        Files.move(decompressed, dest, StandardCopyOption.REPLACE_EXISTING);
                        if (file.isExecutable()) {
                            dest.toFile().setExecutable(true);
                        }
                    }));
                } else if (file.getDownloads().containsKey("raw")) {
                    DownloadInfo download = file.getDownloads().get("raw");
                    FileDownloadTask task = new FileDownloadTask(downloadProvider.injectURLWithCandidates(download.getUrl()), dest.toFile(), new FileDownloadTask.IntegrityCheck("SHA-1", download.getSha1()));
                    task.setName(entry.getKey());
                    if (file.isExecutable()) {
                        dependencies.add(task.thenRunAsync(() -> dest.toFile().setExecutable(true)));
                    } else {
                        dependencies.add(task);
                    }
                } else {
                    continue;
                }
            } else if (entry.getValue() instanceof RemoteFiles.RemoteDirectory) {
                Files.createDirectories(dest);
            } else if (entry.getValue() instanceof RemoteFiles.RemoteLink) {
                RemoteFiles.RemoteLink link = ((RemoteFiles.RemoteLink) entry.getValue());
                Files.deleteIfExists(dest);
                Files.createSymbolicLink(dest, Paths.get(link.getTarget()));
            }
        }
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean doPostExecute() {
        return true;
    }

    @Override
    public void postExecute() throws Exception {
        setResult(new Result(download, javaDownloadsTask.getResult()));
    }

    public static final class Result {
        public final JavaDownloads.JavaDownload download;
        public final RemoteFiles remoteFiles;

        public Result(JavaDownloads.JavaDownload download, RemoteFiles remoteFiles) {
            this.download = download;
            this.remoteFiles = remoteFiles;
        }
    }

    public static final class UnsupportedPlatformException extends Exception {
        public UnsupportedPlatformException(String message) {
            super(message);
        }
    }
}
