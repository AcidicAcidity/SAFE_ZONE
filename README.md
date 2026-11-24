# SafeZone

Spring Boot API, PostgreSQL, самостоятельный JavaFX-клиент и React-клиент в `web-client/`.

## Запуск API и веб-клиента

В корне проекта нужен `.env`. Для новой установки скопируйте `.env.example`, задайте пароль БД, логин и пароль администратора и случайный `JWT_SECRET` длиной не менее 32 байт. Ключ можно сгенерировать командой:

```sh
python3 -c 'import secrets; print(secrets.token_urlsafe(48))'
```

Не заменяйте уже настроенный `.env`. Значения `DB_NAME`, `DB_USER` и `DB_PASSWORD` должны соответствовать существующей БД: изменение файла не меняет пароль уже созданного PostgreSQL volume.

```sh
docker compose -f docker-compose.yml -f web-client/docker-compose.yml up -d --build
```

- Веб-клиент: http://localhost:3000
- API: http://localhost:8080/api/v1
- Готовность API: http://localhost:8080/actuator/health/readiness
- PostgreSQL с этого компьютера: `localhost:5433`

При запуске API создаёт отсутствующего администратора из `ADMIN_LOGIN` / `ADMIN_PASSWORD`, а если складов нет — первый склад и 28 ячеек. Существующие аккаунты, пароли, склады и аренды сохраняются. Конфигурация БД передаётся через Spring из окружения Docker; локальный запуск также читает корневой `.env`. Файлы `.env` не включаются в backend JAR и Docker-образ.

```sh
docker compose -f docker-compose.yml -f web-client/docker-compose.yml ps
docker compose -f docker-compose.yml -f web-client/docker-compose.yml logs -f api
```

Остановить сервисы, сохранив БД:

```sh
docker compose -f docker-compose.yml -f web-client/docker-compose.yml stop
```

## Локальный запуск API и тесты

Требуются Java 17+ и Maven. В `DB_URL` корневого `.env` укажите адрес доступной БД. Docker PostgreSQL публикуется на порту 5433; схема для пустой БД находится в `database/schema.sql` и применяется контейнером PostgreSQL при первом запуске.

```sh
mvn clean test
mvn spring-boot:run
```

Подробности фронтенда, флаги совместимости и браузерные тесты описаны в [web-client/README.md](web-client/README.md). Инструкция десктопного клиента — в [javafx-client/README.md](javafx-client/README.md).
