package db.migration;

import com.google.gson.Gson;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class V1_7__SelectionReferences extends BaseJavaMigration {
    private static final String VALUE_UPDATE_STATEMENT = "UPDATE video SET metadata_values = json_replace(metadata_values, '$.' || ? || '.value', (SELECT DISTINCT json_extract(json_each.value, '$.id') FROM metadata m, json_each(m.options, '$.values') WHERE m.id = ? AND json_extract(json_each.value, '$.name') = json_extract(metadata_values, '$.' || ? || '.value.name')))";

    private static class SelectionRow {
        public Integer id;
        public SelectionMetadataMigrationOptions optionData;

        public SelectionRow(Integer id, SelectionMetadataMigrationOptions optionData) {
            this.id = id;
            this.optionData = optionData;
        }
    }

    private static class SingleSelectionValue {
        public Integer id;
        public String name;
    }

    private static class SelectionMetadataMigrationOptions {
        public String type;
        public List<SingleSelectionValue> values;
        public SingleSelectionValue defaultValue;

        public void assignIds() {
            for(int i = 0; i < values.size(); i++) {
                values.get(i).id = i + 1; // Start with 1
            }

            final String defaultValueName = defaultValue.name;
            defaultValue =
                    values.stream().filter(selection -> selection.name.equals(defaultValueName)).findFirst().orElseThrow(IllegalStateException::new);
        }
    }

    public void migrate(Context context) {
        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(context.getConnection(), true);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final Gson gson = new Gson();

        final RowMapper<SelectionRow> rowMapper = new RowMapper<SelectionRow>() {
            @Override
            public SelectionRow mapRow(ResultSet rs, int rowNum) throws SQLException {
                final int id = rs.getInt(1);
                final SelectionMetadataMigrationOptions options = gson.fromJson(rs.getString(2),
                        SelectionMetadataMigrationOptions.class);
                return new SelectionRow(id, options);
            }
        };

        final List<SelectionRow> selectionMetadata = jdbcTemplate.query("SELECT id, options FROM metadata WHERE type = ?",
                rowMapper, 7);

        selectionMetadata.forEach(metadata -> metadata.optionData.assignIds());
        selectionMetadata.forEach(selection -> {
            jdbcTemplate.update("UPDATE metadata SET options = ? where id = ?", gson.toJson(selection.optionData), selection.id);
            jdbcTemplate.update(VALUE_UPDATE_STATEMENT, selection.id, selection.id, selection.id);
        });
    }
}
