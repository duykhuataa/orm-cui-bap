package dao;

import model.User;

public class UserDAO extends _BaseDAO<User> {

    public UserDAO() {
        super(User.class, "User");
    }
}
