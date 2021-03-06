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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Test de l'utilitaitre de manipulation des dates.
 *
 * @author pchretien
 */
@RunWith(JUnitPlatform.class)
public final class DateUtilTest {

	@Test
	public void testNewDate() {
		final Date date = DateUtil.newDate();
		final Date dateTime = DateUtil.newDateTime();

		assertEquals(date, new DateBuilder(dateTime).build());
	}

	private static void assertEqualsWithChgtHeure(final long expected, final long actual) {
		long gap = expected - actual;
		if (gap != 0) {
			gap = Math.abs(gap) - 3600 * 1000;
		}
		assertEquals(0L, gap);

	}

	//28 jours plus tard
	@Test
	public void testAdd28Days() {
		final Date now = DateUtil.newDate();
		final Date date2 = new DateBuilder(now).addDays(28).toDateTime();
		final long elapsed = date2.getTime() - now.getTime();
		final long expected = 28L * 24 * 3600 * 1000;
		assertEqualsWithChgtHeure(expected, elapsed);
	}

	@Test
	public void testAdd28Weeks() {
		final Date now = DateUtil.newDate();
		final Date date2 = new DateBuilder(now).addWeeks(28).toDateTime();
		final long elapsed = date2.getTime() - now.getTime();
		final long expected = 28L * 7 * 24 * 3600 * 1000;
		//Le résultat est proche à plus ou moins une heure (à cause du chgt d'heure)
		assertEqualsWithChgtHeure(expected, elapsed);
	}

	@Test
	public void testAdd68Secondes() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).addSeconds(68).toDateTime();
		final long elapsed = date2.getTime() - now.getTime();
		final long expected = 68L * 1000;
		Assertions.assertEquals(expected, elapsed);
	}

	@Test
	public void testAdd68Minutes() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).addMinutes(68).toDateTime();
		final long elapsed = date2.getTime() - now.getTime();
		final long expected = 68L * 60 * 1000;
		assertEquals(expected, elapsed);
	}

	@Test
	public void testAdd28Hours() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).addHours(28).toDateTime();
		final long elapsed = date2.getTime() - now.getTime();
		final long expected = 28L * 60 * 60 * 1000;
		assertEquals(expected, elapsed);
	}

	@Test
	public void testAddDaysAddWeeks() {
		final Date now = new Date();
		final Date date2 = new DateBuilder(now).addWeeks(4).toDateTime();
		final Date date3 = new DateBuilder(now).addDays(4 * 7).toDateTime();
		assertEquals(date2, date3);
	}

	@Test
	public void testAddYearsAddMonths() {
		final Date now = new Date();
		final Date date2 = new DateBuilder(now).addMonths(2 * 12).toDateTime();
		final Date date3 = new DateBuilder(now).addYears(2).toDateTime();
		assertEquals(date2, date3);
	}

	//Multi add
	@Test
	public void testMultiAdd() {
		final Date now = new Date();
		final Date date2 = new DateBuilder(now).addDays(28).addYears(2).build();
		final Date date3 = new DateBuilder(now).addDays(28).addMonths(2 * 12).build();
		assertEquals(date2, date3);
	}

	//Date
	@Test
	public void testMultiAdd2() {
		final Date now = new Date();
		final Date date = new DateBuilder(now).addDays(28).addYears(2).addMonths(2).toDateTime();
		final Date date2 = new DateBuilder(now).addDays(28).addYears(2).addMonths(2).build();
		Assertions.assertNotSame(date, date2);
		final Date date3 = new DateBuilder(date).build();
		assertEquals(date2, date3);
		assertEquals(0, date3.getHours());
		assertEquals(0, date3.getMinutes());
		assertEquals(0, date3.getSeconds());
		assertEquals(0, date3.getSeconds());
	}

	//Date : on vérifie que la date est bien positionnée à Minuit.
	@Test
	public void testDate() {
		final Date now = new Date();
		final Date date = new DateBuilder(now).addDays(28).addYears(2).addMonths(2).build();
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, calendar.get(Calendar.MINUTE));
		assertEquals(0, calendar.get(Calendar.SECOND));
		assertEquals(0, calendar.get(Calendar.MILLISECOND));
	}

	@Test
	public void testDaysBetween() {
		final Date today = DateUtil.newDate();
		final Date date2 = new DateBuilder(today).addDays(48).toDateTime();
		final int days = DateUtil.daysBetween(today, date2);
		assertEquals(48, days);
	}

	@Test
	public void testDaysBetweenCEST2CET() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(2011, 2, 1, 0, 0, 0);
		final Date startDate = new DateBuilder(calendar.getTime()).build();
		final Date date2 = new DateBuilder(startDate).addDays(48).build();
		final int days = DateUtil.daysBetween(startDate, date2);
		assertEquals(48, days);
	}

	@Test
	public void testDaysBetweenCET2CEST() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(2011, 8, 20, 0, 0, 0);
		final Date startDate = new DateBuilder(calendar.getTime()).build();
		final Date date2 = new DateBuilder(startDate).addDays(48).build();
		final int days = DateUtil.daysBetween(startDate, date2);
		assertEquals(48, days);
	}

	@Test
	public void testDaysBetweenDateTimeError() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).addMinutes(48).addDays(48).toDateTime();
		assertThrows(IllegalArgumentException.class, () -> {
			final int days = DateUtil.daysBetween(now, date2);
			nop(days);
		});
	}

	private static void nop(final int days) {
		//rien
	}

	@Test
	public void testCompareDateLower() {
		final Date today = DateUtil.newDate();
		final Date date2 = new DateBuilder(today).addDays(20).toDateTime();
		assertTrue(DateUtil.compareDate(today, date2) < 0);
	}

	@Test
	public void testCompareDateGreater() {
		final Date today = DateUtil.newDate();
		final Date date2 = new DateBuilder(today).addDays(-20).toDateTime();
		assertTrue(DateUtil.compareDate(today, date2) > 0);
	}

	@Test
	public void testCompareDateEquals() {
		final Date today = DateUtil.newDate();
		final Date date2 = new DateBuilder(today).toDateTime();
		assertTrue(DateUtil.compareDate(today, date2) == 0);
	}

	@Test
	public void testCompareDateError() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).toDateTime();
		assertThrows(IllegalArgumentException.class, () -> {
			final int compare = DateUtil.compareDate(now, date2);
			nop(compare);
		});
	}

	@Test
	public void testCompareDateTimeLower() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).addMinutes(20).toDateTime();
		assertTrue(DateUtil.compareDateTime(now, date2) < 0);
	}

	@Test
	public void testCompareDateTimeGreater() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).addMinutes(-20).toDateTime();
		assertTrue(DateUtil.compareDateTime(now, date2) > 0);
	}

	@Test
	public void testCompareDateTimeEquals() {
		final Date now = DateUtil.newDateTime();
		final Date date2 = new DateBuilder(now).toDateTime();
		assertTrue(DateUtil.compareDateTime(now, date2) == 0);
	}
}
