package views.month;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.library.R;

import org.joda.time.DateTime;

import controllers.DatePickerController;
import timber.log.Timber;
import views.Day;

/**
 * Created by rahul on 04/12/15.
 */
public class MonthDateView extends RelativeLayout {

    public interface OnMonthChangedListener {
        void onMonthChanged(int currentMonthOfYear);
    }

    private boolean mNeedsRoomNightsAdjustment = false;

    private MonthView.OnDayClickListener mOnDayClickListener = new MonthView.OnDayClickListener() {
        @Override
        public void onDayClick(MonthView.Mode mode, Day day) {
            if (mode == MonthView.Mode.CHECK_IN) {
                View checkinContainer = findViewById(R.id.checkin_container);
                if (checkinContainer != null) {
                    mCheckinDateView.setText(Integer.toString(day.getDate()));
                    mCheckinDayView.setText(day.getDay());
                    mCheckinMonthView.setText(String.format("%s\'%s", day.getMonthName(), Integer.toString(day.getYear()).substring(2)));
                    mCheckinDay = day;
                }
            } else {
                View checkinContainer = findViewById(R.id.checkout_container);
                if (checkinContainer != null) {
                    mCheckoutDateView.setText(Integer.toString(day.getDate()));
                    mCheckoutDayView.setText(day.getDay());
                    mCheckoutMonthView.setText(String.format("%s\'%s", day.getMonthName(), Integer.toString(day.getYear()).substring(2)));
                    mCheckoutDay = day;
                }
            }
            calculateRoomNights();
        }
    };

    private void calculateRoomNights() {
        MonthView.Mode curMode = monthRecycler.getMode();
        if ((mCheckinDay != null && mCheckoutDay == null) || (mCheckinDay == null && mCheckoutDay != null)) {
            mRoomNights = 1;
        } else if (mCheckinDay != null && mCheckoutDay != null) {
            DateTime checkinDateTime, checkoutDateTime;
            checkinDateTime = mCheckinDay.toDateTime();
            checkoutDateTime = mCheckoutDay.toDateTime();
            if (checkoutDateTime.isAfter(checkinDateTime)) {
                mNeedsRoomNightsAdjustment = true;
                if (checkinDateTime.getYear() == checkoutDateTime.getYear()) {
                    mRoomNights = checkoutDateTime.getDayOfYear() - checkinDateTime.getDayOfYear();
                } else {
                    if (checkinDateTime.getYear() < checkoutDateTime.getYear()) {
                        int daysOffset = 0;
                        int checkoutMonth = checkoutDateTime.getMonthOfYear();
                        int checkinMonth = checkinDateTime.getMonthOfYear();
                        daysOffset += checkinDateTime.dayOfMonth().withMaximumValue().getDayOfMonth() - checkinDateTime.getDayOfMonth();
                        for (int j = checkinMonth + 1; j <= 12; j++) {
                            daysOffset += checkinDateTime.dayOfMonth().withMaximumValue().getDayOfMonth();
                        }
                        for (int i = 1; i < checkoutMonth; i++) {
                            daysOffset += checkoutDateTime.withMonthOfYear(i).dayOfMonth().withMaximumValue().getDayOfMonth();
                        }
                        mRoomNights = daysOffset;
                    }
                }
            } else if (checkoutDateTime.isBefore(checkinDateTime)) {
                mNeedsRoomNightsAdjustment = false;
                if (curMode == MonthView.Mode.CHECK_IN) {
                    monthRecycler.setMode(MonthView.Mode.CHECK_OUT);
                    DateTime temp = checkinDateTime.plusDays(mRoomNights);
                    scrollToDay(new Day(temp));
                    monthRecycler.setMode(MonthView.Mode.CHECK_IN);
                } else {
                    monthRecycler.setMode(MonthView.Mode.CHECK_IN);
                    DateTime temp = checkoutDateTime.minusDays(mRoomNights);
                    scrollToDay(new Day(temp));
                    monthRecycler.setMode(MonthView.Mode.CHECK_OUT);
                }
            }
        }
        Timber.v("RoomNights: %s", mRoomNights);
    }

    private TextView mCheckinDateView, mCheckinDayView, mCheckinMonthView;
    private TextView mCheckoutDateView, mCheckoutDayView, mCheckoutMonthView;
    private TextView mRoomNightsView;

    private int mRoomNights;
    private Day mCheckinDay, mCheckoutDay;

    private MonthRecycler monthRecycler;
    private DatePickerController mDatePickerController;

    public MonthDateView(Context context) {
        this(context, null);
    }

    public MonthDateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthDateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View controllerView = inflater.inflate(R.layout.month_controller_view, this, false);
        controllerView.setId(R.id.month_controller);
        addView(controllerView);

