package com.joao;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ReportPlugin ativado.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ReportPlugin desativado.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("report")) {
            if (args.length < 2) {
                sender.sendMessage("§cUso: /report <jogador> <motivo>");
                return true;
            }

            String jogadorReportado = args[0];
            String motivo = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

            sender.sendMessage("§aRelatório enviado para " + jogadorReportado + " com o motivo: " + motivo);
            // Aqui você pode adicionar chamada HTTP depois.

            return true;
        }
        return false;
    }
}