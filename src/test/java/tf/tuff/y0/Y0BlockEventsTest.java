package tf.tuff.y0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import tf.tuff.TuffX;

class Y0BlockEventsTest {

	private ServerMock server;
	private TuffX plugin;
	private WorldMock world;

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(TuffX.class);
		world = new WorldMock(Material.DEEPSLATE, -64, 320, 4);
		world.setName("world");
		server.addWorld(world);
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void noReadyPlayersMeansNoneInWorld() {
		assertFalse(plugin.y0Plugin.hasReadyPlayersInWorld(world));
	}

	@Test
	void physicsBelowY0SchedulesNothingWithoutTuffXPlayers() {
		Block block = world.getBlockAt(3, -20, 5);
		// Task ids come from one counter, so the gap between two probes is the number of tasks scheduled
		int before = server.getScheduler().scheduleSyncDelayedTask(plugin, () -> {}, 1L);

		for (int i = 0; i < 50; i++) {
			plugin.y0Plugin.handleBlockPhysics(new BlockPhysicsEvent(block, block.getBlockData()));
			plugin.y0Plugin.handleBlockFromTo(new BlockFromToEvent(block, block));
		}

		int after = server.getScheduler().scheduleSyncDelayedTask(plugin, () -> {}, 1L);
		assertEquals(1, after - before);
	}
}
