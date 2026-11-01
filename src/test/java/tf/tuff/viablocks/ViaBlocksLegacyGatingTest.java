package tf.tuff.viablocks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import tf.tuff.TuffX;

class ViaBlocksLegacyGatingTest {

	private ServerMock server;
	private TuffX plugin;
	private ViaBlocksPlugin viaBlocks;
	private final Map<UUID, Integer> protocols = new HashMap<>();

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(TuffX.class);
		viaBlocks = plugin.viaBlocksPlugin;
		viaBlocks.protocolLookup = id -> protocols.getOrDefault(id, -1);
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	private PlayerMock player(String name, int protocol) {
		PlayerMock p = new PlayerMock(server, name);
		protocols.put(p.getUniqueId(), protocol);
		return p;
	}

	@Test
	void onlyKnownPre117ProtocolsAreAllowed() {
		assertTrue(viaBlocks.isClientAllowed(player("eagler18", 47)));
		assertTrue(viaBlocks.isClientAllowed(player("c1122", 340)));
		assertTrue(viaBlocks.isClientAllowed(player("c1165", 754)));
		assertFalse(viaBlocks.isClientAllowed(player("c117", 755)));
		assertFalse(viaBlocks.isClientAllowed(player("c263", 775)));
		assertFalse(viaBlocks.isClientAllowed(player("unknown", -1)));
	}

	@Test
	void modernHandshakeIsIgnored() {
		PlayerMock modern = player("modern", 775);
		try {
			viaBlocks.handlePacket(modern, new byte[0]);
		} catch (be.seeseemelk.mockbukkit.UnimplementedOperationException e) {
			// only reached if the handshake was wrongly accepted and the join path ran
		}
		assertFalse(viaBlocks.isPlayerEnabled(modern));
		assertFalse(viaBlocks.isActive());
	}

	@Test
	void blockEventsDoNothingWithoutLegacyPlayers() {
		WorldMock world = new WorldMock(Material.DEEPSLATE, -64, 320, 4);
		server.addWorld(world);
		Block block = world.getBlockAt(1, -10, 1);
		block.setType(Material.DEEPSLATE);

		viaBlocks.blockListener.handleBlockPhysics(new BlockPhysicsEvent(block, block.getBlockData()));

		assertNull(viaBlocks.blockListener.getRecentChange(viaBlocks.blockListener.packLocation(1, -10, 1)));
	}

	@Test
	void gatingCanBeTurnedOff() {
		plugin.getConfig().set("viablocks.only-legacy-clients", false);
		plugin.saveConfig();
		plugin.reloadTuffX();
		assertTrue(viaBlocks.isClientAllowed(player("modern", 775)));
	}
}
