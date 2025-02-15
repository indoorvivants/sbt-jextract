import myscalalib_bindings.*;
import java.nio.file.Paths;
import java.lang.foreign.*;

public class Main {
	public static void main(String[] args) {
		// Load the dynamic library
		String dylibPath = System.getenv("SCALA_NATIVE_LIB");
    
		if (dylibPath == null) {
			System.err.println("First argument has to be path to dynamic library");
			System.exit(1);
		}
		System.load(Paths.get(dylibPath).toAbsolutePath().toString());

		// Run exported functions
		try (Arena arena = Arena.ofConfined()) {
			var config = myscalalib_config.allocate(arena);

			myscalalib_config.label(config, arena.allocateFrom("First test"));
			myscalalib_config.op(config, interface_h.ADD());
			interface_h.myscalalib_run(config, 25.0f, 150.0f);

			myscalalib_config.label(config, arena.allocateFrom("Second"));
			myscalalib_config.op(config, interface_h.MULTIPLY());
			interface_h.myscalalib_run(config, 50.0f, 10.0f);
		}
	}
}
