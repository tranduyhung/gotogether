package chanathip.gotogether;

/**
 * Created by neetc on 10/25/2016.
 */

public class UserData {
    String userUid;
    String password;
    String email;
    String phone;
    String displayname;
    double LocationLat;
    double LocationLng;

    String rank;
    String GroupUid;
    String GroupName;

    String Status;

    String FriendStatus;

    int unreadMassage;

    public void setData(String UserUid, String displayname, String Email){
        this.userUid = UserUid;
        this.displayname = displayname;
        this.email = Email;
    }
}
