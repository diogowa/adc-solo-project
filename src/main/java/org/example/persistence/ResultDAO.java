package org.example.persistence;

public class ResultDAO<T> {
    public final StatusDAO status;
    public final T data;

    private ResultDAO(StatusDAO status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> ResultDAO<T> success(T data) {
        return new ResultDAO<>(StatusDAO.SUCCESS, data);
    }

    public static <T> ResultDAO<T> error(StatusDAO status) {
        return new ResultDAO<>(status, null);
    }
}
