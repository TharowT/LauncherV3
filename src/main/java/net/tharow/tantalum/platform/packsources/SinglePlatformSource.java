/*
 * This file is part of Technic Launcher Core.
 * Copyright ©2015 Syndicate, LLC
 *
 * Technic Launcher Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * as well as a copy of the GNU Lesser General Public License,
 * along with Technic Launcher Core.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tharow.tantalum.platform.packsources;

import net.tharow.tantalum.launchercore.modpacks.sources.IPackSource;
import net.tharow.tantalum.platform.IPlatformApi;
import net.tharow.tantalum.platform.PlatformPackInfoRepository;
import net.tharow.tantalum.rest.io.PackInfo;
import net.tharow.tantalum.solder.ISolderApi;

import java.util.ArrayList;
import java.util.Collection;

public class SinglePlatformSource extends PlatformPackInfoRepository implements IPackSource {
    private String slug;

    public SinglePlatformSource(IPlatformApi platformApi, ISolderApi solderApi, String slug) {
        super(platformApi, solderApi);
        this.slug = slug;
    }

    @Override
    public String getSourceName() {
        return "Platform pack with slug '" + this.slug + "'";
    }

    @Override
    public Collection<PackInfo> getPublicPacks() {
        ArrayList<PackInfo> packs = new ArrayList<PackInfo>(1);
        PackInfo info = getPlatformPackInfo(slug);

        if (info != null) {
            packs.add(info);
        }

        return packs;
    }

    @Override
    public int getPriority(PackInfo packInfo) {
        return 0;
    }
}
