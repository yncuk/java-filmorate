# __Filmorate - сервис по оценке фильмов__
Java 11, REST API с использованием Spring Boot, база данных h2, сборщик Maven, код покрыт тестами (MockMvc). CRUD операции с пользователями, фильмами, данные приходят в формате JSON. Добавил рейтинг фильмам (лайки) и поиск по нему (также на основе этого приложения был групповой проект, коммуникация в GitHub, я добавил функционал поиска по общим фильмам)

# Диаграмма базы данных
![Diagram](/diagram.jpg)
## Пояснения
+ У фильма может быть несколько жанров и одна категория, взаимосвязь настроена через дополнительные таблицы, чтобы не
  хранить жанр и категорию в полях таблицы фильм;
+ У пользователя отдельная таблица friend для хранения поля статус и обратной зависимости к user
+ Таблица user и film связана с помощью таблицы понравившихся фильмов


## Примеры запросов
**Нахождение всех фильмов**
+     SELECT name,
             description,
             release_date,
             duration,
             likes
      FROM film

**Нахождение всех пользователей**
+     SELECT email,
             login,
             name,
             birthday
      FROM user

**Найти понравившиеся фильмы у пользователя с логином "name"**
+      SELECT f.name,
             f.description,
             f.release_date,
             f.duration,
             f.likes
       FROM user AS u
       LEFT JOIN liked_film AS lf ON u.user_id=lf.user_id
       LEFT JOIN film AS f ON lf.film_id=f.film_id
       WHERE u.name = 'name'
