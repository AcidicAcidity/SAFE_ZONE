package com.safeZone.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class createExport {

    private static final char DELIMITER = ';';
    private static final String LINE_SEPARATOR = "\r\n";

    /**
     * Экспорт всей БД одним файлом: находит все таблицы через метаданные соединения,
     * выгружает каждую в свой CSV и упаковывает всё в один zip-архив по указанному пути.
     * Возвращает итоговый путь к файлу
     *
     * Пример использования:
     * String resultPath = CsvExporter.exportDatabase(connection, "C:/export/safezone_dump");
    **/
    public static String exportDatabase(Connection connection, String outputPath) throws SQLException, IOException {
        Path zipPath = Path.of(outputPath);
        if (!zipPath.toString().toLowerCase().endsWith(".zip")) {
            zipPath = Path.of(zipPath.toString() + ".zip");
        }
        if (zipPath.toAbsolutePath().getParent() != null) {
            Files.createDirectories(zipPath.toAbsolutePath().getParent());
        }

        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }
        }

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath));
             Statement statement = connection.createStatement()) {

            for (String table : tableNames) {
                zos.putNextEntry(new ZipEntry(table + ".csv"));
                writeResultSetToStream(statement, table, zos);
                zos.closeEntry();
            }
        }

        return zipPath.toAbsolutePath().toString();
    }

    private static void writeResultSetToStream(Statement statement, String table, ZipOutputStream zos)
            throws SQLException, IOException {
        try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                sb.append(escape(meta.getColumnLabel(i)));
                if (i < columnCount) sb.append(DELIMITER);
            }
            sb.append(LINE_SEPARATOR);

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    sb.append(escape(value == null ? "" : value.toString()));
                    if (i < columnCount) sb.append(DELIMITER);
                }
                sb.append(LINE_SEPARATOR);
            }

            zos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Экспорт произвольного ResultSet в CSV. Колонки и их порядок берутся
     * из метаданных запроса, поэтому метод подходит для любой таблицы/JOIN'а.
     *
     * Пример использования:
     *   try (Connection conn = dbHelper.getConnection();
     *        Statement st = conn.createStatement();
     *        ResultSet rs = st.executeQuery("SELECT * FROM bins")) {
     *       CsvExporter.exportResultSet(rs, Path.of("export/bins.csv"));
     *   }
     */
    public static void exportResultSet(ResultSet rs, Path outputFile) throws SQLException, IOException {
        Files.createDirectories(outputFile.toAbsolutePath().getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // заголовок
            StringBuilder header = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                header.append(escape(meta.getColumnLabel(i)));
                if (i < columnCount) header.append(DELIMITER);
            }
            writer.write(header.toString());
            writer.write(LINE_SEPARATOR);

            // строки
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    row.append(escape(value == null ? "" : value.toString()));
                    if (i < columnCount) row.append(DELIMITER);
                }
                writer.write(row.toString());
                writer.write(LINE_SEPARATOR);
            }
        }
    }

    /**
     * Экспорт уже готовых данных (когда ResultSet не хочется таскать наружу
     * из DBHelper). headers - названия колонок, rows - значения по строкам.
     *
     * Пример:
     *   List<String> headers = List.of("ID", "Размер", "Номер", "Статус");
     *   List<List<Object>> rows = ...; // из List<Bin>
     *   CsvExporter.exportRows(headers, rows, Path.of("export/bins.csv"));
     */
    public static void exportRows(List<String> headers, List<List<Object>> rows, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.toAbsolutePath().getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write(joinAndEscape(headers));
            writer.write(LINE_SEPARATOR);

            for (List<Object> row : rows) {
                List<String> stringValues = row.stream()
                    .map(v -> v == null ? "" : v.toString())
                    .toList();
                writer.write(joinAndEscape(stringValues));
                writer.write(LINE_SEPARATOR);
            }
        }
    }

    private static String joinAndEscape(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            sb.append(escape(values.get(i)));
            if (i < values.size() - 1) sb.append(DELIMITER);
        }
        return sb.toString();
    }

    /**
     * Экранирование по правилам CSV: если значение содержит разделитель,
     * кавычку или перенос строки - оборачиваем в кавычки и удваиваем кавычки внутри.
     */
    private static String escape(String value) {
        boolean needsQuoting = value.indexOf(DELIMITER) >= 0
            || value.indexOf('"') >= 0
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0;

        if (!needsQuoting) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
