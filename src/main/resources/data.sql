delete from films;
AlTER TABLE FILMS ALTER COLUMN FILM_ID RESTART with 1;
delete from users;
AlTER TABLE users ALTER COLUMN user_ID RESTART with 1;
delete from FRIEND;
AlTER TABLE FRIEND ALTER COLUMN PRIMARY_ID RESTART with 1;
delete from CATEGORY;
AlTER TABLE CATEGORY ALTER COLUMN CATEGORY_ID RESTART with 1;
insert into CATEGORY (CATEGORY_NAME) values ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');
delete from GENRE;
AlTER TABLE GENRE ALTER COLUMN GENRE_ID RESTART with 1;
insert into GENRE (GENRE_NAME) values ('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');
delete from FILM_CATEGORY;
delete from FILM_GENRE;
AlTER TABLE FILM_GENRE ALTER COLUMN FILM_GENRE_ID RESTART with 1;
delete from LIKED_FILM;
AlTER TABLE LIKED_FILM ALTER COLUMN LIKED_FILM_ID RESTART with 1;
