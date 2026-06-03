package me.tuicode.thoitietbonmua;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.time.Duration;
import java.util.Random;
import java.util.Set;

public class ThoiTietBonMua extends JavaPlugin implements Listener, CommandExecutor {

    private static final int NGAY_MOI_MUA = 90;
    private static final int TONG_NGAY_CHU_KY = 360;
    private static final long TICKS_MOI_NGAY = 24000L;
    private static final int TICKS_MUA_AXIT = 100;

    private final Random random = new Random();
    private String overrideMua = null;
    private String muaTruoc = null;
    private int tickerMuaAxit = 0;
    private int tickerBoneMeal = 0;
    private int tickerSet = 0;

    @Override
    public void onEnable() {
        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("  ThoiTietBonMua v2.1 đã khởi động!");
        getLogger().info("╚══════════════════════════════════════╝");

        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("season") != null) {
            getCommand("season").setExecutor(this);
        }

        chayHeThongCore();
    }

    @Override
    public void onDisable() {
        getLogger().info("ThoiTietBonMua v2.1 đã tắt.");
    }

    private void chayHeThongCore() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickerMuaAxit++;
                tickerBoneMeal++;
                tickerSet++;

                if (getServer().getWorlds().isEmpty()) return;
                World world = getServer().getWorlds().get(0);
                String muaHienTai = getMuaHienTai(world);

                kiemTraChuyenMua(world, muaHienTai);
                apDungThoiTietMua(world, muaHienTai);

                if (muaHienTai.equals("xuan") && tickerBoneMeal >= 60) {
                    tickerBoneMeal = 0;
                    boneMealMuaXuan(world);
                }

                if (muaHienTai.equals("ha") && tickerSet >= 200) {
                    tickerSet = 0;
                    setMuaHa(world);
                }

                for (Player player : getServer().getOnlinePlayers()) {
                    if (!player.getWorld().equals(world)) {
                        world = player.getWorld();
                        muaHienTai = getMuaHienTai(world);
                    }

                    apDungHieuUngNguoiChoi(player, world, muaHienTai);
                    kiemTraGiaiNhietNuoc(player, world, muaHienTai);

                    if (muaHienTai.equals("thu") && tickerMuaAxit >= TICKS_MUA_AXIT) {
                        apDungMuaAxit(player, world);
                    }

                    apDungMiningFatigueDong(player, muaHienTai);
                    apDungDoiMuaHa(player, world, muaHienTai);
                    capNhatScoreboard(player, world, muaHienTai);
                }

                if (tickerMuaAxit >= TICKS_MUA_AXIT) tickerMuaAxit = 0;
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private String getMuaHienTai(World world) {
        if (overrideMua != null) return overrideMua;
        long ngayTrongNam = getNgayTrongNam(world);
        if (ngayTrongNam < NGAY_MOI_MUA) return "xuan";
        if (ngayTrongNam < NGAY_MOI_MUA * 2) return "ha";
        if (ngayTrongNam < NGAY_MOI_MUA * 3) return "thu";
        return "dong";
    }

    private long getNgayTrongNam(World world) {
        return (world.getFullTime() / TICKS_MOI_NGAY) % TONG_NGAY_CHU_KY;
    }

    private long getNgayTrongMua(World world) {
        if (overrideMua != null) return 1;
        return (getNgayTrongNam(world) % NGAY_MOI_MUA) + 1;
    }

    private long getNgayConLai(World world) {
        if (overrideMua != null) return 0;
        return NGAY_MOI_MUA - getNgayTrongMua(world);
    }

    private long getTongSoNgayMap(World world) {
        return world.getFullTime() / TICKS_MOI_NGAY;
    }

    private void kiemTraChuyenMua(World world, String muaHienTai) {
        if (muaTruoc == null) {
            muaTruoc = muaHienTai;
            return;
        }
        if (muaTruoc.equals(muaHienTai)) return;

        muaTruoc = muaHienTai;

        String tenMuaMoi;
        NamedTextColor mauTitle;
        Sound amThanh;

        switch (muaHienTai) {
            case "xuan" -> {
                tenMuaMoi = "🌸  MÙA XUÂN  🌸";
                mauTitle = NamedTextColor.GREEN;
                amThanh = Sound.ENTITY_PLAYER_LEVELUP;
            }
            case "ha" -> {
                tenMuaMoi = "☀  MÙA HẠ  ☀";
                mauTitle = NamedTextColor.GOLD;
                amThanh = Sound.AMBIENT_BASALT_DELTAS_MOOD;
            }
            case "thu" -> {
                tenMuaMoi = "🍂  MÙA THU  🍂";
                mauTitle = NamedTextColor.RED;
                amThanh = Sound.AMBIENT_CAVE;
            }
            default -> {
                tenMuaMoi = "❄  MÙA ĐÔNG  ❄";
                mauTitle = NamedTextColor.AQUA;
                amThanh = Sound.AMBIENT_UNDERWATER_LOOP;
            }
        }

        Component titleComp = Component.text(tenMuaMoi)
                .color(mauTitle).decorate(TextDecoration.BOLD);
        Component subtitleComp = Component.text("Vạn vật đã thay đổi theo dòng chảy thời gian...")
                .color(NamedTextColor.WHITE);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3000),
                Duration.ofMillis(1000)
        );
        Title title = Title.title(titleComp, subtitleComp, times);

        for (Player p : getServer().getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), amThanh, 1.0f, 1.0f);
        }

        getServer().broadcast(Component.text("═══════════════════════════════════════").color(NamedTextColor.DARK_GRAY));
        getServer().broadcast(Component.text("  🌍  Thời gian chuyển dịch! Server bước vào " + tenMuaMoi).color(mauTitle).decorate(TextDecoration.BOLD));
        getServer().broadcast(Component.text("═══════════════════════════════════════").color(NamedTextColor.DARK_GRAY));
    }

    private void apDungThoiTietMua(World world, String mua) {
        switch (mua) {
            case "xuan", "ha" -> {
                if (world.hasStorm()) world.setStorm(false);
                if (world.isThundering()) world.setThundering(false);
            }
            case "thu" -> {
                if (!world.hasStorm() && random.nextInt(60) == 0) {
                    world.setStorm(true);
                    world.setWeatherDuration(2400);
                }
                if (world.isThundering()) world.setThundering(false);
            }
            case "dong" -> {
                if (!world.hasStorm() && random.nextInt(30) == 0) {
                    world.setStorm(true);
                    world.setWeatherDuration(6000);
                }
                if (!world.isThundering() && random.nextInt(300) == 0) {
                    world.setThundering(true);
                    world.setThunderDuration(1200);
                }
            }
        }
    }

    private void apDungHieuUngNguoiChoi(Player player, World world, String mua) {
        Location loc = player.getLocation();
        switch (mua) {
            case "xuan" -> hieuUngXuan(player, loc);
            case "ha" -> hieuUngHa(player, loc, world);
            case "thu" -> hieuUngThu(player, loc);
            case "dong" -> hieuUngDong(player, loc, world);
        }
    }

    private void hieuUngXuan(Player player, Location loc) {
        for (int i = 0; i < 4; i++) {
            double ox = (random.nextDouble() - 0.5) * 2.5;
            double oy = random.nextDouble() * 2.2;
            double oz = (random.nextDouble() - 0.5) * 2.5;
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(ox, oy, oz), 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (int i = 0; i < 2; i++) {
            double ox = (random.nextDouble() - 0.5) * 3.0;
            double oz = (random.nextDouble() - 0.5) * 3.0;
            loc.getWorld().spawnParticle(Particle.COMPOSTER, loc.clone().add(ox, 0.1, oz), 1, 0.0, 0.3, 0.0, 0.0);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60, 4, true, false));
        player.sendActionBar(Component.text("🌸 Mùa Xuân | Phúc lành tràn đầy — Vạn vật sinh sôi!").color(NamedTextColor.GREEN));
    }

    private void hieuUngHa(Player player, Location loc, World world) {
        if (isDungDuoiTroiNang(player, world)) {
            for (int i = 0; i < 2; i++) {
                double ox = (random.nextDouble() - 0.5) * 1.5;
                double oz = (random.nextDouble() - 0.5) * 1.5;
                loc.getWorld().spawnParticle(Particle.WHITE_SMOKE, loc.clone().add(ox, 2.1, oz), 1);
            }
            for (int i = 0; i < 2; i++) {
                double ox = (random.nextDouble() - 0.5) * 1.8;
                double oy = random.nextDouble() * 1.5;
                double oz = (random.nextDouble() - 0.5) * 1.8;
                loc.getWorld().spawnParticle(Particle.SMALL_FLAME, loc.clone().add(ox, oy, oz), 1, 0, 0, 0, 0);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 80, 0, true, false));
            player.sendActionBar(Component.text("☀ Mùa Hạ | Nóng bức! Nhảy xuống nước hoặc uống nước giải nhiệt!").color(NamedTextColor.GOLD));
        } else {
            player.sendActionBar(Component.text("☀ Mùa Hạ | Mát mẻ dưới bóng râm.").color(NamedTextColor.YELLOW));
        }
    }

    private void hieuUngThu(Player player, Location loc) {
        Color mauLa = switch (random.nextInt(3)) {
            case 0 -> Color.fromRGB(210, 90, 10);
            case 1 -> Color.fromRGB(180, 30, 10);
            default -> Color.fromRGB(220, 160, 20);
        };
        Particle.DustOptions dust = new Particle.DustOptions(mauLa, 1.3f);
        for (int i = 0; i < 5; i++) {
            double ox = (random.nextDouble() - 0.5) * 4.0;
            double oy = random.nextDouble() * 3.5;
            double oz = (random.nextDouble() - 0.5) * 4.0;
            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(ox, oy, oz), 1, dust);
        }

        if (loc.getWorld().hasStorm()) {
            for (int i = 0; i < 3; i++) {
                double ox = (random.nextDouble() - 0.5) * 3.0;
                double oz = (random.nextDouble() - 0.5) * 3.0;
                loc.getWorld().spawnParticle(Particle.FALLING_WATER, loc.clone().add(ox, 2.5, oz), 1);
            }
            player.sendActionBar(Component.text("🍂 Mùa Thu | Mưa axit! Vào nhà ngay!").color(NamedTextColor.RED));
        } else {
            player.sendActionBar(Component.text("🍂 Mùa Thu | Lá vàng rơi — Mùa bội thu!").color(NamedTextColor.GOLD));
        }
    }

    private void hieuUngDong(Player player, Location loc, World world) {
        for (int i = 0; i < 5; i++) {
            double ox = (random.nextDouble() - 0.5) * 4.0;
            double oy = random.nextDouble() * 4.0;
            double oz = (random.nextDouble() - 0.5) * 4.0;
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc.clone().add(ox, oy, oz), 1, 0, -0.1, 0, 0);
        }

        long timeOfDay = world.getTime();
        if (timeOfDay >= 13000 || timeOfDay < 1000) {
            for (int i = 0; i < 6; i++) {
                double ox = (random.nextDouble() - 0.5) * 5.0;
                double oy = (random.nextDouble() - 0.5) * 1.5 + 1.0;
                double oz = (random.nextDouble() - 0.5) * 5.0;
                loc.getWorld().spawnParticle(Particle.CLOUD, loc.clone().add(ox, oy, oz), 1, 0.05, 0.02, 0.05, 0.01);
            }
        }

        boolean macDoDa = checkMacDoDa(player);
        boolean ganNguonNhiet = checkGanNguonNhiet(player);

        if (macDoDa || ganNguonNhiet) {
            player.setFreezeTicks(0);
            String lyDo = macDoDa ? "Giáp da giữ ấm" : "Gần nguồn nhiệt";
            player.sendActionBar(Component.text("❄ Mùa Đông | " + lyDo + " — An toàn!").color(NamedTextColor.AQUA));
        } else {
            int freeze = Math.min(player.getFreezeTicks() + 5, 140);
            player.setFreezeTicks(freeze);
            if (timeOfDay >= 13000 || timeOfDay < 1000) {
                player.sendActionBar(Component.text("❄ Mùa Đông | Sương mù lạnh giá bao phủ! Mặc giáp da hoặc đứng gần lửa!").color(NamedTextColor.RED));
            } else {
                player.sendActionBar(Component.text("❄ Mùa Đông | Lạnh buốt xương! Tìm nguồn nhiệt ngay!").color(NamedTextColor.RED));
            }
        }
    }

    private void apDungMiningFatigueDong(Player player, String mua) {
        if (!mua.equals("dong")) {
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            return;
        }
        if (player.getFreezeTicks() > 70) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 0, true, false));
        } else {
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        }
    }

    private void apDungMuaAxit(Player player, World world) {
        if (!world.hasStorm() || player.isInWater()) return;
        Location loc = player.getLocation();
        int highestY = world.getHighestBlockYAt(loc);
        if (loc.getBlockY() < highestY) return;

        if (player.getHealth() > 1.0) {
            player.damage(1.0);
            player.sendActionBar(Component.text("🌧 Mưa Axit! Vào mái che ngay!").color(NamedTextColor.DARK_RED));
        }
    }

    private void apDungDoiMuaHa(Player player, World world, String mua) {
        if (!mua.equals("ha") || !isDungDuoiTroiNang(player, world)) return;
        player.setExhaustion(Math.min(player.getExhaustion() + 0.1f, 4.0f));
    }

    private void setMuaHa(World world) {
        if (random.nextInt(20) != 0) return;

        for (Player player : getServer().getOnlinePlayers()) {
            if (!player.getWorld().equals(world) || !isDungDuoiTroiNang(player, world)) continue;

            double offsetX = (random.nextDouble() - 0.5) * 16.0;
            double offsetZ = (random.nextDouble() - 0.5) * 16.0;
            Location setLoc = player.getLocation().clone().add(offsetX, 0, offsetZ);
            setLoc.setY(world.getHighestBlockYAt(setLoc));

            world.strikeLightningEffect(setLoc);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);

            if (setLoc.distance(player.getLocation()) <= 5.0 && player.getHealth() > 4.0) {
                player.damage(4.0);
                player.sendMessage(Component.text("[Mùa Hạ] ⚡ Sét suýt đánh trúng bạn! Mất 2 tim!").color(NamedTextColor.YELLOW));
            }
            break;
        }
    }

    private void boneMealMuaXuan(World world) {
        for (Player player : getServer().getOnlinePlayers()) {
            if (!player.getWorld().equals(world)) continue;
            Location loc = player.getLocation();
            int attempts = 0;
            while (attempts < 5) {
                attempts++;
                int bx = loc.getBlockX() + random.nextInt(33) - 16;
                int bz = loc.getBlockZ() + random.nextInt(33) - 16;
                int by = world.getHighestBlockYAt(bx, bz) - 1;

                Block block = world.getBlockAt(bx, by, bz);
                if (block.getType() == Material.GRASS_BLOCK) {
                    block.applyBoneMeal(org.bukkit.block.BlockFace.UP);
                    world.spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 1.2, 0.5), 3, 0.3, 0.3, 0.3, 0);
                    break;
                }
            }
        }
    }

    private boolean isDungDuoiTroiNang(Player player, World world) {
        if (world.hasStorm() || world.isThundering()) return false;
        long time = world.getTime();
        if (time >= 13000 && time <= 23000) return false;
        if (player.isInWater()) return false;
        Location loc = player.getLocation();
        return loc.getBlockY() >= loc.getWorld().getHighestBlockYAt(loc);
    }

    private boolean checkMacDoDa(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] armorPieces = {inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()};
        Set<Material> leatherMats = Set.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
        for (ItemStack armor : armorPieces) {
            if (armor != null && leatherMats.contains(armor.getType())) return true;
        }
        return false;
    }

    private boolean checkGanNguonNhiet(Player player) {
        Location loc = player.getLocation();
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    if (isNguonNhiet(loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isNguonNhiet(Material mat) {
        return switch (mat) {
            case CAMPFIRE, SOUL_CAMPFIRE, FIRE, SOUL_FIRE, TORCH, WALL_TORCH, SOUL_TORCH, SOUL_WALL_TORCH, LANTERN, SOUL_LANTERN, BLAST_FURNACE, FURNACE, SMOKER, MAGMA_BLOCK, LAVA -> true;
            default -> false;
        };
    }

    private void kiemTraGiaiNhietNuoc(Player player, World world, String mua) {
        if (mua.equals("ha") && player.isInWater() && player.hasPotionEffect(PotionEffectType.HUNGER)) {
            player.removePotionEffect(PotionEffectType.HUNGER);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, true, true));
            player.sendActionBar(Component.text("💧 Mát lạnh! Giải nhiệt bằng nước! +Tốc Độ II (10s)").color(NamedTextColor.AQUA));
        }
    }

    @EventHandler
    public void onDrinkWater(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!getMuaHienTai(player.getWorld()).equals("ha")) return;

        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION) return;
        if (!(item.getItemMeta() instanceof PotionMeta meta) || meta.getBasePotionType() != PotionType.WATER) return;

        player.removePotionEffect(PotionEffectType.HUNGER);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, true, true));
        player.sendMessage(Component.text("[Mùa Hạ] ").color(NamedTextColor.GOLD)
                .append(Component.text("Mát lạnh! Giải nhiệt thành công! +Tốc Độ II (10s)").color(NamedTextColor.YELLOW)));
    }

    @EventHandler
    public void onQuaiTanCongNguoiChoi(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        LivingEntity attacker = getAttacker(event.getDamager());
        if (attacker == null || !isQuaiCanXuLy(attacker)) return;

        switch (getMuaHienTai(player.getWorld())) {
            case "ha" -> {
                player.setFireTicks(80);
                player.sendActionBar(Component.text("🔥 Mùa Hạ | Đòn lửa từ quái! Cháy 4 giây!").color(NamedTextColor.RED));
            }
            case "dong" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, true, true));
                player.setFreezeTicks(Math.min(player.getFreezeTicks() + 60, 140));
                player.sendActionBar(Component.text("❄ Mùa Đông | Đòn băng từ quái! Chậm II & Đóng Băng!").color(NamedTextColor.BLUE));
            }
        }
    }

    private LivingEntity getAttacker(Entity damager) {
        if (damager instanceof LivingEntity living) return living;
        if (damager instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof LivingEntity living) return living;
        return null;
    }

    private boolean isQuaiCanXuLy(LivingEntity entity) {
        return entity instanceof Monster || entity instanceof Slime || entity instanceof Phantom;
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        World world = event.getEntity().getWorld();
        Location loc = event.getLocation();
        
        switch (getMuaHienTai(world)) {
            case "xuan" -> {
                if (event.getEntityType() == EntityType.BEE && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                    for (int i = 0; i < 2; i++) {
                        world.spawnEntity(loc.clone().add((random.nextDouble() - 0.5) * 3.0, 0, (random.nextDouble() - 0.5) * 3.0), EntityType.BEE);
                    }
                }
            }
            case "ha" -> {
                long time = world.getTime();
                if (time >= 13000 && time <= 23000 && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                    if (loc.getBlockY() >= world.getHighestBlockYAt(loc) - 1 && random.nextInt(50) == 0) {
                        event.setCancelled(true);
                        world.spawnEntity(loc, EntityType.MAGMA_CUBE);
                    }
                }
            }
            case "thu" -> {
                if (event.getEntity() instanceof Monster && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL && !(event.getEntity() instanceof Witch)) {
                    if (random.nextInt(10) < 3) {
                        event.setCancelled(true);
                        world.spawnEntity(loc, EntityType.WITCH);
                    }
                }
            }
            case "dong" -> {
                if (event.getEntityType() == EntityType.SKELETON && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                    if (loc.getBlockY() >= world.getHighestBlockYAt(loc) - 1) {
                        event.setCancelled(true);
                        world.spawnEntity(loc, EntityType.STRAY);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCayPhatTrien(BlockGrowEvent event) {
        Block block = event.getBlock();
        switch (getMuaHienTai(block.getWorld())) {
            case "xuan" -> {
                if (!random.nextBoolean()) tangTuoiCay(event);
            }
            case "ha" -> {
                Block duoi = block.getRelative(org.bukkit.block.BlockFace.DOWN);
                if (duoi.getType() == Material.FARMLAND && duoi.getBlockData() instanceof org.bukkit.block.data.Farmland farmland) {
                    if (farmland.getMoisture() == 0) {
                        event.setCancelled(true);
                        block.setType(Material.AIR);
                        duoi.setType(Material.DIRT);
                        for (Player p : block.getWorld().getNearbyPlayers(block.getLocation(), 16)) {
                            p.sendActionBar(Component.text("🌵 Mùa Hạ | Cây trồng chết vì hạn hán! Tưới nước đi!").color(NamedTextColor.GOLD));
                        }
                        return;
                    }
                }
                if (random.nextInt(3) == 0) tangTuoiCay(event);
            }
            case "dong" -> event.setCancelled(true);
        }
    }

    private void tangTuoiCay(BlockGrowEvent event) {
        if (event.getNewState().getBlockData() instanceof Ageable ageable) {
            int maxAge = ageable.getMaximumAge();
            if (ageable.getAge() < maxAge) {
                ageable.setAge(Math.min(ageable.getAge() + 1, maxAge));
                event.getNewState().setBlockData(ageable);
            }
        }
    }

    @EventHandler
    public void onCayCoTheThoiMuaDong(BlockGrowEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        if (!getMuaHienTai(world).equals("dong")) return;

        Material mat = block.getType();
        if (mat != Material.WHEAT && mat != Material.CARROTS && mat != Material.POTATOES) return;
        if (block.getY() < world.getHighestBlockYAt(block.getLocation()) - 1) return;

        if (random.nextInt(20) == 0) {
            event.setCancelled(true);
            block.setType(Material.AIR);
            Block farmland = block.getRelative(org.bukkit.block.BlockFace.DOWN);
            if (farmland.getType() == Material.FARMLAND) farmland.setType(Material.DIRT);
        }
    }

    @EventHandler
    public void onAnimalGrow(EntityBreedEvent event) {
        if (getMuaHienTai(event.getEntity().getWorld()).equals("xuan") && random.nextBoolean()) {
            if (event.getEntity() instanceof Ageable parent) {
                Location loc = ((Entity) parent).getLocation();
                loc.getWorld().spawnEntity(loc, ((Entity) parent).getType());
            }
        }
    }

    @EventHandler
    public void onHarvestAutumn(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!getMuaHienTai(block.getWorld()).equals("thu")) return;

        if (!(block.getBlockData() instanceof Ageable ageable) || ageable.getAge() < ageable.getMaximumAge()) return;

        Material dropMat = switch (block.getType()) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            default -> null;
        };
        
        if (dropMat != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMat, 1));
        }
    }

    private void capNhatScoreboard(Player player, World world, String muaHienTai) {
        Scoreboard board = getServer().getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("smp_info", Criteria.DUMMY, Component.text("❖  10A1 SMP  ❖").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        long timeOfDay = world.getTime();
        String iconThoiGian = timeOfDay < 1000 || timeOfDay > 23000 ? "🌅" : timeOfDay < 12000 ? "☀" : timeOfDay < 13000 ? "🌇" : "🌙";
        String tenThoiGian = timeOfDay < 1000 || timeOfDay > 23000 ? "Bình Minh" : timeOfDay < 12000 ? "Ban Ngày" : timeOfDay < 13000 ? "Hoàng Hôn" : "Ban Đêm";

        String tenMuaHienThi = switch (muaHienTai) {
            case "xuan" -> "🌸 Xuân"; case "ha" -> "☀ Hạ"; case "thu" -> "🍂 Thu"; default -> "❄ Đông";
        };
        String maMauMua = switch (muaHienTai) {
            case "xuan" -> "§a"; case "ha" -> "§e"; case "thu" -> "§6"; default -> "§b";
        };

        String trangThaiMua = switch (muaHienTai) {
            case "xuan" -> "§aPháp: Luck V";
            case "ha" -> isDungDuoiTroiNang(player, world) ? "§6☀ Nóng bức!" : "§eMát mẻ";
            case "thu" -> world.hasStorm() ? "§4☠ Mưa Axit!" : "§6Thu hoạch x2";
            default -> player.getFreezeTicks() > 70 ? "§b❄ Đông Cứng: " + player.getFreezeTicks() + "/140" : "§7Nhiệt độ ổn";
        };

        setDong(obj, "§7──────────────────", 15);
        setDong(obj, "§f👤 " + player.getName(), 14);
        setDong(obj, "§7──────────────────", 13);
        setDong(obj, "§aOnline: §f" + getServer().getOnlinePlayers().size() + " người", 12);
        setDong(obj, "§eNgày Map: §f" + getTongSoNgayMap(world), 11);
        setDong(obj, "§7──────────────────", 10);
        setDong(obj, maMauMua + "Mùa: " + tenMuaHienThi + (overrideMua != null ? " §c[Ép]" : ""), 9);
        setDong(obj, overrideMua == null ? "§7Còn lại: §f" + getNgayConLai(world) + " ngày" : "§cChế độ ép mùa", 8);
        setDong(obj, trangThaiMua, 7);
        setDong(obj, "§7──────────────────", 6);
        setDong(obj, "§7Thời Gian:", 5);
        setDong(obj, iconThoiGian + " §f" + tenThoiGian, 4);
        setDong(obj, "§7──────────────────", 3);

        player.setScoreboard(board);
    }

    private void setDong(Objective obj, String text, int score) {
        obj.getScore(text + "§r" + " ".repeat(score % 16)).setScore(score);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("season")) return false;
        if (args.length == 0) { hienThiHelpSeason(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "info" -> xuLyInfo(sender);
            case "set" -> xuLySet(sender, args);
            case "reset" -> xuLyReset(sender);
            default -> hienThiHelpSeason(sender);
        }
        return true;
    }

    private void xuLyInfo(CommandSender sender) {
        World world = getServer().getWorlds().get(0);
        String mua = getMuaHienTai(world);
        String tenMua = switch (mua) { case "xuan" -> "Mùa Xuân 🌸"; case "ha" -> "Mùa Hạ ☀"; case "thu" -> "Mùa Thu 🍂"; default -> "Mùa Đông ❄"; };
        String maMau = switch (mua) { case "xuan" -> "§a"; case "ha" -> "§e"; case "thu" -> "§6"; default -> "§b"; };

        sender.sendMessage("§6=====[ §eTHỜI TIẾT 4 MÙA v2.1 §6]=====");
        sender.sendMessage("§7Mùa Hiện Tại : " + maMau + tenMua + (overrideMua != null ? " §c(Đang bị ép bởi Admin)" : ""));
        sender.sendMessage("§7Tổng Ngày Map : §f" + getTongSoNgayMap(world) + " ngày");
        if (overrideMua == null) sender.sendMessage("§7Còn lại       : §f" + getNgayConLai(world) + " ngày");
        else sender.sendMessage("§cChế độ ép mùa đang hoạt động. Dùng /season reset để tắt.");
        sender.sendMessage("§7Thời tiết     : " + (world.hasStorm() ? "§9Mưa/Tuyết" : "§eTrời trong"));
        sender.sendMessage("§6======================================");
    }

    private void xuLySet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("thoitiet.admin")) { sender.sendMessage("§cBạn không có quyền Admin!"); return; }
        if (args.length < 2) { sender.sendMessage("§cThiếu tham số! Chọn: §exuan, ha, thu, dong"); return; }

        String nhapMua = args[1].toLowerCase();
        if (!nhapMua.matches("xuan|ha|thu|dong")) { sender.sendMessage("§cMùa sai! Chọn: §exuan / ha / thu / dong"); return; }

        overrideMua = nhapMua;
        muaTruoc = nhapMua;
        String tenMuaHienThi = switch (nhapMua) { case "xuan" -> "Mùa Xuân 🌸"; case "ha" -> "Mùa Hạ ☀"; case "thu" -> "Mùa Thu 🍂"; default -> "Mùa Đông ❄"; };
        
        getServer().broadcast(Component.text("[Admin] Server đã ép sang " + tenMuaHienThi + "! (Chế độ ép mùa)").color(NamedTextColor.YELLOW));
        sender.sendMessage("§a[Thành công] Đã ép server sang " + tenMuaHienThi + ".");
    }

    private void xuLyReset(CommandSender sender) {
        if (!sender.hasPermission("thoitiet.admin")) { sender.sendMessage("§cBạn không có quyền Admin!"); return; }
        if (overrideMua == null) { sender.sendMessage("§eServer đang chạy tự động, không cần reset."); return; }

        overrideMua = null;
        muaTruoc = getMuaHienTai(getServer().getWorlds().get(0));
        getServer().broadcast(Component.text("[Admin] Hệ thống mùa quay về tự động theo ngày game!").color(NamedTextColor.GREEN));
        sender.sendMessage("§a[Thành công] Đã gỡ ép mùa. Server tự tính theo ngày.");
    }

    private void hienThiHelpSeason(CommandSender sender) {
        sender.sendMessage("§6=====[ HƯỚNG DẪN /SEASON v2.1 ]=====");
        sender.sendMessage("§e/season info §7- Xem thông tin mùa hiện tại.");
        sender.sendMessage("§e/season set <xuan|ha|thu|dong> §7- Ép mùa. §c(Admin)");
        sender.sendMessage("§e/season reset §7- Gỡ ép mùa. §c(Admin)");
        sender.sendMessage("§6======================================");
    }
}
