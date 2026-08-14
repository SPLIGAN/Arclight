package io.izzel.arclight.neoforge.mod;

import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.mod.ArclightCommon;
import net.neoforged.fml.ModList;
import net.neoforged.fml.classloading.transformation.TransformingClassLoader;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.ClassReader;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * FML 11 / NeoForge 26.1: uses {@link TransformingClassLoader} under
 * {@code net.neoforged.fml.classloading.transformation} (not {@code cpw.mods.modlauncher}).
 */
public class NeoForgeCommonImpl implements ArclightCommon.Api {

	private static final MethodHandle MH_MAYBE_TRANSFORM;

	static {
		try {
			ClassLoader classLoader = NeoForgeCommonImpl.class.getClassLoader();
			if (!(classLoader instanceof TransformingClassLoader transformingClassLoader)) {
				throw new IllegalStateException(
					"Expected TransformingClassLoader, got " + classLoader.getClass().getName()
				);
			}
			Method maybeTransform = TransformingClassLoader.class.getDeclaredMethod(
				"maybeTransformClassBytes",
				byte[].class,
				String.class,
				String.class
			);
			MH_MAYBE_TRANSFORM = Unsafe.lookup().unreflect(maybeTransform).bindTo(transformingClassLoader);
		} catch (Throwable t) {
			throw new IllegalStateException("Unknown FML classloading version", t);
		}
	}

	@Override
	public byte[] platformRemapClass(byte[] cl) {
		String className = new ClassReader(cl).getClassName();
		try {
			// null context = run full processor set (see TransformingClassLoader.maybeTransformClassBytes)
			return (byte[]) MH_MAYBE_TRANSFORM.invokeExact(cl, className.replace('/', '.'), (String) null);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isModLoaded(String modid) {
		return ModList.get() != null
			? ModList.get().isLoaded(modid)
			: FMLLoader.getCurrent().getLoadingModList().getModFileById(modid) != null;
	}

	@Override
	public <T> Set<T> guavaReachableNodes(Graph<T> graph, T node) {
		return Graphs.reachableNodes(graph, node);
	}
}
