package com.hcmiu.thesis.mats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hcmiu.thesis.mats.Model.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by edunetjsc on 4/14/17.
 */

public class HistoryAdapter extends BaseAdapter {

    Context context;
    ArrayList<Trip> trips;
    private static LayoutInflater inflater=null;

    public HistoryAdapter(Context context, ArrayList<Trip> trips) {
        this.context = context;
        this.trips = trips;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return trips.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = inflater.inflate(R.layout.history_item, null);
        TextView date = (TextView) rowView.findViewById(R.id.his_date);
        TextView price = (TextView) rowView.findViewById(R.id.his_price);
        TextView car = (TextView) rowView.findViewById(R.id.his_carname);
        TextView destination = (TextView) rowView.findViewById(R.id.his_destinaion);

        date.setText(new SimpleDateFormat("dd-MM-yyyy").format(trips.get(i).getDate()).toString());
        price.setText(trips.get(i).getPrice());
        car.setText(trips.get(i).getCarname());
        destination.setText("To: "+trips.get(i).getDes_address());
        return rowView;
    }
}
