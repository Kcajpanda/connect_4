package dev.jackb.connectfour;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

record Vec3i(int x, int y, int z) {
    static Vec3i from(ConfigurationSection section, String path) {
        ConfigurationSection value = section.getConfigurationSection(path);
        if (value == null) {
            throw new IllegalArgumentException("Missing location section: " + section.getCurrentPath() + "." + path);
        }
        return from(value);
    }

    static Vec3i from(ConfigurationSection section) {
        return new Vec3i(section.getInt("x"), section.getInt("y"), section.getInt("z"));
    }

    Vec3i plus(Vec3i other) {
        return new Vec3i(x + other.x, y + other.y, z + other.z);
    }

    Vec3i times(int factor) {
        return new Vec3i(x * factor, y * factor, z * factor);
    }

    Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    boolean inside(Vec3i min, Vec3i max) {
        return x >= min.x && x <= max.x
            && y >= min.y && y <= max.y
            && z >= min.z && z <= max.z;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}
