-- Пароли в SAFE_ZONE.sql лежат открытым текстом ('1234'), а PasswordUtil.verify ждёт формат salt:hash.
-- Из-за этого вход admin / TestUser не работает. Этот скрипт кладёт хэши для пароля 1234.
UPDATE users SET password = 'zTsS6AfSA9cV2tMcdXn1dg==:LXQCnyqiVYi+tA2zZU0AmFcg8K8/8pzXXbrn2wcdxAY=' WHERE login = 'admin';
UPDATE users SET password = '+sFsOqEQnbjnCjrAc0Z7LQ==:ly4R0AtQGr3nPOcCyCU2Qxzu+q7JvCCG4segBmghe3M=' WHERE login = 'TestUser';
