# Описание проекта
1. Цель проекта:
  Создать приложение по контролю складских ячеек.


2. Архитектура приложения (консольная версия приложения):
  2.1 main.java (Основной класс, который включает все остальные и запускает приложение)
  2.2 include/* (Папка со всеми классами обеспечивающая работу)
  2.3 data/*    (Папка с БД, токенами, секретами и тд)


Пишем на Maven

## Запуск проекта

### Требования

- Java 17+
- Maven
- PostgreSQL
- pgAdmin или `psql`

### Настройка базы данных

```powershell
psql -U postgres -d postgres

psql -U postgres -d safe_zone -f .\SAFE_ZONE.sql

К корне проекта нужно созда config.json по данному образцу:

`config.json`
{
  "host": "localhost",
  "port": 5432,
  "database": "safe_zone",
  "user": "postgres",
  "password": "your_db_password",
  "token": "placement_tokens"
}

Сборка и запуск

mvn clean compile -U
mvn clean package

java -cp ".;.\target\SAFE_ZONE-0.2.jar" com.safeZone.App

Для Windows рекомендуется
javaw -cp ".;.\target\SAFE_ZONE-0.2.jar" com.safeZone.App

![Anurag's GitHub stats](https://github-readme-stats.vercel.app/api?username=AcidicAcidity&hide=prs,issues)
