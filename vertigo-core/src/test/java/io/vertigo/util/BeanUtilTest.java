/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
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
package io.vertigo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
* @author pchretien
*/
@RunWith(JUnitPlatform.class)
public final class BeanUtilTest {
	public static class Book {
		private String author;

		public String getAuthor() {
			return author;
		}

		public void setAuthor(final String author) {
			this.author = author;
		}

	}

	@Test
	public void testgetAuthor() {
		final Book book = new Book();
		book.setAuthor("Murakami");
		assertEquals("Murakami", BeanUtil.getValue(book, "author"));
	}

	@Test
	public void testFailgetAuthor() {
		final Book book = new Book();
		book.setAuthor("Murakami");
		Assertions.assertThrows(Exception.class,
				() -> BeanUtil.getValue(book, "creator"));
	}

	@Test
	public void testsetAuthor() {
		final Book book = new Book();
		BeanUtil.setValue(book, "author", "Mishima");
		assertEquals("Mishima", BeanUtil.getValue(book, "author"));
	}

	@Test
	public void testFailsetAuthor() {
		final Book book = new Book();
		Assertions.assertThrows(Exception.class,
				() -> BeanUtil.setValue(book, "creator", "Mishima"));
	}
}
