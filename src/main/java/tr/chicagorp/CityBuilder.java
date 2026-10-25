package tr.chicagorp;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;

/** Moda gerek yok: tum yapilar komutla, duz Bukkit API ile uretilir. Oyuncunun durdugu yer = zemin ustu (y). */
public class CityBuilder {
    private final ChicagoRP p;
    public CityBuilder(ChicagoRP p) { this.p = p; }

    public void command(Player pl, String[] a) {
        if (!pl.hasPermission("rp.admin")) return;
        if (a.length == 0) { U.msg(pl, "/sehir dunya | spawn | karakol | ev | dukkan <ad> | mahalle <n> | hapis"); return; }
        Location c = pl.getLocation().getBlock().getLocation();
        switch (a[0]) {
            case "dunya" -> {
                WorldCreator wc = new WorldCreator("sehir").type(WorldType.FLAT).generateStructures(false)
                        .generatorSettings("{\"layers\":[{\"block\":\"minecraft:bedrock\",\"height\":1},{\"block\":\"minecraft:stone\",\"height\":3},{\"block\":\"minecraft:grass_block\",\"height\":1}],\"biome\":\"minecraft:plains\"}");
                World w = Bukkit.createWorld(wc);
                if (w != null) { w.setGameRule(GameRule.DO_MOB_SPAWNING, false); w.setTime(6000); pl.teleport(new Location(w, 0.5, -59, 0.5)); U.msg(pl, "Dunya hazir. Simdi /sehir spawn"); }
            }
            case "spawn" -> { spawn(c); U.msg(pl, "Spawn olustu."); }
            case "karakol" -> { karakol(c); U.msg(pl, "Karakol olustu, hapis noktasi ayarlandi."); }
            case "ev" -> { ev(c); U.msg(pl, "Ev olustu."); }
            case "dukkan" -> { dukkan(c, a.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(a, 1, a.length)) : "DUKKAN"); U.msg(pl, "Dukkan olustu."); }
            case "mahalle" -> { int n = Math.min(6, a.length > 1 ? Integer.parseInt(a[1]) : 3); mahalle(c, n); U.msg(pl, n * n + " ev ve arsa olusturuldu."); }
            case "hapis" -> { setLoc("jail", pl.getLocation()); U.msg(pl, "Hapis noktasi ayarlandi."); }
        }
    }

    private void setLoc(String key, Location l) {
        p.getConfig().set(key + ".world", l.getWorld().getName());
        p.getConfig().set(key + ".x", l.getX()); p.getConfig().set(key + ".y", l.getY()); p.getConfig().set(key + ".z", l.getZ());
        p.saveConfig();
    }

    private void fill(World w, int x1, int y1, int z1, int x2, int y2, int z2, Material m) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) w.getBlockAt(x, y, z).setType(m, false);
    }

    private void shell(World w, int x, int y, int z, int sx, int sy, int sz, Material wall, Material floor, Material roof) {
        fill(w, x, y, z, x + sx - 1, y + sy - 1, z + sz - 1, wall);
        fill(w, x + 1, y, z + 1, x + sx - 2, y + sy - 2, z + sz - 2, Material.AIR);
        fill(w, x, y - 1, z, x + sx - 1, y - 1, z + sz - 1, floor);
        fill(w, x, y + sy - 1, z, x + sx - 1, y + sy - 1, z + sz - 1, roof);
    }

    private void door(Block b, Material m, BlockFace f) {
        Block t = b.getRelative(0, 1, 0);
        b.setType(m, false); t.setType(m, false);
        Door d = (Door) b.getBlockData(); d.setFacing(f); d.setHalf(Bisected.Half.BOTTOM); b.setBlockData(d, false);
        Door u = (Door) t.getBlockData(); u.setFacing(f); u.setHalf(Bisected.Half.TOP); t.setBlockData(u, false);
    }

    private void sign(Block b, String... lines) {
        b.setType(Material.OAK_SIGN, false);
        Sign s = (Sign) b.getState();
        for (int i = 0; i < lines.length && i < 4; i++) s.getSide(Side.FRONT).line(i, Component.text(lines[i]));
        s.update();
    }

    public void spawn(Location c) {
        World w = c.getWorld(); int x = c.getBlockX(), y = c.getBlockY(), z = c.getBlockZ();
        fill(w, x - 20, y, z - 20, x + 20, y + 10, z + 20, Material.AIR);
        fill(w, x - 20, y - 1, z - 20, x + 20, y - 1, z + 20, Material.QUARTZ_BLOCK);
        for (int i = -20; i <= 20; i++) { // cerceve
            w.getBlockAt(x + i, y - 1, z - 20).setType(Material.STONE_BRICKS, false); w.getBlockAt(x + i, y - 1, z + 20).setType(Material.STONE_BRICKS, false);
            w.getBlockAt(x - 20, y - 1, z + i).setType(Material.STONE_BRICKS, false); w.getBlockAt(x + 20, y - 1, z + i).setType(Material.STONE_BRICKS, false);
        }
        fill(w, x - 3, y - 1, z - 3, x + 3, y - 1, z + 3, Material.POLISHED_ANDESITE); // cesme
        fill(w, x - 2, y, z - 2, x + 2, y, z + 2, Material.QUARTZ_SLAB);
        fill(w, x - 1, y, z - 1, x + 1, y, z + 1, Material.WATER);
        for (int dx : new int[]{-18, 18}) for (int dz : new int[]{-18, 18}) {
            fill(w, x + dx, y, z + dz, x + dx, y + 3, z + dz, Material.OAK_FENCE);
            w.getBlockAt(x + dx, y + 4, z + dz).setType(Material.SEA_LANTERN, false);
        }
        sign(w.getBlockAt(x, y, z + 6), "HOSGELDIN", "Spawn Bolgesi", "PvP: /pvp kapat");
        setLoc("spawn", new Location(w, x + .5, y, z + 8.5));
        p.getConfig().set("spawn.radius", 40); p.saveConfig();
        w.setSpawnLocation(x, y, z + 8);
    }

    public void karakol(Location c) {
        World w = c.getWorld(); int x = c.getBlockX(), y = c.getBlockY(), z = c.getBlockZ();
        shell(w, x, y, z, 17, 6, 11, Material.BLUE_CONCRETE, Material.POLISHED_DEEPSLATE, Material.STONE_BRICKS);
        door(w.getBlockAt(x + 8, y, z), Material.IRON_DOOR, BlockFace.NORTH);
        for (int i : new int[]{3, 5, 11, 13}) { w.getBlockAt(x + i, y + 1, z).setType(Material.GLASS_PANE, false); w.getBlockAt(x + i, y + 1, z + 10).setType(Material.GLASS_PANE, false); }
        sign(w.getBlockAt(x + 9, y + 3, z - 1), "KARAKOL", "POLIS");
        // hucreler (arka duvar boyunca, demir parmaklik)
        for (int cell = 0; cell < 3; cell++) {
            int cx = x + 2 + cell * 5;
            fill(w, cx, y, z + 6, cx + 3, y + 2, z + 9, Material.IRON_BARS);
            fill(w, cx + 1, y, z + 7, cx + 2, y + 2, z + 8, Material.AIR);
        }
        w.getBlockAt(x + 3, y + 1, z + 5).setType(Material.IRON_BARS, false);
        w.getBlockAt(x + 3, y, z + 5).setType(Material.IRON_BARS, false); // hucre kapi bosluk
        w.getBlockAt(x + 4, y, z + 4).setType(Material.CRAFTING_TABLE, false); // masa
        w.getBlockAt(x + 12, y, z + 3).setType(Material.CHEST, false);          // kit sandigi
        setLoc("jail", new Location(w, x + 3.5, y, z + 7.5));
    }

    public void ev(Location c) {
        World w = c.getWorld(); int x = c.getBlockX(), y = c.getBlockY(), z = c.getBlockZ();
        shell(w, x, y, z, 9, 5, 9, Material.WHITE_CONCRETE, Material.OAK_PLANKS, Material.DARK_OAK_PLANKS);
        door(w.getBlockAt(x + 4, y, z), Material.OAK_DOOR, BlockFace.NORTH);
        for (int i : new int[]{2, 6}) { w.getBlockAt(x + i, y + 1, z).setType(Material.GLASS_PANE, false); w.getBlockAt(x + i, y + 1, z + 8).setType(Material.GLASS_PANE, false); w.getBlockAt(x, y + 1, z + i).setType(Material.GLASS_PANE, false); w.getBlockAt(x + 8, y + 1, z + i).setType(Material.GLASS_PANE, false); }
        w.getBlockAt(x + 1, y, z + 1).setType(Material.CRAFTING_TABLE, false);
        w.getBlockAt(x + 7, y, z + 1).setType(Material.CHEST, false);
        w.getBlockAt(x + 4, y + 3, z + 4).setType(Material.LANTERN, false);
    }

    public void dukkan(Location c, String name) {
        World w = c.getWorld(); int x = c.getBlockX(), y = c.getBlockY(), z = c.getBlockZ();
        shell(w, x, y, z, 11, 5, 9, Material.TERRACOTTA, Material.SMOOTH_STONE, Material.STONE_BRICKS);
        fill(w, x + 1, y + 1, z, x + 9, y + 2, z, Material.GLASS); // vitrin
        door(w.getBlockAt(x + 5, y, z), Material.OAK_DOOR, BlockFace.NORTH);
        fill(w, x + 2, y, z + 5, x + 8, y, z + 5, Material.QUARTZ_SLAB); // tezgah
        sign(w.getBlockAt(x + 5, y + 3, z - 1), name.length() > 15 ? name.substring(0, 15) : name);
        w.getBlockAt(x + 5, y + 3, z + 4).setType(Material.LANTERN, false);
    }

    public void mahalle(Location c, int n) {
        World w = c.getWorld(); int x0 = c.getBlockX(), y = c.getBlockY(), z0 = c.getBlockZ(), pitch = 17;
        int size = n * pitch + 5;
        fill(w, x0, y, z0, x0 + size, y + 8, z0 + size, Material.AIR);
        fill(w, x0, y - 1, z0, x0 + size, y - 1, z0 + size, Material.BLACK_CONCRETE);
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            int lx = x0 + 5 + i * pitch, lz = z0 + 5 + j * pitch;
            fill(w, lx, y - 1, lz, lx + 11, y - 1, lz + 11, Material.GRASS_BLOCK);
            fill(w, lx - 1, y - 1, lz - 1, lx + 12, y - 1, lz - 1, Material.LIGHT_GRAY_CONCRETE); // kaldirim
            ev(new Location(w, lx + 1, y, lz + 1));
            p.plots.create("ev_" + i + "_" + j, w.getName(), lx - 1, lz - 1, lx + 12, lz + 12, 25000);
        }
        p.plots.save();
    }
}
