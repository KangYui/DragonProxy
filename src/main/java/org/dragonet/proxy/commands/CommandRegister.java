/*
 * GNU LESSER GENERAL PUBLIC LICENSE
 *                       Version 3, 29 June 2007
 *
 * Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 *
 * You can view LICENCE file for details. 
 *
 * @author The Dragonet Team
 */
package org.dragonet.proxy.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dragonet.proxy.DragonProxy;
import org.dragonet.proxy.configuration.Lang;

public final class CommandRegister {

    private final Map<String, ConsoleCommand> commandMap = Collections.synchronizedMap(new HashMap<String, ConsoleCommand>());

    private final DragonProxy proxy;

    public CommandRegister(DragonProxy proxy) {
        this.proxy = proxy;
        registerDefaults();
    }

    public void registerDefaults() {
        commandMap.put("stop", new StopCommand());
        commandMap.put("test", new TestCommand());
    }

    public void callCommand(String cmd) {
        String trimedCmd = cmd.trim();
        String label = null;
        String[] args = null;
        if (!cmd.trim().contains(" ")) {
            label = trimedCmd.toLowerCase();
            args = new String[0];
        } else {
            label = trimedCmd.substring(0, trimedCmd.indexOf(" ")).toLowerCase();
            String argLine = trimedCmd.substring(trimedCmd.indexOf(" ") + 1);
            args = argLine.contains(" ") ? argLine.split(" ") : new String[]{argLine};
        }
        if (label == null) {
            proxy.getLogger().warning(proxy.getLang().get(Lang.COMMAND_NOT_FOUND));
            return;
        }
        ConsoleCommand command = commandMap.get(label);
        if (command == null) {
            proxy.getLogger().warning(proxy.getLang().get(Lang.COMMAND_NOT_FOUND));
            return;
        }
        command.execute(proxy, args);
    }
}
