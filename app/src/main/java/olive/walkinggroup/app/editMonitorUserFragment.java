package olive.walkinggroup.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class editMonitorUserFragment extends AppCompatDialogFragment {
    private Model instance = Model.getInstance();
    private User currentUser = instance.getCurrentUser();
    private final Handler handler = new Handler();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = (MonitorActivity)getActivity();

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.editmonitoractivity,null);

        //createAddMonitorsMeButton();

        DialogInterface.OnClickListener listener_add = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText userEmail = getDialog().findViewById(R.id.txtInputemail);
                String email = userEmail.getText().toString();

                addUserByEmail(email);

                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        activity.recreate();
                    }
                },1000);
            }
        };

        DialogInterface.OnClickListener listener_remove = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText userEmail = (EditText) getDialog().findViewById(R.id.txtInputemail);
                String email = userEmail.getText().toString();

                removeUserByEmail(email);

                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        activity.recreate();
                    }
                },1000);
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
        Toast.makeText((MonitorActivity)getActivity(), R.string.serverrequestsent, Toast.LENGTH_LONG).show();
    }

    private void addUserToList(User user){

        currentUser.addToMonitorsUsers(user);

        User newUser = new User();
        newUser.setId(user.getId());

        Call<List<User>> caller = instance.getProxy().addToMonitorsUsers(currentUser.getId(),newUser);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller,listOfUsers -> setNewList(listOfUsers));
    }

    private void setNewList(List<User> list){
        currentUser.setMonitoredByUsers(list);
    }

    private void removeUserByEmail(String email){
        Call<User> caller = instance.getProxy().getUserByEmail(email);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller, user -> removeUserFromList(user));
        Toast.makeText((MonitorActivity)getActivity(), R.string.serverrequestsent, Toast.LENGTH_LONG).show();
    }

    private void removeUserFromList(User user){

        currentUser.removeFromMonitorsUsers(user);

        Call<Void> caller = instance.getProxy().removeFromMonitorsUsers(currentUser.getId(),user.getId());
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller,returnNothing -> afterRemove(returnNothing));
    }

    private void afterRemove(Void returnNothing){}
}
