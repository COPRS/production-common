package esa.s1pdgs.cpoc.disseminator.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * TODO comment
 *
 */
public final class LogPrintWriter extends PrintWriter {
	public static final Writer NULLWRITER = new Writer() {
		@Override
		public final void write(final char[] _cbuf, final int _off, final int _len) throws IOException {
			// do nothing
		}

		@Override
		public final void flush() throws IOException {
			// do nothing
		}

		@Override
		public final void close() throws IOException {
			// do nothing
		}
	};


	private final StringBuilder stringBuilder = new StringBuilder();
	private final Consumer<String> stringConsumer;

	public LogPrintWriter(Consumer<String> stringConsumer) {
		super(NULLWRITER);
		this.stringConsumer = stringConsumer;
	}

	@Override
	public final void write(final int _c) {
		stringBuilder.append(_c);
	}

	@Override
	public final void write(final char[] _buf, final int _off, final int _len) {
		stringBuilder.append(_buf, _off, _len);
	}

	@Override
	public final void write(final String _s, final int _off, final int _len) {
		stringBuilder.append(_s.substring(_off, _off + _len));
	}

	@Override
	public final void flush() {
		if (stringBuilder.length() != 0) {
			stringConsumer.accept(stringBuilder.toString().trim());
			stringBuilder.setLength(0);
		}
	}

	@Override
	public final void println() {
		flush();
	}

	@Override
	public final void close() {
		flush();
	}
}
