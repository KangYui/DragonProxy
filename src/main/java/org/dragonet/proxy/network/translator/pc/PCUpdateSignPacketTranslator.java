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
package org.dragonet.proxy.network.translator.pc;

import org.dragonet.net.packet.minecraft.BlockEntityDataPacket;
import org.dragonet.net.packet.minecraft.PEPacket;
import org.dragonet.proxy.nbt.tag.CompoundTag;
import org.dragonet.proxy.network.UpstreamSession;
import org.dragonet.proxy.network.translator.PCPacketTranslator;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateSignPacket;

public class PCUpdateSignPacketTranslator implements PCPacketTranslator<ServerUpdateSignPacket> {

    @Override
    public PEPacket[] translate(UpstreamSession session, ServerUpdateSignPacket packet) {
        CompoundTag root = new CompoundTag();
        root.putString("id", "Sign");
        root.putInt("x", packet.getPosition().getX());
        root.putInt("y", packet.getPosition().getY());
        root.putInt("z", packet.getPosition().getZ());
        root.putString("Text1", packet.getLines()[0].getFullText());
        root.putString("Text2", packet.getLines()[1].getFullText());
        root.putString("Text3", packet.getLines()[2].getFullText());
        root.putString("Text4", packet.getLines()[3].getFullText());
        return new PEPacket[]{new BlockEntityDataPacket(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ(), root)};
    }

}
