package com.mde.backend.domain;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeFrame {
	private final LocalDate FirstDate;
	private final LocalDate SecondDate;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd, MMM, yyyy", Locale.ENGLISH);
	
	
	public TimeFrame(int FirstYear, Month FirstMonth, int FirstDay,
			int SecondYear, int SecondDay, Month SecondMonth) {
		this.FirstDate  = LocalDate.of(FirstYear, FirstMonth, FirstDay);
		this.SecondDate  = LocalDate.of(SecondYear, SecondMonth, SecondDay);
	}
	
	@Override
	public String toString() {
		return formatter.format(FirstDate) + "|" + formatter.format(SecondDate);
	}
	public LocalDate getFirstDate() {
		return FirstDate;
	}

	public LocalDate getSecondDate() {
		return SecondDate;
	}

	/*
	 * System.out.println(formatter.format(date)); // prints "05, Jan, 2015"
	date = LocalDate.parse("06, Jan, 2015", formatter);
	System.out.println(date.getDayOfMonth()); // prints "6"
	*/
	 


}
