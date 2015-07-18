package ahxsoft.hdrpr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ProcessImageFragment extends Fragment {


    public static Fragment newInstance() {
        return new ProcessImageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.process_image_fragment, container, false);
        setSpinner(view, R.id.toneMapKinds, R.array.toneMapTypes);
        setSpinner(view, R.id.sizes, R.array.Sizes);
        return view;
    }


    private void setSpinner(View view, int id, int content){
        Spinner spinner = (Spinner) view.findViewById(id);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), content, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
    }
}
