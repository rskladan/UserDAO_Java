package pl.coderslab.entity;

import org.mindrot.jbcrypt.BCrypt;
import pl.coderslab.DBUtil;

import java.sql.*;
import java.util.Arrays;

public class UserDao {

    private static final String CREATE_USER_QUERY =
            "INSERT INTO users(username, email, password) VALUES (?, ?, ?);";

    private static final String READ_USER_QUERY =
            "SELECT * FROM users WHERE id = ?;";
//    * = username, email, password

    private static final String READ_USER_EMAIL_QUERY =
            "SELECT * FROM users WHERE email = ?;";
//    * = username, email, password

    private static final String READ_ALL_USERS_QUERY = "SELECT * FROM users;";

    private static final String UPDATE_USER_QUERY =
            "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?;";

    private static final String CHANGE_PASSWORD_QUERY =
            "UPDATE users SET password = ? WHERE id = ?;";

//    *************************************************************************


    public User create(User user) {

        try (Connection conn = DBUtil.connect()) {

            //            DBUtil.insert(conn, CREATE_USER_QUERY, user.getUserName(), user.getEmail(), hashedPassword);

            PreparedStatement statement = conn.prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashPassword(user.getPassword()));
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();

            if (resultSet.next()) {
                user.setId(resultSet.getInt(1));
            }
            System.out.println("Dodano nowego uzytkownika");
            return user;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User read(int userID) {

        try (Connection conn = DBUtil.connect()) {

            PreparedStatement preparedStatement = conn.prepareStatement(READ_USER_QUERY);
            preparedStatement.setInt(1, userID);
            ResultSet rs_ID = preparedStatement.executeQuery();

            User user = new User();

            if (rs_ID.next()) {
                user.setId(rs_ID.getInt(1));
                user.setUserName(rs_ID.getString("username"));
                user.setEmail(rs_ID.getString("email"));
                user.setPassword(rs_ID.getString("password"));

                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
        return null;

    }

    public void update(User user) {

        try (Connection conn = DBUtil.connect()) {

            PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_USER_QUERY);
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setInt(4, user.getId());
            preparedStatement.executeUpdate();
            System.out.println("Uzytkownik zostal uaktualniony");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changePassword(User user) {

        try (Connection conn = DBUtil.connect()) {

            PreparedStatement preparedStatement = conn.prepareStatement(CHANGE_PASSWORD_QUERY);
            preparedStatement.setString(1, hashPassword(user.getPassword()));
            preparedStatement.setInt(2, user.getId());
            preparedStatement.executeUpdate();
            System.out.println("Haslo zostalo zmienione");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int userID) {

        try (Connection conn = DBUtil.connect()) {

            DBUtil.remove(conn, "users", userID);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Usunieto uzytkownika");
    }

    private User[] addToArray(User u, User[] users) {

        User[] tmpUsers = Arrays.copyOf(users, users.length + 1);
        tmpUsers[users.length] = u;
        return tmpUsers;

    }

    public User[] findAll() {

        try (Connection conn = DBUtil.connect()) {

            User[] allUsers = new User[0];
            PreparedStatement preparedStatement = conn.prepareStatement(READ_ALL_USERS_QUERY);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUserName(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));

                allUsers = addToArray(user, allUsers);

            }

            return allUsers;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }


    private String hashPassword(String password) {

        return BCrypt.hashpw(password, BCrypt.gensalt());

    }


}
