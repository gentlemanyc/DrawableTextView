package com.example.cc.drawabletextview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            int selectedPosition = 4;

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                DrawableTextView textView = new DrawableTextView(MainActivity.this);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,32);
                textView.setTextDrawable(getResources().getDrawable(R.drawable.selector));
                return new RecyclerView.ViewHolder(textView) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                ((DrawableTextView) holder.itemView).setText("DrawableTextView" + position);
                holder.itemView.setSelected(selectedPosition == position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selectedPosition != -1) {
                            notifyItemChanged(selectedPosition);
                        }
                        selectedPosition = position;
                        notifyItemChanged(position);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return 30;
            }
        });

        //初始化TabLayout
        final TabLayout tabLayout = findViewById(R.id.tab);
        PagerAdapter adapter = null;
        tabLayout.setTabsFromPagerAdapter(adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 10;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return false;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return "Title" + position;
            }
        });

        for (int i = 0; i < adapter.getCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                DrawableTextView textView = new DrawableTextView(MainActivity.this);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                textView.setTextDrawable(getResources().getDrawable(R.drawable.selector));
                textView.setText(adapter.getPageTitle(i));
                tab.setCustomView(textView);
            }
        }

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.getTabAt(0).select();
            }
        });
    }
}
