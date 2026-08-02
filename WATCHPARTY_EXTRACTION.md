# WATCHPARTY JAR EXTRACTION

Quelle: `/home/runner/work/MPSQ-Kameras/MPSQ-Kameras/watchparty.jar`

## 1) Vollständige Inhaltsübersicht
- Gesamtdateien: **112**
- Klassen (`*.class`): **106**
- Konfigurationsdateien: **3**
- Assets: **1**
- Eingebettete native/libs (`*.jar`, `*.so`, `*.dll`, `*.dylib`): **0**

### Ressourcen-/Ordnerstruktur
```text
.
META-INF
assets
assets/watchparty
assets/watchparty/lang
dev
dev/watchparty
dev/watchparty/a
dev/watchparty/b
dev/watchparty/c
dev/watchparty/config
dev/watchparty/d
dev/watchparty/e
dev/watchparty/f
dev/watchparty/g
dev/watchparty/mixin
dev/watchparty/screen
dev/watchparty/sync
```

### Alle Dateien
```text
LICENSE_watchparty
META-INF/MANIFEST.MF
assets/watchparty/icon.png
assets/watchparty/lang/en_us.json
dev/watchparty/WatchPartyClient.class
dev/watchparty/a.class
dev/watchparty/a/a.class
dev/watchparty/b/a.class
dev/watchparty/b/a/a.class
dev/watchparty/b/a/b$1.class
dev/watchparty/b/a/b$2.class
dev/watchparty/b/a/b.class
dev/watchparty/b/a/c.class
dev/watchparty/b/a/d.class
dev/watchparty/b/a/e.class
dev/watchparty/b/a/f.class
dev/watchparty/b/a/g.class
dev/watchparty/b/a/h.class
dev/watchparty/b/a/i$1.class
dev/watchparty/b/a/i$2.class
dev/watchparty/b/a/i.class
dev/watchparty/b/a/j.class
dev/watchparty/b/a/k.class
dev/watchparty/b/b.class
dev/watchparty/b/c.class
dev/watchparty/b/d.class
dev/watchparty/b/e.class
dev/watchparty/c/a$1.class
dev/watchparty/c/a$a.class
dev/watchparty/c/a$b.class
dev/watchparty/c/a.class
dev/watchparty/c/b.class
dev/watchparty/config/ConfigManager.class
dev/watchparty/config/PlaybackSettings.class
dev/watchparty/config/SyncSettings.class
dev/watchparty/config/WatchPartyConfig.class
dev/watchparty/d/a$a.class
dev/watchparty/d/a.class
dev/watchparty/d/b.class
dev/watchparty/e/a.class
dev/watchparty/e/b.class
dev/watchparty/e/c.class
dev/watchparty/e/d.class
dev/watchparty/e/e.class
dev/watchparty/e/f.class
dev/watchparty/e/g.class
dev/watchparty/e/h.class
dev/watchparty/e/i$a.class
dev/watchparty/e/i.class
dev/watchparty/e/j.class
dev/watchparty/e/k.class
dev/watchparty/f/a.class
dev/watchparty/f/b.class
dev/watchparty/f/c.class
dev/watchparty/f/d$a.class
dev/watchparty/f/d$b.class
dev/watchparty/f/d$c.class
dev/watchparty/f/d$d.class
dev/watchparty/f/d.class
dev/watchparty/g/a.class
dev/watchparty/g/b.class
dev/watchparty/g/c$a.class
dev/watchparty/g/c.class
dev/watchparty/g/d.class
dev/watchparty/g/e.class
dev/watchparty/mixin/client/WorldRendererMixin.class
dev/watchparty/screen/CropMode.class
dev/watchparty/screen/ScreenBlockPos.class
dev/watchparty/screen/ScreenFace$a.class
dev/watchparty/screen/ScreenFace.class
dev/watchparty/screen/ScreenRegion$1.class
dev/watchparty/screen/ScreenRegion.class
dev/watchparty/screen/a.class
dev/watchparty/screen/b.class
dev/watchparty/screen/c.class
dev/watchparty/screen/d.class
dev/watchparty/screen/e$1.class
dev/watchparty/screen/e.class
dev/watchparty/screen/f.class
dev/watchparty/screen/g.class
dev/watchparty/screen/h.class
dev/watchparty/screen/i.class
dev/watchparty/screen/j.class
dev/watchparty/screen/k$1.class
dev/watchparty/screen/k$a.class
dev/watchparty/screen/k.class
dev/watchparty/screen/l$a.class
dev/watchparty/screen/l.class
dev/watchparty/sync/RelayFormCodec.class
dev/watchparty/sync/RelayIdentity.class
dev/watchparty/sync/RelayIdentityStore.class
dev/watchparty/sync/RelayJsonCodec.class
dev/watchparty/sync/RelayParticipant.class
dev/watchparty/sync/RelayRoomResolver$NormalizedEndpoint.class
dev/watchparty/sync/RelayRoomResolver.class
dev/watchparty/sync/RelayScreenDescriptor.class
dev/watchparty/sync/RelayScreenGrant.class
dev/watchparty/sync/RelayScreenMutationResult.class
dev/watchparty/sync/RelaySessionStore$1.class
dev/watchparty/sync/RelaySessionStore$2.class
dev/watchparty/sync/RelaySessionStore$3.class
dev/watchparty/sync/RelaySessionStore$AuthSession.class
dev/watchparty/sync/RelaySessionStore$CachedSession.class
dev/watchparty/sync/RelaySessionStore$FetchResult.class
dev/watchparty/sync/RelaySessionStore$RoomContext.class
dev/watchparty/sync/RelaySessionStore.class
dev/watchparty/sync/RelaySigning.class
dev/watchparty/sync/RelaySubmissionResult.class
dev/watchparty/sync/ScreenPermission.class
dev/watchparty/sync/SharedScreenSession.class
fabric.mod.json
watchparty.mixins.json
```

