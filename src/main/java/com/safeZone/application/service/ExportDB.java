package com.safezone.application.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ExportDB {


    /**
     * Выгружает всю БД в xlsx-файл в корне проекта.
     * @param jdbcUrl  URL БД, например "jdbc:postgresql://localhost:5432/mydb"
     * @param user     пользователь
     * @param password пароль
     * @return путь к созданному файлу
     */
    public static Path exportDatabaseToXlsx(String jdbcUrl, String user, String password)
            throws SQLException, IOException {

        // Имя файла с временной меткой, чтобы не перезаписывать предыдущие выгрузки
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path outputPath = Paths.get("dump_" + timestamp + ".xlsx").toAbsolutePath();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Workbook workbook = new XSSFWorkbook()) {

            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schema = conn.getSchema(); // для Postgres обычно "public"

            // Получаем список таблиц
            try (ResultSet tables = meta.getTables(catalog, schema, "%",
                    new String[]{"TABLE"})) {

                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    createSheetForTable(workbook, conn, tableName);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                workbook.write(fos);
            }
        }

        System.out.println("БД выгружена в: " + outputPath);
        return outputPath;
    }

    /** Создаёт лист и заполняет его данными одной таблицы */
    private static void createSheetForTable(Workbook workbook, Connection conn, String tableName)
            throws SQLException {

        // Excel: имя листа не длиннее 31 символа и без некоторых символов
        String sheetName = tableName.length() > 31
                ? tableName.substring(0, 31)
                : tableName;
        Sheet sheet = workbook.createSheet(sheetName);

        // Стиль для заголовков
        CellStyle headerStyle = workbook.createCellStyle();
        Font bold = workbook.createFont();
        bold.setBold(true);
        headerStyle.setFont(bold);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCount = rsMeta.getColumnCount();

            // Заголовки
            Row header = sheet.createRow(0);
            for (int i = 1; i <= columnCount; i++) {
                Cell cell = header.createCell(i - 1);
                cell.setCellValue(rsMeta.getColumnLabel(i));
                cell.setCellStyle(headerStyle);
            }

            // Данные
            int rowIdx = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    Cell cell = row.createCell(i - 1);
                    if (value == null) {
                        cell.setBlank();
                    } else if (value instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                    } else if (value instanceof Boolean b) {
                        cell.setCellValue(b);
                    } else if (value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // Авторазмер колонок
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

}
