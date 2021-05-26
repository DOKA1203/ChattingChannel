package com.github.doka.chattingchannel

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.function.Consumer

class ChattingChannelPlugin : JavaPlugin() , Listener{
    private val channelMap:HashMap<Player,String> = hashMapOf()
    private val ch1:ArrayList<Player> = arrayListOf()
    private val ch2:ArrayList<Player> = arrayListOf()
    private lateinit var yml:YamlConfiguration
    private lateinit var inv:Inventory
    override fun onEnable() {
        yml = YamlConfiguration.loadConfiguration(File(dataFolder,"userdata.yml"))
        for (p:Player in Bukkit.getOnlinePlayers()){
            channelMap[p] = yml.getString(p.uniqueId.toString()).toString()
            when(channelMap[p]){
                "Channel_1" -> { ch1.add(p) }
                "Channel_2" -> { ch2.add(p) }
            }
        }
        var c = ItemStack(Material.BLUE_STAINED_GLASS_PANE)
        var m = c.itemMeta;m?.setDisplayName(" ");c.itemMeta = m
        inv = Bukkit.createInventory(null,9,"채팅 채널")
        inv.setItem(0,c);inv.setItem(8,c)
        c = ItemStack(Material.COMPASS);m = c.itemMeta;m?.setDisplayName("채팅 채널 1");c.itemMeta = m
        inv.setItem(1,c)
        c = ItemStack(Material.COMPASS);m = c.itemMeta;m?.setDisplayName("채팅 채널 2");c.itemMeta = m
        inv.setItem(2,c)

        c = ItemStack(Material.BARRIER); m = c.itemMeta;m?.setDisplayName("§cX");c.itemMeta = m
        inv.setItem(3,c);inv.setItem(4,c);inv.setItem(5,c);inv.setItem(6,c);inv.setItem(7,c)

        Bukkit.getPluginManager().registerEvents(this,this)
    }

    override fun onDisable() {
        for (p:Player in Bukkit.getOnlinePlayers()){
            yml.set(p.uniqueId.toString(),channelMap[p])
        }
        yml.save(File(dataFolder,"userdata.yml"))
    }

    @EventHandler
    fun join(event:PlayerJoinEvent){
        if(yml.contains(event.player.uniqueId.toString())){
            channelMap[event.player] = yml.getString(event.player.uniqueId.toString()).toString()
        }
        channelMap[event.player] = "Channel_1"

        when(channelMap[event.player]){
            "Channel_1" -> { ch1.add(event.player) }
            "Channel_2" -> { ch2.add(event.player) }
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent){
        yml.set(event.player.uniqueId.toString(),channelMap[event.player])
        channelMap.remove(event.player)
    }

    @EventHandler
    fun click(event:InventoryClickEvent){
        val player = event.whoClicked as Player
        if(event.view.title != "채팅 채널")return
        event.isCancelled = true
        if(event.slot == 1){
            channelMap[player] = "Channel_1"
            ch2.remove(player);ch1.remove(player)
            ch1.add(player)
        }
        if(event.slot == 2){
            channelMap[player] = "Channel_2"
            ch2.remove(player);ch1.remove(player)
            ch2.add(player)
        }
        player.closeInventory()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(label != "채팅채널")return false
        if(sender !is Player)return false
        val i:Inventory = Bukkit.createInventory(null,9,"채팅 채널")
        copyInventory(i,inv)
        when(channelMap[sender]){
            "Channel_1" -> { i.setItem(1, i.getItem(1)?.let { setGlow(it,true) }) }
            "Channel_2" -> { i.setItem(2, i.getItem(2)?.let { setGlow(it,true) }) }
        }
        sender.openInventory(i)
        return false
    }

    @EventHandler
    fun chat(event: AsyncPlayerChatEvent){
        val p = event.player
        event.isCancelled = true
        when(channelMap[p]){
            "Channel_1" -> { for (player in ch1) { player.sendMessage("${p.displayName} : ${event.message}") } }
            "Channel_2" -> { for (player in ch2) { player.sendMessage("${p.displayName} : ${event.message}") } }
        }
    }

    fun setGlow(item: ItemStack, glow: Boolean): ItemStack {
        val meta = item.itemMeta
        if (glow) {
            meta!!.addEnchant(Enchantment.WATER_WORKER, 70, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        } else {
            meta!!.enchants.keys.forEach(Consumer { enchantment: Enchantment? -> meta.removeEnchant(enchantment!!) })
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        item.itemMeta = meta
        return item
    }
    fun copyInventory(i1: Inventory, i2: Inventory) { for (i in 0 until i1.size.coerceAtMost(i2.size)) i1.setItem(i, i2.getItem(i)) }
}