## 2) Klassen- & Code-Struktur
### Paketübersicht
```text
dev.watchparty: 2
dev.watchparty.a: 1
dev.watchparty.b: 5
dev.watchparty.b.a: 15
dev.watchparty.c: 5
dev.watchparty.config: 4
dev.watchparty.d: 3
dev.watchparty.e: 12
dev.watchparty.f: 8
dev.watchparty.g: 6
dev.watchparty.mixin.client: 1
dev.watchparty.screen: 22
dev.watchparty.sync: 22
```

### Alle Klassen
```text
dev/watchparty/WatchPartyClient.class
dev/watchparty/a.class
dev/watchparty/a/a.class
dev/watchparty/b/a.class
dev/watchparty/b/a/a.class
dev/watchparty/b/a/b$1.class
dev/watchparty/b/a/b$2.class
dev/watchparty/b/a/b.class
dev/watchparty/b/a/c.class
dev/watchparty/b/a/d.class
dev/watchparty/b/a/e.class
dev/watchparty/b/a/f.class
dev/watchparty/b/a/g.class
dev/watchparty/b/a/h.class
dev/watchparty/b/a/i$1.class
dev/watchparty/b/a/i$2.class
dev/watchparty/b/a/i.class
dev/watchparty/b/a/j.class
dev/watchparty/b/a/k.class
dev/watchparty/b/b.class
dev/watchparty/b/c.class
dev/watchparty/b/d.class
dev/watchparty/b/e.class
dev/watchparty/c/a$1.class
dev/watchparty/c/a$a.class
dev/watchparty/c/a$b.class
dev/watchparty/c/a.class
dev/watchparty/c/b.class
dev/watchparty/config/ConfigManager.class
dev/watchparty/config/PlaybackSettings.class
dev/watchparty/config/SyncSettings.class
dev/watchparty/config/WatchPartyConfig.class
dev/watchparty/d/a$a.class
dev/watchparty/d/a.class
dev/watchparty/d/b.class
dev/watchparty/e/a.class
dev/watchparty/e/b.class
dev/watchparty/e/c.class
dev/watchparty/e/d.class
dev/watchparty/e/e.class
dev/watchparty/e/f.class
dev/watchparty/e/g.class
dev/watchparty/e/h.class
dev/watchparty/e/i$a.class
dev/watchparty/e/i.class
dev/watchparty/e/j.class
dev/watchparty/e/k.class
dev/watchparty/f/a.class
dev/watchparty/f/b.class
dev/watchparty/f/c.class
dev/watchparty/f/d$a.class
dev/watchparty/f/d$b.class
dev/watchparty/f/d$c.class
dev/watchparty/f/d$d.class
dev/watchparty/f/d.class
dev/watchparty/g/a.class
dev/watchparty/g/b.class
dev/watchparty/g/c$a.class
dev/watchparty/g/c.class
dev/watchparty/g/d.class
dev/watchparty/g/e.class
dev/watchparty/mixin/client/WorldRendererMixin.class
dev/watchparty/screen/CropMode.class
dev/watchparty/screen/ScreenBlockPos.class
dev/watchparty/screen/ScreenFace$a.class
dev/watchparty/screen/ScreenFace.class
dev/watchparty/screen/ScreenRegion$1.class
dev/watchparty/screen/ScreenRegion.class
dev/watchparty/screen/a.class
dev/watchparty/screen/b.class
dev/watchparty/screen/c.class
dev/watchparty/screen/d.class
dev/watchparty/screen/e$1.class
dev/watchparty/screen/e.class
dev/watchparty/screen/f.class
dev/watchparty/screen/g.class
dev/watchparty/screen/h.class
dev/watchparty/screen/i.class
dev/watchparty/screen/j.class
dev/watchparty/screen/k$1.class
dev/watchparty/screen/k$a.class
dev/watchparty/screen/k.class
dev/watchparty/screen/l$a.class
dev/watchparty/screen/l.class
dev/watchparty/sync/RelayFormCodec.class
dev/watchparty/sync/RelayIdentity.class
dev/watchparty/sync/RelayIdentityStore.class
dev/watchparty/sync/RelayJsonCodec.class
dev/watchparty/sync/RelayParticipant.class
dev/watchparty/sync/RelayRoomResolver$NormalizedEndpoint.class
dev/watchparty/sync/RelayRoomResolver.class
dev/watchparty/sync/RelayScreenDescriptor.class
dev/watchparty/sync/RelayScreenGrant.class
dev/watchparty/sync/RelayScreenMutationResult.class
dev/watchparty/sync/RelaySessionStore$1.class
dev/watchparty/sync/RelaySessionStore$2.class
dev/watchparty/sync/RelaySessionStore$3.class
dev/watchparty/sync/RelaySessionStore$AuthSession.class
dev/watchparty/sync/RelaySessionStore$CachedSession.class
dev/watchparty/sync/RelaySessionStore$FetchResult.class
dev/watchparty/sync/RelaySessionStore$RoomContext.class
dev/watchparty/sync/RelaySessionStore.class
dev/watchparty/sync/RelaySigning.class
dev/watchparty/sync/RelaySubmissionResult.class
dev/watchparty/sync/ScreenPermission.class
dev/watchparty/sync/SharedScreenSession.class
```

