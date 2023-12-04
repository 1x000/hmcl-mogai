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
package org.jackhuang.CU.event;

import org.jackhuang.CU.util.ToStringBuilder;

/**
 * This event gets fired when a minecraft version is being removed.
 * <br>
 * This event is fired on the {@link org.jackhuang.CU.event.EventBus#EVENT_BUS}
 *
 * @author huangyuhui
 */
public class RemoveVersionEvent extends Event {

    private final String version;

    /**
     *
     * @param source {@link org.jackhuang.CU.game.GameRepository}
     * @param version the version id.
     */
    public RemoveVersionEvent(Object source, String version) {
        super(source);
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", source)
                .append("version", version)
                .toString();
    }
}