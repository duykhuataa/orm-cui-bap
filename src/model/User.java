package model;

import java.sql.Timestamp;

public class User {

    private int userId;
    private String username;
    private String password;
    private String oauthId;
    private String email;
    private int roleId;
    private String fullName;
    private String imgPath;
    private Timestamp dateCreated;
    private byte isDeleted;
    private byte isActive;

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getOauthId() {
        return oauthId;
    }

    public String getEmail() {
        return email;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getImgPath() {
        return imgPath;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public byte getIsDeleted() {
        return isDeleted;
    }

    public byte getIsActive() {
        return isActive;
    }

}
