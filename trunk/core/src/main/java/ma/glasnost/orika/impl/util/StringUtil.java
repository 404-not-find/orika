/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.impl.util;

abstract class StringUtil {

	public static String toString(String s) {
		return s;
	}

	public static String toString(float f) {
		return Float.toString(f);
	}

	public static String toString(long l) {
		return Long.toString(l);
	}

	public static String toString(byte b) {
		return Byte.toString(b);
	}

	public static String toString(char c) {
		return Character.toString(c);
	}

	public static String toString(double d) {
		return Double.toString(d);
	}

	public static String toString(short s) {
		return Short.toString(s);
	}

	public static String toString(Object object) {
		return object.toString();
	}
}
