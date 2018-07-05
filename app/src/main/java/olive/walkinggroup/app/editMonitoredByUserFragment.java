package olive.walkinggroup.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class editMonitoredByUserFragment extends AppCompatDialogFragment {
    private Model instance = Model.getInstance();
    private User currentUser = MonitorActivity.getDummy();

    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.editmonitoractivity,null);

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

                //New Function!
                removeUserByEmail(email);
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

        currentUser.addToMonitoredByUsers(user);

        User newUser = new User();
        newUser.setId(user.getId());

        Call<List<User>> caller = instance.getProxy().addToMonitoredByUsers(currentUser.getId(),newUser);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller,listOfUsers -> setNewList(listOfUsers));
    }


    private void setNewList(List<User> list){
        currentUser.setMonitoredByUsers(list);
    }
    // New Add Code Ends

    // New Remove Code Starts
    private void removeUserByEmail(String email){
        Call<User> caller = instance.getProxy().getUserByEmail(email);
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller, user -> removeUserFromList(user));
    }

    private void removeUserFromList(User user){

        currentUser.removeFromMonitoredByUsers(user);

        Call<Void> caller = instance.getProxy().removeFromMonitoredByUsers(currentUser.getId(),user.getId());
        ProxyBuilder.callProxy((MonitorActivity)getActivity(),caller,returnNothing -> afterRemove(returnNothing));
    }

    private void afterRemove(Void returnNothing){}
    //New Remove Code Ends
}
