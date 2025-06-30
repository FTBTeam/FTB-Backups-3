package dev.ftb.mods.ftbbackups.net;

import dev.ftb.mods.ftbbackups.Backups;
import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.client.BackupsClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NotifyDisabledPacket(boolean disabled) implements CustomPacketPayload {
    public static final Type<NotifyDisabledPacket> TYPE = new Type<>(FTBBackups.id("notify_disabled"));
    public static final StreamCodec<FriendlyByteBuf, NotifyDisabledPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, NotifyDisabledPacket::disabled,
            NotifyDisabledPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handler(NotifyDisabledPacket packet, IPayloadContext ignoredCcontext) {
        Backups.LOGGER.info("Server sent NotifyDisabledPacket({})", packet.disabled);
        BackupsClient.setDisabledOnThisServer(packet.disabled);
    }
}
