package org.hugoandrade.calendarviewapp;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

/* 여기서는 별거 없이 안드로이드 버전만 체크한다 생각하면 될 듯*/
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUKLocale(this);

        initializeUI();
    }

    private void initializeUI() {

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView rvOptions = findViewById(R.id.rv_options);
        rvOptions.setHasFixedSize(true);
        rvOptions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        OptionsAdapter adapter = new OptionsAdapter(
                new Option("CalendarView with notes", "Add notes to the calendar view")
        );
        adapter.setOnClickListener(new OptionsAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                Context context = MainActivity.this;
                switch (position) {
                    case 0:

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startActivity(CalendarViewWithNotesActivitySDK21.makeIntent(context));
                        }
                        else {
                            //startActivity(CalendarViewWithNotesActivity.makeIntent(context));
                        }
                        break;
                }
            }
        });
        rvOptions.setAdapter(adapter);
    }

    private static void setUKLocale(Context context) {
        Locale.setDefault(Locale.UK);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(Locale.UK);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    static class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {

        final Option[] mOptions;
        OnClickListener mListener;

        OptionsAdapter(Option... options) {
            mOptions = options;
        }

        @Override
        public OptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            return new ViewHolder(vi.inflate(R.layout.list_item_option, parent, false));
        }

        @Override
        public void onBindViewHolder(OptionsAdapter.ViewHolder holder, int position) {
            Option option = mOptions[holder.getAdapterPosition()];

            holder.tvTitle.setText(option.getTitle());
            holder.tvDescription.setText(option.getDescription());
        }

        @Override
        public int getItemCount() {
            return mOptions.length;
        }

        void setOnClickListener(OnClickListener listener) {
            mListener = listener;
        }

        interface OnClickListener {
            void onClick(int position);
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView tvTitle;
            TextView tvDescription;

            ViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                tvTitle = itemView.findViewById(R.id.tv_title);
                tvDescription = itemView.findViewById(R.id.tv_description);
            }

            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onClick(getAdapterPosition());
            }
        }
    }

    static class Option {

        String mTitle;
        String mDescription;

        Option(String title, String description) {
            mTitle = title;
            mDescription = description;
        }

        String getTitle() {
            return mTitle;
        }

        String getDescription() {
            return mDescription;
        }
    }
}
