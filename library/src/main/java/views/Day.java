package views;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.joda.time.DateTime;

/**
 * Created by rahul on 03/12/15.
 */
public class Day implements Parcelable {
    int year;
    int month;
    String day;
    int date;
    String monthName;

    public Day(DateTime dateTime) {
        year = dateTime.getYear();
        month = dateTime.getMonthOfYear();
        monthName = dateTime.monthOfYear().getAsText();
        day = dateTime.dayOfWeek().getAsText();
        date = dateTime.getDayOfMonth();
    }

    public Day(int date, int month, int year) {
        this.date = date;
        this.month = month;
        this.year = year;
    }

    public Day() {
        DateTime dateTime = new DateTime();
        year = dateTime.getYear();
        month = dateTime.getMonthOfYear();
        monthName = dateTime.monthOfYear().getAsText();
        day = dateTime.dayOfWeek().getAsText();
        date = dateTime.getDayOfMonth();
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public int getDate() {
        return date;
    }

    public String getMonthName() {
        return monthName;
    }

    public DateTime toDateTime() {
        if (date == 0 || month == 0 || year == 0) {
            return new DateTime();
        }

        return new DateTime(year, month, date, 0, 0, 0);
    }

    @Override
    public String toString() {
        return String.format("Date: %s, Day: %s, Month: %s, Year: %s", date, day, month, year);
    }

    @Override
    public boolean equals(Object object) {
        if (this == null || object == null || !(object instanceof Day)) {
            return false;
        }

        Day other = (Day) object;
        if (year == other.getYear()) {
            if (month == other.getMonth()) {
                if (date == other.getDate()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toFormattedString() {
        if (isEmpty()) {
            return null;
        }
        return String.format("%s %s, %s", monthName, date, year);
    }

    public boolean isEmpty() {
        if (date == 0 && month == 0 && year == 0) {
            return true;
        }
        if (TextUtils.isEmpty(monthName) || TextUtils.isEmpty(day)) {
            return true;
        }

        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.year);
        dest.writeInt(this.month);
        dest.writeString(this.day);
        dest.writeInt(this.date);
        dest.writeString(this.monthName);
    }

    protected Day(Parcel in) {
        this.year = in.readInt();
        this.month = in.readInt();
        this.day = in.readString();
        this.date = in.readInt();
        this.monthName = in.readString();
    }

    public static final Creator<Day> CREATOR = new Creator<Day>() {
        public Day createFromParcel(Parcel source) {
            return new Day(source);
        }

        public Day[] newArray(int size) {
            return new Day[size];
        }
    };
}
