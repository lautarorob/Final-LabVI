package com.project.appmusic.data.dto;

public class UserRegistrationDTO extends BaseDTO {

    // Solo declaras las variables específicas de esta vista
    private String name;
    private String email;
    private String password;
    private String confirmPassword;

    public UserRegistrationDTO(String name, String email, String password, String confirmPassword) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}
