package net.tharow.tantalum.minecraftcore.mojang.java;

import net.tharow.tantalum.minecraftcore.mojang.version.io.Download;

public class JavaRuntime {
    private Download manifest;
    private JavaRuntimeInfo version;

    public Download getManifest() {
        return manifest;
    }

    public JavaRuntimeInfo getVersion() {
        return version;
    }
}
