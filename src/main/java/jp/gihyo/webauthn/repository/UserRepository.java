package jp.gihyo.webauthn.repository;

import jp.gihyo.webauthn.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private NamedParameterJdbcTemplate jdbc;

    public UserRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> find(String email) {
        var sql = "SELECT * FROM user WHERE email=:email";
        try {
            var user = jdbc.queryForObject(
                    sql,
                    new MapSqlParameterSource()
                            .addValue("email", email),
                    new BeanPropertyRowMapper<>(User.class));
            return Optional.of(user);
        }catch (EmptyResultDataAccessException ignore) {
            return Optional.empty();
        }
    }

    public void insert(User user) {
        var sql = "INSERT INTO user VALUES(:id, :email)";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", user.id)
                .addValue("email", user.email));
    }
}
