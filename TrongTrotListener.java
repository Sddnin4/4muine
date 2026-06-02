package me.tuicode.thoitietbonmua;

import org.bukkit.World;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class TrongTrotListener implements Listener {

    @EventHandler
    public void onCayPhatTrien(BlockGrowEvent event) {
        World world = event.getBlock().getWorld();
        long tongSoNgay = world.getFullTime() / 24000;
        long ngayTrongNam = tongSoNgay % 360;

        // Nếu đang nằm trong phạm vi 90 ngày của Mùa Xuân
        if (ngayTrongNam < 90) {
            // Ép kiểu sang thuộc tính tăng trưởng theo thời gian của block (1.21 chuẩn)
            if (event.getNewState().getBlockData() instanceof Ageable ageable) {
                int maxAge = ageable.getMaximumAge();
                int currentAge = ageable.getAge();
                
                // Nếu cây chưa chín hoàn toàn, cộng thêm 1 giai đoạn lớn cho nó
                if (currentAge < maxAge) {
                    ageable.setAge(Math.min(maxAge, currentAge + 1));
                    event.getNewState().setBlockData(ageable);
                }
            }
        }
    }
}
