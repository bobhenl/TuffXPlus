package tf.tuff.y0;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import tf.tuff.TuffX;

class Y0LegacyGatingTest {

	private ServerMock server;
	private TuffX plugin;
	private WorldMock world;
	private final Map<UUID, Integer> protocols = new HashMap<>();

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(TuffX.class);
		world = new WorldMock(Material.DEEPSLATE, -64, 320, 4);
		world.setName("world");
		server.addWorld(world);
		plugin.y0Plugin.protocolLookup = id -> protocols.getOrDefault(id, -1);
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	private PlayerMock player(String name, int protocol) {
		PlayerMock p = new PlayerMock(server, name);
		p.setLocation(new Location(world, 0, 64, 0));
		protocols.put(p.getUniqueId(), protocol);
		return p;
	}

	private void sendReady2(PlayerMock p) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream d = new DataOutputStream(out);
		d.writeInt(0);
		d.writeInt(0);
		d.writeInt(0);
		byte[] sub = "ready2".getBytes(StandardCharsets.UTF_8);
		d.writeByte(sub.length);
		d.write(sub);
		try {
			plugin.y0Plugin.handlePacket(p, out.toByteArray());
		} catch (RuntimeException e) {
			// mock server lacks parts of the chunk/messaging path; readiness is decided before that
		}
	}

	@Test
	void onlyKnownPre117ProtocolsAreAllowed() {
		assertTrue(plugin.y0Plugin.isClientAllowed(player("a", 47)));
		assertTrue(plugin.y0Plugin.isClientAllowed(player("b", 754)));
		assertFalse(plugin.y0Plugin.isClientAllowed(player("c", 755)));
		assertFalse(plugin.y0Plugin.isClientAllowed(player("d", 775)));
		assertFalse(plugin.y0Plugin.isClientAllowed(player("e", -1)));
	}

	@Test
	void modernHandshakeIsIgnored() throws IOException {
		PlayerMock modern = player("modern", 775);
		sendReady2(modern);
		assertFalse(plugin.y0Plugin.isReady(modern.getUniqueId()));
		assertFalse(plugin.y0Plugin.hasReadyPlayersInWorld(world));
	}

	@Test
	void legacyHandshakeIsAccepted() throws IOException {
		PlayerMock legacy = player("legacy", 340);
		sendReady2(legacy);
		assertTrue(plugin.y0Plugin.isReady(legacy.getUniqueId()));
	}
}
