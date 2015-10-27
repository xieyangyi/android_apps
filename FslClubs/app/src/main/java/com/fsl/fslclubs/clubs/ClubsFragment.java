package com.fsl.fslclubs.clubs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.main.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by B47714 on 10/14/2015.
 */
public class ClubsFragment extends Fragment {
    private ListView listView;
    private SimpleAdapter adapter;
    private Activity parentActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clubs, null);
        listView = (ListView)view.findViewById(R.id.fragment_clubs_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt("clubID", position+1);
                bundle.putInt("radioButtonCheckId", R.id.main_rbtn_clubs);
                Intent intent = new Intent(parentActivity, ClubsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Club[] clubs = new Club[] {
                new Club(Club.BASKETBALL_ID, getString(R.string.basketball),
                        getString(R.string.basketball_desc), R.drawable.basketball, null),
                new Club(Club.FOOTBALL_ID, getString(R.string.football),
                        getString(R.string.football_desc), R.drawable.football, ""),
                new Club(Club.SNOOKER_ID, getString(R.string.snooker),
                        getString(R.string.snooker_desc), R.drawable.snooker, null),
                new Club(Club.YOGA_ID, getString(R.string.yoga),
                        getString(R.string.yoga_desc), R.drawable.yoga, null),
                new Club(Club.PINGPANG_ID, getString(R.string.pingpang),
                        getString(R.string.pingpang_desc), R.drawable.pingpang, null),
                new Club(Club.TENNIS_ID, getString(R.string.tennis),
                        getString(R.string.tennis_desc), R.drawable.tennis, null),
                new Club(Club.RIDING_ID, getString(R.string.riding),
                        getString(R.string.riding_desc), R.drawable.riding, null),
                new Club(Club.BADMINTON_ID, getString(R.string.badminton),
                        getString(R.string.badminton_desc), R.drawable.badminton, null),
        };
        List<Map<String, Object>> listItems = new ArrayList<>();
        for(int i = 0; i < clubs.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", clubs[i].getIconId());
            map.put("name", clubs[i].getName());
            map.put("desc", clubs[i].getDesc());
            listItems.add(map);
        }

        adapter = new SimpleAdapter(
                getActivity(),
                listItems,
                R.layout.adapter_clubs,
                new String[] {"icon", "name", "desc"},
                new int[] { R.id.adapter_club_icon, R.id.adapter_club_name, R.id.adapter_club_desc }
        );

        parentActivity = getActivity();
    }
}