### Wichtige Klassen/Funktionen (aus `javap -public`)
```text
### dev.watchparty.WatchPartyClient
Compiled from "WatchPartyClient.java"
public final class dev.watchparty.WatchPartyClient implements net.fabricmc.api.ClientModInitializer {
  public dev.watchparty.WatchPartyClient();
  public void onInitializeClient();
}

### dev.watchparty.config.ConfigManager
Compiled from "ConfigManager.java"
public final class dev.watchparty.config.ConfigManager {
  public dev.watchparty.config.ConfigManager();
  public dev.watchparty.config.WatchPartyConfig get();
  public java.nio.file.Path configPath();
  public void loadOrCreate();
  public void save();
}

### dev.watchparty.config.PlaybackSettings
Compiled from "PlaybackSettings.java"
public final class dev.watchparty.config.PlaybackSettings {
  public int videoTextureWidth;
  public int videoTextureHeight;
  public double volume;
  public int brightness;
  public java.util.Map<java.lang.String, java.lang.Double> screenVolumes;
  public dev.watchparty.config.PlaybackSettings();
  public double screenVolume(java.lang.String);
  public void setScreenVolume(java.lang.String, double);
  public double brightnessFactor();
  public void setBrightness(int);
  public static int clampBrightness(int);
}

### dev.watchparty.config.SyncSettings
Compiled from "SyncSettings.java"
public final class dev.watchparty.config.SyncSettings {
  public java.lang.String relayBaseUrl;
  public boolean requireHttps;
  public int relayPollIntervalMillis;
  public int relayRequestTimeoutMillis;
  public int relaySessionStartLeadMillis;
  public int relayHeartbeatIntervalMillis;
  public dev.watchparty.config.SyncSettings();
}

### dev.watchparty.config.WatchPartyConfig
Compiled from "WatchPartyConfig.java"
public final class dev.watchparty.config.WatchPartyConfig {
  public transient int schemaVersion;
  public transient double maxRenderDistanceBlocks;
  public java.lang.String lastRemoteScreenId;
  public java.lang.String lastRemoteView;
  public dev.watchparty.config.PlaybackSettings playback;
  public transient dev.watchparty.config.SyncSettings sync;
  public dev.watchparty.config.WatchPartyConfig();
  public static dev.watchparty.config.WatchPartyConfig defaults();
}

### dev.watchparty.screen.CropMode
Compiled from "CropMode.java"
public final class dev.watchparty.screen.CropMode extends java.lang.Enum<dev.watchparty.screen.CropMode> {
  public static final dev.watchparty.screen.CropMode FIT;
  public static final dev.watchparty.screen.CropMode CROP;
  public static final dev.watchparty.screen.CropMode STRETCH;
  public static dev.watchparty.screen.CropMode[] values();
  public static dev.watchparty.screen.CropMode valueOf(java.lang.String);
  public dev.watchparty.screen.CropMode next();
  public static dev.watchparty.screen.CropMode parse(java.lang.String);
}

### dev.watchparty.screen.ScreenBlockPos
Compiled from "ScreenBlockPos.java"
public final class dev.watchparty.screen.ScreenBlockPos extends java.lang.Record {
  public dev.watchparty.screen.ScreenBlockPos(int, int, int);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public int x();
  public int y();
  public int z();
}

### dev.watchparty.screen.ScreenFace
Compiled from "ScreenFace.java"
public final class dev.watchparty.screen.ScreenFace extends java.lang.Enum<dev.watchparty.screen.ScreenFace> {
  public static final dev.watchparty.screen.ScreenFace DOWN;
  public static final dev.watchparty.screen.ScreenFace UP;
  public static final dev.watchparty.screen.ScreenFace NORTH;
  public static final dev.watchparty.screen.ScreenFace SOUTH;
  public static final dev.watchparty.screen.ScreenFace WEST;
  public static final dev.watchparty.screen.ScreenFace EAST;
  public static dev.watchparty.screen.ScreenFace[] values();
  public static dev.watchparty.screen.ScreenFace valueOf(java.lang.String);
  public dev.watchparty.screen.ScreenFace$a axis();
  public int normalX();
  public int normalY();
  public int normalZ();
  public boolean isCompatibleWith(dev.watchparty.screen.ScreenFace);
  public dev.watchparty.screen.ScreenFace opposite();
  public static java.util.Optional<dev.watchparty.screen.ScreenFace> parse(java.lang.String);
}

### dev.watchparty.screen.ScreenFace$a
Compiled from "ScreenFace.java"
public final class dev.watchparty.screen.ScreenFace$a extends java.lang.Enum<dev.watchparty.screen.ScreenFace$a> {
  public static final dev.watchparty.screen.ScreenFace$a a;
  public static final dev.watchparty.screen.ScreenFace$a b;
  public static final dev.watchparty.screen.ScreenFace$a c;
  public static dev.watchparty.screen.ScreenFace$a[] a();
  public static dev.watchparty.screen.ScreenFace$a a(java.lang.String);
}

### dev.watchparty.screen.ScreenRegion
Compiled from "ScreenRegion.java"
public final class dev.watchparty.screen.ScreenRegion extends java.lang.Record {
  public dev.watchparty.screen.ScreenRegion(dev.watchparty.screen.ScreenBlockPos, dev.watchparty.screen.ScreenBlockPos, dev.watchparty.screen.ScreenFace, dev.watchparty.screen.ScreenFace);
  public java.util.Optional<java.lang.String> validationError();
  public int widthBlocks();
  public int heightBlocks();
  public dev.watchparty.screen.ScreenRegion withFacing(dev.watchparty.screen.ScreenFace);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public dev.watchparty.screen.ScreenBlockPos firstCorner();
  public dev.watchparty.screen.ScreenBlockPos secondCorner();
  public dev.watchparty.screen.ScreenFace surfaceFace();
  public dev.watchparty.screen.ScreenFace facing();
}

### dev.watchparty.screen.ScreenRegion$1
Compiled from "ScreenRegion.java"
class dev.watchparty.screen.ScreenRegion$1 {
}

### dev.watchparty.sync.RelayFormCodec
Compiled from "RelayFormCodec.java"
public final class dev.watchparty.sync.RelayFormCodec {
  public static java.lang.String encode(java.util.Map<java.lang.String, java.lang.String>);
  public static java.util.Map<java.lang.String, java.lang.String> decode(java.lang.String);
  public static java.lang.String encodeComponent(java.lang.String);
  public static java.lang.String decodeComponent(java.lang.String);
}

### dev.watchparty.sync.RelayIdentity
Compiled from "RelayIdentity.java"
public final class dev.watchparty.sync.RelayIdentity extends java.lang.Record {
  public dev.watchparty.sync.RelayIdentity(java.lang.String, java.lang.String, java.lang.String, java.security.PublicKey, java.security.PrivateKey);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String installId();
  public java.lang.String publicKeyBase64();
  public java.lang.String privateKeyBase64();
  public java.security.PublicKey publicKey();
  public java.security.PrivateKey privateKey();
}

### dev.watchparty.sync.RelayIdentityStore
Compiled from "RelayIdentityStore.java"
public final class dev.watchparty.sync.RelayIdentityStore {
  public dev.watchparty.sync.RelayIdentityStore(java.nio.file.Path);
  public synchronized dev.watchparty.sync.RelayIdentity loadOrCreate(java.lang.String);
}

### dev.watchparty.sync.RelayJsonCodec
Compiled from "RelayJsonCodec.java"
public final class dev.watchparty.sync.RelayJsonCodec {
  public static java.lang.String encodeBase64(java.lang.Object);
  public static <T> T decodeBase64(java.lang.String, java.lang.reflect.Type, T);
}

### dev.watchparty.sync.RelayParticipant
Compiled from "RelayParticipant.java"
public final class dev.watchparty.sync.RelayParticipant extends java.lang.Record {
  public dev.watchparty.sync.RelayParticipant(java.lang.String, java.lang.String, java.lang.String, java.lang.String, double, double, double, long);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String installId();
  public java.lang.String playerName();
  public java.lang.String playerUuid();
  public java.lang.String dimensionId();
  public double x();
  public double y();
  public double z();
  public long lastSeenAtMillis();
}

### dev.watchparty.sync.RelayRoomResolver
Compiled from "RelayRoomResolver.java"
final class dev.watchparty.sync.RelayRoomResolver {
}

### dev.watchparty.sync.RelayRoomResolver$NormalizedEndpoint
Compiled from "RelayRoomResolver.java"
final class dev.watchparty.sync.RelayRoomResolver$NormalizedEndpoint extends java.lang.Record {
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String host();
  public java.lang.Integer port();
}

### dev.watchparty.sync.RelayScreenDescriptor
Compiled from "RelayScreenDescriptor.java"
public final class dev.watchparty.sync.RelayScreenDescriptor extends java.lang.Record {
  public dev.watchparty.sync.RelayScreenDescriptor(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, dev.watchparty.screen.ScreenRegion, dev.watchparty.screen.CropMode, int, boolean, double, boolean, java.lang.String, java.lang.String, java.lang.String, java.util.Set<dev.watchparty.sync.ScreenPermission>, java.util.List<dev.watchparty.sync.RelayScreenGrant>);
  public boolean canView();
  public boolean canManage();
  public boolean canControl();
  public boolean ownedBy(java.lang.String);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String roomId();
  public java.lang.String screenId();
  public java.lang.String displayName();
  public java.lang.String activationCode();
  public java.lang.String dimensionId();
  public dev.watchparty.screen.ScreenRegion region();
  public dev.watchparty.screen.CropMode cropMode();
  public int displayRotationDegrees();
  public boolean globalScreen();
  public double volume();
  public boolean spatialAudioEnabled();
  public java.lang.String ownerInstallId();
  public java.lang.String ownerPlayerName();
  public java.lang.String ownerPlayerUuid();
  public java.util.Set<dev.watchparty.sync.ScreenPermission> effectivePermissions();
  public java.util.List<dev.watchparty.sync.RelayScreenGrant> grants();
}

### dev.watchparty.sync.RelayScreenGrant
Compiled from "RelayScreenGrant.java"
public final class dev.watchparty.sync.RelayScreenGrant extends java.lang.Record {
  public dev.watchparty.sync.RelayScreenGrant(java.lang.String, java.lang.String, java.lang.String, java.util.Set<dev.watchparty.sync.ScreenPermission>);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String installId();
  public java.lang.String playerName();
  public java.lang.String playerUuid();
  public java.util.Set<dev.watchparty.sync.ScreenPermission> permissions();
}

### dev.watchparty.sync.RelayScreenMutationResult
Compiled from "RelayScreenMutationResult.java"
public final class dev.watchparty.sync.RelayScreenMutationResult extends java.lang.Record {
  public dev.watchparty.sync.RelayScreenMutationResult(boolean, java.lang.String, dev.watchparty.sync.RelayScreenDescriptor);
  public static dev.watchparty.sync.RelayScreenMutationResult success(java.lang.String, dev.watchparty.sync.RelayScreenDescriptor);
  public static dev.watchparty.sync.RelayScreenMutationResult failure(java.lang.String);
  public java.util.Optional<dev.watchparty.sync.RelayScreenDescriptor> screen();
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public boolean success();
  public java.lang.String message();
  public dev.watchparty.sync.RelayScreenDescriptor descriptor();
}

### dev.watchparty.sync.RelaySessionStore
Compiled from "RelaySessionStore.java"
public final class dev.watchparty.sync.RelaySessionStore {
  public dev.watchparty.sync.RelaySessionStore(dev.watchparty.config.ConfigManager);
  public void shutdown();
  public java.util.Optional<dev.watchparty.sync.SharedScreenSession> session(net.minecraft.class_310, java.lang.String);
  public dev.watchparty.sync.RelaySubmissionResult pushPlaySession(net.minecraft.class_310, java.lang.String, java.lang.String, dev.watchparty.e.c);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> pushPlaySessionAsync(net.minecraft.class_310, java.lang.String, java.lang.String, dev.watchparty.e.c);
  public dev.watchparty.sync.RelaySubmissionResult pushStopSession(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> pushStopSessionAsync(net.minecraft.class_310, java.lang.String);
  public dev.watchparty.sync.RelaySubmissionResult pushPauseToggleSession(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> pushPauseToggleSessionAsync(net.minecraft.class_310, java.lang.String);
  public dev.watchparty.sync.RelaySubmissionResult pushSeekSession(net.minecraft.class_310, java.lang.String, long);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> pushSeekSessionAsync(net.minecraft.class_310, java.lang.String, long);
  public dev.watchparty.sync.RelayScreenMutationResult createScreen(net.minecraft.class_310, java.lang.String, java.lang.String, dev.watchparty.screen.ScreenRegion, dev.watchparty.screen.CropMode, int);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelayScreenMutationResult> createScreenAsync(net.minecraft.class_310, java.lang.String, java.lang.String, dev.watchparty.screen.ScreenRegion, dev.watchparty.screen.CropMode, int);
  public dev.watchparty.sync.RelayScreenMutationResult activateScreen(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelayScreenMutationResult> activateScreenAsync(net.minecraft.class_310, java.lang.String);
  public dev.watchparty.sync.RelaySubmissionResult deactivateScreen(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> deactivateScreenAsync(net.minecraft.class_310, java.lang.String);
  public dev.watchparty.sync.RelayScreenMutationResult updateScreen(net.minecraft.class_310, java.lang.String, java.lang.String, dev.watchparty.screen.ScreenFace, dev.watchparty.screen.CropMode, int, double, boolean);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelayScreenMutationResult> updateScreenAsync(net.minecraft.class_310, java.lang.String, java.lang.String, dev.watchparty.screen.ScreenFace, dev.watchparty.screen.CropMode, int, double, boolean);
  public dev.watchparty.sync.RelayScreenMutationResult rotateScreenCode(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelayScreenMutationResult> rotateScreenCodeAsync(net.minecraft.class_310, java.lang.String);
  public dev.watchparty.sync.RelaySubmissionResult deleteScreen(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> deleteScreenAsync(net.minecraft.class_310, java.lang.String);
  public java.util.List<dev.watchparty.sync.RelayScreenDescriptor> accessibleScreens(net.minecraft.class_310);
  public void invalidateScreenCatalog();
  public dev.watchparty.sync.RelaySubmissionResult grantScreenPermissions(net.minecraft.class_310, java.lang.String, dev.watchparty.sync.RelayParticipant, java.util.EnumSet<dev.watchparty.sync.ScreenPermission>);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> grantScreenPermissionsAsync(net.minecraft.class_310, java.lang.String, dev.watchparty.sync.RelayParticipant, java.util.EnumSet<dev.watchparty.sync.ScreenPermission>);
  public dev.watchparty.sync.RelaySubmissionResult revokeScreenAccess(net.minecraft.class_310, java.lang.String, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> revokeScreenAccessAsync(net.minecraft.class_310, java.lang.String, java.lang.String);
  public java.util.List<dev.watchparty.sync.RelayParticipant> nearbyParticipants(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<java.util.List<dev.watchparty.sync.RelayParticipant>> nearbyParticipantsAsync(net.minecraft.class_310, java.lang.String);
  public java.util.List<dev.watchparty.sync.RelayParticipant> screenViewers(net.minecraft.class_310, java.lang.String);
  public java.util.concurrent.CompletableFuture<java.util.List<dev.watchparty.sync.RelayParticipant>> screenViewersAsync(net.minecraft.class_310, java.lang.String);
  public dev.watchparty.sync.RelaySubmissionResult kickScreenViewer(net.minecraft.class_310, java.lang.String, java.lang.String);
  public java.util.concurrent.CompletableFuture<dev.watchparty.sync.RelaySubmissionResult> kickScreenViewerAsync(net.minecraft.class_310, java.lang.String, java.lang.String);
  public void clear();
  public long currentTimeMillis();
  public java.lang.String installId();
}

### dev.watchparty.sync.RelaySessionStore$1
Compiled from "RelaySessionStore.java"
class dev.watchparty.sync.RelaySessionStore$1 extends com.google.gson.reflect.TypeToken<java.util.List<dev.watchparty.sync.RelayParticipant>> {
}

### dev.watchparty.sync.RelaySessionStore$2
Compiled from "RelaySessionStore.java"
class dev.watchparty.sync.RelaySessionStore$2 extends com.google.gson.reflect.TypeToken<dev.watchparty.sync.RelayScreenDescriptor> {
}

### dev.watchparty.sync.RelaySessionStore$3
Compiled from "RelaySessionStore.java"
class dev.watchparty.sync.RelaySessionStore$3 extends com.google.gson.reflect.TypeToken<java.util.List<dev.watchparty.sync.RelayScreenDescriptor>> {
}

### dev.watchparty.sync.RelaySessionStore$AuthSession
Compiled from "RelaySessionStore.java"
final class dev.watchparty.sync.RelaySessionStore$AuthSession extends java.lang.Record {
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String installId();
  public java.lang.String playerUuid();
  public java.lang.String relayBaseUrl();
  public java.lang.String authToken();
  public long expiresAtMillis();
}

### dev.watchparty.sync.RelaySessionStore$CachedSession
Compiled from "RelaySessionStore.java"
final class dev.watchparty.sync.RelaySessionStore$CachedSession {
}

### dev.watchparty.sync.RelaySessionStore$FetchResult
Compiled from "RelaySessionStore.java"
final class dev.watchparty.sync.RelaySessionStore$FetchResult extends java.lang.Record {
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public dev.watchparty.sync.SharedScreenSession session();
  public java.lang.String message();
  public long clockOffsetMillis();
}

### dev.watchparty.sync.RelaySessionStore$RoomContext
Compiled from "RelaySessionStore.java"
final class dev.watchparty.sync.RelaySessionStore$RoomContext extends java.lang.Record {
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String roomId();
  public java.lang.String playerName();
  public java.lang.String playerUuid();
  public java.lang.String dimensionId();
  public double x();
  public double y();
  public double z();
  public java.lang.String relayBaseUrl();
}

### dev.watchparty.sync.RelaySigning
Compiled from "RelaySigning.java"
public final class dev.watchparty.sync.RelaySigning {
  public static java.lang.String signBase64(java.util.Map<java.lang.String, java.lang.String>, java.security.PrivateKey) throws java.security.GeneralSecurityException;
  public static boolean verifyBase64(java.util.Map<java.lang.String, java.lang.String>, java.lang.String, java.security.PublicKey) throws java.security.GeneralSecurityException;
  public static java.lang.String canonicalPayload(java.util.Map<java.lang.String, java.lang.String>);
  public static java.lang.String installIdFromPublicKey(byte[]) throws java.security.GeneralSecurityException;
  public static java.security.PublicKey decodePublicKey(java.lang.String) throws java.security.GeneralSecurityException;
  public static java.security.PrivateKey decodePrivateKey(java.lang.String) throws java.security.GeneralSecurityException;
  public static java.lang.String encodeBase64(byte[]);
}

### dev.watchparty.sync.RelaySubmissionResult
Compiled from "RelaySubmissionResult.java"
public final class dev.watchparty.sync.RelaySubmissionResult extends java.lang.Record {
  public dev.watchparty.sync.RelaySubmissionResult(boolean, java.lang.String);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public boolean success();
  public java.lang.String message();
}

### dev.watchparty.sync.ScreenPermission
Compiled from "ScreenPermission.java"
public final class dev.watchparty.sync.ScreenPermission extends java.lang.Enum<dev.watchparty.sync.ScreenPermission> {
  public static final dev.watchparty.sync.ScreenPermission VIEW;
  public static final dev.watchparty.sync.ScreenPermission CONTROL;
  public static final dev.watchparty.sync.ScreenPermission MANAGE;
  public static dev.watchparty.sync.ScreenPermission[] values();
  public static dev.watchparty.sync.ScreenPermission valueOf(java.lang.String);
  public static java.util.EnumSet<dev.watchparty.sync.ScreenPermission> parseCsv(java.lang.String);
  public static java.lang.String toCsv(java.util.Set<dev.watchparty.sync.ScreenPermission>);
}

### dev.watchparty.sync.SharedScreenSession
Compiled from "SharedScreenSession.java"
public final class dev.watchparty.sync.SharedScreenSession extends java.lang.Record {
  public dev.watchparty.sync.SharedScreenSession(java.lang.String, boolean, boolean, dev.watchparty.e.c, long, long, int, java.lang.String, java.lang.String, long);
  public static dev.watchparty.sync.SharedScreenSession stopped(java.lang.String);
  public boolean ownedBy(java.lang.String);
  public final java.lang.String toString();
  public final int hashCode();
  public final boolean equals(java.lang.Object);
  public java.lang.String screenId();
  public boolean playing();
  public boolean paused();
  public dev.watchparty.e.c requestedMode();
  public long startEpochMillis();
  public long pausedPositionMillis();
  public int revision();
  public java.lang.String rawSource();
  public java.lang.String ownerInstallId();
  public long leaseExpiresAtMillis();
}
```

