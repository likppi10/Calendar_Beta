package org.hugoandrade.calendarviewapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hugo Andrade on 25/03/2018.
 */

/* 여기에 getter setter 박고 파베랑 연동하면 쓸수 있지 않을까? */
public class Event_firebase implements Parcelable {

    private String Event_Uid;
    private String Id;
    private String Title;
    private Calendar Start_Date;
    private Calendar End_Date;
    private int Color;
    private boolean IsCompleted;

    private int Start_Year;
    private int Start_Month;
    private int Start_Day;

    private int End_Year;
    private int End_Month;
    private int End_Day;

    public Event_firebase(){}

    public Event_firebase(String Event_Uid, String Id, String Title, Calendar Start_Date, Calendar End_Date, int Color, boolean IsCompleted) {
        this.Event_Uid = Event_Uid;
        this.Id = Id;
        this.Title = Title;
        this.Start_Date = Start_Date;
        this.End_Date = End_Date;
        this.Color = Color;
        this.IsCompleted = IsCompleted;
    }

    public Event_firebase(String Event_Uid, String Id, String Title, int Start_Year, int Start_Month, int Start_Day, int End_Year, int End_Month, int End_Day, int Color, boolean IsCompleted) {
        this.Event_Uid = Event_Uid;
        this.Id = Id;
        this.Title = Title;
        this.Start_Year = Start_Year;
        this.Start_Month = Start_Month;
        this.Start_Day = Start_Day;
        this.End_Year = End_Year;
        this.End_Month = End_Month;
        this.End_Day = End_Day;
        this.Color = Color;
        this.IsCompleted = IsCompleted;
    }




    public Map<String, Object> getScheduleInfo(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("ScheduleModel_Uid", Event_Uid);
        docData.put("ScheduleModel_Title", Title);
        docData.put("ScheduleModel_Start_Year", Start_Year);
        docData.put("ScheduleModel_Start_Month", Start_Month);
        docData.put("ScheduleModel_Start_Day", Start_Day);
        docData.put("ScheduleModel_Final_Year", End_Year);
        docData.put("ScheduleModel_Final_Month", End_Month);
        docData.put("ScheduleModel_Final_Day", End_Day);
        docData.put("ScheduleModel_Color", Color);
        docData.put("ScheduleModel_Id", Id);
        docData.put("ScheduleModel_IsCompleted", IsCompleted);
        return  docData;
    }


    public String getId() {
        return this.Id;
    }

    public String getTitle() {
        return this.Title;
    }

    public Calendar getStart_Date() {
        return this.Start_Date;
    }
    public void setStart_Date(Calendar Start_Date) {
        this.Start_Date = Start_Date;
    }
    public Calendar getEnd_Date() {
        return this.End_Date;
    }

    public int getColor() {
        return this.Color;
    }

    public boolean getIsCompleted() {
        return this.IsCompleted;
    }

    protected Event_firebase(Parcel in) {
        Event_Uid = in.readString();
        Id = in.readString();
        Title = in.readString();
        Start_Date = (Calendar) in.readSerializable();
        End_Date = (Calendar) in.readSerializable();
        Color = in.readInt();
        IsCompleted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Event_Uid);
        dest.writeString(Id);
        dest.writeString(Title);
        dest.writeSerializable(Start_Date);
        dest.writeSerializable(End_Date);
        dest.writeInt(Color);
        dest.writeByte((byte) (IsCompleted ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Event_firebase> CREATOR = new Creator<Event_firebase>() {
        @Override
        public Event_firebase createFromParcel(Parcel in) {
            return new Event_firebase(in);
        }

        @Override
        public Event_firebase[] newArray(int size) {
            return new Event_firebase[size];
        }
    };

    public String getEvent_Uid() {
        return this.Event_Uid;
    }

    public void setEvent_Uid(String Event_Uid) {
        this.Event_Uid = Event_Uid;
    }
}
