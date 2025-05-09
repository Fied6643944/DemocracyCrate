package pers.tany.democracycrate.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pers.tany.democracycrate.Main;
import pers.tany.democracycrate.utils.CrateUtil;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;

public class AddInterface implements InventoryHolder, Listener {
    private final String serial;
    private final Inventory inventory;
    private final String crateName;
    private final String probability;
    private final Player player;

    public AddInterface(String crateName, Player player, String probability) {

        this.crateName = crateName;
        this.player = player;
        this.probability = probability;
        this.inventory = Bukkit.createInventory(this, 54, IString.color(Main.message.getString("AddsTitle").replace("[crateName]", crateName)));
        this.serial = IRandom.createRandomString(8);

        Bukkit.getPluginManager().registerEvents(this, Main.plugin);
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getSerial() {
        return serial;
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getInventory().getHolder() instanceof AddInterface && evt.getPlayer() instanceof Player) {
            AddInterface replenishmentInterface = (AddInterface) evt.getInventory().getHolder();
            if (evt.getPlayer().equals(player) && replenishmentInterface.getSerial().equals(serial)) {
                HandlerList.unregisterAll(this);
                int number = 0;
                for (int i = 0; i < 54; i++) {
                    ItemStack addItemStack = inventory.getItem(i);
                    if (CrateUtil.getItemList(crateName).size() >= Main.config.getInt("MaxSize")) {
                        player.sendMessage(IString.color(Main.message.getString("MaxSize").replace("[max]", Main.config.getInt("MaxSize") + "")));
                        break;
                    }
                    if (!IItem.isEmpty(addItemStack)) {
                        if (Main.config.getStringList("BlackType").contains(addItemStack.getType().toString()) || Main.config.getStringList("BlackType").contains(addItemStack.getTypeId() + "")) {
                            player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                            continue;
                        }
                        boolean noAdd = false;
                        if (addItemStack.hasItemMeta() && addItemStack.getItemMeta().hasLore()) {
                            for (String lore : addItemStack.getItemMeta().getLore()) {
                                for (String l : Main.config.getStringList("BlackLore")) {
                                    if (lore.contains(l)) {
                                        noAdd = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if(noAdd){
                            player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                        }
                        CrateUtil.addItem(crateName, ISerializer.serializerItemStack(addItemStack) + ":" + probability);
                        number++;
                    }
                }
                player.sendMessage(IString.color(Main.message.getString("AddsSettingSuccess").replace("[number]", number + "")));
            }
        }
    }
}
