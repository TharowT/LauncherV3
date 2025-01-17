package net.tharow.tantalum.launchercore.modpacks.sources;

import net.tharow.tantalum.launchercore.modpacks.MemoryModpackContainer;
import net.tharow.tantalum.launchercore.modpacks.ModpackModel;
import net.tharow.tantalum.rest.io.PackInfo;

import java.util.Collection;
import java.util.LinkedList;

public class NameFilterPackSource implements IPackSource {
    private MemoryModpackContainer baseModpacks;
    private String filterTerms;

    public NameFilterPackSource(MemoryModpackContainer modpacks, String filter) {
        this.baseModpacks = modpacks;
        this.filterTerms = filter.toUpperCase();
    }

    @Override
    public String getSourceName() {
        return "Installed packs filtered by '" + filterTerms + "'";
    }

    @Override
    public Collection<PackInfo> getPublicPacks() {
        LinkedList<PackInfo> info = new LinkedList<PackInfo>();

        for (ModpackModel modpack : baseModpacks.getModpacks()) {
            if (modpack.getDisplayName().toUpperCase().contains(filterTerms)) {
                info.add(modpack.getPackInfo());
            }
        }

        return info;
    }

    @Override
    public int getPriority(PackInfo info) {
        for (ModpackModel modpack : baseModpacks.getModpacks()) {
            if (modpack.getName().equals(info.getName()))
                return modpack.getPriority();
        }

        return 0;
    }
}