        mCheckinDateView = (TextView) findViewById(R.id.checkin_date);
        mCheckinDayView = (TextView) findViewById(R.id.checkin_day_label);
        mCheckinMonthView = (TextView) findViewById(R.id.checkin_month_label);

        mCheckoutDateView = (TextView) findViewById(R.id.checkout_date);
        mCheckoutDayView = (TextView) findViewById(R.id.checkout_day_label);
        mCheckoutMonthView = (TextView) findViewById(R.id.checkout_month_label);

        mRoomNightsView = (TextView) findViewById(R.id.room_nights_number);

        monthRecycler = new MonthRecycler(context);
        monthRecycler.setId(R.id.month_recycler_view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(BELOW, R.id.month_controller);
        monthRecycler.setLayoutParams(params);
        addView(monthRecycler);

        View checkinView = findViewById(R.id.checkin_container);
        checkinView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                monthRecycler.setMode(MonthView.Mode.CHECK_IN);
            }
        });

        View checkoutView = findViewById(R.id.checkout_container);
        checkoutView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                monthRecycler.setMode(MonthView.Mode.CHECK_OUT);
            }
        });
    }

    public void setDateController(DatePickerController controller) {
        mDatePickerController = controller;
        MonthAdapter monthAdapter = new MonthAdapter(mDatePickerController);
        monthAdapter.setOnDayClickListener(mOnDayClickListener);
        setAdapter(monthAdapter);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof MonthAdapter)) {
            throw new IllegalArgumentException("Adapter should extend MonthAdapter");
        }
        monthRecycler.setAdapter(adapter);
        monthRecycler.scrollToPresent();
    }

    public void setOnMonthChangedListener(OnMonthChangedListener listener) {
        monthRecycler.setOnMonthChangedListener(listener);
    }

    public void scrollToPresent() {
        monthRecycler.scrollToPresent();
    }

    public void scrollToDay(Day day) {
        monthRecycler.scrollToDay(day);
    }

    public View getSelectedDateView() {
        return monthRecycler;
    }

    public static class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

        private static final String TAG = MonthAdapter.class.getSimpleName();

        private MonthView.Mode mMode = MonthView.Mode.CHECK_IN;

        private DatePickerController mController;
        private DateTime mDateTime;
        private int mMonthCount = 12 * 3;
        private MonthView.OnDayClickListener mOnDayClickListener;

        public MonthAdapter(DatePickerController controller) {
            mController = controller;
            mDateTime = mController == null ? new DateTime() : mController.getStartDate().toDateTime();
        }

        public void setOnDayClickListener(MonthView.OnDayClickListener listener) {
            mOnDayClickListener = listener;
        }

        @Override
        public MonthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_view_layout, parent, false);
            view.setLayoutParams(new RecyclerView.LayoutParams(parent.getMeasuredWidth(), RecyclerView.LayoutParams.MATCH_PARENT));
            return new MonthViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MonthViewHolder holder, int position) {
            MonthView monthView = holder.getMonthView();
            monthView.setOnDayClickListener(mOnDayClickListener);
            monthView.setController(mController);
            monthView.setClickable(true);
            monthView.setMode(mMode);

            int presentYear = mDateTime.getYear();
            int actualYear = position / 12 == 0 ? presentYear - 1 : (position / 12 == 1 ? presentYear : presentYear + 1);
            int monthPos = (position) % 12 + 1;
            DateTime dateTime = mDateTime.withYear(actualYear).withMonthOfYear(monthPos).withDayOfWeek(1);
            Timber.v("Pos %s, Year %s, month %s", position, mDateTime.getYear(), mDateTime.withYear(actualYear).withWeekyear(actualYear).withMonthOfYear(monthPos));
            monthView.setStartDate(dateTime);
        }

        @Override
        public int getItemCount() {
            return mMonthCount;
        }

        public int getMonthPositionInAdapter(int monthOfYear, int year) {
            int presentYear = mDateTime.getYear();
            if (year < presentYear) {
                return monthOfYear - 1;
            } else if (year > presentYear) {
                return 24 + monthOfYear - 1;
            } else {
                return 12 + monthOfYear - 1;
            }
        }

        public Day getStartDay() {
            return new Day(mDateTime);
        }

        public DatePickerController getDateController() {
            return mController;
        }

        public void setMode(MonthView.Mode mode) {
            mMode = mode;
            notifyDataSetChanged();
        }

        public MonthView.Mode getMode() {
            return mMode;
        }

        public static class MonthViewHolder extends RecyclerView.ViewHolder {
            MonthView monthView;

            public MonthViewHolder(View view) {
                super(view);
                monthView = (SimpleMonthView) view.findViewById(R.id.month_view);
            }

            public MonthView getMonthView() {
                return monthView;
            }
        }
    }
}
