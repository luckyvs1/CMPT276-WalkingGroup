package olive.walkinggroup.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class editMonitoredByUserFragment extends AppCompatDialogFragment {
    private Model instance = Model.getInstance();
    private User currentUser = FindGroupsActivity.getBob();

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

                //New Function!
                addUserByEmail(email);
            }
        };

        DialogInterface.OnClickListener listener_remove = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    EditText userEmail = (EditText) getDialog().findViewById(R.id.txtInputemail);
                    String email = userEmail.getText().toString();

//                    Integer pos = currentUser.checkEmailPosInList(email,currentUser.getMonitoredByUsers());
//                    if (pos!= null) {
//                        User removeUser = currentUser.getMonitoredByUsers().get(pos);
//                        currentUser.removeFromMonitoredByUsers(removeUser);
//                    }
//                    ((MonitorActivity)getActivity()).populateMonitorsMe();
            }
        };


        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setNegativeButton("Remove User",listener_remove)
                .setPositiveButton("Add User",listener_add)
                .create();
    }

    // New Add Code Starts
    private void addUserByEmail(String email) {
        Call<User> caller = instance.getProxy().getUserByEmail(email);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(), caller, user -> addUserToList(user));
    }

    private void addUserToList(User user){
        if (currentUser.checkEmailPosInList(user.getEmail(),currentUser.getMonitoredByUsers())==null && !user.getEmail().equals("")) {
            //can be deleted
            currentUser.addToMonitoredByUsers(user);

            Log.d("test",user.toString());
            Call<List<User>> caller = instance.getProxy().addToMonitoredByUsers(currentUser.getId(),user);
            ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller,listOfUsers -> refreshMonitorActivity(listOfUsers));
        }

    }

    private void refreshMonitorActivity(List<User> list){
        currentUser.setMonitoredByUsers(list);
        ((MonitorActivity)getActivity()).populateMonitorsMe();
    }
    // New Add Code Ends

    private void refreshMonitorActivityAfterRemove(Void returnNothing){
        ((MonitorActivity)getActivity()).populateMonitorsMe();
    }


    private void removeUserByEmail(String email){
        Call<User> caller = instance.getProxy().getUserByEmail(email);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller, user -> removeUserFromList(user));
    }

    private void removeUserFromList(User user){
        Integer pos = currentUser.checkEmailPosInList(user.getEmail(),currentUser.getMonitoredByUsers());
        if (pos!= null) {

            //can be deleted
            User removeUser = currentUser.getMonitoredByUsers().get(pos);
            currentUser.removeFromMonitoredByUsers(removeUser);

            Call<Void> caller = instance.getProxy().removeFromMonitoredByUsers(currentUser.getId(),user.getId());
            ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller,returnNothing -> refreshMonitorActivityAfterRemove(returnNothing));

        }
        ((MonitorActivity)getActivity()).populateMonitorsMe();
    }

}
