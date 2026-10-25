package tr.chicagorp;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.UUID;

public class Commands implements CommandExecutor {
    private final ChicagoRP p;
    public Commands(ChicagoRP p) { this.p = p; }

    @Override public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player pl)) { U.msg(s, "Bu komut oyuncular icin."); return true; }
        switch (c.getName().toLowerCase()) {
            case "meslek" -> p.jobs.command(pl, a);
            case "pvp" -> p.pvp.toggle(pl, a);
            case "profil" -> profil(pl, a);
            case "kimlik" -> kimlik(pl, a);
            case "ehliyet" -> ehliyet(pl);
            case "arsa" -> p.plots.command(pl, a);
            case "jenerator" -> p.plots.generator(pl, a);
            case "kilit" -> p.plots.lock(pl);
            case "tutukla" -> p.police.arrest(pl, a);
            case "polis" -> p.police.kit(pl);
            case "siparis" -> p.orders.command(pl, a);
            case "tarla", "kimya", "elalti" -> p.chain.command(c.getName().toLowerCase(), pl, a);
            case "sehir" -> p.city.command(pl, a);
            case "para" -> U.msg(pl, "Bakiye: &a" + (long) p.data.money(pl.getUniqueId()) + " TL");
        }
        return true;
    }

    private void profil(Player pl, String[] a) {
        Player t = a.length > 0 ? Bukkit.getPlayer(a[0]) : pl;
        if (t == null) { U.msg(pl, "Oyuncu bulunamadi."); return; }
        UUID u = t.getUniqueId();
        U.msg(pl, "&e" + t.getName() + " &7| Bakiye: &a" + (long) p.data.money(u) + " &7| Seviye: &b" + p.data.level(u)
                + " &7| Meslek: &f" + p.data.job(u) + " &7| Arsa: &f" + p.plots.owned(u) + " &7| Jenerator: &f" + p.data.gens(u));
    }

    private void kimlik(Player pl, String[] a) {
        Player t = pl;
        if (a.length > 0) {
            if (!p.police.isPolice(pl)) { U.msg(pl, "Baskasinin kimligini sadece polis gorebilir."); return; }
            t = Bukkit.getPlayer(a[0]);
            if (t == null) { U.msg(pl, "Oyuncu bulunamadi."); return; }
        }
        UUID u = t.getUniqueId();
        U.msg(pl, "&eKIMLIK &7TC: &f" + p.data.get(u, "tc", "?") + " &7Dogum: &f" + p.data.get(u, "dogum", "?")
                + " &7Kan: &f" + p.data.get(u, "kan", "?") + " &7Sabika: &f" + p.data.get(u, "sabika", 0));
    }

    private void ehliyet(Player pl) {
        UUID u = pl.getUniqueId();
        if ((boolean) p.data.get(u, "ehliyet", false)) { U.msg(pl, "Zaten ehliyetin var."); return; }
        double price = p.getConfig().getDouble("ehliyet-price");
        if (!p.data.take(u, price)) { U.msg(pl, "Yetersiz bakiye (" + (long) price + " TL)."); return; }
        p.data.set(u, "ehliyet", true);
        U.msg(pl, "Ehliyet alindi. Arac eklentin bu degeri kontrol etmeli: players.yml -> ehliyet");
    }
}
