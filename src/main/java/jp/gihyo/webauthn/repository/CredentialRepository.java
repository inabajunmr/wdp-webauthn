package jp.gihyo.webauthn.repository;

import jp.gihyo.webauthn.entity.Credential;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CredentialRepository {

    private NamedParameterJdbcTemplate jdbc;

    public CredentialRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Credential> find(byte[] credentialId) {
        var sql = "SELECT * FROM credential " +
                " WHERE credential_id = :credentialId";
        try {
            var credential = jdbc.queryForObject(sql,
                    new MapSqlParameterSource().addValue("credentialId", credentialId),
                    new BeanPropertyRowMapper<>(
                            Credential.class
                    )
            );
            return Optional.of(credential);
        }catch (EmptyResultDataAccessException ignore) {
            return Optional.empty();
        }
    }

    public List<Credential> finds(byte[] userId) {
        var sql = "SELECT FROM credential WHERE user_id=:userId";
            return jdbc.query(
                    sql,
                    new MapSqlParameterSource()
                            .addValue("userId", userId),
                    new BeanPropertyRowMapper<>(Credential.class));
    }

    public void insert(Credential credential) {
        var sql = "INSERT INTO credential VALUES " +
                "(:credentialId, :userId, :publicKey, :signatureCounter)";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("credentialId", credential.credentialId)
                .addValue("userId", credential.userId)
                .addValue("publicKey", credential.publicKey)
                .addValue("signatureCounter", credential.signatureCounter));
    }

}
