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

package net.tharow.tantalum.platform.http;

import net.tharow.tantalum.platform.IPlatformApi;
import net.tharow.tantalum.platform.io.NewsData;
import net.tharow.tantalum.platform.io.PlatformPackInfo;
import net.tharow.tantalum.rest.RestObject;
import net.tharow.tantalum.rest.RestfulAPIException;
import net.tharow.tantalum.utilslib.Utils;

public class HttpPlatformApi implements IPlatformApi {
    private String platformUrl;
    private String launcherBuild;

    public HttpPlatformApi(String rootUrl, String launcherBuild) {
        this.platformUrl = rootUrl;
        this.launcherBuild = launcherBuild;
    }

    public String getPlatformUri(String packSlug) {
        return platformUrl + "modpack.php?slug=" + packSlug;// + "?build="+launcherBuild;
    }

    @Override
    public PlatformPackInfo getPlatformPackInfoForBulk(String packSlug) throws RestfulAPIException {
        return getPlatformPackInfo(packSlug);
    }

    @Override
    public PlatformPackInfo getPlatformPackInfo(String packSlug) throws RestfulAPIException {
        String url = getPlatformUri(packSlug);
        return RestObject.getRestObject(PlatformPackInfo.class, url);
    }

    @Override
    public void incrementPackRuns(String packSlug) {
        String url = platformUrl + "modpack.php?slug=" + packSlug + "&stat=run";//?build="+launcherBuild;
        Utils.pingHttpURL(url);
    }

    @Override
    public void incrementPackInstalls(String packSlug) {
        String url = platformUrl + "modpack.php?slug=" + packSlug + "&stat=install";
        Utils.pingHttpURL(url);
    }

    @Override
    public NewsData getNews() throws RestfulAPIException {
        String url = platformUrl + "news.php?build="+launcherBuild;
        return RestObject.getRestObject(NewsData.class, url);
    }
}
