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
package org.jackhuang.CU.mod.server;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.jackhuang.CU.download.DefaultDependencyManager;
import org.jackhuang.CU.download.GameBuilder;
import org.jackhuang.CU.game.DefaultGameRepository;
import org.jackhuang.CU.mod.MinecraftInstanceTask;
import org.jackhuang.CU.mod.Modpack;
import org.jackhuang.CU.mod.ModpackConfiguration;
import org.jackhuang.CU.mod.ModpackInstallTask;
import org.jackhuang.CU.task.Task;
import org.jackhuang.CU.util.gson.JsonUtils;
import org.jackhuang.CU.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerModpackLocalInstallTask extends Task<Void> {

    private final File zipFile;
    private final Modpack modpack;
    private final ServerModpackManifest manifest;
    private final String name;
    private final DefaultGameRepository repository;
    private final List<Task<?>> dependencies = new ArrayList<>();
    private final List<Task<?>> dependents = new ArrayList<>(4);

    public ServerModpackLocalInstallTask(DefaultDependencyManager dependencyManager, File zipFile, Modpack modpack, ServerModpackManifest manifest, String name) {
        this.zipFile = zipFile;
        this.modpack = modpack;
        this.manifest = manifest;
        this.name = name;
        this.repository = dependencyManager.getGameRepository();
        File run = repository.getRunDirectory(name);

        File json = repository.getModpackConfiguration(name);
        if (repository.hasVersion(name) && !json.exists())
            throw new IllegalArgumentException("Version " + name + " already exists.");

        GameBuilder builder = dependencyManager.gameBuilder().name(name);
        for (ServerModpackManifest.Addon addon : manifest.getAddons()) {
            builder.version(addon.getId(), addon.getVersion());
        }

        dependents.add(builder.buildAsync());
        onDone().register(event -> {
            if (event.isFailed())
                repository.removeVersionFromDisk(name);
        });

        ModpackConfiguration<ServerModpackManifest> config = null;
        try {
            if (json.exists()) {
                config = JsonUtils.GSON.fromJson(FileUtils.readText(json), new TypeToken<ModpackConfiguration<ServerModpackManifest>>() {
                }.getType());

                if (!ServerModpackProvider.INSTANCE.getName().equals(config.getType()))
                    throw new IllegalArgumentException("Version " + name + " is not a Server modpack. Cannot update this version.");
            }
        } catch (JsonParseException | IOException ignore) {
        }
        dependents.add(new ModpackInstallTask<>(zipFile, run, modpack.getEncoding(), Collections.singletonList("/overrides"), any -> true, config).withStage("CU.modpack"));
        dependents.add(new MinecraftInstanceTask<>(zipFile, modpack.getEncoding(), Collections.singletonList("/overrides"), manifest, ServerModpackProvider.INSTANCE, modpack.getName(), modpack.getVersion(), repository.getModpackConfiguration(name)).withStage("CU.modpack"));
    }

    @Override
    public List<Task<?>> getDependents() {
        return dependents;
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() throws Exception {
    }
}