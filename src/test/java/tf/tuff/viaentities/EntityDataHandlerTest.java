package tf.tuff.viaentities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class EntityDataHandlerTest {

	public static class TypedPacket {
		public String getType() { return "entity.minecraft.zombie"; }
		public int getId() { return 1; }
	}

	public static class PlainPacket {
		public int id() { return 1; }
	}

	@Test
	void findsTypeAndIdMethods() throws Exception {
		Method[] methods = EntityDataHandler.TYPE_ID_METHODS.get(TypedPacket.class);
		assertEquals(2, methods.length);
		assertEquals("getType", methods[0].getName());
		assertEquals("getId", methods[1].getName());
		assertEquals("entity.minecraft.zombie", methods[0].invoke(new TypedPacket()));
	}

	@Test
	void classWithoutMethodsGetsEmptyResult() {
		assertEquals(0, EntityDataHandler.TYPE_ID_METHODS.get(PlainPacket.class).length);
	}

	@Test
	void lookupIsCachedPerClass() {
		assertSame(EntityDataHandler.TYPE_ID_METHODS.get(TypedPacket.class),
			EntityDataHandler.TYPE_ID_METHODS.get(TypedPacket.class));
	}
}
