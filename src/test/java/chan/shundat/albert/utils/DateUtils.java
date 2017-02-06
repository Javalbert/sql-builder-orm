package chan.shundat.albert.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public final class DateUtils {
	public static Date combineDateTime(Date date, Date time) {
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);
		
		Calendar newDateCal = Calendar.getInstance();
		newDateCal.setTime(date);
		newDateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
		newDateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
		newDateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
		newDateCal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));
		
		return newDateCal.getTime();
	}
	
	public static int compareDownTo(int field, Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        switch (field) {
	        case Calendar.MILLISECOND:
	            if (Integer.compare(cal1.get(Calendar.MILLISECOND), cal2.get(Calendar.MILLISECOND)) != 0) {
	                break;
	            }
            case Calendar.SECOND:
                if (Integer.compare(cal1.get(Calendar.SECOND), cal2.get(Calendar.SECOND)) != 0) {
                    break;
                }
            case Calendar.MINUTE:
                if (Integer.compare(cal1.get(Calendar.MINUTE), cal2.get(Calendar.MINUTE)) != 0) {
                    break;
                }
            case Calendar.HOUR_OF_DAY:
                if (Integer.compare(cal1.get(Calendar.HOUR_OF_DAY), cal2.get(Calendar.HOUR_OF_DAY)) != 0) {
                    break;
                }
            case Calendar.DATE:
                if (Integer.compare(cal1.get(Calendar.DATE), cal2.get(Calendar.DATE)) != 0) {
                    break;
                }
            case Calendar.MONTH:
                if (Integer.compare(cal1.get(Calendar.MONTH), cal2.get(Calendar.MONTH)) != 0) {
                    break;
                }
            case Calendar.YEAR:
                if (Integer.compare(cal1.get(Calendar.YEAR), cal2.get(Calendar.YEAR)) != 0) {
                    break;
                }
                return 0;
        }

        return date1.compareTo(date2);
    }
	
	public static int compareDownTo(ChronoField field, LocalDateTime date1, LocalDateTime date2) {
		Objects.requireNonNull(field, "field cannot be null");
		Objects.requireNonNull(date1, "date1 cannot be null");
		Objects.requireNonNull(date2, "date2 cannot be null");
		
        switch (field) {
	        case NANO_OF_SECOND:
	            if (Integer.compare(date1.get(ChronoField.NANO_OF_SECOND), date2.get(ChronoField.NANO_OF_SECOND)) != 0) {
	                break;
	            }
	        case MILLI_OF_SECOND:
	            if (Integer.compare(date1.get(ChronoField.MILLI_OF_SECOND), date2.get(ChronoField.MILLI_OF_SECOND)) != 0) {
	                break;
	            }
            case SECOND_OF_MINUTE:
                if (Integer.compare(date1.get(ChronoField.SECOND_OF_MINUTE), date2.get(ChronoField.SECOND_OF_MINUTE)) != 0) {
                    break;
                }
            case MINUTE_OF_HOUR:
                if (Integer.compare(date1.get(ChronoField.MINUTE_OF_HOUR), date2.get(ChronoField.MINUTE_OF_HOUR)) != 0) {
                    break;
                }
            case HOUR_OF_DAY:
                if (Integer.compare(date1.get(ChronoField.HOUR_OF_DAY), date2.get(ChronoField.HOUR_OF_DAY)) != 0) {
                    break;
                }
            case DAY_OF_MONTH:
                if (Integer.compare(date1.get(ChronoField.DAY_OF_MONTH), date2.get(ChronoField.DAY_OF_MONTH)) != 0) {
                    break;
                }
            case MONTH_OF_YEAR:
                if (Integer.compare(date1.get(ChronoField.MONTH_OF_YEAR), date2.get(ChronoField.MONTH_OF_YEAR)) != 0) {
                    break;
                }
            case YEAR:
                if (Integer.compare(date1.get(ChronoField.YEAR), date2.get(ChronoField.YEAR)) != 0) {
                    break;
                }
                return 0;
            default:
            	break;
        }

        return date1.compareTo(date2);
    }
	
	public static int compareDownTo(ChronoField field, ZonedDateTime date1, ZonedDateTime date2) {
		Objects.requireNonNull(field, "field cannot be null");
		Objects.requireNonNull(date1, "date1 cannot be null");
		Objects.requireNonNull(date2, "date2 cannot be null");
		
		date1 = date1.withZoneSameInstant(ZoneOffset.UTC);
		date2 = date2.withZoneSameInstant(ZoneOffset.UTC);
		
		return compareDownTo(field, date1.toLocalDateTime(), date2.toLocalDateTime());
	}

    public static Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    public static DateFormat getIso8601Format() {
    	return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }
    
    public static Date newDate(int year) {
    	return newDate(year, 1, 1, 0, 0, 0, 0);
    }
    
    public static Date newDate(int year, int month) {
    	return newDate(year, month, 1, 0, 0, 0, 0);
    }
    
    public static Date newDate(int year, int month, int date) {
    	return newDate(year, month, date, 0, 0, 0, 0);
    }
    
    public static Date newDate(int year, int month, int date, int hour) {
    	return newDate(year, month, date, hour, 0, 0, 0);
    }
    
    public static Date newDate(int year, int month, int date, int hour, int minute) {
    	return newDate(year, month, date, hour, minute, 0, 0);
    }
    
    public static Date newDate(int year, int month, int date, int hour, int minute, int second) {
    	return newDate(year, month, date, hour, minute, second, 0);
    }
    
    /**
     * 
     * @param year
     * @param month range is from 1 (January) to 12 (December)
     * @param date
     * @param hour
     * @param minute
     * @param second
     * @param millis
     * @return
     */
    public static Date newDate(int year, int month, int date, int hour, int minute, int second, int millis) {
    	Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DATE, date);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, millis);
		return calendar.getTime();
    }
    
	public static Date truncateTo(Calendar calendar, int field) {
		switch (field) {
			case Calendar.YEAR: calendar.set(Calendar.MONTH, 0);
			case Calendar.MONTH: calendar.set(Calendar.DATE, 1);
			case Calendar.DATE: calendar.set(Calendar.HOUR_OF_DAY, 0);
			case Calendar.HOUR_OF_DAY: calendar.set(Calendar.MINUTE, 0);
			case Calendar.MINUTE: calendar.set(Calendar.SECOND, 0);
			case Calendar.SECOND: calendar.set(Calendar.MILLISECOND, 0); break;
		}
		return calendar.getTime();
	}
	
	public static Date truncateTo(Date date, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return truncateTo(calendar, field);
	}
	
	public DateUtils() {}
}