## 3) Konfigurationen
### Dateiliste
```text
assets/watchparty/lang/en_us.json
fabric.mod.json
watchparty.mixins.json
```

### `fabric.mod.json`
```json
{
  "schemaVersion": 1,
  "id": "watchparty",
  "version": "0.7.8",
  "name": "WatchParty",
  "description": "A clientsided minecraft mod for hosting watch parties with your friends.",
  "authors": [
    "Chaotischer"
  ],
  "icon": "assets/watchparty/icon.png",
  "contact": {
    "homepage": "https://example.invalid/watchparty",
    "sources": "https://example.invalid/watchparty"
  },
  "license": "ARR",
  "environment": "client",
  "entrypoints": {
    "client": [
      "dev.watchparty.WatchPartyClient"
    ]
  },
  "mixins": [
    "watchparty.mixins.json"
  ],
  "depends": {
    "fabricloader": ">=0.16.10",
    "minecraft": ">=1.21.5 <=1.21.11",
    "java": ">=21",
    "fabric-api": "*",
    "mcef": ">=2.1.6-1.21.5"
  },
  "suggests": {
    "modmenu": "*",
    "cloth-config": "*"
  }
}
```

### `watchparty.mixins.json`
```json
{
  "required": true,
  "package": "dev.watchparty.mixin.client",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "WorldRendererMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

### Sprachdatei `assets/watchparty/lang/en_us.json`
```json
{
  "category.watchparty": "WatchParty",
  "key.watchparty.remote": "Open WatchParty Remote",
  "watchparty.command.reload": "WatchParty config reloaded."
}
```

## 4) Assets
```text
assets/watchparty/icon.png
```

## 5) Dependencies & Libraries (identifiziert)
- **Declared Mod-Dependencies** (aus `fabric.mod.json -> depends`):
  - `fabricloader`: `>=0.16.10`
  - `minecraft`: `>=1.21.5 <=1.21.11`
  - `java`: `>=21`
  - `fabric-api`: `*`
  - `mcef`: `>=2.1.6-1.21.5`
- **Suggested Mods** (aus `fabric.mod.json -> suggests`):
  - `modmenu`: `*`
  - `cloth-config`: `*`
- **Build/Runtime-Hinweise** (aus `META-INF/MANIFEST.MF`):
  - `Manifest-Version: 1.0`
  - `Fabric-Jar-Type: classes`
  - `Fabric-Loom-Mixin-Remap-Type: static`
  - `Fabric-Gradle-Version: 8.14.4`
  - `Fabric-Loom-Version: 1.13.3`
  - `Fabric-Mixin-Compile-Extensions-Version: 0.6.0`
  - `Fabric-Minecraft-Version: 1.21.8`
  - `Fabric-Tiny-Remapper-Version: 0.12.0`
  - `Fabric-Loader-Version: 0.17.3`
  - `Fabric-Mixin-Version: 0.16.5+mixin.0.8.7`
  - `Fabric-Mixin-Group: net.fabricmc`
  - `Fabric-Mapping-Namespace: intermediary`
- **Bundled third-party libraries im JAR**: keine gefunden (keine nested JARs/nativen Binaries).

## 6) Ergebnis vor Löschung
- Die vollständige Dateiliste, Klassenstruktur, Konfigurationen und Assets wurden dokumentiert.
- Danach kann `watchparty.jar` sicher aus dem Projekt entfernt werden, um „Mod in Mod“-Probleme zu vermeiden.
