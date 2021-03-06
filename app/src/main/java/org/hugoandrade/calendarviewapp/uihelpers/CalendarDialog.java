package org.hugoandrade.calendarviewapp.uihelpers;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.hugoandrade.calendarviewapp.R;
import org.hugoandrade.calendarviewapp.data.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/* 커스텀 날짜 다이얼로그 */
/* 커스텀 날짜 다이얼로그 */
public class CalendarDialog {

    @SuppressWarnings("unused")
    private static final String TAG = CalendarDialog.class.getSimpleName();

    private final static Calendar sToday = Calendar.getInstance();

    private static final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final float MIN_OFFSET = 0f;
    private static final float MAX_OFFSET = 0.5f;

    private static final float MIN_ALPHA = 0.5f;
    private static final float MIN_SCALE = 0.8f;

    private final Context mContext;

    private Calendar mSelectedDate = sToday;

    private List<Event> mEventList = new ArrayList<>();
    private OnCalendarDialogListener mListener;

    private AlertDialog mAlertDialog;
    private View mView;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    private Handler mHandler;

    CalendarDialog(Context context) {
        mContext = context;
        mHandler = new Handler();

        buildView();
    }

    public void setSelectedDate(Calendar selectedDate) {
        mSelectedDate = selectedDate;
        mViewPagerAdapter.setSelectedDate(mSelectedDate);
        mViewPager.setCurrentItem(mViewPagerAdapter.initialPageAndDay.first);
    }

    public void setEventList(List<Event> eventList) {
        Log.d("파이어ㅓ", " eventList 확인 : " + eventList);
        mEventList = eventList;
        mViewPagerAdapter.notifyDataSetChanged();
    }

    void setOnCalendarDialogListener(OnCalendarDialogListener listener) {
        mListener = listener;
    }

