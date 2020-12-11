package com.example.datamining;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;


public class FixUpAdapter extends ArrayAdapter<HouseDetailsDesc> {

    public ArrayList<HouseDetailsDesc> fixup_properties;
    private Context context_adapter;
    int resource_adapter;

    public FixUpAdapter(Context context, int resource, ArrayList<HouseDetailsDesc> objects) {
        super(context, resource, objects);
        context_adapter = context;
        resource_adapter = resource;
        fixup_properties = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context_adapter);
            v = vi.inflate(resource_adapter, null);
        }

        String type = getItem(position).getType();
        String address = getItem(position).getAddress();
        String locality = getItem(position).getLocality();
        String state = getItem(position).getState();
        String zipcode = getItem(position).getZipcode();
        Double price = getItem(position).getPrice();
        Double area = getItem(position).getArea();
        Double estrent = getItem(position).getEstRent();
        Double bedroom = getItem(position).getBedrooms();
        Double bathroom = getItem(position).getBathrooms();
        Double Lat = getItem(position).getLatitude();
        Double Lng = getItem(position).getLongitude();
        String Lot = getItem(position).getLot();
        int year = getItem(position).getYear();
        Double hoa = getItem(position).getHoa();
        String description = getItem(position).getDescription();

        //Create a house object with the information
        HouseDetailsDesc housedet = new HouseDetailsDesc(type, address, locality, state, zipcode, price, area, estrent, bedroom, bathroom,Lat, Lng , Lot, year,hoa,description);


        TextView tv_address = (TextView) v.findViewById(R.id.fixup_address);
        TextView tv_area = (TextView) v.findViewById(R.id.fixup_area);
        TextView tv_estrent = (TextView) v.findViewById(R.id.fixup_rent);
        TextView tv_bedroom = (TextView) v.findViewById(R.id.fixup_bed);
        TextView tv_bathroom = (TextView) v.findViewById(R.id.fixup_bath);
        TextView tv_pr = (TextView) v.findViewById(R.id.fixup_pr);
        TextView tv_ps = (TextView) v.findViewById(R.id.fixup_ps);
        TextView tv_lot_size = (TextView) v.findViewById(R.id.fixup_lot);
        ImageView tv_type = (ImageView) v.findViewById(R.id.fixup_type);
        TextView tv_hoa = (TextView) v.findViewById(R.id.fixup_hoa);

        TextView tv_price_bef = (TextView) v.findViewById(R.id.fixup_price_before);
        TextView tv_price_aft = (TextView) v.findViewById(R.id.fixup_price_after);


        tv_address.setText(housedet.getAddress());
        tv_area.setText(Integer.toString((int) Math.round(housedet.getArea()))+" sqft");
        tv_estrent.setText(Integer.toString((int) Math.round(housedet.getEstRent()))+"$");
        tv_bedroom.setText(Integer.toString((int) Math.round(housedet.getBedrooms())));
        tv_bathroom.setText(Integer.toString((int) Math.round(housedet.getBathrooms())));
        tv_pr.setText(String.format("%.2f",((housedet.getEstRent()/housedet.getPrice())*100)));
        tv_ps.setText((int) Math.round(housedet.getPrice()/housedet.getArea())+"$");
        tv_lot_size.setText(housedet.getLot());
        tv_hoa.setText((int) Math.round(housedet.getHoa())+"$");
        tv_price_bef.setText(format(Math.round(housedet.getPrice())));


        switch (housedet.getType()){
            case "Single Family":
                tv_type.setBackgroundResource(R.drawable.ic_single_family);
                break;
            case "Townhouse":
                tv_type.setBackgroundResource(R.drawable.ic_townhouse);
                break;
            case "Condo":
                tv_type.setBackgroundResource(R.drawable.ic_condo);
                break;
        }
        double final_value;
        double price_remodel = FixUpInit.group_calc(FixUpInit.unique_list,FixUpInit.uniques_houses_2d,housedet);
        double diff_less_percent = price_remodel - (0.05*price_remodel);
        double diff_more_percent = price_remodel + (0.2*price_remodel);
        if(diff_less_percent<housedet.getPrice()){
            final_value = price_remodel - ((price_remodel-housedet.getPrice())/2);
        }
        else{
            final_value = diff_less_percent;
        }

        tv_price_aft.setText(format(Math.round(final_value))+" - "+format(Math.round(diff_more_percent)));
        //Log.d("excel_log1", housedet.getAddress() + "                    " + Math.round(final_value) + "                    " + Math.round(diff_more_percent));


        return v;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }


    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
