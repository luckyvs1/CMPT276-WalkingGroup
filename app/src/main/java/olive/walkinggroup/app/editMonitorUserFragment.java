package olive.walkinggroup.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class editMonitorUserFragment extends AppCompatDialogFragment {
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

                if (user.checkEmailPosInList(email,user.getMonitorsUsers())==null && !email.equals("")){
                    user.addToMonitorsUsers(newUser);
                }
                ((MonitorActivity)getActivity()).populateIMonitor();
            }
        };

        DialogInterface.OnClickListener listener_remove = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText userEmail = (EditText) getDialog().findViewById(R.id.txtInputemail);
                String email = userEmail.getText().toString();

                Integer pos = user.checkEmailPosInList(email,user.getMonitorsUsers());
                if (pos!= null) {
                    User removeUser = user.getMonitorsUsers().get(pos);
                    user.removeFromMonitorsUsers(removeUser);
                }
                ((MonitorActivity)getActivity()).populateIMonitor();
            }
        };


        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setNegativeButton("Remove User",listener_remove)
                .setPositiveButton("Add User",listener_add)
                .create();
    }
}
