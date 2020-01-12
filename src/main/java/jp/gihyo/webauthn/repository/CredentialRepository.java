package jp.gihyo.webauthn.repository;

import jp.gihyo.webauthn.entity.Credential;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CredentialRepository {

    private NamedParameterJdbcTemplate jdbc;

    public CredentialRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
