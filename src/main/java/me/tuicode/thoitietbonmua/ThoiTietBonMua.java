package me.tuicode.thoitietbonmua;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
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

import java.util.Random;

public final class ThoiTietBonMua extends JavaPlugin implements Listener, CommandExecutor {

    private final Random random = new Random();
    private String overrideMua = null; 

    @Override
    public void onEnable() {
        getLogger().info("He thong 4 Mua 2.0 - CAP NHAT QUAI VAT DOT BIEN DON DANH!");
        getServer().getPluginManager().registerEvents(this, this);
        
        if (getCommand("season") != null) {
            getCommand("season").setExecutor(this);
        }
        
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
            
            long ngayTrongNam = getNgayTrongNam(world);
            long ngayTrongMua = (ngayTrongNam % 90) + 1;

            String muaHienTai = "";
            Location loc = player.getLocation();

            if (ngayTrongNam >= 90 && ngayTrongNam < 180 && player.isInWater()) {
                player.removePotionEffect(PotionEffectType.HUNGER);
            }

            boolean macDoDa = checkMacDoDa(player);
            boolean ganNguonNhiet = checkGanNguonNhiet(player);

            // ==========================================
            // XỬ LÝ ACTION BAR VÀ SHADER ẢO THEO MÙA
            // ==========================================
            if (ngayTrongNam < 90) {
                muaHienTai = "Xuân";
                if (ngayTrongMua == 1 && !world.hasStorm()) world.setStorm(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 0, false, false, false));
                player.sendActionBar(Component.text("§a🌸 Mùa xuân ấm áp: Vạn vật sinh sôi, tỉ lệ xuất hiện động vật tăng cao!"));
                
                world.spawnParticle(Particle.CHERRY_LEAF, loc.add(random.nextDouble()*14 - 7, 5, random.nextDouble()*14 - 7), 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(random.nextDouble()*6 - 3, 1, random.nextDouble()*6 - 3), 1, 0, 0, 0, 0);

            } else if (ngayTrongNam < 180) {
                muaHienTai = "Hạ";
                if (world.hasStorm()) world.setStorm(false);
                
                if (isDungDuoiTroiNang(player)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0, false, false, false));
                    player.sendActionBar(Component.text("§6☀️ Hạ nắng gắt: Quái đánh gây CHÁY, hãy cẩn thận khi ra ngoài!"));
                    world.spawnParticle(Particle.WHITE_SMOKE, player.getLocation().add(random.nextDouble()*4 - 2, 0.1, random.nextDouble()*4 - 2), 1, 0, 0.1, 0, 0.01);
                } else {
                    player.sendActionBar(Component.text("§e☀️ Mùa hạ nóng nực: Đang trốn trong bóng râm mát mẻ."));
                }

            } else if (ngayTrongNam < 270) {
                muaHienTai = "Thu";
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false));
                player.sendActionBar(Component.text("§e🍂 Mùa thu hoài niệm: Mùa gặt hái bội thu x2 quả, lá vàng rơi đầy đường..."));
                
                Particle.DustOptions laPhong = new Particle.DustOptions(Color.fromRGB(211, 84, 0), 1.2f);
                world.spawnParticle(Particle.DUST, loc.add(random.nextDouble()*14 - 7, 6, random.nextDouble()*14 - 7), 2, laPhong);

            } else {
                muaHienTai = "Đông";
                if (!world.hasStorm()) world.setStorm(true);
                
                if (isDungDuoiTroiNang(player)) {
                    if (macDoDa) {
                        player.setFreezeTicks(0);
                        player.sendActionBar(Component.text("§b🧥 Bộ giáp da dày cộp đã giữ ấm hoàn hảo cho bạn trước bão tuyết!"));
                    } else if (ganNguonNhiet) {
                        player.setFreezeTicks(0);
                        player.sendActionBar(Component.text("§e🔥 Bạn đang sưởi ấm gần nguồn lửa, thanh đóng băng đã tan hết!"));
                    } else {
                        player.setFreezeTicks(Math.min(player.getFreezeTicks() + 20, 140));
                        player.sendActionBar(Component.text("§b❄️ Đông buốt giá: Quái đánh gây LÀM CHẬM & ĐÓNG BĂNG màn hình!"));
                    }
                } else {
                    player.setFreezeTicks(0);
                    player.sendActionBar(Component.text("§3❄️ Mùa đông lạnh buốt: Đã an toàn bên trong nhà sưởi ấm."));
                }
                world.spawnParticle(Particle.SNOWFLAKE, loc.add(random.nextDouble()*10 - 5, 4, random.nextDouble()*10 - 5), 2, 0, 0, 0, 0);
            }

            // ==========================================
            // CẬP NHẬT GIAO DIỆN BẢNG SCOREBOARD
            // ==========================================
            long timeOfDay = world.getTime();
            String iconThoiGian = (timeOfDay >= 0 && timeOfDay < 12000) ? "☀️ Ngày" : "🌙 Đêm";

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            Objective obj = board.registerNewObjective("smp_info", Criteria.DUMMY, Component.text("§a§l   10A1 SMP   "));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            obj.getScore("§fTên: §e" + player.getName()).setScore(5);
            obj.getScore("§fOnline: §a" + soNguoiOnline + "/20").setScore(4);
            obj.getScore("§fNgày: §6" + tongSoNgay).setScore(3);
            
            String maMau = "§f";
            if (muaHienTai.equals("Xuân")) maMau = "§d";
            else if (muaHienTai.equals("Hạ")) maMau = "§6";
            else if (muaHienTai.equals("Thu")) maMau = "§e";
            else if (muaHienTai.equals("Đông")) maMau = "§b";
            
            String tagEp = (overrideMua != null) ? " §c[F]" : "";
            obj.getScore("§fMùa: " + maMau + muaHienTai + tagEp).setScore(2);
            obj.getScore("§fThời gian: §b" + iconThoiGian).setScore(1);

            player.setScoreboard(board);
        }
    }

    private long getNgayTrongNam(World world) {
        if (overrideMua != null) {
            if (overrideMua.equals("xuan")) return 0;
            if (overrideMua.equals("ha")) return 90;
            if (overrideMua.equals("thu")) return 180;
            if (overrideMua.equals("dong")) return 270;
        }
        return (world.getFullTime() / 24000) % 360;
    }

    private boolean isDungDuoiTroiNang(Player player) {
        Location loc = player.getLocation();
        return loc.getBlockY() >= player.getWorld().getHighestBlockYAt(loc);
    }

    private boolean checkMacDoDa(Player player) {
        PlayerInventory inv = player.getInventory();
        return (inv.getHelmet() != null && inv.getHelmet().getType() == Material.LEATHER_HELMET) ||
               (inv.getChestplate() != null && inv.getChestplate().getType() == Material.LEATHER_CHESTPLATE) ||
               (inv.getLeggings() != null && inv.getLeggings().getType() == Material.LEATHER_LEGGINGS) ||
               (inv.getBoots() != null && inv.getBoots().getType() == Material.LEATHER_BOOTS);
    }

    private boolean checkGanNguonNhiet(Player player) {
        Location pLoc = player.getLocation();
        int r = 4; 
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Material block = pLoc.clone().add(x, y, z).getBlock().getType();
                    if (block == Material.CAMPFIRE || block == Material.SOUL_CAMPFIRE ||
                        block == Material.FIRE || block == Material.SOUL_FIRE ||
                        block == Material.TORCH || block == Material.WALL_TORCH) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onDrinkWater(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POTION) {
            if (event.getItem().getItemMeta() instanceof PotionMeta meta) {
                if (meta.getBasePotionType() == PotionType.WATER) {
                    Player p = event.getPlayer();
                    long ngayTrongNam = getNgayTrongNam(p.getWorld());
                    if (ngayTrongNam >= 90 && ngayTrongNam < 180) { 
                        p.removePotionEffect(PotionEffectType.HUNGER);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); 
                        p.sendMessage("§a🥤 Đã uống chai nước mát! Xóa đói cực nhanh và tăng tốc chạy trốn nắng gắt!");
                    }
                }
            }
        }
    }

    // ==========================================
    // CƠ CHẾ MỚI: QUÁI VẬT TẤN CÔNG GÂY HIỆU ỨNG THEO MÙA
    // ==========================================
    @EventHandler
    public void onQuaiTanCongNguoiChoi(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        LivingEntity attacker = null;
        Entity damager = event.getDamager();

        // Kiểm tra xem là quái đánh trực tiếp hay bắn tên/projectile trúng
        if (damager instanceof LivingEntity) {
            attacker = (LivingEntity) damager;
        } else if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof LivingEntity) {
                attacker = (LivingEntity) projectile.getShooter();
            }
        }

        // Lọc điều kiện: Phải là quái vật tấn công (bỏ qua nếu là người chơi khác hoặc mob thân thiện)
        if (attacker == null || attacker instanceof Player) return;
        if (attacker instanceof org.bukkit.entity.Monster || attacker instanceof org.bukkit.entity.Slime || attacker instanceof org.bukkit.entity.Phantom) {
            
            long ngayTrongNam = getNgayTrongNam(player.getWorld());

            // ☀️ ĐÒN ĐÁNH MÙA HẠ: Gây cháy người chơi 4 giây
            if (ngayTrongNam >= 90 && ngayTrongNam < 180) {
                player.setFireTicks(80); // 80 ticks = 4 giây cháy
                player.sendMessage("§c🔥 Bạn bị quái vật thiêu cháy do ảnh hưởng của khí hậu mùa hạ oi bức!");
            }
            // ❄️ ĐÒN ĐÁNH MÙA ĐÔNG: Gây Chậm Rãi II (5 giây) + Tích thêm đóng băng màn hình
            else if (ngayTrongNam >= 270) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)); // 100 ticks = 5 giây Slowness II
                player.setFreezeTicks(Math.min(player.getFreezeTicks() + 60, 140)); // Ép tăng độ lạnh màn hình
                player.sendMessage("§b❄️ Đòn đánh băng giá của quái vật khiến bạn bị tê cứng và di chuyển chậm chạp!");
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        World world = event.getLocation().getWorld();
        long ngayTrongNam = getNgayTrongNam(world);

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            if (ngayTrongNam < 90) { 
                if (random.nextInt(100) < 40) {
                    world.spawnEntity(event.getLocation().add(random.nextDouble()*2, 0, random.nextDouble()*2), event.getEntityType());
                }
            } 
            else if (ngayTrongNam >= 270) { 
                if (random.nextInt(100) < 55) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getEntity() instanceof Zombie zombie) {
                    zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 1));
                    zombie.setCustomName("§bZombie Băng Giá (Trâu Hơn)");
                    zombie.setCustomNameVisible(true);
                }
            }
        }
    }

    @EventHandler
    public void onCayPhatTrien(BlockGrowEvent event) {
        long ngayTrongNam = getNgayTrongNam(event.getBlock().getWorld());
        if (ngayTrongNam < 90) {
            if (event.getNewState().getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
                int maxAge = ageable.getMaximumAge();
                int currentAge = ageable.getAge();
                if (currentAge < maxAge) {
                    ageable.setAge(Math.min(maxAge, currentAge + 1));
                    event.getNewState().setBlockData(ageable);
                }
            }
        } else if (ngayTrongNam >= 90 && ngayTrongNam < 180) {
            Block blockDuoi = event.getBlock().getLocation().subtract(0, 1, 0).getBlock();
            if (blockDuoi.getType() == Material.FARMLAND && random.nextInt(100) < 35) {
                blockDuoi.setType(Material.DIRT);
            }
        } else if (ngayTrongNam >= 270) {
            if (random.nextInt(100) < 60) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnimalGrow(org.bukkit.event.entity.EntityBreedEvent event) {
        long ngayTrongNam = getNgayTrongNam(event.getEntity().getWorld());
        if (ngayTrongNam >= 270) {
            if (event.getEntity() instanceof org.bukkit.entity.Ageable baby) baby.setAge(-48000);
        }
    }

    @EventHandler
    public void onHarvestAutumn(BlockBreakEvent event) {
        long ngayTrongNam = getNgayTrongNam(event.getBlock().getWorld());
        if (ngayTrongNam >= 180 && ngayTrongNam < 270) {
            Block b = event.getBlock();
            if (b.getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    ItemStack extraDrop = new ItemStack(b.getType() == Material.WHEAT ? Material.WHEAT : b.getType().createBlockData().getPlacementMaterial(), 1);
                    b.getWorld().dropItemNaturally(b.getLocation(), extraDrop);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("season")) {
            if (args.length == 0) {
                sender.sendMessage("§e=====[ HỆ THỐNG 4 MÙA 2.0 ]=====");
                sender.sendMessage("§b/season info §7- Xem thông tin mùa hiện tại và số ngày còn lại.");
                sender.sendMessage("§b/season set <xuan|ha|thu|dong> §7- Ép server sang mùa mong muốn.");
                sender.sendMessage("§b/season reset §7- Gỡ bỏ ép mùa, quay lại tự động theo ngày.");
                return true;
            }

            if (args[0].equalsIgnoreCase("info")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cLệnh này chỉ dùng được trong game!");
                    return true;
                }
                long totalTicks = player.getWorld().getFullTime();
                long tongSoNgay = totalTicks / 24000;
                long ngayTrongNam = getNgayTrongNam(player.getWorld());
                long ngayTrongMua = (ngayTrongNam % 90) + 1;
                long ngayConLai = 90 - ngayTrongMua;

                String tenMuaFormat = "";
                if (ngayTrongNam < 90) tenMuaFormat = "§d🌸 Mùa Xuân";
                else if (ngayTrongNam < 180) tenMuaFormat = "§6☀️ Mùa Hạ";
                else if (ngayTrongNam < 270) tenMuaFormat = "§e🍂 Mùa Thu";
                else tenMuaFormat = "§b❄️ Mùa Đông";

                sender.sendMessage("§a=================================");
                sender.sendMessage("§f🌍 Khí hậu hiện tại: " + tenMuaFormat);
                sender.sendMessage("§f📅 Tổng số ngày tích lũy map: §6" + tongSoNgay + " ngày");
                sender.sendMessage("§f⏳ Thời gian còn lại của mùa: §e" + ngayConLai + " ngày");
                if (overrideMua != null) {
                    sender.sendMessage("§c⚠️ CHẾ ĐỘ: Đang ép mùa cưỡng bức bởi Admin (" + overrideMua.toUpperCase() + ")");
                }
                sender.sendMessage("§a=================================");
                return true;
            }

            if (args[0].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("thoitiet.admin")) {
                    sender.sendMessage("§cBạn không có quyền hạn Admin để dùng lệnh này.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cThiếu tên mùa! Vui lòng chọn: xuan, ha, thu, dong");
                    return true;
                }
                String nhapMua = args[1].toLowerCase();
                if (nhapMua.equals("xuan") || nhapMua.equals("ha") || nhapMua.equals("thu") || nhapMua.equals("dong")) {
                    overrideMua = nhapMua;
                    sender.sendMessage("§a[Thành công] Đã kích hoạt chế độ ép mùa: " + nhapMua.toUpperCase());
                    Bukkit.broadcast(Component.text("§c§l[Admin] Khí hậu toàn bộ server đã chuyển sang " + nhapMua.toUpperCase() + "!"));
                } else {
                    sender.sendMessage("§cTên mùa sai cú pháp! Hãy gõ: xuan / ha / thu / dong");
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("reset")) {
                if (!sender.hasPermission("thoitiet.admin")) {
                    sender.sendMessage("§cBạn không có quyền hạn Admin để dùng lệnh này.");
                    return true;
                }
                overrideMua = null;
                sender.sendMessage("§a[Thành công] Đã gỡ bỏ ép mùa. Server quay lại tính mùa theo số ngày tự nhiên.");
                Bukkit.broadcast(Component.text("§a§l[Admin] Khí hậu server đã quay trở về chế độ tự động theo ngày."));
                return true;
            }
        }
        return false;
    }
}
