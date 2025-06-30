package dev.ftb.mods.ftbbackups.net;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class FTBBackupsNetHandler {
    public static void init(final PayloadRegistrar registrar) {
        registrar.playToClient(BackupProgressPacket.TYPE, BackupProgressPacket.STREAM_CODEC, BackupProgressPacket::handler);
        registrar.playToClient(NotifyDisabledPacket.TYPE, NotifyDisabledPacket.STREAM_CODEC, NotifyDisabledPacket::handler);
    }
}
