package vn.iotstar.service;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    Optional<User> findByUsernameOrEmail(String login);
    Optional<UserDTO> getUserDtoByLogin(String login);
    long countUsers();
}