    public void show() {
        long delayMillis = 100L;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayedShow();
            }
        }, delayMillis);
    }

    /* 날짜 창 빌드*/
    private void buildView() {
        mView = View.inflate(mContext, R.layout.dialog_calendar, null);
        mViewPager = mView.findViewById(R.id.viewPager_calendar);
        // Disable clip to padding
        mViewPager.setClipToPadding(false);
        // set padding manually, the more you set the padding the more you see of prev & next page
        mViewPager.setPadding(160, 0, 160, 0);
        mViewPager.setPageMargin(60);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                updatePager(mViewPager.findViewWithTag(position), 1f - positionOffset);
                updatePager(mViewPager.findViewWithTag(position + 1), positionOffset);
                updatePager(mViewPager.findViewWithTag(position + 2), 0);
                updatePager(mViewPager.findViewWithTag(position - 1), 0);
            }
        });
        mViewPagerAdapter = new ViewPagerAdapter(mSelectedDate, mEventList);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(mViewPagerAdapter.initialPageAndDay.first);

        /* 다른 곳 누르면 닫힘 */
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dismissDialog();
                return false;
            }
        });
        mAlertDialog = new AlertDialog.Builder(mContext).create();
    }

    /* 이거 뭘라나*/
    private void delayedShow() {
        if (mAlertDialog.getWindow() != null)
            mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        mAlertDialog.setCanceledOnTouchOutside(true);
//////////////////////////////////////////////////////////////////////////////////////////////
        mAlertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        //alert.setContentView(view);
        mAlertDialog.show();
        mAlertDialog.setContentView(mView);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = mAlertDialog.getWindow();

        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    private void dismissDialog() {
        mAlertDialog.dismiss();
    }

    private void updatePager(View view, float offset) {
        if (view == null)
            return;

        float adjustedOffset = (1.0f - 0.0f) * (offset - MIN_OFFSET) / (MAX_OFFSET - MIN_OFFSET) + 0.0f;
        adjustedOffset = adjustedOffset > 1f ? 1f : adjustedOffset;
        adjustedOffset = adjustedOffset < 0f ? 0f : adjustedOffset;

        float alpha = adjustedOffset * (1f - MIN_ALPHA) + MIN_ALPHA;
        float scale = adjustedOffset * (1f - MIN_SCALE) + MIN_SCALE;

        view.setAlpha(alpha);
        view.setScaleY(scale);
    }

    /* 날짜 창을 뷰페이저 어댑터로 나열한다.*/
    private class ViewPagerAdapter extends PagerAdapter {

        private static final String DEFAULT_MIN_DATE = "01/01/1992";
        private static final String DEFAULT_MAX_DATE = "01/01/2100";

        private Calendar mMinDate = getCalendarObjectForLocale(DEFAULT_MIN_DATE, Locale.getDefault());
        private Calendar mMaxDate = getCalendarObjectForLocale(DEFAULT_MAX_DATE, Locale.getDefault());

        private Pair<Integer, Calendar> initialPageAndDay;

        private int TOTAL_COUNT;

        ViewPagerAdapter(Calendar selectedDate, List<Event> eventList) {
            mEventList = eventList;

            // Total number of pages (between min and max date)
            TOTAL_COUNT = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(mMaxDate.getTimeInMillis() - mMinDate.getTimeInMillis()));

            int initialPosition = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(selectedDate.getTimeInMillis() - mMinDate.getTimeInMillis()));

            initialPageAndDay = new Pair<>(initialPosition, selectedDate);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {

            final Calendar day = (Calendar) initialPageAndDay.second.clone();
            day.add(Calendar.DAY_OF_MONTH, position - initialPageAndDay.first);

/////////// /* R.layout.pager_calendar_day : 날짜를 클릭했을 때 나오는 창 */
            LayoutInflater inflater = LayoutInflater.from(collection.getContext());
            View view = inflater.inflate(R.layout.pager_calendar_day, collection, false);
            view.setTag(position);

            TextView tvDay = view.findViewById(R.id.tv_calendar_day);
            TextView tvDayOfWeek = view.findViewById(R.id.tv_calendar_day_of_week);
            RecyclerView rvDay = view.findViewById(R.id.rv_calendar_events);
            View rlNoAlerts = view.findViewById(R.id.rl_no_events);
            View fabCreate = view.findViewById(R.id.fab_create_event);

            List<Event> eventList = getCalendarEventsOfDay(day);

            if (diffYMD(day, sToday) == -1) {
                fabCreate.setVisibility(View.INVISIBLE);
                fabCreate.setOnClickListener(null);
            }
            else {
                fabCreate.setVisibility(View.VISIBLE);
                fabCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null)
                            mListener.onCreateEvent(day);
                    }
                });
            }

            tvDay.setText(new SimpleDateFormat("d", Locale.getDefault()).format(day.getTime()));
            tvDayOfWeek.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(day.getTime()));

            rvDay.setLayoutManager(new LinearLayoutManager(collection.getContext(), LinearLayoutManager.VERTICAL, false));
            rvDay.setAdapter(new CalendarEventAdapter(eventList));
            rvDay.setVisibility(eventList.size() == 0? View.GONE : View.VISIBLE);

            rlNoAlerts.setVisibility(eventList.size() == 0? View.VISIBLE : View.GONE);

            collection.addView(view);

            return new ViewHolder(view);
        }

        @Override
        public int getCount() {
            return TOTAL_COUNT;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            final ViewHolder holder = (ViewHolder) object;
            return view == holder.container;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(((ViewHolder) object).container);
        }

        private void setSelectedDate(Calendar selectedDate) {
            int position = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(selectedDate.getTimeInMillis() - mMinDate.getTimeInMillis()));

            initialPageAndDay = new Pair<>(position, selectedDate);
        }

        private class ViewHolder {
            final View container;

            ViewHolder(View container) {
                this.container = container;
            }
        }

        private List<Event> getCalendarEventsOfDay(Calendar day) {
            List<Event> eventList = new ArrayList<>();
            for (Event e : mEventList) {
                if (diffYMD(e.getMarked_Date(), day) == 0)

                    eventList.add(e);
            }
            return eventList;
        }

        private Calendar getCalendarObjectForLocale(String date, Locale locale) {
            Calendar calendar = Calendar.getInstance(locale);
            DateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            if (date == null || date.isEmpty()) {
                return calendar;
            }

            try {
                final Date parsedDate = DATE_FORMATTER.parse(date);
                if (calendar == null)
                    calendar = Calendar.getInstance();
                calendar.setTime(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return calendar;
        }

    }

    /* 날짜 창안의 일정을 어댑터로 나열한다.*/
    private class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.ViewHolder>{

        private final List<Event> mCalendarEvents;

        CalendarEventAdapter(List<Event> events) {
            mCalendarEvents = events;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
/////////////* R.layout.list_item_calendar_event : 날짜를 클릭했을 때 나오는 창에서 일정 item */
            View v = vi.inflate(R.layout.list_item_calendar_event, parent, false);



            v.setLongClickable(true);
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

//                        YMDCalendar day = CalendardayList.get(i);
//                        tvDay.setText(String.valueOf(day.day));
//                        final FrameLinearLayout container = (FrameLinearLayout)viewCalendarList.get(i);

//                            viewCalendarList.get(i).setOnTouchListener(new OnTouchListener() {
//                                @Override
//                                public boolean onTouch(View view, MotionEvent motionEvent) {
//                                    if (isOffsetDay) {
//                                        tvDay.setTextColor(mAttributes.get(Attr.offsetDayTextColor));
//                                    } else {
//                                        tvDay.setTextColor(mAttributes.get(Attr.selectedDayTextColor));
//                                    }
//                                    container.setFrameColor(mAttributes.get(Attr.selectedDayBorderColor));
//                                    container.setBackgroundColor(mAttributes.get(Attr.selectedDayBackgroundColor));
//                                    return false;
//                                }
//                            });

                    /*드래그 리스너*/
                    view.setOnDragListener(new View.OnDragListener() {
                        @Override
                        public boolean onDrag(View view, DragEvent dragEvent) {
//                                    if (isOffsetDay) {
//                                        tvDay.setTextColor(mAttributes.get(Attr.offsetDayTextColor));
//                                    } else {
//                                        tvDay.setTextColor(mAttributes.get(Attr.selectedDayTextColor));
//                                    }
//                                    container.setFrameColor(mAttributes.get(Attr.selectedDayBorderColor));
//                                    container.setBackgroundColor(mAttributes.get(Attr.selectedDayBackgroundColor));

                            //이벤트를 받음

                            switch(dragEvent.getAction()){

                                //드래그가 시작되면
                                case DragEvent.ACTION_DRAG_STARTED:
                                    //클립 설명이 텍스트면
//                                            if(dragEvent.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
//                                                //btn.setText("Drop OK");//버튼의 글자를 바꿈
//                                                return true;
//                                            }else{//인텐트의 경우 이쪽으로 와서 드래그를 받을 수가 없다.
//                                                return false;
//                                            }
                                    dismissDialog();
                                    return true;
                                //드래그가 뷰의 경계안으로 들어오면
                                case DragEvent.ACTION_DRAG_ENTERED:
                                    //btn.setText("Enter");//버튼 글자 바꿈
                                    return true;

                                //드래그가 뷰의 경계밖을 나가면
                                case DragEvent.ACTION_DRAG_EXITED:
                                    //btn.setText("Exit");//버튼 글자 바꿈
                                    return true;

                                //드래그가 드롭되면
                                case DragEvent.ACTION_DROP:
                                    //클립데이터의 값을 가져옴
//                                            String text = dragEvent.getClipData().getItemAt(0).getText().toString();
//                                            Log.d("캘린더","text : " + text);
                                    //btn.setText(text);


                                    return true;

//                                        //드래그 성공 취소 여부에 상관없이 모든뷰에게
//                                        case DragEvent.ACTION_DRAG_ENDED:
//                                            if(dragEvent.getResult()){//드래그 성공시
//                                                Toast.makeText(DragButton.this, "Drag & Drop 완료", 0).show();
//                                            }else{//드래그 실패시
//                                                btn.setText("Target");
//                                            }
//                                            return true;
                            }
                            return true;
                        }
                    });

                    /*드래그 앤드롭*/
                    // Create a new ClipData.Item from the ImageView object's tag
                    ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());

                    // Create a new ClipData using the tag as a label, the plain text MIME type, and
                    // the already-created item. This will create a new ClipDescription object within the
                    // ClipData, and set its MIME type entry to "text/plain"
                    ClipData dragData = new ClipData(
                            (CharSequence) view.getTag(),
                            new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                            item);

                    // Instantiates the drag shadow builder.
                    View.DragShadowBuilder myShadow = new MyDragShadowBuilder(view);

                    // Starts the drag

                    view.startDrag(dragData,  // the data to be dragged
                            myShadow,  // the drag shadow builder
                            null,      // no need to use local data
                            0          // flags (not currently used, set to 0)
                    );

                    return true;
                }
            });
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Event event = mCalendarEvents.get(position);

            String defaultTitle = holder.itemView.getContext().getString(R.string.event_default_title);
            String title = event.getTitle() == null ? defaultTitle : event.getTitle();

            holder.tvEventName.setText(title);
            holder.rclEventIcon.setBackgroundColor(event.getColor());
            holder.tvEventStatus.setText(timeFormat.format(event.getMarked_Date().getTime()));
        }

        @Override
        public int getItemCount() {
            return mCalendarEvents.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            View rclEventIcon;
            TextView tvEventName;
            TextView tvEventStatus;

            ViewHolder(View view) {
                super(view);
                rclEventIcon = view.findViewById(R.id.rcl_calendar_event_icon);
                tvEventName = view.findViewById(R.id.tv_calendar_event_name);
                tvEventStatus = view.findViewById(R.id.tv_calendar_event_status);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onEventClick(mCalendarEvents.get(getAdapterPosition()));
            }
        }
    }


    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth() / 2;

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() / 2;

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }
    /* 일정이 클릭되는 것, 일정이 만들어지는 것 리스너*/
    public interface OnCalendarDialogListener {
        void onEventClick(Event event);
        void onCreateEvent(Calendar calendar);
    }

    private static int diffYMD(Calendar date1, Calendar date2) {
        Log.d("파이어k", " date1.get(Calendar.YEAR) 확인 : " + date1.get(Calendar.YEAR));
        Log.d("파이어k", " date2.get(Calendar.YEAR) 확인 : " + date2.get(Calendar.YEAR));
        Log.d("파이어k", " date1.get(Calendar.MONTH) 확인 : " + date1.get(Calendar.MONTH));
        Log.d("파이어k", " date2.get(Calendar.MONTH) 확인 : " + date2.get(Calendar.MONTH));
        Log.d("파이어k", " date1.get(Calendar.DAY_OF_MONTH) 확인 : " + date1.get(Calendar.DAY_OF_MONTH));
        Log.d("파이어k", " date2.get(Calendar.DAY_OF_MONTH) 확인 : " + date2.get(Calendar.DAY_OF_MONTH));
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH))
            return 0;

        return date1.before(date2) ? -1 : 1;
    }

    /* 다이얼로그의 빌더 */
    public static class Builder  {

        private final CalendarDialogParams P;

        public static Builder instance(Context context) {
            return new Builder(context);
        }

        private Builder(Context context) {
            P = new CalendarDialogParams(context);
        }

        public Builder setEventList(List<Event> calendarEventList) {
            P.mEventList = new ArrayList<>();
            P.mEventList = calendarEventList;
            return this;
        }

        public Builder setSelectedDate(Calendar selectedDate) {
            P.mSelectedDate = selectedDate;
            return this;
        }

        public Builder setOnItemClickListener(OnCalendarDialogListener listener) {
            P.mOnCalendarDialogListener = listener;
            return this;
        }

        public CalendarDialog create() {
            CalendarDialog calendarDialog = new CalendarDialog(P.mContext);

            P.apply(calendarDialog);

            return calendarDialog;
        }
    }

    /* 날짜 창의 날짜, 일정 리스트, 리스너*/
    private static class CalendarDialogParams {

        Context mContext;

        Calendar mSelectedDate = sToday;
        List<Event> mEventList = new ArrayList<>();

        OnCalendarDialogListener mOnCalendarDialogListener;

        CalendarDialogParams(Context context) {
            mContext = context;
        }

        void apply(CalendarDialog calendarDialog) {
            calendarDialog.setSelectedDate(mSelectedDate);
            calendarDialog.setEventList(mEventList);
            calendarDialog.setOnCalendarDialogListener(mOnCalendarDialogListener);
        }
    }
}