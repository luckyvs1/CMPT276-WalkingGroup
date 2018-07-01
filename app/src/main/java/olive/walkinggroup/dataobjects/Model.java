package olive.walkinggroup.dataobjects;

import android.content.res.Resources;
import android.provider.Settings;

import olive.walkinggroup.R;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;

public class Model {
    private User currentUser;
    private WGServerProxy proxy;

    private Model instance;

    private Model(){
        // Prevent external instancing
    }

    public Model getInstance() {
        if (instance == null) {
            instance = new Model();

            String apiKey = Resources.getSystem().getString(R.string.apikey);
            proxy = ProxyBuilder.getProxy(apiKey, null);
        }
        return instance;
    }

    public void updateProxy(String token) {
        String apiKey = Resources.getSystem().getString(R.string.apikey);
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public WGServerProxy getProxy() {
        return proxy;
    }
}
