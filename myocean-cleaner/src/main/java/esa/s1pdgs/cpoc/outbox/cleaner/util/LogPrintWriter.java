package esa.s1pdgs.cpoc.outbox.cleaner.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Consumer;

public final class LogPrintWriter extends PrintWriter {

	private final StringBuilder stringBuilder = new StringBuilder();
	private final Consumer<String> stringConsumer;

	// --------------------------------------------------------------------------

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

	public LogPrintWriter(Consumer<String> stringConsumer) {
		super(NULLWRITER);
		this.stringConsumer = stringConsumer;
	}

	// --------------------------------------------------------------------------

	@Override
	public final void write(final int _c) {
		this.stringBuilder.append(_c);
	}

	@Override
	public final void write(final char[] _buf, final int _off, final int _len) {
		this.stringBuilder.append(_buf, _off, _len);
	}

	@Override
	public final void write(final String _s, final int _off, final int _len) {
		this.stringBuilder.append(_s.substring(_off, _off + _len));
	}

	@Override
	public final void flush() {
		if (this.stringBuilder.length() != 0) {
			this.stringConsumer.accept(this.stringBuilder.toString().trim());
			this.stringBuilder.setLength(0);
		}
	}

	@Override
	public final void println() {
		this.flush();
	}

	@Override
	public final void close() {
		this.flush();
	}

}
