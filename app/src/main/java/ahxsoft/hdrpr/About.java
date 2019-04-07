package ahxsoft.hdrpr;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class About extends Fragment {
    private static final String ARG_SECTION_NUMBER = "about";
    private final String source = "<body><h1>HDR Preview</h1><big><p>Author: Augusto Burgos</p>" +
                    "<p>Email: ahxsoft@outlook.com</p><br/>" +
                    "<p>Special Thanks to the Georgia Tech academic staff of CS-6475 Summer 2015.</p>" +
                    "<p><strong>Instructor:</strong> Irfan Essa.</p>" +
                    "<p><strong>Head TA:</strong> Daniel Castro.</p>" +
                    "<p><strong>TAs/Graders:</strong> Julia Deeb, Vibhav Gupta, Gokul Raghuraman, Ritika Rao</p><big/></body>";

    public static About newInstance(int sectionNumber) {
        About fragment = new About();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.about, container, false);
        setStyleText(rootView);

        Button close = (Button) rootView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HDRPR parent = (HDRPR) getActivity();
                parent.goToDashboard();
            }
        });

        return rootView;
    }

    private void setStyleText(View rootView) {
        TextView about = (TextView) rootView.findViewById(R.id.about);
        about.setText(Html.fromHtml(source), TextView.BufferType.SPANNABLE);
    }


}
