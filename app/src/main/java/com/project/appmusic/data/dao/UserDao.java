package com.project.appmusic.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.project.appmusic.data.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users_table WHERE username = :username LIMIT 1")
    UserEntity getUserByName(String username);

    @Query("SELECT * FROM users_table")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users_table WHERE email = :email LIMIT 1")
    UserEntity findByEmail(String email);

    @Query("SELECT * FROM users_table WHERE id = :id")
    UserEntity getUserById(int id);

    @androidx.room.Update
    void updateUser(UserEntity user);

    @Query("DELETE FROM users_table WHERE id = :userId")
    void deleteUserById(int userId);

    @androidx.room.Query("SELECT COUNT(*) FROM users_table WHERE email = :email")
    int checkEmailExists(String email);
}