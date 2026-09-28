package vn.iotstar.service.impl;

import org.springframework.stereotype.Service;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String login) {
        return userRepository.findByUsernameOrEmail(login);
    }

    @Override
    public Optional<UserDTO> getUserDtoByLogin(String login) {
        return userRepository.findByUsernameOrEmail(login)
                .map(userMapper::toDTO);
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }
}
