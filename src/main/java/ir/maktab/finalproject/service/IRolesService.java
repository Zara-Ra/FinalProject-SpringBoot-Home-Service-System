package ir.maktab.finalproject.service;

public interface IRolesService<T> {
    T signUp(T role);

    T signIn(String email, String password);

    T changePassword(T role, String oldPassword, String newPassword);
}