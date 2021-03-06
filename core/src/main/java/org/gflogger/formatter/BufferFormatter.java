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

package org.gflogger.formatter;

import java.nio.*;

import org.gflogger.util.DirectBufferUtils;
import sun.misc.FloatingDecimal;

/**
 * BufferFormatter
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class BufferFormatter {

	private static final boolean USE_DIRECT_BUFFER =
			Boolean.parseBoolean( System.getProperty( "gflogger.direct", "true" ) );

	public static ByteBuffer allocate(final int capacity){
		return USE_DIRECT_BUFFER ?
			ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()) :
			ByteBuffer.allocate(capacity);
	}

	public static void purge(Buffer buffer){
		//TODO RC: move .allocate() to DirectBufferUtils also?
		DirectBufferUtils.releaseBuffer( buffer );
	}

	public static int roundUpNextPower2(int x) {
		// HD, Figure 3-3
		x = x - 1;
		x = x | (x >> 1);
		x = x | (x >> 2);
		x = x | (x >> 4);
		x = x | (x >> 8);
		x = x | (x >>16);
		return x + 1;
	}

	public static ByteBuffer append1(final ByteBuffer buffer, CharSequence s){
		final int length = s != null ? s.length() : 0;
		buffer.put((byte) length);
		if (length == 0) return buffer;
		return append(buffer, s, 0, length);
	}

	public static ByteBuffer append(final ByteBuffer buffer, CharSequence s){
		return append(buffer, s, 0, s != null ? s.length() : 0);
	}

	public static ByteBuffer append(final ByteBuffer buffer, CharSequence s, int start, int end){
		if (s == null){
			assert !(buffer.remaining() < 4);
			return buffer.put((byte) 'n').put((byte) 'u').put((byte) 'l').put((byte) 'l');
		}
		assert !(buffer.remaining() < (end - start));
		for(int i = start; i < end; i++){
			buffer.put((byte) s.charAt(i));
		}
		return buffer;
	}

	public static ByteBuffer append(final ByteBuffer buffer, boolean b){
		if (b){
			assert !(buffer.remaining() < 4);
			return buffer.put((byte) 't').put((byte) 'r').put((byte) 'u').put((byte) 'e');
		}
		assert !(buffer.remaining() < 5);
		return buffer.put((byte) 'f').put((byte) 'a').put((byte) 'l').put((byte) 's').put((byte) 'e');
	}

	public static CharBuffer append(final CharBuffer buffer, boolean b){
		if (b){
			assert !(buffer.remaining() < 4);
			return buffer.put('t').put('r').put('u').put('e');
		}
		assert !(buffer.remaining() < 5);
		return buffer.put('f').put('a').put('l').put('s').put('e');
	}

	public static CharBuffer append(final CharBuffer buffer, CharSequence s){
		return append(buffer, s, 0, s != null ? s.length() : 0);
	}

	public static CharBuffer append(final CharBuffer buffer, CharSequence s, int start, int end){
		if (s != null){
			assert !(buffer.remaining() < (end - start));
			for(int i = start; i < end; i++){
				buffer.put(s.charAt(i));
			}
			return buffer;
		}
		assert !(buffer.remaining() < 4);
		return buffer.put('n').put('u').put('l').put('l');
	}

	public static CharBuffer append(final CharBuffer buffer, byte b) {
		char sign = 0;
		int i = b;
		int size = 0;
		if (i < 0) {
			sign = '-';
			i = -i;
			size++;
		}
		size += i > 100 ? 3 : i > 10 ? 2 : 1;
		assert !(buffer.remaining() < size);
		int j = i;
		if (sign != 0){
			buffer.put(sign);
		}
		// positive byte values is in 0 .. 128
		if (i >= 100){
			buffer.put(DIGIT_ONES[1]);
			i -= 100;
		}

		if (i >= 10 || j >= 100){
			buffer.put(DIGIT_TENS[i]);
		}

		buffer.put(DIGIT_ONES[i]);

		return buffer;
	}

	public static CharBuffer append(final CharBuffer buffer, int i) {
		if (i == Integer.MIN_VALUE) {
		 // uses java.lang.Integer string constant of MIN_VALUE
			return append(buffer, Integer.toString(i));
		}

		put(buffer, i);
		return buffer;
	}

	public static ByteBuffer append(final ByteBuffer buffer, char i) {
		buffer.put((byte) i);
		return buffer;
	}

	public static ByteBuffer append(final ByteBuffer buffer, int i) {
		if (i == Integer.MIN_VALUE) {
		 // uses java.lang.Integer string constant of MIN_VALUE
			return append(buffer, Integer.toString(i));
		}

		put(buffer, i);
		return buffer;
	}

	public static ByteBuffer append(final ByteBuffer buffer, long i) {
		if (i == Long.MIN_VALUE){
			// uses java.lang.Long string constant of MIN_VALUE
			return append(buffer, Long.toString(i));
		}
		put(buffer, i);
		return buffer;
	}

	public static CharBuffer append(final CharBuffer buffer, long i) {
		if (i == Long.MIN_VALUE){
			// uses java.lang.Long string constant of MIN_VALUE
			return append(buffer, Long.toString(i));
		}
		put(buffer, i);
		return buffer;
	}


	public static ByteBuffer append(final ByteBuffer buffer, double i, int precision) {
		put(buffer, i, precision < 0 ? 8 : precision, true);
		return buffer;
	}

	public static CharBuffer append(final CharBuffer buffer, double i, int precision) {
		put(buffer, i, precision < 0 ? 8 : precision, true);
		return buffer;
	}

	public static ByteBuffer append(final ByteBuffer buffer, double v) {
		put(buffer, v);
		return buffer;
	}

	public static CharBuffer append(final CharBuffer buffer, double v) {
		put(buffer, v);
		return buffer;
	}

	public final static long[] LONG_SIZE_TABLE = {
		10L,
		100L,
		1000L,
		10000L,
		100000L,
		1000000L,
		10000000L,
		100000000L,
		1000000000L,
		10000000000L,
		100000000000L,
		1000000000000L,
		10000000000000L,
		100000000000000L,
		1000000000000000L,
		10000000000000000L,
		100000000000000000L,
		1000000000000000000L,
		Long.MAX_VALUE};

	public final static char [] DIGIT_TENS = {
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	} ;

	public final static char [] DIGIT_ONES = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	} ;

	public final static byte[] BDIGIT_TENS = {
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	} ;

	public final static byte[] BDIGIT_ONES = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	} ;

	/**
	 * All possible chars for representing a number as a String
	 */
	public final static char[] DIGITS = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	};

	public final static byte[] BDIGITS = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	};

	// I use the "invariant division by multiplication" trick to
	// accelerate Integer.toString.  In particular we want to
	// avoid division by 10.
	//
	// The "trick" has roughly the same performance characteristics
	// as the "classic" Integer.toString code on a non-JIT VM.
	// The trick avoids .rem and .div calls but has a longer code
	// path and is thus dominated by dispatch overhead.  In the
	// JIT case the dispatch overhead doesn't exist and the
	// "trick" is considerably faster than the classic code.
	//
	// TODO-FIXME: convert (x * 52429) into the equiv shift-add
	// sequence.
	//
	// RE:  Division by Invariant Integers using Multiplication
	//	  T Gralund, P Montgomery
	//	  ACM PLDI 1994
	//

	// based on java.lang.Integer.getChars(int i, int index, char[] buf)
	private static void put(final CharBuffer buffer, int i) {
		int size = numberOfDigits( i );

		assert !(buffer.remaining() < size);

		if (i < 0) {
			buffer.put('-');
			size--;
			i = -i;
		}

		int q, r;
		int charPos = size;

		int oldPos = buffer.position();

		// Generate two digits per iteration
		while (i >= 65536) {
			q = i / 100;
		// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			putAt(buffer, oldPos + (--charPos), DIGIT_ONES[r]);
			putAt(buffer, oldPos + (--charPos), DIGIT_TENS[r]);
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i <= 65536, i);
		for (;;) {
			// 52429 = (1 << 15) + (1 << 14) + (1 << 11) + (1 << 10) + (1 << 7) + (1 << 6) + (1 << 3) + (1 << 2) + 1
			// 52429 = 32768 + 16384 + 2048 + 1024 + 128 + 64 + 8 + 4 + 1
			/*/
			q = ((i << 15) + (i << 14) + (i << 11) + (i << 10) + (i << 7) + (i << 6) + (i << 3) + (i << 2) + i) >> (16 + 3);
			/*/
			q = (i * 52429) >>> (16+3);
			//*/
			r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
			putAt(buffer, oldPos + (--charPos), DIGITS[r]);
			i = q;
			if (i == 0) break;
		}

		buffer.position(oldPos + size);
	}

	// based on java.lang.Integer.getChars(int i, int index, char[] buf)
	private static void put(final ByteBuffer buffer, int i) {
		int size = numberOfDigits( i );

		assert !(buffer.remaining() < size);

		int q, r;
		int charPos = size;

		int oldPos = buffer.position();

		char sign = 0;

		if (i < 0) {
			sign = '-';
			i = -i;
		}

		// Generate two digits per iteration
		while (i >= 65536) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			buffer.put( oldPos + (--charPos), (byte)DIGIT_ONES[r]);
			buffer.put( oldPos + (--charPos), (byte)DIGIT_TENS[r]);
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i <= 65536, i);
		for (;;) {
			// 52429 = (1 << 15) + (1 << 14) + (1 << 11) + (1 << 10) + (1 << 7) + (1 << 6) + (1 << 3) + (1 << 2) + 1
			// 52429 = 32768 + 16384 + 2048 + 1024 + 128 + 64 + 8 + 4 + 1
			/*/
			q = ((i << 15) + (i << 14) + (i << 11) + (i << 10) + (i << 7) + (i << 6) + (i << 3) + (i << 2) + i) >> (16 + 3);
			/*/
			q = (i * 52429) >>> (16+3);
			//*/
			r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
			buffer.put( oldPos + (--charPos), (byte)DIGITS[r]);
			i = q;
			if (i == 0) break;
		}

		if (sign != 0) {
			buffer.put( oldPos + (--charPos), (byte)sign);
		}

		buffer.position(oldPos + size);
	}

	private final static long[] LONG_POWERS_OF_TEN = {
			1L,
			10L,
			100L,
			1000L,
			10000L,
			100000L,
			1000000L,
			10000000L,
			100000000L,
			1000000000L,
			10000000000L,
			100000000000L,
			1000000000000L,
			10000000000000L,
			100000000000000L,
			1000000000000000L,
			10000000000000000L,
			100000000000000000L,
			1000000000000000000L
	};
	/**
	 * @return for given positive long x, returns the number of decimal digits required
	 *         to represent value of x.
	 */
	public static int numberOfDigits(final long x) {
		if( x < 0 ) {
			if( x == Long.MIN_VALUE ) {
				// Life is hard: -Long.MIN_VALUE == Long.MIN_VALUE
				return 1 + LONG_POWERS_OF_TEN.length;
			} else {
				return 1 + numberOfDigits( -x );
			}
		}
		for( int pow = 0; pow < LONG_POWERS_OF_TEN.length; pow++ ) {
			final long tenPower = LONG_POWERS_OF_TEN[pow];
			if( x < tenPower ) {
				return Math.max( pow, 1 );
			}
		}
		return LONG_POWERS_OF_TEN.length;
	}

	public final static int[] INT_POWERS_OF_TEN = {
			1,
			10,
			100,
			1000,
			10000,
			100000,
			1000000,
			10000000,
			100000000,
			1000000000
	};

	/**
	 * @return for given positive int x, returns the number of decimal digits required
	 *         to represent value of x.
	 */
	public static int numberOfDigits(final int x) {
		if( x < 0 ) {
			if( x == Integer.MIN_VALUE ) {
				// Life is hard: -Long.MIN_VALUE == Long.MIN_VALUE
				return 1 + INT_POWERS_OF_TEN.length;
			} else {
				return 1 + numberOfDigits( -x );
			}
		}
		for( int pow = 0; pow < INT_POWERS_OF_TEN.length; pow++ ) {
			final long tenPower = INT_POWERS_OF_TEN[pow];
			if( x < tenPower ) {
				return Math.max( pow, 1 );
			}
		}
		return INT_POWERS_OF_TEN.length;
	}

	// based on java.lang.Long.getChars(int i, int index, char[] buf)
	private static void put(final CharBuffer buffer, long i) {
		final int size = numberOfDigits( i );

		assert !(buffer.remaining() < size);

		int oldPos = buffer.position();

		long q;
		int r;
		int charPos = size;
		char sign = 0;

		if (i < 0) {
			sign = '-';
			i = -i;
		}

		// Get 2 digits/iteration using longs until quotient fits into an int
		while (i > Integer.MAX_VALUE) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
			i = q;
			putAt(buffer, oldPos + (--charPos), DIGIT_ONES[r]);
			putAt(buffer, oldPos + (--charPos), DIGIT_TENS[r]);
		}

		// Get 2 digits/iteration using ints
		int q2;
		int i2 = (int)i;
		while (i2 >= 65536) {
			q2 = i2 / 100;
			// really: r = i2 - (q * 100);
			r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
			i2 = q2;
			putAt(buffer, oldPos + (--charPos), DIGIT_ONES[r]);
			putAt(buffer, oldPos + (--charPos), DIGIT_TENS[r]);
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i2 <= 65536, i2);
		for (;;) {
			// 52429 = (1 << 15) + (1 << 14) + (1 << 11) + (1 << 10) + (1 << 7) + (1 << 6) + (1 << 3) + (1 << 2) + 1
			/*/
			q2 = ((i2 << 15) + (i2 << 14) + (i2 << 11) + (i2 << 10) + (i2 << 7) + (i2 << 6) + (i2 << 3) + (i2 << 2) + i2) >> (16 + 3);
			/*/
			q2 = (i2 * 52429) >>> (16+3);
			//*/
			r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
			putAt(buffer, oldPos + (--charPos), DIGITS[r]);
			i2 = q2;
			if (i2 == 0) break;
		}
		if (sign != 0) {
			putAt(buffer, oldPos + (--charPos), sign);
		}
		buffer.position(oldPos + size);
	}

	// based on java.lang.Long.getChars(int i, int index, char[] buf)
	private static void put(final ByteBuffer buffer, long l) {
		final int size = numberOfDigits( l );

		assert !(buffer.remaining() < size);

		int oldPos = buffer.position();

		long q;
		int r;
		int charPos = size;
		char sign = 0;

		if (l < 0) {
			sign = '-';
			l = -l;
		}

		// Get 2 digits/iteration using longs until quotient fits into an int
		while (l > Integer.MAX_VALUE) {
			q = l / 100;
			// really: r = i - (q * 100);
			r = (int)(l - ((q << 6) + (q << 5) + (q << 2)));
			l = q;
			buffer.put( oldPos + (--charPos), (byte)DIGIT_ONES[r] );
			buffer.put( oldPos + (--charPos), (byte)DIGIT_TENS[r] );
		}

		// Get 2 digits/iteration using ints
		int q2;
		int i2 = (int)l;
		while (i2 >= 65536) {
			q2 = i2 / 100;
			// really: r = i2 - (q * 100);
			r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
			i2 = q2;
			buffer.put( oldPos + (--charPos), (byte)DIGIT_ONES[r] );
			buffer.put( oldPos + (--charPos), (byte)DIGIT_TENS[r] );
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i2 <= 65536, i2);
		for (;;) {
			// 52429 = (1 << 15) + (1 << 14) + (1 << 11) + (1 << 10) + (1 << 7) + (1 << 6) + (1 << 3) + (1 << 2) + 1
			/*/
			q2 = ((i2 << 15) + (i2 << 14) + (i2 << 11) + (i2 << 10) + (i2 << 7) + (i2 << 6) + (i2 << 3) + (i2 << 2) + i2) >> (16 + 3);
			/*/
			q2 = (i2 * 52429) >>> (16+3);
			//*/
			r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
			buffer.put(oldPos + (--charPos), (byte)DIGITS[r]);
			i2 = q2;
			if (i2 == 0) break;
		}
		if (sign != 0) {
			buffer.put(oldPos + (--charPos), (byte)sign);
		}
		buffer.position(oldPos + size);
	}

	private static void putAt(final CharBuffer buffer, int pos, char b){
		buffer.position(pos);
		buffer.append(b);
	}

	/**
	 * Bit 63 represents the sign of the floating-point number.
	 *
	 * @see java.lang.Double#doubleToLongBits(double)
	 */
	public final static long	SIGN_MASK		= 0x8000000000000000L;

	/**
	 * Bits 62-52 represent the exponent.
	 *
	 * @see java.lang.Double#doubleToLongBits(double)
	 */
	public final static long	EXP_MASK		= 0x7ff0000000000000L;

	/**
	 * Bits 51-0 represent the significant (sometimes called the mantissa) of
	 * the floating-point number.
	 *
	 * @see java.lang.Double#doubleToLongBits(double)
	 */
	public final static long    MANTISSA_MASK = 0x000fffffffffffffL;

	public static final long	EXP_BIAS		= 1023;

	public static final int		EXP_SHIFT		= 52;

	/**
	 * assumed High-Order bit
	 */
	public static final long	FRACT_HOB		= (1L << EXP_SHIFT);

	/**
	 * exponent of 1.0
	 */
	public static final long	EXP_ONE			= EXP_BIAS << EXP_SHIFT;

	public static final String	INFINITY		= "Infinity";
	public static final String	NAN				= "NaN";
	public static final String	ZERO_DOT_ZERO	= "0.0";

	/**
	 * log2(2^53) = 15.9
	 */
	public static final int		DOUBLE_DIGITS	= 15;

	private static void put(final ByteBuffer buffer, double v) {
		if (Double.isNaN(v)){
			append(buffer, NAN);
			return;
		}
		long bits = Double.doubleToRawLongBits(v);

		boolean isNegative = (bits & SIGN_MASK) != 0;
		if (isNegative){
			// reset sign bit
			bits = bits & ~SIGN_MASK;
			v = Double.longBitsToDouble(bits);
			buffer.put((byte) '-');
		}
		if (v == Double.POSITIVE_INFINITY){
			append(buffer, INFINITY);
			return;
		}
		if (bits == 0){
			append(buffer, ZERO_DOT_ZERO);
			return;
		}

		final int digits = guessFractionDigits( bits );

		put(buffer, v, digits, false);
	}

	static int guessFractionDigits( final long doubleBits ) {
		//RC: here we try to guess digits-after-the-point required to represent this
		// double, but sometimes we fail: like for -1.0000000000000002E15, there it is
		// guessed we need 0 digit, so last meaningful digit (2) is lost

		final long exponent = ((EXP_MASK & doubleBits ) >> EXP_SHIFT) - EXP_BIAS;
		final long significant = MANTISSA_MASK & doubleBits;
		//restore implicit leading bit
		final long mantissa = (1L << (EXP_SHIFT + 1)) | significant;

		long fractBits = mantissa;
		double d2 = Double.longBitsToDouble( EXP_ONE | ( fractBits &~ FRACT_HOB ) );
		int decExp = (int)Math.floor((d2-1.5D)*0.289529654D + 0.176091259 + exponent * 0.301029995663981 );

		// do not handle negative dec exp
		return DOUBLE_DIGITS - ( decExp > 0 ? decExp : 0 );
	}

	private static void put(final ByteBuffer buffer,
	                        double v,
	                        final int digits,
	                        final boolean forceTailZeros) {
		if (Double.isNaN(v)){
			append(buffer, NAN);
			return;
		}

		long bits = Double.doubleToRawLongBits(v);

		boolean isNegative = (bits & SIGN_MASK) != 0;
		if (isNegative){
			// reset sign bit
			bits = bits & ~SIGN_MASK;
			v = Double.longBitsToDouble(bits);
			buffer.put((byte) '-');
		}
		if (v == Double.POSITIVE_INFINITY){
			append(buffer, INFINITY);
			return;
		}
		if (bits == 0){
			append(buffer, ZERO_DOT_ZERO);
			return;
		}

		//scientific notation required -> fallback to JDK
		if ((v > 0 && (v > 1e18 || v < 1e-18)) ||
				(v < 0 && (v < -1e18 || v > -1e-18))){
			append(buffer, toString(v));
			return;
		}

		final long integerPart = (long)v;
		put(buffer, integerPart);
		buffer.put((byte) '.');


		//wrap digits to [0, LONG_POWERS_OF_TEN.length-1] inclusive
		final int realDigits = Math.min(
				Math.max( 0, digits ),
				LONG_POWERS_OF_TEN.length-1
		);
		final long multiplier = LONG_POWERS_OF_TEN[realDigits];

		final long remainderAsLong = (long)((v - integerPart) * multiplier);

		final int oldPos = buffer.position();

		// add leading zeros

		if (remainderAsLong != 0){
			final int remainderDigits = numberOfDigits(remainderAsLong);

			final int leadingZeros = realDigits-remainderDigits;
			for(int i = 0; i < leadingZeros; i++){
				buffer.put((byte) '0');
			}
		}

		put(buffer, remainderAsLong);

		if (remainderAsLong !=0 || forceTailZeros){
			final int pos = buffer.position();
			final int limit = buffer.limit();

			if (pos - oldPos < digits && pos < limit){
				int j = digits - (pos - oldPos);
				j = j < limit - pos ? j : limit - pos;
				for(int i = 0; i < j; i++){
					buffer.put((byte) '0');
				}
			}
		}
	}

	private static void put(final CharBuffer buffer, double v) {
		if (Double.isNaN(v)){
			append(buffer, NAN);
			return;
		}
		long d = Double.doubleToRawLongBits(v);

		boolean isNegative = (d & SIGN_MASK) != 0;
		if (isNegative){
			// reset sign bit
			d = d & ~SIGN_MASK;
			v = Double.longBitsToDouble(d);
			buffer.put('-');
		}
		if (v == Double.POSITIVE_INFINITY){
			append(buffer, INFINITY);
			return;
		}

		if (d == 0){
			append(buffer, ZERO_DOT_ZERO);
			return;
		}

		final int digits = guessFractionDigits( d );

		put(buffer, v, digits, false);
	}

	private static String toString(double v) {
		// All exceptional cases have been covered
		// TODO: this leads to garbage
		final String javaFormatString = FloatingDecimal.toJavaFormatString(v);
		return javaFormatString;
	}

	private static void put(final CharBuffer buffer,
	                        double v,
	                        final int digits,
	                        final boolean forceTailZeros) {
		if (Double.isNaN(v)){
			append(buffer, NAN);
			return;
		}

		long bits = Double.doubleToRawLongBits(v);

		boolean isNegative = (bits & SIGN_MASK) != 0;
		if (isNegative){
			// reset sign bit
			bits = bits & ~SIGN_MASK;
			v = Double.longBitsToDouble(bits);
			buffer.put('-');
		}
		if (v == Double.POSITIVE_INFINITY){
			append(buffer, INFINITY);
			return;
		}
		if (bits == 0){
			append(buffer, ZERO_DOT_ZERO);
			return;
		}

		if ((v > 0 && (v > 1e18 || v < 1e-18)) ||
				(v < 0 && (v < -1e18 || v > -1e-18))){
			append(buffer, toString(v));
			return;
		}

		final long integerPart = (long)v;
		put(buffer, integerPart);
		buffer.put('.');

		//wrap digits to [0, LONG_POWERS_OF_TEN.length-1] inclusive
		final int realDigits = Math.min(
				Math.max( 0, digits ),
				LONG_POWERS_OF_TEN.length-1
		);
		final long multiplier = LONG_POWERS_OF_TEN[realDigits];

		//it's better to use round here, but then we'll be forced to deal specifically
		// with numbers like 0.(9)
		final long remainderAsLong = (long)((v - integerPart) * multiplier);

		int oldPos = buffer.position();

		// add leading zeros
		if (remainderAsLong != 0){
			final int remainderDigits = numberOfDigits(remainderAsLong);

			final int leadingZeros = realDigits-remainderDigits;
			for(int i = 0; i < leadingZeros; i++){
				buffer.put('0');
			}
		}

		put(buffer, remainderAsLong);

		if (remainderAsLong !=0 || forceTailZeros){
			final int pos = buffer.position();
			final int limit = buffer.limit();

			if (pos - oldPos < digits && pos < limit){
				int j = digits - (pos - oldPos);
				j = j < limit - pos ? j : limit - pos;
				for(int i = 0; i < j; i++){
					buffer.put('0');
				}
			}
		}
	}
}
