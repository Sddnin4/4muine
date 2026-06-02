package me.tuicode.thoitietbonmua;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public final class ThoiTietBonMua extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin Thoi Tiet 4 Mua + Scoreboard da gop code hoan hao!");
        
        // Đăng ký bộ lắng nghe sự kiện trồng trọt nội bộ
        getServer().getPluginManager().registerEvents(new TrongTrotListenerNoiBo(), this);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                chayHeThongCore();
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void chayHeThongCore() {
        int soNguoiOnline = Bukkit.getOnlinePlayers().size();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            
            long totalTicks = world.getFullTime();
            long tongSoNgay = totalTicks / 24000; 
            long ngayTrongNam = tongSoNgay % 360; 
            long ngayTrongMua = (ngayTrongNam % 90) + 1;

            String muaHienTai = "";

            if (ngayTrongNam < 90) {
                muaHienTai = "Xuân";
                if (ngayTrongMua == 1 && !world.hasStorm()) {
                    world.setStorm(true);
                    world.setWeatherDuration(12000); 
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 0, false, false, false));
                
            } else if (ngayTrongNam < 180) {
                muaHienTai = "Hạ";
                if (world.hasStorm()) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
                if (isDungDuoiTroiNang(player)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0, false, false, false));
                }
                
            } else if (ngayTrongNam < 270) {
                muaHienTai = "Thu";
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false));
                
            } else {
                muaHienTai = "Đông";
                if (!world.hasStorm()) {
                    world.setStorm(true);
                    world.setWeatherDuration(24000);
                }
                if (world.hasStorm() && isDungDuoiTroiNang(player)) {
                    player.setFreezeTicks(Math.min(player.getFreezeTicks() + 20, 140));
                }
            }

            long timeOfDay = world.getTime();
            String iconThoiGian = (timeOfDay >= 0 && timeOfDay < 12000) ? "☀️ Ngày" : "🌙 Đêm";

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            
            String titleText = "§a§l   10A1 SMP   ";
            Objective obj = board.registerNewObjective("smp_info", Criteria.DUMMY, Component.text(titleText));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            obj.getScore("§fTên: §e" + player.getName()).setScore(5);
            obj.getScore("§fOnline: §a" + soNguoiOnline + "/20").setScore(4);
            obj.getScore("§fNgày: §6" + tongSoNgay).setScore(3);
            
            String maMau = "§f";
            if (muaHienTai.equals("Xuân")) maMau = "§d";
            else if (muaHienTai.equals("Hạ")) maMau = "§6";
            else if (muaHienTai.equals("Thu")) maMau = "§e";
            else if (muaHienTai.equals("Đông")) maMau = "§b";
            
            obj.getScore("§fMùa: " + maMau + muaHienTai).setScore(2);
            obj.getScore("§fThời gian: §b" + iconThoiGian).setScore(1);

            player.setScoreboard(board);
        }
    }

    private boolean isDungDuoiTroiNang(Player player) {
        Location loc = player.getLocation();
        int highestBlockY = player.getWorld().getHighestBlockYAt(loc);
        return loc.getBlockY() >= highestBlockY;
    }

    // LỚP LẮNG NGHE ĐƯỢC GỘP NỘI BỘ VÀO ĐÂY ĐỂ TRANH LỖI TÌM FILE TRÊN GITHUB
    private static class TrongTrotListenerNoiBo implements Listener {
        @EventHandler
        public void onCayPhatTrien(BlockGrowEvent event) {
            World world = event.getBlock().getWorld();
            long tongSoNgay = world.getFullTime() / 24000;
            long ngayTrongNam = tongSoNgay % 360;

            if (ngayTrongNam < 90) {
                if (event.getNewState().getBlockData() instanceof Ageable ageable) {
                    int maxAge = ageable.getMaximumAge();
                    int currentAge = ageable.getAge();
                    if (currentAge < maxAge) {
                        ageable.setAge(Math.min(maxAge, currentAge + 1));
                        event.getNewState().setBlockData(ageable);
                    }
                }
            }
        }
    }
}
