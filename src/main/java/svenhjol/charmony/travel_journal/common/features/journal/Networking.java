package svenhjol.charmony.travel_journal.common.features.journal;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.travel_journal.TravelJournal;
import svenhjol.charmony.travel_journal.helpers.StreamHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Networking extends Setup<Journal> {
    public Networking(Journal feature) {
        super(feature);
    }

    public record S2CSendBookmarkToPlayer(Bookmark bookmark, BufferedImage photo, String sender) implements CustomPacketPayload {
        public static final Type<S2CSendBookmarkToPlayer> TYPE = new Type<>(TravelJournal.id("server_send_bookmark_to_player"));
        public static final StreamCodec<FriendlyByteBuf, S2CSendBookmarkToPlayer> CODEC =
            StreamCodec.of(S2CSendBookmarkToPlayer::encode, S2CSendBookmarkToPlayer::decode);

        public static void send(ServerPlayer player, Bookmark bookmark, BufferedImage photo, String sender) {
            ServerPlayNetworking.send(player, new S2CSendBookmarkToPlayer(bookmark, photo, sender));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CSendBookmarkToPlayer packet) {
            buf.writeUtf(packet.sender());

            var holder = new BookmarkAndPhoto(packet.bookmark(), packet.photo());
            buf.writeByteArray(holder.write().toByteArray());
        }

        private static S2CSendBookmarkToPlayer decode(FriendlyByteBuf buf) {
            var sender = buf.readUtf();

            var bytes = buf.readByteArray();
            var inputStream = new ByteArrayInputStream(bytes);
            var holder = BookmarkAndPhoto.read(inputStream);

            return new S2CSendBookmarkToPlayer(holder.bookmark(), holder.photo(), sender);
        }
    }

    public record C2SSendBookmarkToPlayer(Bookmark bookmark, BufferedImage photo, UUID recipient) implements CustomPacketPayload {
        public static final Type<C2SSendBookmarkToPlayer> TYPE = new Type<>(TravelJournal.id("client_send_bookmark_to_player"));
        public static final StreamCodec<FriendlyByteBuf, C2SSendBookmarkToPlayer> CODEC =
            StreamCodec.of(C2SSendBookmarkToPlayer::encode, C2SSendBookmarkToPlayer::decode);

        public static void send(Bookmark bookmark, BufferedImage photo, UUID recipient) {
            ClientPlayNetworking.send(new C2SSendBookmarkToPlayer(bookmark, photo, recipient));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SSendBookmarkToPlayer packet) {
            var holder = new BookmarkAndPhoto(packet.bookmark(), packet.photo());
            buf.writeUUID(packet.recipient());
            buf.writeByteArray(holder.write().toByteArray());
        }

        private static C2SSendBookmarkToPlayer decode(FriendlyByteBuf buf) {
            var recipient = buf.readUUID();

            var bytes = buf.readByteArray();
            var inputStream = new ByteArrayInputStream(bytes);
            var holder = BookmarkAndPhoto.read(inputStream);

            return new C2SSendBookmarkToPlayer(holder.bookmark(), holder.photo(), recipient);
        }
    }

    public record C2SPlayerSettings(boolean allowReceiving, List<String> allowFrom) implements CustomPacketPayload {
        public static final Type<C2SPlayerSettings> TYPE = new Type<>(TravelJournal.id("client_player_settings"));
        public static final StreamCodec<FriendlyByteBuf, C2SPlayerSettings> CODEC =
            StreamCodec.of(C2SPlayerSettings::encode, C2SPlayerSettings::decode);

        public static void send(boolean allowReceiving, List<String> allowFrom) {
            ClientPlayNetworking.send(new C2SPlayerSettings(allowReceiving, allowFrom));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SPlayerSettings packet) {
            buf.writeBoolean(packet.allowReceiving());
            var joined = String.join(",", packet.allowFrom());
            buf.writeUtf(joined);
        }

        private static C2SPlayerSettings decode(FriendlyByteBuf buf) {
            var allowReceiving = buf.readBoolean();
            var allowFrom = Arrays.stream(buf.readUtf().split(","))
                .filter(s -> !s.isEmpty())
                .toList();

            return new C2SPlayerSettings(allowReceiving, allowFrom);
        }
    }

    private record BookmarkAndPhoto(Bookmark bookmark, BufferedImage photo) {
        public static BookmarkAndPhoto read(ByteArrayInputStream inputStream) {
            try {
                var jsonLength = StreamHelper.readInt(inputStream);
                var jsonString = new String(inputStream.readNBytes(jsonLength));
                var bookmark = new Gson().fromJson(jsonString, Bookmark.class);

                var photoLength = StreamHelper.readInt(inputStream);
                var photo = ImageIO.read(new ByteArrayInputStream(inputStream.readNBytes(photoLength)));

                return new BookmarkAndPhoto(bookmark, photo);
            } catch (Exception e) {
                throw new RuntimeException("Could not read object: " + e.getMessage());
            }
        }

        public ByteArrayOutputStream write() {
            var outputStream = new ByteArrayOutputStream();
            var photoStream = new ByteArrayOutputStream();

            try {
                ImageIO.write(photo(), "png", photoStream);

                var photoBytes = photoStream.toByteArray();
                var jsonBytes = bookmark().toJsonString().getBytes(StandardCharsets.UTF_8);

                outputStream.write(StreamHelper.intToBytes(jsonBytes.length));
                outputStream.write(jsonBytes);
                outputStream.write(StreamHelper.intToBytes(photoBytes.length));
                outputStream.write(photoBytes);

                return outputStream;
            } catch (Exception e) {
                throw new RuntimeException("Could not write object: " + e.getMessage());
            }
        }
    }
}
