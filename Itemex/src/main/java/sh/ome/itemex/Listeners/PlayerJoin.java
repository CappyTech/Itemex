package sh.ome.itemex.Listeners;
/**
 * Event listener that notifies players about payouts and sales when they join.
 */

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.functions.sqliteDb;

import java.util.List;
import java.util.Map;

import static sh.ome.itemex.commands.commands.format_price;
import static sh.ome.itemex.commands.commands.get_meta;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //getLogger().info("# DEBUG - PLAYER JOINED THE SERVER");

        // create setting entry for player, if not exist
        sqliteDb.player_settings(e.getPlayer().getUniqueId().toString(), "", true);

        // PRINT PAYOUTS IF AVAILABLE
        sqliteDb.Payout[] payouts;
        payouts= sqliteDb.getPayout(e.getPlayer().getUniqueId().toString());

        if(payouts.length != 0)
            e.getPlayer().sendMessage(ChatColor.BLUE + "----------------------------\nITEMEX VAULT\n----------------------------\n" + ChatColor.RESET);

        for (int i = 0; i < payouts.length; i++) {
            if(payouts[i] == null) { //skip empty entries
                break;
            }
            e.getPlayer().sendMessage(ChatColor.GOLD + " [" + payouts[i].amount + "] " + ChatColor.RESET + get_meta(payouts[i].itemid) );
            //TextComponent message = new TextComponent(ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("sq_you_can_with") + " /ix withdraw " + ItemexCommand.get_meta(payouts[i].itemid) +" " + payouts[i].amount);
            //message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + ItemexCommand.get_meta(payouts[i].itemid) +" " + payouts[i].amount));
            //e.getPlayer().spigot().sendMessage(message);
        }

        if(payouts.length != 0) {
            TextComponent message = new TextComponent(ChatColor.BLUE + "\n(" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") "+ Itemex.language.getString("sq_you_can_with") + " /ix withdraw ");
            message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix withdraw "));
            e.getPlayer().spigot().sendMessage(message);
        }


        // if there are more than 3 entries
        if( payouts.length > 3)
            e.getPlayer().sendMessage(Itemex.language.getString("pj_withdraw"));


        // Check if there are sold items (from notification db entry)
        List<Map<String, Object>> sellNotifications = sqliteDb.get_sellNotification(e.getPlayer().getUniqueId().toString());

        double sum = 0.0;
        for (Map<String, Object> sellNotification : sellNotifications) {
            String itemid = (String) sellNotification.get("itemid");
            double price = Double.parseDouble((String) sellNotification.get("price"));
            int amount = Integer.parseInt((String) sellNotification.get("amount"));
            sum = sum + price;
        }


        if(payouts.length != 0 && Itemex.sales_notification) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "\n\n----------------------------\nSALES ON ITEMEX\n----------------------------\n" + ChatColor.RESET);
            e.getPlayer().sendMessage("XXXXX" + Itemex.language.getString("pj_last_sales1") + ChatColor.GREEN + format_price(sum) + ChatColor.RESET + Itemex.language.getString("pj_last_sales2") + "\n.\n.");
        }
    }
}
