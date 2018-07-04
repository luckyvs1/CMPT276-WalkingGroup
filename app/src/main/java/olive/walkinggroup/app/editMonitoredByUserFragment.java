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
    private User currentUser = instance.getCurrentUser();

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
                addUserByEmail(email);
//                User newUser = new User();
//
//                newUser.setEmail(email);
//                newUser.setName("Bob");
//
//                if (user.checkEmailPosInList(email,user.getMonitoredByUsers())==null && !email.equals("")) {
//                    user.addToMonitoredByUsers(newUser);
//                }
//                ((MonitorActivity)getActivity()).populateMonitorsMe();
            }
        };

        DialogInterface.OnClickListener listener_remove = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    EditText userEmail = (EditText) getDialog().findViewById(R.id.txtInputemail);
                    String email = userEmail.getText().toString().replace("@","%40");

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

    private void addUserByEmail(String email) {
        Call<User> caller = instance.getProxy().getUserByEmail(email);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(), caller, user -> addUserToList(user));
    }

    private void addUserToList(User user){
        if (user.checkEmailPosInList(user.getEmail(),user.getMonitoredByUsers())==null && !user.getEmail().equals("")) {
            user.addToMonitoredByUsers(user);
            Log.d("test",user.toString());
            Call<List<User>> caller = instance.getProxy().addToMonitoredByUsers(user.getId(),currentUser);
            ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller,listOfUsers -> refreshMonitorActivity(listOfUsers));
        }

    }

    private void refreshMonitorActivity(List<User> list){
        currentUser.setMonitoredByUsers(list);
        //((MonitorActivity)getActivity()).populateMonitorsMe();
    }

    private void removeUserByEmail(String email){
        Call<User> caller = instance.getProxy().getUserByEmail(email);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller, user -> removeUserFromList(user));
    }

    private void removeUserFromList(User user){
        Integer pos = user.checkEmailPosInList(user.getEmail(),user.getMonitoredByUsers());
        if (pos!= null) {
            User removeUser = user.getMonitoredByUsers().get(pos);
            user.removeFromMonitoredByUsers(removeUser);
        }
        ((MonitorActivity)getActivity()).populateMonitorsMe();
    }

}
