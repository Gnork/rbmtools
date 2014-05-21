package berlin.iconn.rbm.cl;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class OCL implements AutoCloseable {

	public final CLContext context;
	public final CLDevice device;
	public final CLCommandQueue queue;

	private Map<String, CLKernel> kernels;

	public OCL() {
		this.context = CLContext.create(CLDevice.Type.GPU);
		this.device = this.context.getMaxFlopsDevice();
		//this.queue = this.device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
		this.queue = this.device.createCommandQueue();
	}

	@Override
	public void close() throws Exception {
		this.context.release();
//		System.out.println("Context released");
	}

	/**
	 * Besorge einen geladenen Kernel.
	 *
	 * @param kernelName
	 * @return
	 */
	public CLKernel getKernel(final String kernelName) {
		return this.kernels.get(kernelName).clone();
	}

	private CLProgram loadProgram(final String filename) throws IOException {
		InputStream url = getClass().getClassLoader().getResourceAsStream(filename); 
		return this.context.createProgram(url);
	}

	/**
	 * Läd den .cl Code aus der Datei und kompiliert die dort enthaltenen Kernels.
	 * Anschließend können die Kernels über die getKernel Methode verwendet werden.
	 *
	 * @param filename
	 * @param buildOptions
	 * @throws IOException
	 */
	public void buildProgram(final String filename, final String... buildOptions) throws IOException {
		CLProgram program = loadProgram(filename);
		StringBuilder options = new StringBuilder("");
		for (String option : buildOptions) {
			options.append(option);
			options.append(' ');
		}
		program.build(options.toString(), this.device);
		this.kernels = program.createCLKernels();
	}
}