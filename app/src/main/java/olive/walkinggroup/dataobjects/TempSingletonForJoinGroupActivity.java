package olive.walkinggroup.dataobjects;

public class TempSingletonForJoinGroupActivity {
    private static TempSingletonForJoinGroupActivity instance;
    private Group group;

    private TempSingletonForJoinGroupActivity() {
        // Prevents external instancing
    }
    public static TempSingletonForJoinGroupActivity getInstance() {
        if (instance == null) {
            instance = new TempSingletonForJoinGroupActivity();
        }
        return instance;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
