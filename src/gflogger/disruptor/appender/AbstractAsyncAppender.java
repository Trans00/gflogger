/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gflogger.disruptor.appender;

import static gflogger.formatter.BufferFormatter.allocate;
import gflogger.Layout;
import gflogger.LogLevel;
import gflogger.disruptor.DLogEntryItem;
import gflogger.helpers.LogLog;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractAsyncAppender implements DAppender {

	// inner thread buffer
	protected final CharBuffer charBuffer;
	protected final ByteBuffer byteBuffer;

	protected LogLevel logLevel = LogLevel.ERROR;
	protected Layout layout;
	protected boolean immediateFlush = false;
	protected int bufferedIOThreshold = 50;
	protected long awaitTimeout = 100;
	protected boolean multichar;

	public AbstractAsyncAppender() {
		// 4M
		this(1 << 22);
	}

	public AbstractAsyncAppender(final int bufferSize) {
		this(bufferSize, false);
	}

	public AbstractAsyncAppender(final int bufferSize, boolean multichar) {
		this.multichar = multichar;
		// unicode char has 2 bytes
		byteBuffer = allocate(multichar ? bufferSize << 1 : bufferSize);
		byteBuffer.clear();
		charBuffer = multichar ? byteBuffer.asCharBuffer() : null;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public boolean isMultichar() {
		return multichar;
	}

	public synchronized void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public void setLayout(final Layout layout) {
		this.layout = layout;
	}

	public void setImmediateFlush(final boolean immediateFlush) {
		this.immediateFlush = immediateFlush;
	}

	public void setBufferedIOThreshold(final int bufferedIOThreshold) {
		this.bufferedIOThreshold = bufferedIOThreshold;
	}

	public void setAwaitTimeout(final long awaitTimeout) {
		this.awaitTimeout = awaitTimeout;
	}

	@Override
	public void onEvent(DLogEntryItem event, long sequence, boolean endOfBatch)
			throws Exception {
		// System.out.println(">" + getName() + " " + sequence + " " + endOfBatch);
		// handle entry that has a log level equals or higher than required

		final LogLevel entryLevel = event.getLogLevel();
		assert entryLevel != null;
		final boolean hasProperLevel = logLevel.compareTo(entryLevel) >= 0;

		if (multichar) {
			final CharBuffer eventBuffer = event.getCharBuffer();
			if (hasProperLevel) {
				eventBuffer.flip();
				final int size = layout.size(event);
				final int position = charBuffer.position();
				final int limit = charBuffer.limit();
				if (position + size >= limit){
					flushBuffer();
					charBuffer.clear();
				}
				formatMessage(event, eventBuffer);
				processCharBuffer();

				if (immediateFlush) {
					flushBuffer();
				}
			}
			eventBuffer.clear();
		} else {
			final ByteBuffer eventBuffer = event.getBuffer();
			if (hasProperLevel) {
				eventBuffer.flip();
				final int size = layout.size(event);
				final int position = byteBuffer.position();
				final int limit = byteBuffer.limit();
				if (position + size >= limit){
					flushBuffer();
					byteBuffer.clear();
				}
				formatMessage(event, eventBuffer);

				if (immediateFlush) {
					flushBuffer();
				}
			}
			eventBuffer.clear();
		}
	}

	@Override
	public void flush() {
		try{
			flushBuffer();
		} catch (RuntimeException e){
			LogLog.error("[" + Thread.currentThread().getName() +
				"] exception at " + getName() + " - " + e.getMessage(), e);
		}
	}

	protected void processCharBuffer() {
		// empty

	}

	protected void flushBuffer(){
		// empty
	}

	protected void formatMessage(DLogEntryItem entry, final ByteBuffer buffer) {
		final int position = buffer.position();
		final int limit = buffer.limit();

		try {
			layout.format(byteBuffer, entry);
		} catch (RuntimeException e){
			LogLog.error("[" + Thread.currentThread().getName()
				+ "] exception at " + getName() + " pos: " +
				position + ", limit:" + limit +
				" - " + e.getMessage(), e);
		} finally {
			buffer.position(position);
			buffer.limit(limit);
		}
	}

	protected void formatMessage(DLogEntryItem entry, final CharBuffer buffer) {
		final int position = buffer.position();
		final int limit = buffer.limit();

		try {
			layout.format(charBuffer, entry);
		} catch (RuntimeException e){
			LogLog.error("[" + Thread.currentThread().getName()
					+ "] exception at " + getName() + " pos: " +
					position + ", limit:" + limit +
					" - " + e.getMessage(), e);
		} finally {
			buffer.position(position);
			buffer.limit(limit);
		}
	}


	protected abstract String getName();

	@Override
	public void onStart() {
		LogLog.debug("[" + Thread.currentThread().getName() + "] " +
			getName() + " is starting");
	}

	@Override
	public void onShutdown() {
		LogLog.debug("[" + Thread.currentThread().getName() + "] " +
			getName() + " is about to shutdown");
		immediateFlush = true;
		flushBuffer();
	}
}
