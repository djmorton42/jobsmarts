package ca.quadrilateral.jobsmarts.data.api;

import java.util.Collection;

import ca.quadrilateral.jobsmarts.api.User;

public interface IUserDataService {
    Collection<User> getAllUsers();
}
