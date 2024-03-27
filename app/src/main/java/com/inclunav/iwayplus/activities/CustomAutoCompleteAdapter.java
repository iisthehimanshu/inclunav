package com.inclunav.iwayplus.activities;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import java.util.ArrayList;
import java.util.List;

public class CustomAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private List<String> originalList;
    private List<String> filteredList;
    private ItemFilter filter = new ItemFilter();

    public CustomAutoCompleteAdapter(Context context, List<String> list) {
        super(context, android.R.layout.simple_dropdown_item_1line, list);
        this.originalList = list;
        this.filteredList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public String getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && !constraint.toString().isEmpty()) {
                List<String> filteredItems = new ArrayList<>();
                for (String item : originalList) {
                    // Implement your custom filtering logic here
                    if (item.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredItems.add(item);
                    }
                }
                results.count = filteredItems.size();
                results.values = filteredItems;
            } else {
                // If no constraint is given, return the original list
                results.count = originalList.size();
                results.values = originalList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (List<String>) results.values;
            notifyDataSetChanged();
        }
    }
}
