package me.tuicode.thoitietbonmua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public final class ThoiTietBonMua extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin Thoi Tiet 4 Mua + Scoreboard Fix 1.21 dang hoat dong!");
        
        // Đăng ký trực tiếp Listener ngay trong nội bộ để tránh lỗi ép package trên GitHub
        getServer().getPluginManager().registerEvents(new TrongTrotListener(), this);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                chayHeThongCore();
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void chayHeThongCore() {
        int soNguoiOnline = Bukkit.getOnlinePlayers().size();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            
            long totalTicks = world.getFullTime();
            long tongSoNgay = totalTicks / 24000; 
            long ngayTrongNam = tongSoNgay % 360; 
            long ngayTrongMua = (ngayTrongNam % 90) + 1;

            String muaHienTai = "";
            NamedTextColor mauMua = NamedTextColor.WHITE;

            if (ngayTrongNam < 90) {
                muaHienTai = "Xuân";
                mauMua = NamedTextColor.LIGHT_PURPLE;
                if (ngayTrongMua == 1 && !world.hasStorm()) {
                    world.setStorm(true);
                    world.setWeatherDuration(12000); 
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 0, false, false, false));
                
            } else if (ngayTrongNam < 180) {
                muaHienTai = "Hạ";
                mauMua = NamedTextColor.GOLD;
                if (world.hasStorm()) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
                if (isDungDuoiTroiNang(player)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0, false, false, false));
                }
                
            } else if (ngayTrongNam < 270) {
                muaHienTai = "Thu";
                mauMua = NamedTextColor.YELLOW;
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false));
                
            } else {
                muaHienTai = "Đông";
                mauMua = NamedTextColor.AQUA;
                if (!world.hasStorm()) {
                    world.setStorm(true);
                    world.setWeatherDuration(24000);
                }
                // Sửa lỗi hàm setInPowderSnow bằng cách tăng độ đóng băng tự nhiên
                if (world.hasStorm() && isDungDuoiTroiNang(player)) {
                    player.setFreezeTicks(Math.min(player.getFreezeTicks() + 20, 140));
                }
            }

            long timeOfDay = world.getTime();
            String iconThoiGian = (timeOfDay >= 0 && timeOfDay < 12000) ? "☀️ Ngày" : "🌙 Đêm";

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            
            // Sửa triệt để lỗi getScore() bằng cách ép mượt về String màu truyền thống để không bị kẹt build
            String titleText = "§a§l   10A1 SMP   ";
            Objective obj = board.registerNewObjective("smp_info", Criteria.DUMMY, Component.text(titleText));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            obj.getScore("§fTên: §e" + player.getName()).setScore(5);
            obj.getScore("§fOnline: §a" + soNguoiOnline + "/20").setScore(4);
            obj.getScore("§fNgày: §6" + tongSoNgay).setScore(3);
            
            // Đổi màu chữ Mùa theo hệ thống mã màu cổ điển để tương thích 100%
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
}
