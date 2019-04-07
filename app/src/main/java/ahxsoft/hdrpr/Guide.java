package ahxsoft.hdrpr;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Guide extends Fragment {
    private static final String ARG_SECTION_NUMBER = "about";
    private final String source = "<body><h1>HDR Preview Quick Guide</h1><big><p>Author: Augusto Burgos</p>" +
                    "<p>Email: ahxsoft@outlook.com</p><br/>" +
                    "<p>This app is intended to help you take better HDR images, because it can be used while you are on site of the image. HDR Preview will produce a really nice HDR image, really quick, which in turn will help you determine if you have the right shot.</p>"+
                    "<p>In order to use the app, you just need a sequence of images of the exact same subject / scene. Each with a clear difference in the exposure value. Each image should be darker than the next or the opposite will also work. You will need at least 3 images. </p>"+
                    "<p>After you have the images, just open the app and click the button \"create new image\" and provide a name for the image, then, adjust the exposure values if needed in the screen and hit process.</p>"+
                    "<p>This is nice Guide how to make HDR images http://goo.gl/c9DIbs</p>" +
            "<big/></body>";

    public static Guide newInstance(int sectionNumber) {
        Guide fragment = new Guide();
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
        about.setMovementMethod(LinkMovementMethod.getInstance());
    }


}
