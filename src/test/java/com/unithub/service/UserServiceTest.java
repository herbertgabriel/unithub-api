package com.unithub.service;

import com.unithub.dto.request.login.CreateUserDTO;
import com.unithub.dto.request.user.UpdateUserDTO;
import com.unithub.model.Course;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.CourseRepository;
import com.unithub.repository.RoleRepository;
import com.unithub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        CreateUserDTO dto = new CreateUserDTO(
                "test@example.com",
                "password123",
                "password123",
                "1234567890",
                "Test User",
                1L
        );

        Role basicRole = new Role();
        basicRole.setName("ALUNO");

        Course course = new Course();

        when(roleRepository.findByName("ALUNO")).thenReturn(basicRole);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(dto.password())).thenReturn("encodedPassword");

        // Act
        userService.createUser(dto);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_PasswordMismatch() {
        // Arrange
        CreateUserDTO dto = new CreateUserDTO(
                "test@example.com",
                "password123",
                "differentPassword",
                "1234567890",
                "Test User",
                1L
        );

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Passwords do not match", exception.getReason());
    }

    @Test
    void testUpdateUserProfile_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UpdateUserDTO dto = new UpdateUserDTO(
                "newemail@example.com",
                "newPassword123",
                "newPassword123",
                "9876543210",
                null // O campo "name" deve ser null para evitar a exceção "Name not allowed"
        );

        User user = new User();
        user.setUserId(userId);
        user.setEmail("oldemail@example.com");

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authService.getAuthenticatedUser(authentication)).thenReturn(user);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.encode(dto.password())).thenReturn("encodedNewPassword");

        // Act
        userService.updateUserProfile(dto, authentication);

        // Assert
        assertEquals("newemail@example.com", user.getEmail());
        assertEquals("encodedNewPassword", user.getPassword());
        assertEquals("9876543210", user.getTelephone());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUserProfile_PasswordMismatch() {
        // Arrange
        UpdateUserDTO dto = new UpdateUserDTO(
                null,
                "password123",
                "differentPassword",
                null,
                null
        );

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authService.getAuthenticatedUser(authentication)).thenReturn(new User());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.updateUserProfile(dto, authentication));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Passwords do not match", exception.getReason());
    }

    @Test
    void testUpdateUserProfile_AuthServiceNull() {
        // Arrange
        UpdateUserDTO dto = new UpdateUserDTO(
                "newemail@example.com",
                "password123",
                "password123",
                "1234567890",
                "Test User"
        );

        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.updateUserProfile(dto, null));
    }
}