# Диаграмма базы данных
![Diagram](/diagram.jpg)
## Пояснения
+ У фильма может быть несколько жанров и одна категория, взаимосвязь настроена через дополнительные таблицы, чтобы не
  хранить жанр и категорию в полях таблицы фильм;
+ У пользователя отдельная таблица friend для хранения поля статус и обратной зависимости к user, также статус дружбы
  в отдельной таблице
+ Таблица user и film связана с помощью таблицы понравившихся фильмов

//Хорошая диаграмма - аккуратная и полная. Единственное, я думаю, что все-таки лучше не выносить отдельно friends_status, а оставить в таблице friend просто булево поле status, чтобы не усложнять диаграмму - ведь там будет только два значения да/нет.

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
