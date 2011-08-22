package net.sacredlabyrinth.phaed.simpleclans.listeners;

import net.sacredlabyrinth.phaed.simpleclans.Helper;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

/**
 *
 * @author phaed
 */
public class SCEntityListener extends EntityListener
{
    private SimpleClans plugin;

    /**
     *
     * @param plugin
     */
    public SCEntityListener()
    {
        plugin = SimpleClans.getInstance();
    }

    /**
     *
     * @param event
     */
    @Override
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player victim = (Player) event.getEntity();

            String attackerName = plugin.getDeathManager().pollLastAttacker(victim.getName());

            if (attackerName != null)
            {
                Player attacker = plugin.getServer().getPlayer(attackerName);

                if (attacker != null)
                {
                    Clan aclan = plugin.getClanManager().getClan(attacker);
                    Clan vclan = plugin.getClanManager().getClan(victim);

                    ClanPlayer atp = plugin.getClanManager().getClanPlayer(attacker);
                    ClanPlayer vtp = plugin.getClanManager().getClanPlayer(victim);

                    // record kill for attacker

                    if (aclan != null && atp != null && plugin.getClanManager().isVerified(aclan))
                    {
                        if (vclan == null || !plugin.getClanManager().isVerified(vclan))
                        {
                            atp.addCivilianKill();
                        }
                        else
                        {
                            if (aclan.isRival(vclan.getTag()))
                            {
                                atp.addRivalKill();
                                plugin.getClanManager().serverAnnounce(Helper.parseColors(aclan.getColorTag()) + ChatColor.AQUA + atp.getName() + " killed rival " + Helper.parseColors(vclan.getColorTag()) + ChatColor.AQUA + Helper.toColor(plugin.getSettingsManager().getClanChatBracketColor()) + plugin.getSettingsManager().getClanChatTagBracketRight() + " " + Helper.toColor(plugin.getSettingsManager().getClanChatNameColor()) + plugin.getSettingsManager().getClanChatPlayerBracketLeft() + vtp.getName() + plugin.getSettingsManager().getClanChatPlayerBracketRight());
                            }
                            else if (aclan.isAlly(vclan.getTag()))
                            {
                                // do not reacord ally kills
                            }
                            else if (aclan.equals(vclan))
                            {
                                // do not record same clan kills
                            }
                            else
                            {
                                atp.addNeutralKill();
                                plugin.getClanManager().serverAnnounce(Helper.parseColors(aclan.getColorTag()) + ChatColor.AQUA + atp.getName() + " killed " + Helper.parseColors(vclan.getColorTag()) + ChatColor.AQUA + Helper.toColor(plugin.getSettingsManager().getClanChatBracketColor()) + plugin.getSettingsManager().getClanChatTagBracketRight() + " " + Helper.toColor(plugin.getSettingsManager().getClanChatNameColor()) + plugin.getSettingsManager().getClanChatPlayerBracketLeft() + vtp.getName() + plugin.getSettingsManager().getClanChatPlayerBracketRight());
                            }
                        }

                        plugin.getStorageManager().updateClanPlayer(atp);
                    }

                    // record death for victim

                    if (vclan != null && vtp != null && plugin.getClanManager().isVerified(vclan))
                    {
                        vtp.addDeath();
                        plugin.getStorageManager().updateClanPlayer(vtp);
                    }
                }
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Player attacker = null;
        Player victim = null;

        if (event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

            if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
            {
                attacker = (Player) sub.getDamager();
                victim = (Player) sub.getEntity();
            }
        }

        if (event instanceof EntityDamageByProjectileEvent)
        {
            EntityDamageByProjectileEvent sub = (EntityDamageByProjectileEvent) event;

            if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
            {
                attacker = (Player) sub.getDamager();
                victim = (Player) sub.getEntity();
            }
        }

        if (attacker != null && victim != null)
        {
            Clan aclan = plugin.getClanManager().getClan(attacker);
            Clan vclan = plugin.getClanManager().getClan(victim);

            ClanPlayer vtp = plugin.getClanManager().getClanPlayer(victim);

            if (vclan != null)
            {
                if (aclan != null)
                {
                    if (vtp != null)
                    {
                        // personal ff enabled, allow damage

                        if (vtp.isFriendlyFire())
                        {
                            return;
                        }
                    }

                    // clan ff enabled, allow damage

                    if (vclan.isFriendlyFire())
                    {
                        return;
                    }

                    // same clan, deny damage

                    if (vclan.equals(aclan))
                    {
                        event.setCancelled(true);
                        return;
                    }

                    // ally clan, deny damage

                    if (vclan.isAlly(aclan.getTag()))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            plugin.getDeathManager().addDamager(victim.getName(), attacker.getName());
        }
    }
}
