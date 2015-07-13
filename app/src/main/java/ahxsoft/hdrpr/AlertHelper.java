package ahxsoft.hdrpr;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.Objects;
import java.util.concurrent.Callable;

public class AlertHelper {

    public static EditTextDialog getNewEditTextDialog(FragmentActivity context, int title, int positive, int negative, int message){
        return new EditTextDialog(context, title, positive, negative, message);
    }

    public static void show(FragmentActivity activity, String text){
        android.widget.Toast toast = android.widget.Toast.makeText(activity, text, android.widget.Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showLong(FragmentActivity activity, String text){
        android.widget.Toast toast = android.widget.Toast.makeText(activity, text, android.widget.Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showLong(FragmentActivity activity, int text){
        android.widget.Toast toast = android.widget.Toast.makeText(activity, text, android.widget.Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showShort(FragmentActivity activity, String text){
        android.widget.Toast toast = android.widget.Toast.makeText(activity, text, android.widget.Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showShort(FragmentActivity activity, int text){
        android.widget.Toast toast = android.widget.Toast.makeText(activity, text, android.widget.Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void toastOnUiThread(final FragmentActivity activity, final String text) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                showLong(activity, text);
            }
        });
    }

    public static void toastOnUiThread(final FragmentActivity activity, final int text) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                showLong(activity, activity.getString(text));
            }
        });
    }
}




class EditTextDialog {
    private final String title;
    private final String positive;
    private final String negative;
    private final String message;
    private FragmentActivity context;

    EditTextDialog(FragmentActivity context, int title, int positive, int negative, int message) {
        this.context = context;
        this.title = context.getString(title);
        this.positive = context.getString(positive);
        this.negative = context.getString(negative);
        this.message = context.getString(message);
    }

    public void show(final CallableReturn<Void, String> pos, final Callable<Void> neg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = li.inflate(R.layout.editext_dialog, null);

        builder.setView(dialogView);
        builder.setMessage(message).setPositiveButton(positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    EditText editTextValue = (EditText) dialogView.findViewById(R.id.editTextValue);
                    String textValue = editTextValue.getText().toString();
                    if(textValue.trim().equals("")){
                        AlertHelper.showLong(context, R.string.nameCannotBeEmpty);
                        return;
                    }
                    pos.call(textValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).setNegativeButton(negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    neg.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).create();
        builder.show();
    }
}