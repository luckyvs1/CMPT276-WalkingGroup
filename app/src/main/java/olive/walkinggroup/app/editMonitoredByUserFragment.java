package olive.walkinggroup.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class editMonitoredByUserFragment extends AppCompatDialogFragment {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.editmonitoractivity,null);

        //createAddMonitorsMeButton();

        DialogInterface.OnClickListener listener_add = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText userEmail = getDialog().findViewById(R.id.txtInputemail);
                String email = userEmail.getText().toString();

                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName("Bob");

                if (user.checkEmailPosInList(email,user.getMonitoredByUsers())==null && !email.equals("")) {
                    user.addToMonitoredByUsers(newUser);
                }
                ((MonitorActivity)getActivity()).populateMonitorsMe();
            }
        };

        DialogInterface.OnClickListener listener_remove = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    EditText userEmail = (EditText) getDialog().findViewById(R.id.txtInputemail);
                    String email = userEmail.getText().toString();

                    Integer pos = user.checkEmailPosInList(email,user.getMonitoredByUsers());
                    if (pos!= null) {
                        User removeUser = user.getMonitoredByUsers().get(pos);
                        user.removeFromMonitoredByUsers(removeUser);
                    }
                    ((MonitorActivity)getActivity()).populateMonitorsMe();
            }
        };


        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setNegativeButton("Remove User",listener_remove)
                .setPositiveButton("Add User",listener_add)
                .create();
    }
}
